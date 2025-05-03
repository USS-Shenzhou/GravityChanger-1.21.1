package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.game.CoreModeConfig;
import cn.ussshenzhou.gravitywar.game.IntruderModeConfig;
import cn.ussshenzhou.gravitywar.network.Util;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class IntruderModeConfigPacket {
    public Map<Direction, List<BlockPos>> spawnPos = new HashMap<>();
    public Map<Direction, List<BlockPos>> spots = new HashMap<>();

    public IntruderModeConfigPacket(Map<Direction, List<BlockPos>> spawnPos, Map<Direction, List<BlockPos>> spots) {
        this.spawnPos = spawnPos;
        this.spots = spots;
    }

    @Decoder
    public IntruderModeConfigPacket(FriendlyByteBuf buf) {
        this.spawnPos = buf.readMap(Util.MAP_CODEC_0, Util.MAP_CODEC_1);
        this.spots = buf.readMap(Util.MAP_CODEC_0, Util.MAP_CODEC_1);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(spawnPos, Util.MAP_CODEC_0, Util.MAP_CODEC_1);
        buf.writeMap(spots, Util.MAP_CODEC_0, Util.MAP_CODEC_1);
    }

    @ClientHandler
    public void handler(IPayloadContext context) {
        ConfigHelper.getConfigWrite(IntruderModeConfig.class, coreModeConfig -> {
            coreModeConfig.spawnPos = spawnPos;
            coreModeConfig.spots = spots;
        });
    }
}
