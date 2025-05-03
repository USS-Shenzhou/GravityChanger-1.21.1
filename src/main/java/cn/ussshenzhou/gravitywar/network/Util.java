package cn.ussshenzhou.gravitywar.network;

import cn.ussshenzhou.gravitywar.gui.TradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author USS_Shenzhou
 */
public class Util {
    public static final StreamCodec<FriendlyByteBuf, Direction> MAP_CODEC_0 = StreamCodec.ofMember((dir, b) -> {
        b.writeEnum(dir);
    }, b -> b.readEnum(Direction.class));
    public static final StreamCodec<FriendlyByteBuf, List<BlockPos>> MAP_CODEC_1 = StreamCodec.ofMember((posList, b) -> {
        b.writeCollection(posList, (buffer, p) -> buffer.writeBlockPos(p));
    }, b -> b.readCollection(ArrayList::new, buffer -> buffer.readBlockPos()));

    public static void openTradeScreen() {
        Minecraft.getInstance().setScreen(new TradeScreen());
    }
}
