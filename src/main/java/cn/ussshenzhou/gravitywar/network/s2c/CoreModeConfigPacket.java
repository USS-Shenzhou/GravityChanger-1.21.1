package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.game.CoreModeConfig;
import cn.ussshenzhou.gravitywar.network.Util;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class CoreModeConfigPacket {
    public Map<Direction, List<BlockPos>> cores = new HashMap<>();

    public CoreModeConfigPacket(Map<Direction, List<BlockPos>> cores) {
        this.cores = cores;
    }

    @Decoder
    public CoreModeConfigPacket(FriendlyByteBuf buf) {
        this.cores = buf.readMap(Util.MAP_CODEC_0, Util.MAP_CODEC_1);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(cores, Util.MAP_CODEC_0, Util.MAP_CODEC_1);
    }

    @ClientHandler
    public void handler(IPayloadContext context) {
        ConfigHelper.getConfigWrite(CoreModeConfig.class, coreModeConfig -> {
            coreModeConfig.cores = cores;
        });
    }
}
