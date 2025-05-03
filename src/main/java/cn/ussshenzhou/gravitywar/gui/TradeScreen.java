package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.t88.gui.screen.TScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("DataFlowIssue")
public class TradeScreen extends TScreen {
    public static final int BUTTON_WIDTH = 80;
    private static final int INVENTORY_OFFSET = BUTTON_WIDTH / 2;

    private final InventoryScreen inventory = new InventoryScreen(minecraft.player) {
        @Override
        protected void init() {
            super.init();
            this.children().stream().filter(renderable -> renderable instanceof ImageButton).findFirst().ifPresent(this::removeWidget);
        }

        @Override
        public void renderBackground(GuiGraphics p_295206_, int p_295457_, int p_294596_, float p_296351_) {
            this.renderBg(p_295206_, p_296351_, p_295457_, p_294596_);
        }

        @Override
        protected void renderBg(GuiGraphics p_281500_, float p_281299_, int p_283481_, int p_281831_) {
            int i = this.leftPos;
            int j = this.topPos;
            p_281500_.blit(INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
            TradeScreen.renderEntityInInventoryFollowsMouse(p_281500_, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, p_283481_, p_281831_, this.minecraft.player);
        }

        @Override
        public void containerTick() {
            if (this.minecraft.gameMode.hasInfiniteItems()) {
                this.minecraft
                        .setScreen(
                                new CreativeModeInventoryScreen(
                                        this.minecraft.player, this.minecraft.player.connection.enabledFeatures(), this.minecraft.options.operatorItemsTab().get()
                                )
                        );
            }
        }
    };
    private final TradePanel tradePanel = new TradePanel();

    public TradeScreen() {
        super(Component.empty());
        inventory.init(minecraft, 0, 0);
        this.add(tradePanel);
    }

    @Override
    public void layout() {
        inventory.resize(minecraft, width, height);
        tradePanel.setBounds(inventory.getGuiLeft() - INVENTORY_OFFSET - 2 - 6, inventory.getGuiTop(), BUTTON_WIDTH + 6, inventory.getYSize());
        super.layout();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton) || inventory.mouseClicked(pMouseX - INVENTORY_OFFSET, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        return super.mouseReleased(pMouseX, pMouseY, pButton) || inventory.mouseReleased(pMouseX - INVENTORY_OFFSET, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY) || inventory.mouseDragged(pMouseX - INVENTORY_OFFSET, pMouseY, pButton, pDragX
                , pDragY);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double deltaX, double deltaY) {
        return super.mouseScrolled(pMouseX, pMouseY, deltaX, deltaY) || inventory.mouseScrolled(pMouseX - INVENTORY_OFFSET, pMouseY, deltaX, deltaY);
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        if (inventory.width == 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(INVENTORY_OFFSET, 0, 0);
        inventory.render(graphics, pMouseX - INVENTORY_OFFSET, pMouseY, pPartialTick);
        graphics.pose().popPose();
    }

    @Override
    public void tick() {
        inventory.tick();
        super.tick();
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static void renderEntityInInventoryFollowsMouse(
            GuiGraphics p_282802_,
            int p_275688_,
            int p_275245_,
            int p_275535_,
            int p_294406_,
            int p_294663_,
            float p_275604_,
            float p_275546_,
            float p_295352_,
            LivingEntity p_275689_
    ) {
        float f = (float) (p_275688_ + p_275535_) / 2.0F;
        float f1 = (float) (p_275245_ + p_294406_) / 2.0F;
        float f2 = (float) Math.atan((double) ((f - p_275546_) / 40.0F));
        float f3 = (float) Math.atan((double) ((f1 - p_295352_) / 40.0F));
        // Forge: Allow passing in direct angle components instead of mouse position
        TradeScreen.renderEntityInInventoryFollowsAngle(p_282802_, p_275688_, p_275245_, p_275535_, p_294406_, p_294663_, p_275604_, f2, f3, p_275689_);
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static void renderEntityInInventoryFollowsAngle(
            GuiGraphics p_282802_,
            int p_275688_,
            int p_275245_,
            int p_275535_,
            int p_294406_,
            int p_294663_,
            float p_275604_,
            float angleXComponent,
            float angleYComponent,
            LivingEntity p_275689_
    ) {
        float f = (float) (p_275688_ + p_275535_) / 2.0F;
        float f1 = (float) (p_275245_ + p_294406_) / 2.0F;
        //p_282802_.enableScissor(p_275688_, p_275245_, p_275535_, p_294406_);
        float f2 = angleXComponent;
        float f3 = angleYComponent;
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(f3 * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf1);
        float f4 = p_275689_.yBodyRot;
        float f5 = p_275689_.getYRot();
        float f6 = p_275689_.getXRot();
        float f7 = p_275689_.yHeadRotO;
        float f8 = p_275689_.yHeadRot;
        p_275689_.yBodyRot = 180.0F + f2 * 20.0F;
        p_275689_.setYRot(180.0F + f2 * 40.0F);
        p_275689_.setXRot(-f3 * 20.0F);
        p_275689_.yHeadRot = p_275689_.getYRot();
        p_275689_.yHeadRotO = p_275689_.getYRot();
        Vector3f vector3f = new Vector3f(0.0F, p_275689_.getBbHeight() / 2.0F + p_275604_, 0.0F);
        InventoryScreen.renderEntityInInventory(p_282802_, f, f1, p_294663_, vector3f, quaternionf, quaternionf1, p_275689_);
        p_275689_.yBodyRot = f4;
        p_275689_.setYRot(f5);
        p_275689_.setXRot(f6);
        p_275689_.yHeadRotO = f7;
        p_275689_.yHeadRot = f8;
        //p_282802_.disableScissor();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
