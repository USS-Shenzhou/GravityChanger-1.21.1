package gravity_changer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import gravity_changer.plating.GravityPlatingBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class GravityDataComponents
{
    public static void init() {}

    public static final DataComponentType<CompoundTag> SIDE_DATA_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ResourceLocation.fromNamespaceAndPath(GravityChangerMod.NAMESPACE, "side_data"),
            DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).build()
    );
}
