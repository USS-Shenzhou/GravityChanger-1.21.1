package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author USS_Shenzhou
 */
@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @ModifyArg(method = "renderNameTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", ordinal = 1),
            index = 3
    )
    private int gwChangeColor(int color, @Local(argsOnly = true) T entity) {
        var team = GameManager.getTeam(entity.getUUID());
        return team.map((Direction direction) -> ColorHelper.getARGB(direction, 0xff))
                .orElse(color);
    }
}
