package cn.ussshenzhou.gravitywar.network;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.gui.MainScreen;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class ForceChooseScreenPacket {

    public ForceChooseScreenPacket() {

    }

    @Decoder
    public ForceChooseScreenPacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handlerC(IPayloadContext context) {
        Minecraft.getInstance().setScreen(new MainScreen());
    }

    @ServerHandler
    public void handlerS(IPayloadContext context) {
        if (context.player().hasPermissions(4)) {
            context.player().getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> !GameManager.PLAYER_TO_TEAM.containsKey(p.getUUID()))
                    .filter(p -> !p.hasPermissions(2))
                    .forEach(p -> NetworkHelper.sendToPlayer(p, new ForceChooseScreenPacket()));
        }
    }
}
