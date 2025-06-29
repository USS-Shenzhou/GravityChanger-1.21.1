package cn.ussshenzhou.gravitywar.gui;

/**
 * @author USS_Shenzhou
 */
public abstract class CoreHintHUD {
    public static class Prep extends AutoCloseHintHUD {
        public Prep() {
            super("死斗", "准备阶段", "熟悉地形和操作\n挖掘矿石收集资源，找到村民进行交易");
            this.add(team);
        }
    }

    public static class Battle extends AutoCloseHintHUD {
        public Battle() {
            super("死斗", "战斗阶段", "地图上的末影水晶就是队伍核心\n摧毁敌方核心，保卫己方核心\n准备迎接随机事件!");
        }
    }

    public static class Final extends AutoCloseHintHUD {
        public Final() {
            super("死斗", "决胜阶段", "XDM加油冲冲冲!");
        }
    }
}
