package cn.ussshenzhou.gravitywar.input;

import cn.ussshenzhou.madparticle.designer.input.DesignerKeyInput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ModKeyMappingRegistry {
    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(ModKeyInput.PLAY_SCREEN);
        event.register(ModKeyInput.OP_SCREEN);
    }
}
