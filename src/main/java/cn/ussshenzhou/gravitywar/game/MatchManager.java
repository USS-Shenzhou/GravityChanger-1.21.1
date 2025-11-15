package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.entity.CoreEntity;
import cn.ussshenzhou.gravitywar.entity.ModEntities;
import cn.ussshenzhou.gravitywar.network.UtilS;
import cn.ussshenzhou.gravitywar.network.s2c.ChangePhasePacket;
import cn.ussshenzhou.gravitywar.network.s2c.IntruderModeConfigPacket;
import cn.ussshenzhou.gravitywar.network.s2c.RandomEventPacket;
import cn.ussshenzhou.gravitywar.network.s2c.TeamFailPacket;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.madparticle.api.AddParticleHelperC;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static cn.ussshenzhou.gravitywar.game.GameManager.*;
import static cn.ussshenzhou.gravitywar.game.ServerGameManager.*;
import static cn.ussshenzhou.gravitywar.game.ClientGameManager.*;

/**
 * @author USS_Shenzhou
 */
public abstract class MatchManager {
    private int tick = 0;
    private BlockPos poi = null;

    public void startServer() {
        phasePrep();
        //UtilS.delay(this::phaseBattle, getConfig().preparePhase * 20);
        //UtilS.delay(this::phaseFinal, (getConfig().preparePhase + getConfig().battlePhase) * 20);
        CompletableFuture
                .runAsync(
                        () -> getServer().execute(this::phaseBattle),
                        CompletableFuture.delayedExecutor(getConfig().preparePhase, TimeUnit.SECONDS)
                );
        CompletableFuture
                .runAsync(
                        () -> getServer().execute(this::phaseFinal),
                        CompletableFuture.delayedExecutor(getConfig().preparePhase + getConfig().battlePhase, TimeUnit.SECONDS)
                );
    }

    public void phasePrep() {
        var pkt = new ChangePhasePacket(MatchPhase.PREP);
        phase = MatchPhase.PREP;
        forEachS(p -> {
            NetworkHelper.sendToPlayer(p, pkt);
            p.load(new CompoundTag());
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200, 1, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 20, 5, false, true));
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
        StreamSupport.stream(getLevel().getEntities().getAll().spliterator(), false)
                .filter(entity -> entity instanceof CoreEntity)
                .toList()
                .forEach(core -> {
                    getLevel().explode(core,
                            null,
                            new SimpleExplosionDamageCalculator(true, true, Optional.of(0.5f), Optional.empty()),
                            core.position(),
                            3,
                            false,
                            Level.ExplosionInteraction.BLOCK);
                    core.remove(Entity.RemovalReason.KILLED);
                });
    }

    public static ArmorStand FAKE_OWNER = null;

    private static ArrayList<RandomEvent> events = new ArrayList<>();

