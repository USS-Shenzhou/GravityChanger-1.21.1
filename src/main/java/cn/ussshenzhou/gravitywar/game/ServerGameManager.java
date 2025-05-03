package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.network.s2c.StartCPacket;
import cn.ussshenzhou.gravitywar.network.s2c.TeamPlayerNumberPacket;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ServerGameManager extends GameManager {

    public static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static ServerLevel getLevel(){
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

    public static void start() {
        var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
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
        manager.start();
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
}
