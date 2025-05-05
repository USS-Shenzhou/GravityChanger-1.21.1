package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.game.MatchMode;
import cn.ussshenzhou.gravitywar.network.ForceChooseScreenPacket;
import cn.ussshenzhou.gravitywar.network.c2s.KickNeutralPlayersPacket;
import cn.ussshenzhou.gravitywar.network.c2s.SetModePacket;
import cn.ussshenzhou.gravitywar.network.c2s.StartSPacket;
import cn.ussshenzhou.t88.gui.advanced.TLabelButton;
import cn.ussshenzhou.t88.gui.combine.TTitledSelectList;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TButton;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.gui.widegt.TSelectList;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public class OpScreen extends TScreen {
    private final TTitledSelectList<Component> neutralPlayerList = new TTitledSelectList<>(Component.literal("未选取队伍的玩家"), new TSelectList<>());

    private final TLabelButton coreMod = new TLabelButton(Component.literal("死斗"), button -> {
        NetworkHelper.sendToServer(new SetModePacket(MatchMode.CORE));
    });
    private final TLabelButton intruderMod = new TLabelButton(Component.literal("入侵"), button -> {
        NetworkHelper.sendToServer(new SetModePacket(MatchMode.INTRUDER));
    });
    private final TLabelButton siegeMod = new TLabelButton(Component.literal("围攻"), button -> {
        NetworkHelper.sendToServer(new SetModePacket(MatchMode.INTRUDER));
    });

    private final TButton start = new TButton(Component.literal("开始对局"), button -> {
        NetworkHelper.sendToServer(new StartSPacket());
    });
    private final TButton stop = new TButton(Component.literal("强制结束对局"), button -> {

    });

    private final TButton notify = new TButton(Component.literal("强制弹出选队界面"), button -> {
        NetworkHelper.sendToServer(new ForceChooseScreenPacket());
    });
    private final TButton kick = new TButton(Component.literal("踢出列表中的非OP"), button -> {
        NetworkHelper.sendToServer(new KickNeutralPlayersPacket());
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

        this.add(siegeMod);
        siegeMod.setFontSize(14);

        this.add(neutralPlayerList);
        this.add(notify);
        this.add(kick);

        notify.setFGColor(0xffffee22);
        kick.setFGColor(0xffff2222);
        stop.setFGColor(0xffff2222);

        neutralPlayerList.getComponent().setHorizontalAlignment(HorizontalAlignment.LEFT);
        update();
    }

    public void update() {
        var list = neutralPlayerList.getComponent();
        list.clearElement();
        Minecraft.getInstance().level.players().stream()
                .filter(p -> !GameManager.PLAYER_TO_TEAM.containsKey(p.getUUID()))
                //.filter(p -> !p.hasPermissions(2))
                .forEach(p -> list.addElement(p.getDisplayName()));
    }

    @Override
    public void layout() {
        title0.setAbsBounds(10, 10, title0.getPreferredSize());
        LayoutHelper.BBottomOfA(title1, 2, title0, title1.getPreferredSize());
        neutralPlayerList.setBounds((int) (width * 0.05), (int) (height * 0.15), 120, (int) (height * 0.8));
        LayoutHelper.BRightOfA(notify, 10, neutralPlayerList, 100, 20);
        LayoutHelper.BBottomOfA(kick, 10, notify);
        LayoutHelper.BBottomOfA(coreMod, 40, notify, 80, 40);
        LayoutHelper.BRightOfA(intruderMod, 10, coreMod);
        LayoutHelper.BRightOfA(siegeMod, 10, intruderMod);

        LayoutHelper.BBottomOfA(start, 8, coreMod, 80, 20);
        LayoutHelper.BBottomOfA(stop, 8, intruderMod, 80, 20);
        super.layout();
    }
}
