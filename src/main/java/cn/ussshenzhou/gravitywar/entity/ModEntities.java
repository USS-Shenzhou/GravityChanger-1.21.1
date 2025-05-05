package cn.ussshenzhou.gravitywar.entity;

import cn.ussshenzhou.gravitywar.GravityWar;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, GravityWar.MODID);

    public static final Supplier<EntityType<CoreEntity>> CORE_ENTITY_TYPE = ENTITY_TYPES.register("core", () ->
            EntityType.Builder.of(CoreEntity::new, MobCategory.MISC)
                    .sized(2f, 2f)
                    .noSave()
                    .clientTrackingRange(10)
                    .updateInterval(5)
                    .eyeHeight(1)
                    .build("core")
    );
}
