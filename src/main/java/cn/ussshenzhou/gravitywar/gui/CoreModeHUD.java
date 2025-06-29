package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.entity.CoreEntity;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.gravitywar.util.ColorHelper;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
public class CoreModeHUD extends TPanel {
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
            if (timer.getTime() <= 0) {
                this.setUpdateGui(false);
                this.setText(Component.literal("Final"));
            }
            super.tickT();
        }
    };
    private final ColorfulImage downTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.DOWN);
    private final ColorfulImage upTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.UP);
    private final ColorfulImage northTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.NORTH);
    private final ColorfulImage southTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.SOUTH);
    private final ColorfulImage eastTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.EAST);
    private final ColorfulImage westTeamStatus = new ColorfulImage(
            ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_3.png"),
            Direction.WEST);

    private final TLabel core0 = new TLabel(Component.literal("当前区域的核心"));
    private final TProgressBar core1 = new TProgressBar();
    private final TProgressBar core2 = new TProgressBar();
    private final TProgressBar core3 = new TProgressBar();

    public CoreModeHUD() {
        this.add(timer);
        timer.setCountdown(true);
        var cfg = ConfigHelper.getConfigRead(GravityWarConfig.class);
        timer.setCountDownSec(cfg.battlePhase + cfg.preparePhase);
        timer.start();
        timer.setShowFullFormat(true);
        timer.setFontSize(14);
        timer.setHorizontalAlignment(HorizontalAlignment.CENTER);
        timer.setShowUpto(TTimer.TimeCategory.MIN);

        this.add(downTeamStatus);
        this.add(upTeamStatus);
        this.add(northTeamStatus);
        this.add(southTeamStatus);
        this.add(eastTeamStatus);
        this.add(westTeamStatus);

        ClientGameManager.getMyTeam().ifPresent(d -> {
            var statusWithBackground = getRing(d);
            statusWithBackground.setBorder(new Border(ColorHelper.getARGB(d, 0x80), 4));
        });

        this.add(core1);
        core1.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        this.add(core2);
        core2.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        this.add(core3);
        core3.setTextMode(TProgressBar.TextMode.VALUE_INT_SLASH_MAX);
        this.add(core0);
        core0.setHorizontalAlignment(HorizontalAlignment.CENTER);
    }

    private ColorfulImage getRing(Direction d) {
        return switch (d) {
            case UP -> upTeamStatus;
            case NORTH -> northTeamStatus;
            case SOUTH -> southTeamStatus;
            case EAST -> eastTeamStatus;
            case WEST -> westTeamStatus;
            case DOWN -> downTeamStatus;
        };
    }

    @Override
    public void tickT() {
        GameManager.TEAM_TO_PLAYER.forEach((direction, uuids) -> {
            var n = uuids.stream()
                    .map(ClientGameManager::getPlayerC)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(player -> {
                        return Minecraft.getInstance().getConnection() != null &&
                                Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID()) != null &&
                                Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID()).getGameMode() == GameType.SURVIVAL;
                    })
                    .filter(LivingEntity::isAlive)
                    .count();
            getRing(direction).playerNumber.setText(Component.literal(String.valueOf(n)));
        });
        int[] cores = new int[6];
        int[] playerCount = new int[6];
        StreamSupport.stream(Minecraft.getInstance().level.getEntities().getAll().spliterator(), false)
                .forEach(e -> {
                    if (e instanceof CoreEntity) {
                        cores[DirectionHelper.getPyramidRegion(e.position()).ordinal()]++;
                    } else if (e instanceof Player && e.isAlive() && GameManager.PLAYER_TO_TEAM.containsKey(e.getUUID())) {
                        playerCount[GameManager.PLAYER_TO_TEAM.get(e.getUUID()).ordinal()]++;
                    }
                });
        for (var d : Direction.values()) {
            getRing(d).playerNumber.setText(Component.literal(String.valueOf(playerCount[d.ordinal()])));
            int i = Mth.clamp(cores[d.ordinal()], 0, 3);
            getRing(d).setImageLocation(ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/core_" + i + ".png"));
        }

        var thisDir = DirectionHelper.getPyramidRegion(Minecraft.getInstance().player.position());
        var thisCores = StreamSupport.stream(Minecraft.getInstance().level.getEntities().getAll().spliterator(), false)
                .filter(e -> e instanceof CoreEntity && DirectionHelper.getPyramidRegion(e.position()) == thisDir)
                .limit(3)
                .toList();

        BiConsumer<TProgressBar, CoreEntity> process = (bar, entity) -> {
            bar.setProgressBarColor(ColorHelper.getARGB(thisDir, 0xd0));
            bar.setMaxValue((entity).getMaxHealth());
            bar.setValue((entity).getHealth());
        };

        try {
            for (int i = 0; i < thisCores.size(); i++) {
                switch (i) {
                    case 0 -> process.accept(core1, (CoreEntity) thisCores.get(0));
                    case 1 -> process.accept(core2, (CoreEntity) thisCores.get(1));
                    case 2 -> process.accept(core3, (CoreEntity) thisCores.get(2));
                }
            }
        } catch (IndexOutOfBoundsException ignored) {
        }


        super.tickT();
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(0, 0, screenWidth, screenHeight);
        super.resizeAsHud(screenWidth, screenHeight);
    }

    @Override
    public void layout() {
        timer.setBounds(width / 2 - 34, 6, 68, timer.getPreferredSize().y + 10);

        LayoutHelper.BLeftOfA(eastTeamStatus, 4, timer, 32, 32);
        LayoutHelper.BLeftOfA(northTeamStatus, 4, eastTeamStatus, 32, 32);
        LayoutHelper.BLeftOfA(downTeamStatus, 4, northTeamStatus, 32, 32);
        LayoutHelper.BRightOfA(westTeamStatus, 4, timer, 32, 32);
        LayoutHelper.BRightOfA(southTeamStatus, 4, westTeamStatus, 32, 32);
        LayoutHelper.BRightOfA(upTeamStatus, 4, southTeamStatus, 32, 32);

        int gap = 4;
        int w = (int) (width / 6f);
        core0.setBounds(width - w - 4, height / 2 - gap / 2 - gap * 2 - 30, w, 10);
        LayoutHelper.BBottomOfA(core1, gap, core0);
        LayoutHelper.BBottomOfA(core2, gap, core1);
        LayoutHelper.BBottomOfA(core3, gap, core2);

        super.layout();
    }

    public static class ColorfulImage extends TImage {
        private final Vector3f color;
        private final TLabel playerNumber = new TLabel(Component.literal("0"));

        public ColorfulImage(ResourceLocation imageLocation, Direction direction) {
            super(imageLocation);
            this.color = ColorHelper.getRGB3f(direction);
            this.add(playerNumber);
            playerNumber.setHorizontalAlignment(HorizontalAlignment.CENTER);
            playerNumber.setForeground(ColorHelper.getARGB(direction, 0xd0));
            this.alpha = 0xd0 / 255f;
        }

        @Override
        public void layout() {
            playerNumber.setBounds(0, 0, width, height);
            super.layout();
        }

        @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
        @Override
        public void render(GuiGraphics guigraphics, int pMouseX, int pMouseY, float pPartialTick) {
            if (imageLocation != null) {
                RenderSystem.setShaderColor(1, 1, 1, alpha);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                switch (imageFit) {
                    case FILL -> {
                        float panelWHRatio = width / (float) height;
                        float imageWHRatio = imageWidth / (float) imageHeight;
                        if (panelWHRatio > imageWHRatio) {
                            blitWithColor(guigraphics, imageLocation, this.x, this.y, width, height,
                                    0, (imageHeight - imageWidth / panelWHRatio) / 2,
                                    imageWidth, (int) (imageWidth / panelWHRatio),
                                    (int) (imageWidth * scale), (int) (imageHeight * scale));
                        } else {
                            blitWithColor(guigraphics, imageLocation, this.x, this.y, width, height,
                                    (imageWidth - imageHeight * panelWHRatio) / 2, 0,
                                    (int) (imageHeight * panelWHRatio), imageHeight,
                                    (int) (imageWidth * scale), (int) (imageHeight * scale));
                        }
                    }
                    case FIT -> {
                        float panelWHRatio = width / (float) height;
                        float imageWHRatio = imageWidth / (float) imageHeight;
                        if (panelWHRatio > imageWHRatio) {
                            blitWithColor(guigraphics, imageLocation, (int) (this.x + (width - height * imageWHRatio) / 2), this.y,
                                    (int) (height * imageWHRatio), height,
                                    0, 0,
                                    imageWidth, imageHeight,
                                    (int) (imageWidth * scale), (int) (imageHeight * scale));
                        } else {
                            blitWithColor(guigraphics, imageLocation, this.x, (int) (this.y + (height - width / imageWHRatio) / 2),
                                    width, (int) (width / imageWHRatio),
                                    0, 0,
                                    imageWidth, imageHeight,
                                    (int) (imageWidth * scale), (int) (imageHeight * scale));
                        }
                    }
                    case STRETCH ->
                            blitWithColor(guigraphics, imageLocation, this.x, this.y, width, height, 0, 0, imageWidth, imageHeight, (int) (imageWidth * scale), (int) (imageHeight * scale));
                    case TILE ->
                            blitWithColor(guigraphics, imageLocation, this.x, this.y, width, height, 0, 0, width, height, (int) (imageWidth * scale), (int) (imageHeight * scale));
                }
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            renderBackground(guigraphics, pMouseX, pMouseY, pPartialTick);
            if (border != null) {
                renderBorder(guigraphics, pMouseX, pMouseY, pPartialTick);
            }
            renderChildren(guigraphics, pMouseX, pMouseY, pPartialTick);
            var t = this.getTooltip();
            if (t != null) {
                var scroll = getParentScroll();
                t.refreshTooltipForNextRenderPass(this.isInRange(pMouseX + scroll.x, pMouseY + scroll.y), this.isFocused(), this.getRectangle());
            }
        }

        private void blitWithColor(GuiGraphics guigraphics, ResourceLocation atlasLocation, int x, int y, int width, int height, float uOffset, float vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
            blitWithColor(guigraphics, atlasLocation, x, x + width, y, y + height, 0, uWidth, vHeight, uOffset, vOffset, textureWidth, textureHeight);
        }

        private void blitWithColor(GuiGraphics guigraphics, ResourceLocation atlasLocation, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
            blitWithColor(
                    guigraphics,
                    atlasLocation,
                    x1, x2, y1, y2,
                    blitOffset,
                    (uOffset + 0.0F) / (float) textureWidth,
                    (uOffset + (float) uWidth) / (float) textureWidth,
                    (vOffset + 0.0F) / (float) textureHeight,
                    (vOffset + (float) vHeight) / (float) textureHeight,
                    color.x, color.y, color.z, alpha
            );
        }

        private void blitWithColor(
                GuiGraphics guigraphics,
                ResourceLocation atlasLocation,
                int x1,
                int x2,
                int y1,
                int y2,
                int blitOffset,
                float minU,
                float maxU,
                float minV,
                float maxV,
                float red,
                float green,
                float blue,
                float alpha
        ) {
            RenderSystem.setShaderTexture(0, atlasLocation);
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.enableBlend();
            Matrix4f matrix4f = guigraphics.pose().last().pose();
            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.addVertex(matrix4f, (float) x1, (float) y1, (float) blitOffset)
                    .setUv(minU, minV)
                    .setColor(red, green, blue, alpha);
            bufferbuilder.addVertex(matrix4f, (float) x1, (float) y2, (float) blitOffset)
                    .setUv(minU, maxV)
                    .setColor(red, green, blue, alpha);
            bufferbuilder.addVertex(matrix4f, (float) x2, (float) y2, (float) blitOffset)
                    .setUv(maxU, maxV)
                    .setColor(red, green, blue, alpha);
            bufferbuilder.addVertex(matrix4f, (float) x2, (float) y1, (float) blitOffset)
                    .setUv(maxU, minV)
                    .setColor(red, green, blue, alpha);
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            RenderSystem.disableBlend();
        }
    }
}
