package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.gui.CoreModeHUD;
import cn.ussshenzhou.gravitywar.gui.IntruderModeHUD;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class TimeCheckPacket {
    private long start;

    public TimeCheckPacket(long start) {
        this.start = start;
    }

    @Decoder
    public TimeCheckPacket(FriendlyByteBuf buf) {
        this.start = buf.readVarLong();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(start);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        HudManager.getChildren()
                .forEach(t -> {
                    if (t instanceof CoreModeHUD coreModeHUD) {
                        coreModeHUD.timer.setStartMs(start);
                    } else if (t instanceof IntruderModeHUD intruderModeHUD) {
                        intruderModeHUD.timer.setStartMs(start);
                    }
                });
    }
}
