package cn.ussshenzhou.gravitywar.input;

import cn.ussshenzhou.gravitywar.gui.*;
import cn.ussshenzhou.t88.gui.HudManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ModKeyInput {
    public static final KeyMapping PLAY_SCREEN = new KeyMapping(
            "打开主界面", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.categories.gravitywar"
    );
    public static final KeyMapping OP_SCREEN = new KeyMapping(
            "打开管理员界面", KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.categories.gravitywar"
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (PLAY_SCREEN.consumeClick()) {
            minecraft.setScreen(new MainScreen());
        } else if (OP_SCREEN.consumeClick() && minecraft.player != null && minecraft.player.hasPermissions(4)) {
            minecraft.setScreen(new OpScreen());
        }
        HudManager.addIfSameClassNotExist(new CoreModeHUD());
    }
}
