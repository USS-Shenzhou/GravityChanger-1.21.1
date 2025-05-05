package cn.ussshenzhou.gravitywar.gui;

/**
 * @author USS_Shenzhou
 */
public abstract class IntruderHintHUD {
    public static class Prep extends AutoCloseHintHUD {
        public Prep() {
            super("入侵", "准备阶段", "熟悉地形和操作，收集资源，打造装备");
        }
    }

    public static class Battle extends AutoCloseHintHUD {
        public Battle() {
            super("入侵", "战斗阶段", "入侵敌方关键点，防守己方关键点");
        }
    }

    public static class Final extends AutoCloseHintHUD {
        public Final() {
            super("入侵", "决胜阶段", "XDM加油冲冲冲!");
        }
    }
}
