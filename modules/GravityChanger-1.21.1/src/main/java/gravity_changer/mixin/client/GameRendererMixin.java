package gravity_changer.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gravity_changer.RotationAnimation;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;
    
    /*@WrapOperation(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;"
        ),
        remap = false
    )
    private Matrix4f inject_renderWorld(Matrix4f matrix, Quaternionfc quat, Operation<Matrix4f> original, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        matrix = original.call(matrix, quat);
        if (this.mainCamera.getEntity() != null) {
            Entity focusedEntity = this.mainCamera.getEntity();
            Direction gravityDirection = GravityChangerAPI.getGravityDirection(focusedEntity);
            RotationAnimation animation = GravityChangerAPI.getRotationAnimation(focusedEntity);
            if (animation == null) {
                return matrix;
            }
            long timeMs = focusedEntity.level().getGameTime() * 50 + (long) (deltaTracker.getGameTimeDeltaTicks() * 50);
            Quaternionf currentGravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs);
    
            if (animation.isInAnimation()) {
                // make sure that frustum culling updates when running rotation animation
                Minecraft.getInstance().levelRenderer.needsUpdate();
            }

            matrix = matrix.rotation(currentGravityRotation);
        }
        return matrix;
    }*/
}
