package cn.ussshenzhou.gravitywar.network.c2s;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.MatchPhase;
import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.network.annotation.ServerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class StartSPacket {


    public StartSPacket() {
    }

    @Decoder
    public StartSPacket(FriendlyByteBuf buffer) {

    }

    @Encoder
    public void encode(FriendlyByteBuf buffer) {

    }

    @ServerHandler
    public void handler(IPayloadContext context) {
        if (context.player().hasPermissions(4) && ServerGameManager.phase == MatchPhase.CHOOSE) {
            ServerGameManager.start();
        }
    }
}
