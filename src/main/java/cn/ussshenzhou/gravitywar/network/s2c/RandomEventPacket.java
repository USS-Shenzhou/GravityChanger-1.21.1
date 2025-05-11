package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.RandomEvent;
import cn.ussshenzhou.gravitywar.network.UtilC;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class RandomEventPacket {
    public RandomEvent event;

    public RandomEventPacket(RandomEvent event) {
        this.event = event;
    }

    @Decoder
    public RandomEventPacket(FriendlyByteBuf buf) {
        this.event = buf.readEnum(RandomEvent.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(event);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handlerC(IPayloadContext context) {
        UtilC.handleRandomEventPacket(this, context);
    }

}
