package cn.ussshenzhou.gravitywar.util;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GravityWar.MODID);

    public static final Supplier<Item> LAVA_BOTTLE = ITEMS.register("lava_bottle", () -> new PotionItem(new Item.Properties().stacksTo(1)) {
        @Override
        public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
            if (!pLevel.isClientSide && pEntityLiving instanceof ServerPlayer player) {
                ServerGameManager.getLeaderOf(player.getUUID())
                        .map(pLevel::getPlayerByUUID)
                        .ifPresentOrElse(p -> {
                            if (!TradeHelper.isKaMu(p)) {
                                player.setRemainingFireTicks(80);
                                pStack.shrink(1);
                            }
                        }, () -> {
                            player.setRemainingFireTicks(80);
                            pStack.shrink(1);
                        });

                return pStack;
            }
            return super.finishUsingItem(pStack, pLevel, pEntityLiving);
        }
    });
}
