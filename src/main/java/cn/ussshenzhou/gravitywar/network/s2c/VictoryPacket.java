package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.network.UtilC;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class VictoryPacket {
    public Direction victory;

    public VictoryPacket(Direction victory) {
        this.victory = victory;
    }

    @Decoder
    public VictoryPacket(FriendlyByteBuf buf) {
        this.victory = buf.readEnum(Direction.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(victory);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        UtilC.handleVictoryPacket(this, context);
    }

}
