package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.network.c2s.PickTeamPacket;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.t88.gui.advanced.TLabelButton;
import cn.ussshenzhou.t88.gui.screen.TScreen;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TImage;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * @author USS_Shenzhou
 */
public class MainScreen extends TScreen {
    private final TImage background = new TImage(ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/cube.png"));
    private final TLabelButton downTeamButton = new TLabelButton(Component.empty());
    private final TLabelButton upTeamButton = new TLabelButton(Component.empty());
    private final TLabelButton northTeamButton = new TLabelButton(Component.empty());
    private final TLabelButton southTeamButton = new TLabelButton(Component.empty());
    private final TLabelButton eastTeamButton = new TLabelButton(Component.empty());
    private final TLabelButton westTeamButton = new TLabelButton(Component.empty());
    private final TLabel title0 = new TLabel(Component.literal("重力战争"));
    private final TLabel title1 = new TLabel(Component.literal("选择你想要加入的队伍。"));

    public void update() {
        downTeamButton.setText(Component.literal("黑队 (" + ClientGameManager.getPlayerNumber()[Direction.DOWN.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(downTeamButton, Direction.DOWN);
        upTeamButton.setText(Component.literal("橙队 (" + ClientGameManager.getPlayerNumber()[Direction.UP.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(upTeamButton, Direction.UP);
        northTeamButton.setText(Component.literal("粉队 (" + ClientGameManager.getPlayerNumber()[Direction.NORTH.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(northTeamButton, Direction.NORTH);
        southTeamButton.setText(Component.literal("黄队 (" + ClientGameManager.getPlayerNumber()[Direction.SOUTH.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(southTeamButton, Direction.SOUTH);
        eastTeamButton.setText(Component.literal("蓝队 (" + ClientGameManager.getPlayerNumber()[Direction.EAST.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(eastTeamButton, Direction.EAST);
        westTeamButton.setText(Component.literal("红队 (" + ClientGameManager.getPlayerNumber()[Direction.WEST.ordinal()] + "/" + GameManager.maxPlayerPerTeam + ")"));
        checkNumber(westTeamButton, Direction.WEST);
    }

    private void checkNumber(TLabelButton button, Direction direction) {
        if (ClientGameManager.getPlayerNumber()[direction.ordinal()] >= GameManager.maxPlayerPerTeam) {
            button.setOnPress(b -> {
            });
            button.setForeground(0xffa0a0a0);
            button.getButton().active = false;
        } else {
            button.setOnPress(b -> {
                NetworkHelper.sendToServer(new PickTeamPacket(direction));
            });
            button.setForeground(0xffffffff);
            button.getButton().active = true;
        }
    }

    public MainScreen() {
        super(Component.empty());
        this.add(background);
        this.add(downTeamButton);
        downTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.DOWN, 0x60));
        downTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.DOWN, 0xaa));
        downTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.DOWN, 0xff), 1));

        this.add(upTeamButton);
        upTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.UP, 0x60));
        upTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.UP, 0xaa));
        upTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.UP, 0xff), 1));

        this.add(northTeamButton);
        northTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.NORTH, 0x60));
        northTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.NORTH, 0xaa));
        northTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.NORTH, 0xff), 1));

        this.add(southTeamButton);
        southTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.SOUTH, 0x60));
        southTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.SOUTH, 0xaa));
        southTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.SOUTH, 0xff), 1));

        this.add(eastTeamButton);
        eastTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.EAST, 0x60));
        eastTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.EAST, 0xaa));
        eastTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.EAST, 0xff), 1));

        this.add(westTeamButton);
        westTeamButton.setNormalBackGround(ColorHelper.getARGB(Direction.WEST, 0x60));
        westTeamButton.setHoverBackGround(ColorHelper.getARGB(Direction.WEST, 0xaa));
        westTeamButton.setBorder(new Border(ColorHelper.getARGB(Direction.WEST, 0xff), 1));

        this.add(title0);
        title0.setFontSize(14);
        this.add(title1);
        update();
    }

    @Override
    public void layout() {
        title0.setAbsBounds(10, 10, title0.getPreferredSize());
        LayoutHelper.BBottomOfA(title1, 2, title0, title1.getPreferredSize());

        int backgroundSize = (int) (height * 0.6f);
        background.setAbsBounds(
                (width - backgroundSize) / 2,
                (height - backgroundSize) / 2,
                backgroundSize,
                backgroundSize
        );
        int buttonWidth = 70;
        int buttonHeight = 20;
        downTeamButton.setAbsBounds(
                (width - buttonWidth) / 2,
                (int) (height * 0.8),
                buttonWidth, buttonHeight
        );
        upTeamButton.setAbsBounds(
                (width - buttonWidth) / 2,
                (int) (height * 0.2 - buttonHeight),
                buttonWidth, buttonHeight
        );
        northTeamButton.setAbsBounds(
                ((width - backgroundSize) / 2 - buttonWidth) / 2,
                (height - buttonHeight) / 2,
                buttonWidth, buttonHeight
        );
        southTeamButton.setAbsBounds(
                ((width - backgroundSize) / 2 - buttonWidth) / 2 + (width - (width - backgroundSize) / 2),
                (height - buttonHeight) / 2,
                buttonWidth, buttonHeight
        );
        eastTeamButton.setAbsBounds(
                (int) (width / 2f + backgroundSize * 0.6),
                (int) (height / 2f - backgroundSize * 0.6),
                buttonWidth, buttonHeight
        );
        westTeamButton.setAbsBounds(
                (int) (width / 2f - backgroundSize * 0.6 - buttonWidth),
                (int) (height / 2f + backgroundSize * 0.6),
                buttonWidth, buttonHeight
        );
        super.layout();
    }
}
