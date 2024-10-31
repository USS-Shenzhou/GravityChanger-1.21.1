package gravity_changer.item;

import gravity_changer.GravityComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.List;

// based on AmethystGravity
public class GravityAnchorItem extends Item {
    public final Direction direction;
    
    public static final EnumMap<Direction, GravityAnchorItem> ITEM_MAP = new EnumMap<>(Direction.class);
    
    static {
        for (Direction direction : Direction.values()) {
            ITEM_MAP.put(direction, new GravityAnchorItem(direction, new Properties()));
        }
    }
    
    public static void init() {
        for (Direction direction : Direction.values()) {
            Registry.register(
                BuiltInRegistries.ITEM, getItemId(direction), ITEM_MAP.get(direction)
            );
        }
        
        GravityComponent.GRAVITY_UPDATE_EVENT.register((entity, component) -> {
            if(entity instanceof LivingEntity living)
            {
                for (ItemStack handSlot : living.getHandSlots()) {
                    Item item = handSlot.getItem();
                    if (item instanceof GravityAnchorItem anchorItem) {
                        component.applyGravityDirectionEffect(
                                anchorItem.direction,
                                null, 1000000, 0
                        );
                    }
                }
            }
        });
    }
    
    public static ResourceLocation getItemId(Direction direction) {
        return ResourceLocation.fromNamespaceAndPath("gravity_changer", "gravity_anchor_" + direction.getName());
    }
    
    public GravityAnchorItem(Direction _direction, Properties settings) {
        super(settings);
        direction = _direction;
    }
    
    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(
            Component.translatable("gravity_changer.gravity_anchor.tooltip.0")
                .withStyle(ChatFormatting.GRAY)
        );
        
        tooltip.add(
            Component.translatable("gravity_changer.gravity_anchor.tooltip.1")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
