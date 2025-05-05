package cn.ussshenzhou.gravitywar.network.c2s;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.gui.MainScreen;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class KickNeutralPlayersPacket {

    public KickNeutralPlayersPacket() {

    }

    @Decoder
    public KickNeutralPlayersPacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
    }

    @ServerHandler
    public void handlerS(IPayloadContext context) {
        if (context.player().hasPermissions(4)) {
            context.player().getServer().getPlayerList().getPlayers().stream()
                    .filter(p -> !GameManager.PLAYER_TO_TEAM.containsKey(p.getUUID()))
                    .filter(p -> !p.hasPermissions(2))
                    .forEach(p -> p.connection.disconnect(Component.translatable("multiplayer.disconnect.kicked")));
        }
    }
}
