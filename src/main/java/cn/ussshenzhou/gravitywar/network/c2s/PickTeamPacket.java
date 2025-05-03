package cn.ussshenzhou.gravitywar.network.c2s;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.network.annotation.ServerHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class PickTeamPacket {
    private Direction team;

    public PickTeamPacket(Direction team) {
        this.team = team;
    }

    @Decoder
    public PickTeamPacket(FriendlyByteBuf buf) {
        this.team = buf.readEnum(Direction.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(team);
    }

    @ServerHandler
    public void handler(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ServerGameManager.pickTeam(player, team);
        }
    }
}
