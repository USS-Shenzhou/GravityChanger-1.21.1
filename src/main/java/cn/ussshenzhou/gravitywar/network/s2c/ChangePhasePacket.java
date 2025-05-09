package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.MatchPhase;
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
public class ChangePhasePacket {
    public MatchPhase phase;

    public ChangePhasePacket(MatchPhase phase) {
        this.phase = phase;
    }

    @Decoder
    public ChangePhasePacket(FriendlyByteBuf buf) {
        this.phase = buf.readEnum(MatchPhase.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(phase);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handlerC(IPayloadContext context) {
        Util.handleChangePhasePacket(this, context);
    }

}
