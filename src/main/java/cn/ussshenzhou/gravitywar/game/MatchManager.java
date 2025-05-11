package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.entity.CoreEntity;
import cn.ussshenzhou.gravitywar.entity.ModEntities;
import cn.ussshenzhou.gravitywar.network.s2c.ChangePhasePacket;
import cn.ussshenzhou.gravitywar.network.s2c.IntruderModeConfigPacket;
import cn.ussshenzhou.gravitywar.network.s2c.RandomEventPacket;
import cn.ussshenzhou.gravitywar.network.s2c.TeamFailPacket;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.StreamSupport;

import static cn.ussshenzhou.gravitywar.game.GameManager.*;
import static cn.ussshenzhou.gravitywar.game.ServerGameManager.*;
import static cn.ussshenzhou.gravitywar.game.ClientGameManager.*;

/**
 * @author USS_Shenzhou
 */
public abstract class MatchManager {
    private int tick = 0;

    public void startServer() {
        phasePrep();
        TaskHelper.addServerTask(this::phaseBattle, getConfig().preparePhase * 20);
        TaskHelper.addServerTask(this::phaseFinal, (getConfig().preparePhase + getConfig().battlePhase) * 20);
    }

    public void phasePrep() {
        var pkt = new ChangePhasePacket(MatchPhase.PREP);
        forEachS(p -> {
            NetworkHelper.sendToPlayer(p, pkt);
            p.load(new CompoundTag());
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

    public static ArmorStand FAKE_OWNER = null;

    public void serverTick() {
        //autoGravityDirection();

        //random event
        if (phase == MatchPhase.FINAL) {
            if (tick % (120 * 20) == 0) {
                do {
                    event = RandomEvent.values()[ThreadLocalRandom.current().nextInt(RandomEvent.values().length)];
                } while (event == lastEvent);
                lastEvent = event;
                NetworkHelper.sendToAllPlayers(new RandomEventPacket(event));
                TaskHelper.addServerTask(() -> event = null, event.time);
                switch (event) {
                    case FOG -> {
                        //none
                    }
                    case RANDOM_GRAVITY -> {
                        TaskHelper.addServerTask(() -> {
                            forEachS(p -> {
                                var dir = Direction.values()[p.getUUID().hashCode() / 6];
                                GravityChangerAPIProxy.setBaseGravityDirection(p, dir);
                            });
                        }, 10 * 20);
                        TaskHelper.addServerTask(() -> {
                            PLAYER_TO_TEAM.entrySet().stream()
                                    .forEach(e -> {
                                        getPlayerS(e.getKey()).ifPresent(p -> GravityChangerAPIProxy.setBaseGravityDirection(p, e.getValue()));
                                    });
                        }, event.time * 20);
                    }
                    case LOW_GRAVITY -> {
                        forEachS(p -> {
                            GravityChangerAPIProxy.setBaseGravityStrength(p, 0.15);
                        });
                        TaskHelper.addServerTask(() -> {
                            forEachS(p -> {
                                GravityChangerAPIProxy.setBaseGravityStrength(p, 1);
                            });
                        }, event.time * 20);
                    }
                    case RESPAWN_BEACON -> {
                        //TODO
                    }
                    case FIREBALL -> {
                        //none
                    }
                    case CORE_REVIVE -> {
                        //TODO
                    }
                    case HIGH_KNOCKBACK -> {
                        //none
                    }
                    case ULTRA_BOUNCE -> {
                        //none
                    }
                }
            }
            tick++;
        } else {
            tick = 0;
        }
        if (event == RandomEvent.FIREBALL) {
            if (FAKE_OWNER == null) {
                FAKE_OWNER = EntityType.ARMOR_STAND.create(getLevel());
                FAKE_OWNER.setCustomName(Component.empty());
            }
            var random = ThreadLocalRandom.current();
            var vec = new Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble());
            LargeFireball largefireball = new LargeFireball(getLevel(), FAKE_OWNER, vec.normalize(), 2);
            largefireball.setPos(0, 0, 0);
            getLevel().playSeededSound(null, 0, 0, 0, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 1, 1, 42L);
            getLevel().addFreshEntity(largefireball);
        }
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

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        AddParticleHelper.addParticleClient(
                ParticleTypes.ASH, SpriteFrom.RANDOM,
                400, InheritableBoolean.TRUE, 10,
                0, 0, 0, 3.0f, 3.0f, 3.0f,
                0, 0, 0, 0.02f, 0.02f, 0.02f,
                0.99f, 0, InheritableBoolean.FALSE, 0, 0, 0, 0, 0, InheritableBoolean.FALSE,
                0, 0, ParticleRenderTypes.INSTANCED, 1, 0.711f, 0.270f, 1, 1, ChangeMode.LINEAR,
                5, 5, ChangeMode.LINEAR, false, null, 0.0001f, 0, 0, 0, 0, 6, new CompoundTag()
        );
        if (event == RandomEvent.FOG) {
            StreamSupport.stream(getMC().level.getEntities().getAll().spliterator(), false)
                    .filter(entity -> entity instanceof CoreEntity)
                    .forEach(entity -> {
                        var pos = entity.position();
                        AddParticleHelper.addParticleClient(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE, SpriteFrom.RANDOM,
                                100, InheritableBoolean.TRUE, 15,
                                (float) pos.x, (float) pos.y, (float) pos.z, 10.0f, 10.0f, 10.0f,
                                0, 0, 0, 0.03f, 0.03f, 0.03f,
                                0.995f, 0, InheritableBoolean.FALSE, 0, 0, 0, 0, 0, InheritableBoolean.FALSE,
                                0, 0, ParticleRenderTypes.INSTANCED, 1, 1, 1, 0.3f, 0.2f, ChangeMode.LINEAR,
                                20, 20, ChangeMode.LINEAR, false, null, 0.0001f, 0, 0, 0, 0, 1, new CompoundTag()
                        );
                    });
        }
    }

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
                    if (posList == null) {
                        return;
                    }
                    var pos = posList.get(ThreadLocalRandom.current().nextInt(posList.size()));
                    teleportWithDiffuse(p, pos);
                    GravityChangerAPIProxy.setBaseGravityDirection(p, direction);
                    p.setGameMode(GameType.SURVIVAL);
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
            var cfg = getConfig();
            //borderCheck
            if (phase == MatchPhase.PREP) {
                forEachS(p -> {
                    var d = DirectionHelper.distanceToBoundary(p.getEyePosition());
                    if (d <= 0.5) {
                        var posList = getConfig().corePos.get(PLAYER_TO_TEAM.get(p.getUUID()));
                        if (posList != null) {
                            teleportWithDiffuse(p, posList.get(ThreadLocalRandom.current().nextInt(posList.size())));
                        }
                        return;
                    }
                    if (d >= 4.5) {
                        return;
                    }
                    d = Mth.clamp(d, 0, 4.5);
                    d = (4.5 - d) * 7;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, (int) d, true, false));
                });
            }
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
                if (TEAM_TO_PLAYER.get(team) == null) {
                    continue;
                }
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
            super.clientTick();
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
            super.clientTick();
        }
    }
}
