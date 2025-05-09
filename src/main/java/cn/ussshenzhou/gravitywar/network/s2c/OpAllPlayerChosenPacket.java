package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.network.Util;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class OpAllPlayerChosenPacket {
    public Map<Direction, HashSet<UUID>> team2Players = new HashMap<>();

    public OpAllPlayerChosenPacket(Map<Direction, HashSet<UUID>> team2Players) {
        this.team2Players = team2Players;
    }

    @Decoder
    public OpAllPlayerChosenPacket(FriendlyByteBuf buf) {
        this.team2Players = buf.readMap(Util.CODEC_DIRECTION, Util.CODEC_UUID_SET);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(team2Players, Util.CODEC_DIRECTION, Util.CODEC_UUID_SET);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        Util.handleOpAllPlayerChosenPacket(this);
    }

}
