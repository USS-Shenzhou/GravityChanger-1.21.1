package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.network.Util;
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
public class TimeCheckPacket {
    public long start;

    public TimeCheckPacket(long start) {
        this.start = start;
    }

    @Decoder
    public TimeCheckPacket(FriendlyByteBuf buf) {
        this.start = buf.readVarLong();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(start);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        Util.handleTimeCheckPacket(this);
    }

}
