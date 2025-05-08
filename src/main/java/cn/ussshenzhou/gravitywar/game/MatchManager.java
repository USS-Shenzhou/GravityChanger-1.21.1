package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.entity.CoreEntity;
import cn.ussshenzhou.gravitywar.entity.ModEntities;
import cn.ussshenzhou.gravitywar.network.s2c.ChangePhasePacket;
import cn.ussshenzhou.gravitywar.network.s2c.IntruderModeConfigPacket;
import cn.ussshenzhou.gravitywar.network.s2c.TeamFailPacket;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static cn.ussshenzhou.gravitywar.game.GameManager.*;
import static cn.ussshenzhou.gravitywar.game.ServerGameManager.*;

/**
 * @author USS_Shenzhou
 */
public abstract class MatchManager {
    private long startMs = 0;

    public void startServer() {
        phasePrep();
        CompletableFuture
                .runAsync(
                        this::phaseBattle,
                        CompletableFuture.delayedExecutor(getConfig().preparePhase, TimeUnit.SECONDS)
                );
        CompletableFuture
                .runAsync(
                        this::phaseFinal,
                        CompletableFuture.delayedExecutor(getConfig().preparePhase + getConfig().battlePhase, TimeUnit.SECONDS)
                );
    }

    public void phasePrep() {
        var pkt = new ChangePhasePacket(MatchPhase.PREP);
        forEachS(p -> {
            NetworkHelper.sendToPlayer(p, pkt);
        });
    }

    public void phaseBattle() {
        if (phase != MatchPhase.PREP) {
            return;
        }
        phase = MatchPhase.BATTLE;
        var pkt = new ChangePhasePacket(MatchPhase.BATTLE);
        forEachS(p -> NetworkHelper.sendToPlayer(p, pkt));
    }

    public void phaseFinal() {
        if (phase != MatchPhase.BATTLE) {
            return;
        }
        phase = MatchPhase.FINAL;
        var pkt = new ChangePhasePacket(MatchPhase.FINAL);
        forEachS(p -> NetworkHelper.sendToPlayer(p, pkt));
    }

    public void serverTick() {
        //autoGravityDirection();
    }

    /*protected void autoGravityDirection() {
        getLevel().getAllEntities().forEach(entity -> {
            if (entity instanceof Player player) {
                if (!PLAYER_TO_TEAM.containsKey(player.getUUID())) {
                    return;
                }
                var tags = entity.getTags();
                //remove, add
                String[] tagTo = {null, null};
                tags.stream()
                        .filter(s -> s.startsWith("gw_rot_cd_"))
                        .findAny()
                        .ifPresentOrElse(tag -> {
                            int t = Integer.parseInt(tag.replace("gw_rot_cd_", ""));
                            t--;
                            tagTo[0] = tag;
                            if (t > 0) {
                                tagTo[1] = "gw_rot_cd_" + t;
                            }
                        }, () -> {
                            var currentG = GravityChangerAPIProxy.getGravityDirection(entity);
                            var correctG = phase == MatchPhase.CHOOSE ? Direction.DOWN : DirectionHelper.getPyramidRegion(entity.getEyePosition());
                            if (currentG != correctG) {
                                GravityChangerAPIProxy.setBaseGravityDirection(entity, correctG);
                                entity.addTag("gw_rot_cd_" + 20);
                            }
                        });
                if (tagTo[0] != null) {
                    entity.removeTag(tagTo[0]);
                }
                if (tagTo[1] != null) {
                    entity.addTag(tagTo[1]);
                }
            } else {
                var currentG = GravityChangerAPIProxy.getGravityDirection(entity);
                var correctG = DirectionHelper.getPyramidRegion(entity.getEyePosition());
                if (currentG != correctG) {
                    GravityChangerAPIProxy.setBaseGravityDirection(entity, correctG);
                }
            }
        });
    }*/

    public abstract void clientTick();

    public static MatchManager create(MatchMode mode) {
        return switch (mode) {
            case CORE -> new Core();
            case INTRUDER -> new Intruder();
            case SIEGE -> new Siege();
        };
    }

