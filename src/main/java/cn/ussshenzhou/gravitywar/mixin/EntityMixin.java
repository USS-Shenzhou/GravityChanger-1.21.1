package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.gravitywar.util.InterimCalculation;
import cn.ussshenzhou.gravitywar.util.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author USS_Shenzhou
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityExtension {

    @Shadow
    public abstract boolean touchingUnloadedChunk();

    @Shadow
    public abstract AABB getBoundingBox();

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Shadow
    @Deprecated
    public abstract boolean isPushedByFluid();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vec3 deltaMovement);

    @Shadow
    protected abstract void setFluidTypeHeight(FluidType type, double height);

    @Unique
    private Vec3 gravityChangerCompat0(Entity instance) {
        var vec3d = instance.getDeltaMovement();
        Direction gravityDirection = GravityChangerAPIProxy.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecPlayerToWorld(vec3d, gravityDirection);
    }

    @Unique
    private Vec3 gravityChangerCompat1(Vec3 vec3d) {
        Direction gravityDirection = GravityChangerAPIProxy.getGravityDirection((Entity) (Object) this);
        if (gravityDirection == Direction.DOWN) {
            return vec3d;
        }

        return RotationUtil.vecWorldToPlayer(vec3d, gravityDirection);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateFluidHeightAndDoFluidPushing() {
        if (this.touchingUnloadedChunk()) {
            return;
        } else {
            AABB aabb = this.getBoundingBox().deflate(0.001);
            int i = Mth.floor(aabb.minX);
            int j = Mth.ceil(aabb.maxX);
            int k = Mth.floor(aabb.minY);
            int l = Mth.ceil(aabb.maxY);
            int i1 = Mth.floor(aabb.minZ);
            int j1 = Mth.ceil(aabb.maxZ);
            double d0 = 0.0;
            boolean flag = this.isPushedByFluid();
            boolean flag1 = false;
            Vec3 vec3 = Vec3.ZERO;
            int k1 = 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            it.unimi.dsi.fastutil.objects.Object2ObjectMap<net.neoforged.neoforge.fluids.FluidType, InterimCalculation> interimCalcs = null;

            for (int l1 = i; l1 < j; l1++) {
                for (int i2 = k; i2 < l; i2++) {
                    for (int j2 = i1; j2 < j1; j2++) {
                        blockpos$mutableblockpos.set(l1, i2, j2);
                        FluidState fluidstate = this.level().getFluidState(blockpos$mutableblockpos);
                        net.neoforged.neoforge.fluids.FluidType fluidType = fluidstate.getFluidType();
                        if (!fluidType.isAir()) {
                            double d1 = (double) ((float) i2 + fluidstate.getHeight(this.level(), blockpos$mutableblockpos));
                            if (d1 >= aabb.minY) {
                                flag1 = true;
                                if (interimCalcs == null) {
                                    interimCalcs = new it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap<>();
                                }
                                InterimCalculation interim = interimCalcs.computeIfAbsent(fluidType, t -> new InterimCalculation());
                                interim.fluidHeight = Math.max(d1 - aabb.minY, interim.fluidHeight);
                                if (this.isPushedByFluid(fluidType)) {
                                    Vec3 vec31 = fluidstate.getFlow(this.level(), blockpos$mutableblockpos);
                                    if (interim.fluidHeight < 0.4D) {
                                        vec31 = vec31.scale(interim.fluidHeight);
                                    }

                                    interim.flowVector = interim.flowVector.add(vec31);
                                    interim.blockCount++;
                                }
                            }
                        }
                    }
                }
            }

            if (interimCalcs != null) {
                interimCalcs.forEach((fluidType, interim) -> {
                    if (interim.flowVector.length() > 0.0D) {
                        if (interim.blockCount > 0) {
                            interim.flowVector = interim.flowVector.scale(1.0D / (double) interim.blockCount);
                        }

                        if (!((Entity) (Object) this instanceof Player)) {
                            interim.flowVector = interim.flowVector.normalize();
                        }

                        //noinspection DataFlowIssue
                        Vec3 vec32 = this.gravityChangerCompat0((Entity) (Object) this);
                        interim.flowVector = interim.flowVector.scale(this.getFluidMotionScale(fluidType));
                        double d2 = 0.003;
                        if (Math.abs(vec32.x) < 0.003D && Math.abs(vec32.z) < 0.003D && interim.flowVector.length() < 0.0045000000000000005D) {
                            interim.flowVector = interim.flowVector.normalize().scale(0.0045000000000000005D);
                        }
                        //noinspection DataFlowIssue
                        this.setDeltaMovement(this.gravityChangerCompat0((Entity) (Object) this).add(gravityChangerCompat1(interim.flowVector)));
                    }

                    this.setFluidTypeHeight(fluidType, interim.fluidHeight);
                });
            }
        }
    }

}
