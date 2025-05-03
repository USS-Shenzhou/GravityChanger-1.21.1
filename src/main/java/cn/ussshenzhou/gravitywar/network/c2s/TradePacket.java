package cn.ussshenzhou.gravitywar.network.c2s;

import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.network.annotation.ServerHandler;
import cn.ussshenzhou.t88.util.InventoryHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class TradePacket {
    public final UUID uuid;
    public final ItemStack from;
    public final ItemStack to;

    public TradePacket(UUID uuid, ItemStack from, ItemStack to) {
        this.uuid = uuid;
        this.from = from;
        this.to = to;
    }

    @Decoder
    public TradePacket(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        from = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
        to = ItemStack.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
    }

    @Encoder
    public void writeToNet(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, from);
        ItemStack.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, to);
    }

    public void serverHandler(Player player) {
        if (player == null) {
            return;
        }
        var inventory = player.getInventory();
        if (InventoryHelper.consume(inventory, from)) {
            tryAdd(player, to);
        }
    }

    private void tryAdd(Player player, ItemStack item) {
        if (!player.addItem(item.copy())) {
            player.drop(item.copy(), false);
        }
        player.getInventory().setChanged();
    }

    @ServerHandler
    public void handler(IPayloadContext context) {
        //NetworkHelper.sendTo(PacketDistributor.PLAYER.with(context::getSender), this);
        MinecraftServer minecraftServer = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        var player = minecraftServer.getPlayerList().getPlayer(uuid);
        serverHandler(player);
    }

}