    public static class Core extends MatchManager {

        @Override
        public void startServer() {
            super.startServer();
            PLAYER_TO_TEAM.forEach((uuid, direction) -> {
                getPlayerS(uuid).ifPresent(p -> {
                    var posList = getConfig().corePos.get(direction);
                    var pos = posList.get(ThreadLocalRandom.current().nextInt(posList.size()));
                    teleportWithDiffuse(p, pos);
                });
            });
            getConfig().corePos.forEach((direction, blockPos) -> {
                blockPos.forEach(p -> {
                    var core = ModEntities.CORE_ENTITY_TYPE.get().create(ServerGameManager.getLevel());
                    core.setPos(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    ServerGameManager.getLevel().addFreshEntity(core);
                });
            });
        }

        @Override
        public void serverTick() {
            super.serverTick();
            //borderCheck
            if (phase == MatchPhase.PREP) {
                forEachS(p -> {
                    var d = DirectionHelper.distanceToBoundary(p.getEyePosition());
                    if (d <= 0.5) {
                        var posList = getConfig().corePos.get(PLAYER_TO_TEAM.get(p.getUUID()));
                        teleportWithDiffuse(p, posList.get(ThreadLocalRandom.current().nextInt(posList.size())));
                        return;
                    }
                    d = Mth.clamp(d, 0, 4.5);
                    d = (4.5 - d) * 7;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, (int) d, true, false));
                });
            }
            var cfg = getConfig();
            //victory check
            if (System.currentTimeMillis() - ServerGameManager.startMs >= (cfg.preparePhase + cfg.battlePhase + cfg.finalPhase) * 1000L) {
                ServerGameManager.end();
                return;
            }
            int[] coreNumbers = new int[6];
            StreamSupport.stream(getLevel().getAllEntities().spliterator(), false)
                    .filter(entity -> entity instanceof CoreEntity)
                    .forEach(entity -> {
                        coreNumbers[DirectionHelper.getPyramidRegion(entity.position()).ordinal()]++;
                    });
            List<Direction> failed = new ArrayList<>();
            for (var team : teamsOnGround) {
                var playerNumber = TEAM_TO_PLAYER.get(team).parallelStream()
                        .map(ServerGameManager::getPlayerS)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(LivingEntity::isAlive)
                        .count();
                if (playerNumber == 0 && coreNumbers[team.ordinal()] == 0) {
                    failed.add(team);
                }
            }
            if (!failed.isEmpty()) {
                StringBuilder message = new StringBuilder();
                failed.forEach(o -> {
                    teamsOnGround.remove(o);
                    message.append(DirectionHelper.getName(o)).append(' ');
                });
                message.append("失败");
                NetworkHelper.sendToAllPlayers(new TeamFailPacket(message.toString()));
            }
        }

        @Override
        public void clientTick() {

        }
    }

    //TODO
    public static class Siege extends Core {

        @Override
        public void startServer() {
            super.startServer();
        }

        @Override
        public void serverTick() {
            super.serverTick();
        }

        @Override
        public void clientTick() {
            super.clientTick();
        }
    }

    //TODO
    public static class Intruder extends MatchManager {

        @Override
        public void startServer() {
            super.startServer();
            var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
            NetworkHelper.sendToAllPlayers(new IntruderModeConfigPacket(cfg.spotPos));
        }

        @Override
        public void serverTick() {
            super.serverTick();
            //borderCheck
            if (phase == MatchPhase.PREP) {
                forEachS(p -> {
                    var d = DirectionHelper.distanceToBoundary(p.getEyePosition());
                    if (d <= 0.5) {
                        var posList = getConfig().spawnPos.get(PLAYER_TO_TEAM.get(p.getUUID()));
                        teleportWithDiffuse(p, posList.get(ThreadLocalRandom.current().nextInt(posList.size())));
                        return;
                    }
                    d = Mth.clamp(d, 0, 4.5);
                    d = (4.5 - d) * 7;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, (int) d, true, false));
                });
            }
        }

        @Override
        public void clientTick() {

        }
    }
}
