package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class IntruderModeConfigPacket {
    public Map<Direction, List<BlockPos>> spots = new HashMap<>();

    public IntruderModeConfigPacket(Map<Direction, List<BlockPos>> spots) {
        this.spots = spots;
    }

    @Decoder
    public IntruderModeConfigPacket(FriendlyByteBuf buf) {
        this.spots = buf.readMap(UtilS.CODEC_DIRECTION, UtilS.CODEC_BLOCK_POS_LIST);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(spots, UtilS.CODEC_DIRECTION, UtilS.CODEC_BLOCK_POS_LIST);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        ConfigHelper.getConfigWrite(GravityWarConfig.class, c -> {
            c.spotPos = spots;
        });
    }
}
