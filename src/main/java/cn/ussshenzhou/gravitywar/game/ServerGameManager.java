package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.entity.CoreEntity;
import cn.ussshenzhou.gravitywar.entity.ModEntities;
import cn.ussshenzhou.gravitywar.network.UtilS;
import cn.ussshenzhou.gravitywar.network.s2c.*;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.gravitywar.util.TradeHelper;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ServerGameManager extends GameManager {

    protected static long startMs = 0;
    protected static Set<Direction> teamsOnGround = new HashSet<>();
    public static final HashMap<UUID, Integer> PLAYER_DEATH = new HashMap<>();

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
        if (phase != MatchPhase.CHOOSE) {
            return;
        }
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
                TEAM_TO_PLAYER.computeIfPresent(dir, (direction, uuids) -> {
                    uuids.remove(uuid);
                    return uuids;
                });
            }
        }
    }

    public static Optional<UUID> getLeaderOf(UUID uuid) {
        if (!PLAYER_TO_TEAM.containsKey(uuid)) {
            return Optional.empty();
        }
        var set = TEAM_TO_PLAYER.get(PLAYER_TO_TEAM.get(uuid)).stream()
                .map(ServerGameManager::getPlayerS)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> p.hasPermissions(2))
                .collect(Collectors.toSet());
        return set.stream()
                .filter(TradeHelper::isKaMu)
                .findFirst()
                .or(() -> set.stream().findAny())
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
        NetworkHelper.sendToAllPlayers(new SubtitlePacket("对战将于10秒后开始"));
        NetworkHelper.sendToAllPlayers(new DingPacket());
        UtilS.delayMs(() -> {
            NetworkHelper.sendToAllPlayers(new SubtitlePacket("对战将于5秒后开始"));
            NetworkHelper.sendToAllPlayers(new DingPacket());
        }, 5);
        //handle neutral players
        if (mode != MatchMode.SIEGE) {
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
        }
        //real start
        UtilS.delay(() -> {
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
            teamsOnGround.clear();
            teamsOnGround.addAll(List.of(Direction.values()));
            cfg.villagerPos.forEach(blockPos -> {
                var villager = EntityType.VILLAGER.create(getLevel());
                if (villager != null) {
                    villager.setNoAi(true);
                    villager.teleportTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    GravityChangerAPIProxy.setBaseGravityDirection(villager, DirectionHelper.getPyramidRegion(blockPos));
                    villager.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 5, false, false));
                    getLevel().addFreshEntity(villager);
                }
            });
        }, 10);
    }

    public static void end() {
        PLAYER_DEATH.clear();
        manager = null;
        UtilS.delay(() -> {
            var pkt = new ChangePhasePacket(MatchPhase.CHOOSE);
            forEachS(p -> NetworkHelper.sendToPlayer(p, pkt));
            PLAYER_TO_TEAM.forEach((uuid, direction) -> {
                getPlayerS(uuid).ifPresent(p -> {
                    GravityChangerAPIProxy.setBaseGravityDirection(p, Direction.DOWN);
                });
            });
            NetworkHelper.sendToAllPlayers(new TeamPlayerNumberPacket(new int[6]));
            GameManager.clear();
            ArrayList<Entity> coresToRemove = new ArrayList<>();
            getLevel().getEntities().getAll().forEach(entity -> {
                if (entity instanceof CoreEntity || entity instanceof Villager) {
                    coresToRemove.add(entity);
                }
            });
            coresToRemove.forEach(entity -> entity.remove(Entity.RemovalReason.DISCARDED));
            teamsOnGround.clear();
        }, 10);
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
            if (phase == MatchPhase.PREP) {
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
            ((ServerPlayer) player).setGameMode(GameType.SURVIVAL);
        }
    }

    @SubscribeEvent
    public static void revivePos(PlayerRespawnPositionEvent event) {
        var old = event.getDimensionTransition();
        event.setDimensionTransition(new DimensionTransition(
                old.newLevel(),
                event.getEntity().position(),
                new Vec3(0, 0, 0),
                event.getEntity().getYRot(),
                event.getEntity().getXRot(),
                false,
                DimensionTransition.DO_NOTHING
        ));
    }

    @SubscribeEvent
    public static void revive(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (PLAYER_TO_TEAM.containsKey(player.getUUID()) && phase != MatchPhase.CHOOSE) {
            var team = PLAYER_TO_TEAM.get(player.getUUID());
            GravityChangerAPIProxy.setBaseGravityDirection(player, team);
            var deathTime = PLAYER_DEATH.compute(player.getUUID(), (uuid, integer) -> integer == null ? 0 : ++integer) * 10;
            NetworkHelper.sendToPlayer((ServerPlayer) player, new SubtitlePacket("将于 " + deathTime + " 秒后复活"));
            ((ServerPlayer) player).setGameMode(GameType.SPECTATOR);
            UtilS.delay(() -> {
                getPlayerS(player.getUUID()).ifPresent(p -> {
                    switch (GameManager.mode) {
                        case CORE, SIEGE -> {
                            if (beaconPos != null && beaconTeam == team && getLevel().getBlockState(beaconPos).getBlock() == Blocks.BEACON) {
                                teleportWithDiffuse(p, beaconPos);
                                p.setGameMode(GameType.SURVIVAL);
                            } else {
                                var posList = StreamSupport.stream(getLevel().getEntities().getAll().spliterator(), false)
                                        .filter(entity -> entity instanceof CoreEntity)
                                        .filter(core -> DirectionHelper.getPyramidRegion(core.position()) == team)
                                        .toList();
                                if (!posList.isEmpty()) {
                                    var pos = posList.get(ThreadLocalRandom.current().nextInt(posList.size()));
                                    teleportWithDiffuse(p, pos.blockPosition());
                                    p.setGameMode(GameType.SURVIVAL);
                                }
                            }
                        }
                        case INTRUDER -> {
                            var posList = getConfig().spawnPos.get(team);
                            var pos = posList.get(ThreadLocalRandom.current().nextInt(posList.size()));
                            p.teleportTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        }
                    }
                });
            }, deathTime);
        }
    }

    @SubscribeEvent
    public static void itemEntityChangeGravity(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity || event.getEntity() instanceof FallingBlockEntity) {
            GravityChangerAPIProxy.setBaseGravityDirection(event.getEntity(), DirectionHelper.getPyramidRegion(event.getEntity().position()));
        }
    }

    @SubscribeEvent
    public static void higherKnockBack(LivingKnockBackEvent event) {
        if (GameManager.event == RandomEvent.HIGH_KNOCKBACK) {
            event.setStrength(event.getStrength() * 5);
        }
    }

    @SubscribeEvent
    public static void cancelFallenDamage(LivingFallEvent event) {
        if (GameManager.event == RandomEvent.ULTRA_BOUNCE) {
            event.setDamageMultiplier(0);
        }
    }

    public static BlockPos beaconPos = null;
    public static Direction beaconTeam = null;

    @SubscribeEvent
    public static void reviveBeacon(UseItemOnBlockEvent event) {
        if (event.getItemStack().getItem() == Items.BEACON
                && event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK
                && event.getCancellationResult() == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                && event.getPlayer() != null) {
            if (PLAYER_TO_TEAM.containsKey(event.getPlayer().getUUID())) {
                beaconPos = new BlockPos(event.getPos());
                beaconTeam = PLAYER_TO_TEAM.get(event.getPlayer().getUUID());
                TEAM_TO_PLAYER.get(beaconTeam).stream()
                        .map(ServerGameManager::getPlayerS)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(ServerPlayer::isSpectator)
                        .forEach(player -> {
                            player.setGameMode(GameType.SURVIVAL);
                            player.teleportTo(beaconPos.getX() + 0.5, beaconPos.getY() + 0.5, beaconPos.getZ() + 0.5);
                        });
            }
        }
    }

    @SubscribeEvent
    public static void reviveCore(UseItemOnBlockEvent event) {
        if (event.getItemStack().getItem() == Items.END_CRYSTAL
                && event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK
                && event.getCancellationResult() == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                && event.getPlayer() != null) {
            event.setCanceled(true);
            event.getItemStack().shrink(1);
            var pos = event.getPos();
            var core = ModEntities.CORE_ENTITY_TYPE.get().create(ServerGameManager.getLevel());
            core.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            ServerGameManager.getLevel().addFreshEntity(core);
            NetworkHelper.sendToAllPlayers(new SubtitlePacket(DirectionHelper.getName(DirectionHelper.getPyramidRegion(pos)) + " 已激活新核心"));
        }
    }
}
