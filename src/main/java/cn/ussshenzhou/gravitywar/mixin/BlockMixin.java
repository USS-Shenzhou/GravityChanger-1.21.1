package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.game.RandomEvent;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "updateEntityAfterFallOn", at = @At("HEAD"), cancellable = true)
    private void gwBounce(BlockGetter level, Entity entity, CallbackInfo ci) {
        if (GameManager.event == RandomEvent.ULTRA_BOUNCE) {
            ci.cancel();
            bounceUp(entity);
        }
    }

    @Unique
    private void bounceUp(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        var dir = GravityChangerAPIProxy.getGravityDirection(entity);
        if (dir != null) {
            switch (dir) {
                case UP, DOWN:
                    entity.setDeltaMovement(vec3.x, -vec3.y * 0.8, vec3.z);
                case NORTH, SOUTH:
                    entity.setDeltaMovement(vec3.x, vec3.y, -vec3.z * 0.8);
                case EAST, WEST:
                    entity.setDeltaMovement(-vec3.x * 0.8, vec3.y, vec3.z);
            }
        }
    }
}
