package cn.ussshenzhou.gravitywar.network;

import cn.ussshenzhou.gravitywar.gui.TradeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public class Util {
    public static final StreamCodec<FriendlyByteBuf, Direction> CODEC_DIRECTION = StreamCodec.ofMember((dir, b) -> {
        b.writeEnum(dir);
    }, b -> b.readEnum(Direction.class));
    public static final StreamCodec<FriendlyByteBuf, List<BlockPos>> CODEC_BLOCK_POS_LIST = StreamCodec.ofMember((posList, b) -> {
        b.writeCollection(posList, (buffer, p) -> buffer.writeBlockPos(p));
    }, b -> b.readCollection(ArrayList::new, buffer -> buffer.readBlockPos()));
    public static final StreamCodec<FriendlyByteBuf, HashSet<UUID>> CODEC_UUID_SET = StreamCodec.ofMember((list, b) -> {
        b.writeCollection(list, (buffer, p) -> buffer.writeUUID(p));
    }, b -> b.readCollection(HashSet::new, buffer -> buffer.readUUID()));

    public static void openTradeScreen() {
        Minecraft.getInstance().setScreen(new TradeScreen());
    }
}
