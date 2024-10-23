package gravity_changer.mixin;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin extends Entity {
    public PrimedTntMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

}
