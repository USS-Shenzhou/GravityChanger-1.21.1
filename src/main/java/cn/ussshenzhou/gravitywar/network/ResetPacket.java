package cn.ussshenzhou.gravitywar.network;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class ResetPacket {


    public ResetPacket() {
    }

    @Decoder
    public ResetPacket(FriendlyByteBuf buffer) {

    }

    @Encoder
    public void encode(FriendlyByteBuf buffer) {

    }

    @ServerHandler
    public void handler(IPayloadContext context) {
        if (context.player().hasPermissions(4)) {
            ServerGameManager.end();
            NetworkHelper.sendToAllPlayers(this);
        }
    }

    @ClientHandler
    public void handlerC(IPayloadContext context) {
        ClientGameManager.end();
    }
}
