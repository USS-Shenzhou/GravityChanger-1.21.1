package cn.ussshenzhou.gravitywar.gui;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.network.c2s.TradePacket;
import cn.ussshenzhou.gravitywar.util.TradeHelper;
import cn.ussshenzhou.t88.gui.advanced.THoverSensitiveImageButton;
import cn.ussshenzhou.t88.gui.container.TVerticalScrollContainer;
import cn.ussshenzhou.t88.gui.util.Border;
import cn.ussshenzhou.t88.gui.util.HorizontalAlignment;
import cn.ussshenzhou.t88.gui.util.LayoutHelper;
import cn.ussshenzhou.t88.gui.widegt.TItem;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.gui.widegt.TPanel;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.checkerframework.checker.units.qual.A;

import static cn.ussshenzhou.gravitywar.gui.TradeScreen.BUTTON_WIDTH;
import static net.minecraft.world.item.Items.*;
import static net.minecraft.world.item.Items.DIAMOND_SWORD;

/**
 * @author USS_Shenzhou
 */
public class TradePanel extends TVerticalScrollContainer {

    public TradePanel() {
        super();
        initFromProfession();
    }

    private void initFromProfession() {
        addTitle("资源");
        ClientGameManager.getMyTeam().ifPresent(team -> {
            addTrade(COBBLESTONE, 16, switch (team) {
                case DOWN -> DARK_OAK_LOG;
                case UP -> ACACIA_LOG;
                case NORTH -> CHERRY_LOG;
                case SOUTH -> BAMBOO_BLOCK;
                case WEST -> MANGROVE_LOG;
                case EAST -> WARPED_STEM;
            }, 32);
            addTrade(switch (team) {
                case DOWN -> DARK_OAK_LOG;
                case UP -> ACACIA_LOG;
                case NORTH -> CHERRY_LOG;
                case SOUTH -> BAMBOO_BLOCK;
                case WEST -> MANGROVE_LOG;
                case EAST -> WARPED_STEM;
            }, 32, COBBLESTONE, 16);
        });
        addTrade(COBBLESTONE, 16, COOKED_CHICKEN, 12);
        addTrade(COBBLESTONE, 16, COOKED_BEEF, 8);
        addTrade(RAW_IRON, 32, IRON_INGOT, 30);
        addTitle("装备");
        addTrade(IRON_INGOT, 7, IRON_HELMET, Enchantments.PROTECTION, 2);
        addTrade(IRON_INGOT, 10, IRON_CHESTPLATE, Enchantments.PROTECTION, 2);
        addTrade(IRON_INGOT, 9, IRON_LEGGINGS, Enchantments.PROTECTION, 2);
        addTrade(IRON_INGOT, 6, IRON_BOOTS, Enchantments.PROTECTION, 2);
        addTrade(IRON_INGOT, 3, IRON_SWORD, Enchantments.SHARPNESS, 2);

        addTrade(DIAMOND, 7, DIAMOND_HELMET, Enchantments.PROTECTION, 2);
        addTrade(DIAMOND, 10, DIAMOND_CHESTPLATE, Enchantments.PROTECTION, 2);
        addTrade(DIAMOND, 9, DIAMOND_LEGGINGS, Enchantments.PROTECTION, 2);
        var enc = Minecraft.getInstance().level.registryAccess().lookup(Registries.ENCHANTMENT).get();
        var t = new ItemStack(DIAMOND_BOOTS);
        t.enchant(enc.get(Enchantments.PROTECTION).get(), 2);
        t.enchant(enc.get(Enchantments.FEATHER_FALLING).get(), 2);
        addTrade(DIAMOND, 8, t);
        addTrade(DIAMOND, 3, DIAMOND_SWORD, Enchantments.SHARPNESS, 2);

        add(new SelfTradeButton(new ItemStack(COBBLESTONE, 16), new ItemStack(BOW))
                .setTooltip(Tooltip.create(Component.literal("§c谨慎购买\n§f弓箭在不同区域会有不同的重力方向。")))
        );
        add(new SelfTradeButton(new ItemStack(COBBLESTONE, 32), new ItemStack(ARROW, 64))
                .setTooltip(Tooltip.create(Component.literal("§c谨慎购买\n§f弓箭在不同区域会有不同的重力方向。")))
        );


        if (TradeHelper.isKaMu(Minecraft.getInstance().player)) {
            assertVoid();
            add(new SelfTradeButton(new ItemStack(COBBLESTONE, 8), TradeHelper.LAVA_BOTTLE.copy())
                    .setTooltip(Tooltip.create(Component.literal("§8上古失落的彩蛋\n§6家乡特产。\n§7只有你能进行此交易")))
            );
        }
        if (TradeHelper.isMelor(Minecraft.getInstance().player)) {
            assertVoid();
            add(new SelfTradeButton(new ItemStack(Items.COBBLESTONE, 128), TradeHelper.MELOR_SWORD_C.get().copy())
                    .setTooltip(Tooltip.create(Component.literal("§8上古失落的彩蛋\n§b《方块杯空岛冠军》\n§7只有你能进行此交易\n§8本来想给个茄子的但是懒得画。")))
            );
        }
    }

