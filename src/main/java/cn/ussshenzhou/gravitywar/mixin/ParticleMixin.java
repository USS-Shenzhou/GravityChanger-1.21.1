package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.madparticle.util.AABBHelper;
import cn.ussshenzhou.madparticle.util.MovementHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@Mixin(Particle.class)
public abstract class ParticleMixin {
    @Shadow
    protected double xo;
    @Shadow
    protected double yo;
    @Shadow
    protected double zo;
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;
    @Shadow
    protected double xd;
    @Shadow
    protected double yd;
    @Shadow
    protected double zd;
    @Shadow
    protected boolean onGround;
    @Shadow
    protected int age;
    @Shadow
    protected int lifetime;
    @Shadow
    protected float gravity;
    @Shadow
    protected float friction = 0.98F;
    @Shadow
    protected boolean speedUpWhenYMotionIsBlocked = false;

    @Shadow
    public abstract void remove();

    @Shadow
    private boolean stoppedByCollision;

    @Shadow
    protected boolean hasPhysics;

    @Shadow
    @Final
    private static double MAXIMUM_COLLISION_VELOCITY_SQUARED;

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    public abstract void setBoundingBox(AABB bb);

    @Shadow
    protected abstract void setLocationFromBoundingbox();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            handleGravity();
            this.move(this.xd, this.yd, this.zd);
            if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd = this.xd * (double) this.friction;
            this.yd = this.yd * (double) this.friction;
            this.zd = this.zd * (double) this.friction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }
        }
    }

    @Unique
    private void handleGravity() {
        switch (DirectionHelper.getPyramidRegion(this.x, this.y, this.z)) {
            case DOWN -> this.yd = this.yd - 0.04 * (double) this.gravity;
            case UP -> this.yd = this.yd + 0.04 * (double) this.gravity;
            case NORTH -> this.zd = this.zd - 0.04 * (double) this.gravity;
            case SOUTH -> this.zd = this.zd + 0.04 * (double) this.gravity;
            case WEST -> this.xd = this.xd - 0.04 * (double) this.gravity;
            case EAST -> this.xd = this.xd + 0.04 * (double) this.gravity;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void move(double x, double y, double z) {
        if (!this.stoppedByCollision) {
            double d0 = x;
            double d1 = y;
            double d2 = z;
            if (this.hasPhysics
                    && (x != 0.0 || y != 0.0 || z != 0.0)
                    && x * x + y * y + z * z < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
                x = vec3.x;
                y = vec3.y;
                z = vec3.z;
            }

            if (x != 0.0 || y != 0.0 || z != 0.0) {
                this.setBoundingBox(this.getBoundingBox().move(x, y, z));
                this.setLocationFromBoundingbox();
            }

            switch (DirectionHelper.getPyramidRegion(this.x, this.y, this.z)) {
                case DOWN, UP -> {
                    if (Math.abs(d1) >= 1.0E-5F && Math.abs(y) < 1.0E-5F) {
                        this.stoppedByCollision = true;
                    }

                    this.onGround = d1 != y && d1 < 0.0;
                    if (d0 != x) {
                        this.xd = 0.0;
                    }

                    if (d2 != z) {
                        this.zd = 0.0;
                    }
                }
                case NORTH, SOUTH -> {
                    if (Math.abs(d2) >= 1.0E-5F && Math.abs(z) < 1.0E-5F) {
                        this.stoppedByCollision = true;
                    }

                    this.onGround = d2 != z && d2 < 0.0;
                    if (d0 != x) {
                        this.xd = 0.0;
                    }

                    if (d1 != y) {
                        this.yd = 0.0;
                    }
                }
                case WEST, EAST -> {
                    if (Math.abs(d0) >= 1.0E-5F && Math.abs(x) < 1.0E-5F) {
                        this.stoppedByCollision = true;
                    }

                    this.onGround = d0 != x && d0 < 0.0;
                    if (d1 != y) {
                        this.yd = 0.0;
                    }

                    if (d2 != z) {
                        this.zd = 0.0;
                    }
                }
            }
        }
    }
}
