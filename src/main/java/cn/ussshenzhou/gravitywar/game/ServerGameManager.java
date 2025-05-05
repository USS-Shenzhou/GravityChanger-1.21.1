package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.network.s2c.*;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ServerGameManager extends GameManager {

    private static long startMs = 0;

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static ServerLevel getLevel() {
        return getServer().overworld();
    }

    public static Optional<ServerPlayer> getPlayerS(UUID uuid) {
        return Optional.ofNullable(getServer().getPlayerList().getPlayer(uuid));
    }

    public static void pickTeam(ServerPlayer player, Direction team) {
        if (PLAYER_TO_TEAM.containsKey(player.getUUID())) {
            PLAYER_TO_TEAM.remove(player.getUUID());
            TEAM_TO_PLAYER.values().forEach(set -> set.remove(player.getUUID()));
        }
        TEAM_TO_PLAYER.computeIfAbsent(team, k -> new HashSet<>()).add(player.getUUID());
        PLAYER_TO_TEAM.put(player.getUUID(), team);
        var number = new int[6];
        TEAM_TO_PLAYER.forEach((direction, uuids) -> number[direction.ordinal()] = uuids.size());
        NetworkHelper.sendToAllPlayers(new TeamPlayerNumberPacket(number));
        var packet = new OpAllPlayerChosenPacket(TEAM_TO_PLAYER);
        getServer().getPlayerList().getOps().getEntries().forEach(entry -> {
            if (entry.getUser() != null) {
                getPlayerS(entry.getUser().getId()).ifPresent(p -> NetworkHelper.sendToPlayer(p, packet));
            }
        });
        teleportWithDiffuse(player, getConfig().waitingPos.get(team));
    }

    @SubscribeEvent
    public static void checkPlayerOnline(ServerTickEvent.Pre event) {
        if (phase != MatchPhase.CHOOSE) {
            return;
        }
        var offline = new ArrayList<UUID>();
        PLAYER_TO_TEAM.forEach((uuid, direction) -> getPlayerS(uuid).ifPresentOrElse(player -> {
        }, () -> offline.add(uuid)));
        for (UUID uuid : offline) {
            var dir = PLAYER_TO_TEAM.remove(uuid);
            if (dir == null) {
                TEAM_TO_PLAYER.get(dir).remove(uuid);
            }
        }
    }

    public static Optional<UUID> getLeaderOf(UUID uuid) {
        if (!PLAYER_TO_TEAM.containsKey(uuid)) {
            return Optional.empty();
        }
        return TEAM_TO_PLAYER.get(PLAYER_TO_TEAM.get(uuid)).stream()
                .map(ServerGameManager::getPlayerS)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> p.hasPermissions(2))
                .findFirst()
                .map(Entity::getUUID);
    }

    public static void forEachS(Consumer<ServerPlayer> action) {
        PLAYER_TO_TEAM.keySet().stream()
                .map(ServerGameManager::getPlayerS)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(action);
    }

    public static void teleportWithDiffuse(Player player, BlockPos pos) {
        if (pos == null) {
            return;
        }
        var r = ThreadLocalRandom.current();
        player.teleportTo(pos.getX() + r.nextDouble() * 2 - 1,
                pos.getY() + r.nextDouble() * 2 - 1,
                pos.getZ() + r.nextDouble() * 2 - 1);

    }

    public static void start() {
        //note all
        var notify = new ClientboundSetSubtitleTextPacket(Component.literal("对战将于10秒后开始"));
        getServer().getPlayerList().getPlayers()
                .forEach(player -> player.connection.send(notify));
        NetworkHelper.sendToAllPlayers(new DingPacket());
        //handle neutral players
        var neutralPlayers = getServer().getPlayerList().getPlayers().stream()
                .filter(player -> !player.hasPermissions(2) && !PLAYER_TO_TEAM.containsKey(player.getUUID()))
                .collect(Collectors.toSet());
        maxPlayerPerTeam = (int) getServer().getPlayerList().getPlayers().stream()
                .filter(p -> !p.hasPermissions(2))
                .count() / 6 + 1;
        while (!neutralPlayers.isEmpty()) {
            var player = neutralPlayers.iterator().next();
            TEAM_TO_PLAYER.entrySet().stream()
                    .filter(e -> e.getValue().size() < maxPlayerPerTeam)
                    .findAny()
                    .ifPresent(e -> {
                        e.getValue().add(player.getUUID());
                        PLAYER_TO_TEAM.put(player.getUUID(), e.getKey());
                    });
        }

        //real start
        TaskHelper.addServerTask(() -> {
            var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
            NetworkHelper.sendToAllPlayers(new DingPacket());
            NetworkHelper.sendToAllPlayers(new StartCPacket(
                    PLAYER_TO_TEAM,
                    phase,
                    mode,
                    maxPlayerPerTeam,
                    victoryScore,
                    cfg.preparePhase,
                    cfg.battlePhase,
                    cfg.finalPhase
            ));
            manager = MatchManager.create(mode);
            manager.startServer();
            startMs = System.currentTimeMillis();
        }, 10);
    }

    public static void end() {
        manager = null;
        GameManager.clear();
        NetworkHelper.sendToAllPlayers(new TeamPlayerNumberPacket(new int[6]));
    }

    @SubscribeEvent
    public static void matchTick(ServerTickEvent.Pre event) {
        if (manager != null) {
            manager.serverTick();
        }
    }

    @SubscribeEvent
    public static void cancelFriendlyFire(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Player player0
                && event.getSource().getEntity() instanceof Player player1) {
            if (player0.getId() != player1.getId()
                    && PLAYER_TO_TEAM.get(player0.getUUID()) == PLAYER_TO_TEAM.get(player1.getUUID())) {
                event.setCanceled(true);
            } else if (phase == MatchPhase.PREP) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (PLAYER_TO_TEAM.containsKey(player.getUUID()) && phase == MatchPhase.CHOOSE) {
            TEAM_TO_PLAYER.get(PLAYER_TO_TEAM.get(player.getUUID())).remove(player.getUUID());
            PLAYER_TO_TEAM.remove(player.getUUID());
            var number = new int[6];
            TEAM_TO_PLAYER.forEach((direction, uuids) -> number[direction.ordinal()] = uuids.size());
            NetworkHelper.sendToAllPlayers(new TeamPlayerNumberPacket(number));
        }
    }

    @SubscribeEvent
    public static void reconnect(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (PLAYER_TO_TEAM.containsKey(player.getUUID()) && phase != MatchPhase.CHOOSE) {
            var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
            NetworkHelper.sendToPlayer((ServerPlayer) player, new DingPacket());
            NetworkHelper.sendToPlayer((ServerPlayer) player, new StartCPacket(
                    PLAYER_TO_TEAM,
                    phase,
                    mode,
                    maxPlayerPerTeam,
                    victoryScore,
                    cfg.preparePhase,
                    cfg.battlePhase,
                    cfg.finalPhase
            ));
            NetworkHelper.sendToPlayer((ServerPlayer) player, new TimeCheckPacket(startMs));
        }
    }
}
