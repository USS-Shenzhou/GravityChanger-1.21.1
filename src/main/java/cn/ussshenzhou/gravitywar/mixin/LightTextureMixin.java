package cn.ussshenzhou.gravitywar.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author USS_Shenzhou
 */
@Mixin(LightTexture.class)
public class LightTextureMixin {

    @ModifyArg(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"),index = 2)
    private int onUpdateLightTexture(int x) {
        return 0xffffffff;
    }
}
