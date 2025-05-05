package cn.ussshenzhou.gravitywar.util;

import cn.ussshenzhou.madparticle.command.MadParticleCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class ModCommandRegister {
    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        new ModCommand(event.getDispatcher());
    }
}
