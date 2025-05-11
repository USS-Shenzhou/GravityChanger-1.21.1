package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
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
public class SubtitlePacket {
    public String message;

    public SubtitlePacket(String message) {
        this.message = message;
    }

    @Decoder
    public SubtitlePacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handlerC(IPayloadContext context) {
        UtilC.handleSubtitlePacket(this, context);
    }
}
