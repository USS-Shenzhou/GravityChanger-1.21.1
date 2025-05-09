package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.gui.VictoryHUD;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class VictoryPacket {
    private Direction victory;

    public VictoryPacket(Direction victory) {
        this.victory = victory;
    }

    @Decoder
    public VictoryPacket(FriendlyByteBuf buf) {
        this.victory = buf.readEnum(Direction.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(victory);
    }

    @ClientHandler
    @OnlyIn(Dist.CLIENT)
    public void handler(IPayloadContext context) {
        var player = context.player();
        player.level()
                .playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS,
                        0.5F,
                        1,
                        false
                );
        HudManager.add(new VictoryHUD(victory));
        TaskHelper.addClientTask(ClientGameManager::end, 200);
    }
}
