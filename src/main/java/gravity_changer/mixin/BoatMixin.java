package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public class BoatMixin {
    @Inject(method = "getDefaultGravity", at = @At("RETURN"), cancellable = true)
    protected void modifyGravity(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(cir.getReturnValue() * GravityChangerAPI.getGravityStrength(((Entity) (Object) this)));
    }
}