    public void serverTick() {
        if (phase != MatchPhase.CHOOSE) {
            forEachS(p -> {
                if (p.position().distanceToSqr(0, 0, 0) + p.getEyePosition().distanceToSqr(0, 0, 0) <= 16 + 16) {
                    p.setRemainingFireTicks(60);
                }
            });
        }
        autoGravityDirection();
        //random event
        if (phase == MatchPhase.BATTLE) {
            if (tick > 0 && tick % (100 * 20) == 0) {
                if (events.isEmpty()) {
                    events.addAll(List.of(RandomEvent.values()));
                }
                event = events.get(ThreadLocalRandom.current().nextInt(events.size()));
                events.remove(event);
                lastEvent = event;
                NetworkHelper.sendToAllPlayers(new RandomEventPacket(event));
                UtilS.delay(() -> event = null, event.time);
                switch (event) {
                    case FOG -> {
                        //none
                    }
                    case RANDOM_GRAVITY -> {
                        UtilS.delay(() -> {
                            forEachS(p -> {
                                var dir = Direction.values()[Mth.abs(p.getUUID().hashCode()) / 6];
                                GravityChangerAPIProxy.setBaseGravityDirection(p, dir);
                            });
                        }, 10);
                        UtilS.delay(() -> {
                            PLAYER_TO_TEAM.entrySet().stream()
                                    .forEach(e -> {
                                        getPlayerS(e.getKey()).ifPresent(p -> GravityChangerAPIProxy.setBaseGravityDirection(p, e.getValue()));
                                    });
                        }, event.time);
                    }
                    case LOW_GRAVITY -> {
                        forEachS(p -> {
                            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60 * 20, 6, false, false));
                            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60 * 20, 0, false, false));
                        });
                    }
                    case RESPAWN_BEACON -> {
                        var random = ThreadLocalRandom.current();
                        var pos = new BlockPos(random.nextInt(120) - 60, random.nextInt(120) - 60, random.nextInt(120) - 60);
                        poi = pos;
                        getLevel().setBlock(pos, Blocks.BARREL.defaultBlockState(), 1 | 2);
                        BarrelBlockEntity barrelBlockEntity = (BarrelBlockEntity) getLevel().getBlockEntity(pos);
                        var item = new ItemStack(Items.BEACON);
                        item.set(DataComponents.ITEM_NAME, Component.literal("价值3000分的复活信标"));
                        item.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("放置于地图的任何地方，确保周围有足够的空间。\n立即复活等待中的队友，并持续作为队伍的优先复活点。"))));
                        if (barrelBlockEntity != null) {
                            barrelBlockEntity.setItem(0, item);
                        } else {
                            UtilS.delay(() -> getServer().execute(() -> {
                                BarrelBlockEntity b = (BarrelBlockEntity) getLevel().getBlockEntity(pos);
                                if (b != null) {
                                    b.setItem(0, item);
                                }
                            }), 1);
                        }
                    }
                    case FIREBALL -> {
                        //none
                    }
                    case CORE_REVIVE -> {
                        var random = ThreadLocalRandom.current();
                        var pos = new BlockPos(random.nextInt(120) - 60, random.nextInt(120) - 60, random.nextInt(120) - 60);
                        poi = pos;
                        getLevel().setBlock(pos, Blocks.BARREL.defaultBlockState(), 1 | 2);
                        BarrelBlockEntity barrelBlockEntity = (BarrelBlockEntity) getLevel().getBlockEntity(pos);
                        var item = new ItemStack(Items.END_CRYSTAL);
                        item.set(DataComponents.ITEM_NAME, Component.literal("备用隐藏能源").withColor(0xFF6C14));
                        item.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("带回并放置于你的队伍对应的区域"))));
                        if (barrelBlockEntity != null) {
                            barrelBlockEntity.setItem(0, item);
                        } else {
                            UtilS.delay(() -> getServer().execute(() -> {
                                BarrelBlockEntity b = (BarrelBlockEntity) getLevel().getBlockEntity(pos);
                                if (b != null) {
                                    b.setItem(0, item);
                                }
                            }), 1);
                        }
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
            tick = -6060;
        }
        if (event == RandomEvent.FIREBALL) {
            if (FAKE_OWNER == null) {
                FAKE_OWNER = EntityType.ARMOR_STAND.create(getLevel());
                FAKE_OWNER.setCustomName(Component.empty());
            }
            var random = ThreadLocalRandom.current();
            var vec = new Vec3(2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1, 2 * random.nextDouble() - 1);
            LargeFireball largefireball = new LargeFireball(getLevel(), FAKE_OWNER, vec.normalize(), 2){
                @Override
                public boolean displayFireAnimation() {
                    return false;
                }
            };
            largefireball.setPos(0, 0, 0);
            getLevel().playSeededSound(null, 0, 0, 0, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 1, 1, 42L);
            getLevel().addFreshEntity(largefireball);
        }

        if (poi != null && (event == RandomEvent.RESPAWN_BEACON || event == RandomEvent.CORE_REVIVE)) {
            var entity = getLevel().getBlockEntity(poi);
            if (entity instanceof BarrelBlockEntity e && e.hasAnyOf(Set.of(Items.BEACON, Items.END_CRYSTAL))) {
                getServer().getCommands().performPrefixedCommand(getServer().createCommandSourceStack().withPosition(new Vec3(poi.getX(), poi.getY(), poi.getZ())),
                        "mp minecraft:ash RANDOM 350 TRUE 30 ~ ~ ~ 0.0 0.0 0.0 0.0 0.0 0.0 0.25 0.25 0.25 FALSE 0 0 0 1.0 1.0 0.0 0.0 0 0 0 0 0.00001 FALSE 0 0 INSTANCED 0.446 0.088 0.861 6 1 1 LINEAR 1.00 4.00 LINEAR @a {\"indexed\":1,\"tenet\":1}");
            }
        }
    }

    protected void autoGravityDirection() {
        getLevel().getAllEntities().forEach(entity -> {
            if (entity instanceof Player player) {
                if (!PLAYER_TO_TEAM.containsKey(player.getUUID())) {
                    return;
                }
                var tags = entity.getTags();
                if (!tags.contains("gw_auto_rot")) {
                    return;
                }
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
                                tagTo[1] = "gw_rot_cd_" + 40;
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
    }

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        AddParticleHelperC.addParticleClient(
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
                        AddParticleHelperC.addParticleClient(
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
                    if (d <= 0.25) {
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
                    d = (4.5 - d) / 4.5 * 6;
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, (int) d, true, false));
                });
            }
            //victory check
            if (System.currentTimeMillis() - ServerGameManager.startMs >= (cfg.preparePhase + cfg.battlePhase + cfg.finalPhase) * 1000L) {
                ServerGameManager.end();
                return;
            }
            if (teamsOnGround.isEmpty()) {
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
                    message.append(DirectionHelper.getTeamName(o)).append(' ');
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
    public static class Intruder extends Core {

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
