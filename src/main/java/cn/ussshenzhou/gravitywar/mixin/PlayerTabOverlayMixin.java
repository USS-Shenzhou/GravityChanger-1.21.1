package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author USS_Shenzhou
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

    @ModifyReturnValue(method = "getNameForDisplay", at = @At("RETURN"))
    private Component gwGetNameForDisplay(Component original, @Local(argsOnly = true) PlayerInfo playerInfo) {
        if (GameManager.PLAYER_TO_TEAM.containsKey(playerInfo.getProfile().getId())) {
            original.getStyle().withColor(ColorHelper.getRGB(GameManager.PLAYER_TO_TEAM.get(playerInfo.getProfile().getId())));
        }
        return original;
    }
}
