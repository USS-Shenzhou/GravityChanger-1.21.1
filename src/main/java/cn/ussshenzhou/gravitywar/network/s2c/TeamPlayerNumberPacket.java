package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class TeamPlayerNumberPacket {
    private int[] number;

    public TeamPlayerNumberPacket(int[] number) {
        this.number = number;
    }

    @Decoder
    public TeamPlayerNumberPacket(FriendlyByteBuf buf) {
        this.number = buf.readVarIntArray(6);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarIntArray(number);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        ClientGameManager.setPlayerNumber(number);
    }
}
