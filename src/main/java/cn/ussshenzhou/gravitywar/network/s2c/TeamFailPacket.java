package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class TeamFailPacket {
    String message;

    public TeamFailPacket(String message) {
        this.message = message;
    }

    @Decoder
    public TeamFailPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message);
    }

    @ClientHandler
    public void handlerC(IPayloadContext context) {
        var player = context.player();
        player.level()
                .playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE,
                        SoundSource.PLAYERS,
                        1.2F,
                        0.7f,
                        false
                );
        Minecraft.getInstance().gui.setSubtitle(Component.literal(message));
    }

}
