package cn.ussshenzhou.gravitywar.gui;

/**
 * @author USS_Shenzhou
 */
public class SubtitleHUD extends AutoCloseHintHUD {
    public SubtitleHUD(String text) {
        super("", "", text);
        life = 6 * 20;
    }
}
