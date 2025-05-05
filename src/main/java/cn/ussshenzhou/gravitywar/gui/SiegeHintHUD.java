package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;

import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public abstract class SiegeHintHUD {
    public static class Prep extends AutoCloseHintHUD {
        public Prep() {
            super("围攻", "准备阶段", "熟悉地形和操作，收集资源，打造装备");
        }
    }

    public static class Battle extends AutoCloseHintHUD {
        public Battle() {
            super("围攻", "战斗阶段", "");
            StringBuilder text = new StringBuilder();
            if (Minecraft.getInstance().player == null) {
                return;
            }
            if (Minecraft.getInstance().player.getPermissionLevel() > 0) {
                text.append("抵御全体玩家们的进攻!");
            } else {
                text.append("所有玩家，联合起来!");
                GameManager.TEAM_TO_PLAYER.entrySet().stream()
                        .map(entry -> new Tuple<>(entry.getKey(), entry.getValue().stream().findAny()))
                        .filter(tuple -> tuple.getB().isPresent() && ClientGameManager.getPlayerC(tuple.getB().get()).map(p -> p.hasPermissions(4)).orElse(false))
                        .findAny()
                        .ifPresent(tuple -> text.append("一起进攻up主所在的")
                                .append(DirectionHelper.getName(tuple.getA()))
                                .append("!")
                        );
            }
            this.desc.setText(Component.literal(text.toString()));
        }
    }

    public static class Final extends AutoCloseHintHUD {
        public Final() {
            super("围攻", "决胜阶段", "XDM加油冲冲冲!");
        }
    }
}
