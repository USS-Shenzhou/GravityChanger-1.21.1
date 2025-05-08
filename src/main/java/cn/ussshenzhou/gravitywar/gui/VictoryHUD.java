package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class VictoryHUD extends AutoCloseHintHUD {
    public VictoryHUD(Direction victory) {
        super(switch (GameManager.mode) {
            case CORE -> "死斗";
            case INTRUDER -> "入侵";
            case SIEGE -> "围攻";
        }, "", "");

        phase.setText(Component.literal(DirectionHelper.getName(victory) + " 胜利").withColor(ColorHelper.getRGB(victory)));
    }
}
