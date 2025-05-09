package cn.ussshenzhou.gravitywar.util;

import cn.ussshenzhou.t88.util.InventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class TradeHelper {

    //-----Eastern Eggs-----

    public static ItemStack LAVA_BOTTLE = null;

    @SubscribeEvent
    public static void justInit(PlayerEvent.PlayerLoggedInEvent event) {
        if (LAVA_BOTTLE == null) {
            LAVA_BOTTLE = new ItemStack(ModItems.LAVA_BOTTLE.get());
            LAVA_BOTTLE.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0x00ff9900), List.of(
                    new MobEffectInstance(MobEffects.REGENERATION, 200, 1),
                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0)
            )));
            LAVA_BOTTLE.set(DataComponents.ITEM_NAME, Component.literal("一小瓶热腾腾的饮料").withColor(0xFF6C14));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final Supplier<ItemStack> MELOR_SWORD_C = () -> {
        var i = new ItemStack(Items.DIAMOND_SWORD);
        var enc = Minecraft.getInstance().level.registryAccess().lookup(Registries.ENCHANTMENT).get();
        i.enchant(enc.get(Enchantments.LOOTING).get(), 2);
        i.enchant(enc.get(Enchantments.SHARPNESS).get(), 6);

        i.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        i.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("不可丢出"))));
        i.set(DataComponents.ITEM_NAME, Component.literal("《方块杯空岛冠军》").withColor(0x21FFFD));
        var tag = new CompoundTag();
        tag.putBoolean("undroppable", true);
        i.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return i;
    };

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final Supplier<ItemStack> MELOR_SWORD_S = () -> {
        var i = new ItemStack(Items.DIAMOND_SWORD);
        var enc = ServerLifecycleHooks.getCurrentServer().registryAccess().lookup(Registries.ENCHANTMENT).get();
        i.enchant(enc.get(Enchantments.LOOTING).get(), 2);
        i.enchant(enc.get(Enchantments.SHARPNESS).get(), 6);

        i.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        i.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("不可丢出"))));
        i.set(DataComponents.ITEM_NAME, Component.literal("《方块杯空岛冠军》").withColor(0x21FFFD));
        var tag = new CompoundTag();
        tag.putBoolean("undroppable", true);
        i.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return i;
    };

    @SubscribeEvent
    public static void unDroppableItem(ItemTossEvent event) {
        if (undroppable(event.getEntity().getItem())) {
            event.setCanceled(true);
            event.getPlayer().addItem(event.getEntity().getItem());
            event.getPlayer().getInventory().setChanged();
        }
    }

    public static boolean undroppable(ItemStack itemStack) {
        return itemStack.has(DataComponents.CUSTOM_DATA) && itemStack.get(DataComponents.CUSTOM_DATA).contains("undroppable");
    }

    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            var old = event.getOriginal();
            var nev = event.getEntity();
            if (!nev.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                InventoryHelper.getAllAsStream(old.getInventory())
                        .filter(TradeHelper::undroppable)
                        .forEach(nev::addItem);
            }
        }
    }

    @SubscribeEvent
    public static void evenItemFrame(PlayerInteractEvent.EntityInteract event) {
        if (undroppable(event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    public static boolean isKaMu(Player player) {
        if (player == null) {
            return false;
        }
        var name = player.getGameProfile().getName();
        return "KaMuaMua".equals(name) || "Dev".equals(name);
    }

    public static boolean isMelor(Player player) {
        if (player == null) {
            return false;
        }
        var name = player.getGameProfile().getName();
        return "Melor_".equals(name) || "Dev".equals(name);
    }
}