    private void addTitle(String string) {
        var l = new TLabel(Component.literal(string));
        l.setHorizontalAlignment(HorizontalAlignment.CENTER);
        l.setBorder(new Border(0xff000000, -1));
        add(l);
    }

    private void addTrade(Item from, int amount0, Item to, int amount1) {
        add(new SelfTradeButton(new ItemStack(from, amount0), new ItemStack(to, amount1)));
    }

    private void addTrade(Item from, int amount0, ItemStack to) {
        add(new SelfTradeButton(new ItemStack(from, amount0), to.copy()));
    }

    private void addTrade(Item from, int amount0, Item to, ResourceKey<Enchantment> enchantment, int level) {
        var enc = Minecraft.getInstance().level.registryAccess().lookup(Registries.ENCHANTMENT).get();
        var t = new ItemStack(to);
        t.enchant(enc.get(enchantment).get(), level);
        addTrade(from, amount0, t);
    }

    private void assertVoid() {
        for (int i = 0; i < 5; i++) {
            add(new TPanel());
        }
    }

    @Override
    public void layout() {
        for (int i = 0; i < children.size(); i++) {
            if (i == 0) {
                children.get(i).setBounds(0, 0, BUTTON_WIDTH, 20);
            } else {
                LayoutHelper.BBottomOfA(children.get(i), 0, children.get(i - 1));
            }
        }
        super.layout();
    }

    @Override
    protected void renderBackground(GuiGraphics guigraphics, int pMouseX, int pMouseY, float pPartialTick) {
        //super.renderBackground(guigraphics, pMouseX, pMouseY, pPartialTick);
    }

    public static class SelfTradeButton extends THoverSensitiveImageButton {
        protected final TItem from, to;

        public SelfTradeButton(ItemStack from, ItemStack to) {
            super(Component.literal("→"), button -> NetworkHelper.sendToServer(new TradePacket(Minecraft.getInstance().player.getUUID(), from, to)),
                    ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/button.png"),
                    ResourceLocation.fromNamespaceAndPath(GravityWar.MODID, "textures/gui/button_highlighted.png"));
            this.from = new TItem(from);
            this.to = new TItem(to);
            this.add(this.from);
            this.add(this.to);
            this.text.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }

        @Override
        public void layout() {
            from.setBounds(2, 2, from.getPreferredSize());
            to.setBounds(width - to.getPreferredSize().x - 2, 2, to.getPreferredSize());
            super.layout();
        }
    }

    public static class TradeOnceButton extends SelfTradeButton {

        public TradeOnceButton(ItemStack from, ItemStack to) {
            super(from, to);
            this.button.setOnPress(button -> {
                NetworkHelper.sendToServer(new TradePacket(Minecraft.getInstance().player.getUUID(), from, to));
                getParentInstanceOf(TradePanel.class).remove(this);
                getParentInstanceOf(TradePanel.class).layout();
            });
        }
    }
}
