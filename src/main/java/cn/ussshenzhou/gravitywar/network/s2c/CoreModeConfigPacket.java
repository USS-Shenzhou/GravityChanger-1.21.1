package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.t88.config.ConfigHelper;
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
//@NetPacket(modid = GravityWar.MODID)
@Deprecated
public class CoreModeConfigPacket {
    public Map<Direction, List<BlockPos>> cores = new HashMap<>();

    public CoreModeConfigPacket(Map<Direction, List<BlockPos>> cores) {
        this.cores = cores;
    }

    //@Decoder
    public CoreModeConfigPacket(FriendlyByteBuf buf) {
        this.cores = buf.readMap(UtilS.CODEC_DIRECTION, UtilS.CODEC_BLOCK_POS_LIST);
    }

    //@Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(cores, UtilS.CODEC_DIRECTION, UtilS.CODEC_BLOCK_POS_LIST);
    }

    //@ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        ConfigHelper.getConfigWrite(GravityWarConfig.class, c -> {
            c.corePos = cores;
        });
    }
}
