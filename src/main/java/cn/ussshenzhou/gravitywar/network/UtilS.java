package cn.ussshenzhou.gravitywar.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static cn.ussshenzhou.gravitywar.game.ServerGameManager.getServer;

/**
 * @author USS_Shenzhou
 */
public class UtilS {
    public static final StreamCodec<FriendlyByteBuf, Direction> CODEC_DIRECTION = StreamCodec.ofMember((dir, b) -> {
        b.writeEnum(dir);
    }, b -> b.readEnum(Direction.class));
    public static final StreamCodec<FriendlyByteBuf, List<BlockPos>> CODEC_BLOCK_POS_LIST = StreamCodec.ofMember((posList, b) -> {
        b.writeCollection(posList, (buffer, p) -> buffer.writeBlockPos(p));
    }, b -> b.readCollection(ArrayList::new, buffer -> buffer.readBlockPos()));
    public static final StreamCodec<FriendlyByteBuf, HashSet<UUID>> CODEC_UUID_SET = StreamCodec.ofMember((list, b) -> {
        b.writeCollection(list, (buffer, p) -> buffer.writeUUID(p));
    }, b -> b.readCollection(HashSet::new, buffer -> buffer.readUUID()));

    public static void delay(Runnable runnable, int delay) {
        CompletableFuture
                .runAsync(
                        () -> getServer().execute(runnable),
                        CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS)
                );
    }

    public static void delayMs(Runnable runnable, int delay) {
        CompletableFuture
                .runAsync(
                        () -> getServer().execute(runnable),
                        CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS)
                );
    }
}
