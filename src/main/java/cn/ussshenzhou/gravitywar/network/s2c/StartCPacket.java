package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.*;
import cn.ussshenzhou.gravitywar.network.UtilC;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class StartCPacket {
    public Map<UUID, Direction> playerToTeam;
    public MatchPhase phase;
    public MatchMode mode;
    public int maxPlayerPerTeam;
    public int victoryScore;
    public int preparePhase;
    public int battlePhase;
    public int finalPhase;

    public StartCPacket(Map<UUID, Direction> playerToTeam, MatchPhase phase, MatchMode mode, int maxPlayerPerTeam, int victoryScore, int preparePhase, int battlePhase, int finalPhase) {
        this.playerToTeam = playerToTeam;
        this.phase = phase;
        this.mode = mode;
        this.maxPlayerPerTeam = maxPlayerPerTeam;
        this.victoryScore = victoryScore;
        this.preparePhase = preparePhase;
        this.battlePhase = battlePhase;
        this.finalPhase = finalPhase;
    }

    @Decoder
    public StartCPacket(FriendlyByteBuf buf) {
        this.playerToTeam = buf.readMap(StreamCodec.ofMember((uuid, b) -> {
                    b.writeUUID(uuid);
                }, b -> b.readUUID()),
                StreamCodec.ofMember((dir, b) -> {
                    b.writeEnum(dir);
                }, b -> b.readEnum(Direction.class)));
        this.phase = buf.readEnum(MatchPhase.class);
        this.mode = buf.readEnum(MatchMode.class);
        this.maxPlayerPerTeam = buf.readVarInt();
        this.victoryScore = buf.readVarInt();
        this.preparePhase = buf.readVarInt();
        this.battlePhase = buf.readVarInt();
        this.finalPhase = buf.readVarInt();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(playerToTeam, StreamCodec.ofMember((uuid, b) -> {
                    b.writeUUID(uuid);
                }, b -> b.readUUID()),
                StreamCodec.ofMember((dir, b) -> {
                    b.writeEnum(dir);
                }, b -> b.readEnum(Direction.class)));
        buf.writeEnum(phase);
        buf.writeEnum(mode);
        buf.writeVarInt(maxPlayerPerTeam);
        buf.writeVarInt(victoryScore);
        buf.writeVarInt(preparePhase);
        buf.writeVarInt(battlePhase);
        buf.writeVarInt(finalPhase);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        UtilC.handleStartCPacket(this);
    }

}
