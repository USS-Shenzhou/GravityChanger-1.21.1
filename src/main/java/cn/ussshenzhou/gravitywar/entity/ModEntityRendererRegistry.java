package cn.ussshenzhou.gravitywar.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModEntityRendererRegistry {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CORE_ENTITY_TYPE.get(), CoreEntityRenderer::new);
    }

    @SubscribeEvent
    public static void createDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.CORE_ENTITY_TYPE.get(),
                LivingEntity.createLivingAttributes()
                        .add(Attributes.MAX_HEALTH, 60)
                        .add(Attributes.FOLLOW_RANGE, 1)
                        .build()
        );
    }
}
