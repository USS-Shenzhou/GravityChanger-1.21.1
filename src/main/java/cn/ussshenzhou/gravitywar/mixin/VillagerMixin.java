package cn.ussshenzhou.gravitywar.mixin;

import cn.ussshenzhou.gravitywar.network.UtilC;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author USS_Shenzhou
 */
@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {

    @Shadow protected abstract void setUnhappy();

    @Shadow protected abstract void startTrading(Player player);

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.is(Items.VILLAGER_SPAWN_EGG) || !this.isAlive() || this.isSleeping() || player.isSecondaryUseActive()) {
            return super.mobInteract(player, hand);
        } else if (this.isBaby()) {
            this.setUnhappy();
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        } else {
            if (!this.level().isClientSide) {
                boolean flag = this.getOffers().isEmpty();
                if (hand == InteractionHand.MAIN_HAND) {
                    if (flag) {
                        this.setUnhappy();
                    }

                    //player.awardStat(Stats.TALKED_TO_VILLAGER);
                }

                if (flag) {
                    return InteractionResult.CONSUME;
                }

                //this.startTrading(player);
            } else {
                UtilC.openTradeScreen();
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
    }
}
