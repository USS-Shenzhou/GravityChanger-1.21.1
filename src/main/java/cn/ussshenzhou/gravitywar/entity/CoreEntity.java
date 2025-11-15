package cn.ussshenzhou.gravitywar.entity;

import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.madparticle.api.AddParticleHelperC;
import cn.ussshenzhou.madparticle.api.AddParticleHelperS;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
public class CoreEntity extends Mob {
    public int time;

    protected CoreEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setNoAi(true);
        this.time = this.random.nextInt(100000);
    }

    @Override
    public void tick() {
        this.time++;

        if (level().isClientSide) {
            var color = ColorHelper.getRGB3f(DirectionHelper.getPyramidRegion(this.position()));
            var tag = new CompoundTag();
            var pos = this.getEyePosition();
            AddParticleHelperC.addParticleClient(
                    ParticleTypes.WHITE_ASH,
                    SpriteFrom.RANDOM,
                    35,
                    InheritableBoolean.TRUE,
                    (int) ((getMaxHealth() - getHealth()) / getMaxHealth() * 10),
                    pos.x, pos.y, pos.z, 0.2f, 0.2f, 0.2f,
                    0, 0, 0, 0.1f, 0.1f, 0.1f,
                    0.98f, 0.0f, InheritableBoolean.FALSE, 0, 0, 0, 0, 0,
                    InheritableBoolean.FALSE, 0, 0, ParticleRenderTypes.INSTANCED,
                    color.x, color.y, color.z,
                    1, 1, ChangeMode.LINEAR,
                    1.25f, 1.25f, ChangeMode.LINEAR,
                    false, null,
                    0.01f,
                    0, 0, 0, 0,
                    1,
                    tag
            );
            AddParticleHelperC.addParticleClient(
                    ParticleTypes.WHITE_ASH,
                    SpriteFrom.RANDOM,
                    160,
                    InheritableBoolean.TRUE,
                    1,
                    pos.x, pos.y, pos.z, 0.2f, 0.2f, 0.2f,
                    0, 0, 0, 0.1f, 0.1f, 0.1f,
                    0.995f, 0.0f, InheritableBoolean.FALSE, 0, 0, 0, 0, 0,
                    InheritableBoolean.FALSE, 0, 0, ParticleRenderTypes.INSTANCED,
                    color.x, color.y, color.z,
                    1, 1, ChangeMode.LINEAR,
                    2f, 2f, ChangeMode.LINEAR,
                    false, null,
                    0.01f,
                    0, 0, 0, 0,
                    3,
                    tag
            );
        }

        super.tick();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            if (reason == RemovalReason.KILLED) {
                level().explode(this,
                        level().damageSources().explosion(getLastHurtByMob(), getLastHurtByMob()),
                        new SimpleExplosionDamageCalculator(true, true, Optional.of(0.5f), Optional.empty()),
                        this.position(),
                        3,
                        false,
                        Level.ExplosionInteraction.BLOCK
                );
                //var dir = DirectionHelper.getPyramidRegion(this.position());
                //ServerGameManager.forEachS(p -> {
                //    if (p.position().distanceTo(this.position()) <= 20) {
                //        GravityChangerAPIProxy.setBaseGravityDirection(p, dir);
                //    }
                //});
                if (getLastHurtByMob() instanceof Player player) {
                    var d = DirectionHelper.getPyramidRegion(getPosition(0));
                    if (d != null) {
                        var p = ServerGameManager.TEAM_TO_PLAYER.get(d);
                        if (p != null) {
                            p.stream()
                                    .map(ServerGameManager::getPlayerS)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .forEach(p0 -> p0.addTag("gw_auto_rot"));
                        }
                    }
                }
                ((ServerLevel) level()).playSeededSound(null, this, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BEACON_DEACTIVATE), SoundSource.BLOCKS, 0.8f, 1.3f, 42L);
            }
            var color = ColorHelper.getRGB3f(DirectionHelper.getPyramidRegion(this.position()));
            var tag = new CompoundTag();
            var pos = this.getEyePosition();
            AddParticleHelperS.addParticleServer(
                    ((ServerLevel) level()),
                    ParticleTypes.WHITE_ASH,
                    SpriteFrom.RANDOM,
                    40,
                    InheritableBoolean.TRUE,
                    400,
                    pos.x, pos.y, pos.z, 0.2f, 0.2f, 0.2f,
                    0, 0, 0, 0.8f, 0.8f, 0.8f,
                    0.999f, 0.0f, InheritableBoolean.FALSE, 0, 0, 0, 0, 0,
                    InheritableBoolean.FALSE, 0, 0, ParticleRenderTypes.INSTANCED,
                    color.x, color.y, color.z,
                    1, 0.2f, ChangeMode.LINEAR,
                    2f, 2f, ChangeMode.LINEAR,
                    false, null,
                    0.01f,
                    0, 0, 0, 0,
                    4,
                    tag
            );
        }
        super.remove(reason);
    }

    @Override
    public void move(MoverType type, Vec3 pos) {

    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source) ||
                source.is(DamageTypes.IN_WALL);
    }
}
