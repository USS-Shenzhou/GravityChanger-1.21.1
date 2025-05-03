package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.MatchMode;
import cn.ussshenzhou.gravitywar.network.c2s.SetModePacket;
import cn.ussshenzhou.gravitywar.network.c2s.StartSPacket;
import cn.ussshenzhou.t88.gui.advanced.TLabelButton;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class OpScreen extends TScreen {
    private final TLabelButton coreMod = new TLabelButton(Component.literal("死斗"), button -> {
        NetworkHelper.sendToServer(new SetModePacket(MatchMode.CORE));
    });
    private final TLabelButton intruderMod = new TLabelButton(Component.literal("入侵"), button -> {
        NetworkHelper.sendToServer(new SetModePacket(MatchMode.INTRUDER));
    });

    private final TButton start = new TButton(Component.literal("开始对局"), button -> {
        NetworkHelper.sendToServer(new StartSPacket());
    });
    private final TButton stop = new TButton(Component.literal("强制结束对局"), button -> {

    });
    private final TLabel title0 = new TLabel(Component.literal("重力战争"));
    private final TLabel title1 = new TLabel(Component.literal("管理员界面"));


    public OpScreen() {
        super(Component.empty());

        this.add(start);
        this.add(stop);
        this.add(coreMod);
        this.add(intruderMod);
        this.add(title0);
        title0.setFontSize(14);
        this.add(title1);
        coreMod.setFontSize(14);
        intruderMod.setFontSize(14);
    }

    @Override
    public void layout() {
        title0.setAbsBounds(10, 10, title0.getPreferredSize());
        LayoutHelper.BBottomOfA(title1, 2, title0, title1.getPreferredSize());
        coreMod.setBounds(
                (int) (width * 0.2),
                (int) (height * 0.2),
                (int) (width * 0.2),
                (int) (height * 0.6)
        );
        intruderMod.setBounds(
                (int) (width * 0.6),
                (int) (height * 0.2),
                (int) (width * 0.2),
                (int) (height * 0.6)
        );
        LayoutHelper.BBottomOfA(start, 8, coreMod, (int) (width * 0.2), 20);
        LayoutHelper.BBottomOfA(stop, 8, intruderMod, (int) (width * 0.2), 20);
        super.layout();
    }
}
