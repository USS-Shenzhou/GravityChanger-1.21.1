package cn.ussshenzhou.gravitywar.mixin;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author USS_Shenzhou
 */
@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {

    @Inject(method = "isVisibleAccordingToBookData", at = @At("HEAD"), cancellable = true)
    private void keepInvisible(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        cir.cancel();
    }
}
