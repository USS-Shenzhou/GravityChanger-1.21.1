package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * @author USS_Shenzhou
 */
public abstract class AutoCloseHintHUD extends TPanel {
    protected static final int LIFE = 10 * 20;
    protected int age = 0;
    protected final TLabel mode = new TLabel();
    protected final TLabel phase = new TLabel();
    protected final TLabel desc = new TLabel();
    protected final TLabel team = new TLabel();

    public AutoCloseHintHUD(String mode, String phase, String desc) {
        this.mode.setText(Component.literal(mode));
        this.phase.setText(Component.literal(phase));
        this.desc.setText(Component.literal(desc));
        this.add(this.mode);
        this.add(this.phase);
        this.add(this.desc);
        this.mode.setFontSize(14);
        this.phase.setFontSize(21);
        this.mode.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.phase.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.desc.setHorizontalAlignment(HorizontalAlignment.CENTER);
        this.mode.setAutoScroll(false);
        this.phase.setAutoScroll(false);
        this.desc.setAutoScroll(false);

        ClientGameManager.getMyTeam()
                .ifPresent(d -> {
                    this.team.setForeground(ColorHelper.getARGB(d, 0xff));
                    this.team.setBackground(ColorHelper.getARGB(d, 0x60));
                    this.team.setText(Component.literal(DirectionHelper.getName(d)));
                });
        this.add(this.team);
        this.team.setFontSize(21);
        team.setHorizontalAlignment(HorizontalAlignment.LEFT);
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(0, 0, screenWidth, screenHeight);
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void layout() {
        var phaseSize = phase.getPreferredSize();
        phase.setBounds((width - phaseSize.x) / 2, (height - phaseSize.y) / 2, phaseSize);
        var modeSize = mode.getPreferredSize();
        mode.setBounds((width - modeSize.x) / 2, phase.getYT() - modeSize.y - 6, modeSize);
        var descSize = desc.getPreferredSize();
        desc.setBounds((width - descSize.x) / 2, phase.getYT() + phase.getHeight() + 6, descSize);

        team.setBounds((int) (width * 0.8), (int) (height * 0.1), team.getPreferredSize());
        super.layout();
    }

    @Override
    public void tickT() {
        age++;
        if (age > LIFE) {
            HudManager.remove(this);
        }
        super.tickT();
    }


}
