package cn.ussshenzhou.gravitywar.entity;

import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.madparticle.api.AddParticleHelper;
import cn.ussshenzhou.madparticle.command.inheritable.InheritableBoolean;
import cn.ussshenzhou.madparticle.particle.enums.ChangeMode;
import cn.ussshenzhou.madparticle.particle.enums.ParticleRenderTypes;
import cn.ussshenzhou.madparticle.particle.enums.SpriteFrom;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
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
            AddParticleHelper.addParticleClient(
                    ParticleTypes.WHITE_ASH,
                    SpriteFrom.RANDOM,
                    40,
                    InheritableBoolean.TRUE,
                    (int) ((getMaxHealth() - getHealth()) / getMaxHealth() * 10),
                    pos.x, pos.y, pos.z, 0.2f, 0.2f, 0.2f,
                    0, 0, 0, 0.1f, 0.1f, 0.1f,
                    0.985f, 0.0f, InheritableBoolean.FALSE, 0, 0, 0, 0, 0,
                    InheritableBoolean.FALSE, 0, 0, ParticleRenderTypes.INSTANCED,
                    color.x, color.y, color.z,
                    1, 1, ChangeMode.LINEAR,
                    2f, 2f, ChangeMode.LINEAR,
                    false, null,
                    0.01f,
                    0, 0, 0, 0,
                    1,
                    tag
            );
        }

        super.tick();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason == Entity.RemovalReason.KILLED) {
            level().explode(this,
                    level().damageSources().explosion(getLastHurtByMob(), getLastHurtByMob()),
                    new SimpleExplosionDamageCalculator(true, true, Optional.of(0.5f), Optional.empty()),
                    this.position(),
                    4,
                    false,
                    Level.ExplosionInteraction.BLOCK
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
}
