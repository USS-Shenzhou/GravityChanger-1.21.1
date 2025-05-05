package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import cn.ussshenzhou.t88.gui.widegt.TProgressBar;
import cn.ussshenzhou.t88.gui.widegt.TTimer;
import net.minecraft.core.Direction;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author USS_Shenzhou
 */
public class IntruderModeHUD extends TPanel {
    public final TTimer timer = new TTimer() {
        @Override
        public void tickT() {
            if (this.getTime() <= 60 * 1000) {
                timer.setShowUpto(TTimer.TimeCategory.SEC);
                timer.setShowMillis(true);
                timer.setBackground(0xaaff0000);
            } else {
                timer.setShowUpto(TimeCategory.MIN);
                timer.setShowMillis(false);
                timer.setBackground(0x00000000);
            }
            super.tickT();
        }
    };
    private final TProgressBar downTeamProgress = new TProgressBar(GameManager.victoryScore);
    private final TProgressBar upTeamProgress = new TProgressBar(GameManager.victoryScore);
    private final TProgressBar northTeamProgress = new TProgressBar(GameManager.victoryScore);
    private final TProgressBar southTeamProgress = new TProgressBar(GameManager.victoryScore);
    private final TProgressBar eastTeamProgress = new TProgressBar(GameManager.victoryScore);
    private final TProgressBar westTeamProgress = new TProgressBar(GameManager.victoryScore);

    public IntruderModeHUD() {
        this.add(timer);
        timer.setCountdown(true);
        var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
        timer.setCountDownSec(cfg.battlePhase + cfg.finalPhase + cfg.preparePhase);
        timer.start();
        timer.setShowFullFormat(true);
        timer.setFontSize(14);
        timer.setHorizontalAlignment(HorizontalAlignment.CENTER);
        timer.setShowUpto(TTimer.TimeCategory.MIN);

        this.add(downTeamProgress);
        downTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.DOWN, 0xd0));
        downTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        downTeamProgress.setMaxValue(cfg.victoryPoints);

        this.add(upTeamProgress);
        upTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.UP, 0xd0));
        upTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        upTeamProgress.setMaxValue(cfg.victoryPoints);

        this.add(northTeamProgress);
        northTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.NORTH, 0xd0));
        northTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        northTeamProgress.setMaxValue(cfg.victoryPoints);

        this.add(southTeamProgress);
        southTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.SOUTH, 0xd0));
        southTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        southTeamProgress.setMaxValue(cfg.victoryPoints);

        this.add(eastTeamProgress);
        eastTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.EAST, 0xd0));
        eastTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        eastTeamProgress.setMaxValue(cfg.victoryPoints);

        this.add(westTeamProgress);
        westTeamProgress.setProgressBarColor(ColorHelper.getARGB(Direction.WEST, 0xd0));
        westTeamProgress.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        westTeamProgress.setMaxValue(cfg.victoryPoints);

        ClientGameManager.getMyTeam().ifPresent(d -> {
            var statusWithBackground = switch (d) {
                case UP -> upTeamProgress;
                case NORTH -> northTeamProgress;
                case SOUTH -> southTeamProgress;
                case EAST -> eastTeamProgress;
                case WEST -> westTeamProgress;
                case DOWN -> downTeamProgress;
            };
            statusWithBackground.setBorder(new Border(ColorHelper.getARGB(d, 0x80), 2));
        });
    }

    @Override
    public void tickT() {
        //TODO
        super.tickT();
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(0, 0, screenWidth, screenHeight);
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void layout() {
        int gap = 4;
        int w = (int) (width / 6f);
        downTeamProgress.setBounds(width - w - 4, height / 2 - gap / 2 - gap * 2 - 30, w, 10);
        LayoutHelper.BBottomOfA(northTeamProgress, gap, downTeamProgress);
        LayoutHelper.BBottomOfA(eastTeamProgress, gap, northTeamProgress);
        LayoutHelper.BBottomOfA(westTeamProgress, gap, eastTeamProgress);
        LayoutHelper.BBottomOfA(southTeamProgress, gap, westTeamProgress);
        LayoutHelper.BBottomOfA(upTeamProgress, gap, southTeamProgress);
        timer.setAbsBounds(width / 2 - 34, 6, 68, timer.getPreferredSize().y + 10);
        super.layout();
    }
}
