package cn.ussshenzhou.gravitywar.network.s2c;

import cn.ussshenzhou.gravitywar.GravityWar;
import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.MatchPhase;
import cn.ussshenzhou.gravitywar.gui.AutoCloseHintHUD;
import cn.ussshenzhou.gravitywar.gui.CoreHintHUD;
import cn.ussshenzhou.gravitywar.gui.IntruderHintHUD;
import cn.ussshenzhou.gravitywar.gui.SiegeHintHUD;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = GravityWar.MODID)
public class ChangePhasePacket {
    private MatchPhase phase;

    public ChangePhasePacket(MatchPhase phase) {
        this.phase = phase;
    }

    @Decoder
    public ChangePhasePacket(FriendlyByteBuf buf) {
        this.phase = buf.readEnum(MatchPhase.class);
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(phase);
    }

    @ClientHandler
    public void handlerC(IPayloadContext context) {
        AutoCloseHintHUD toAdd = switch (ClientGameManager.mode) {
            case SIEGE -> switch (phase) {
                case CHOOSE -> null;
                case PREP -> new SiegeHintHUD.Prep();
                case BATTLE -> new SiegeHintHUD.Battle();
                case FINAL -> new SiegeHintHUD.Final();
            };
            case CORE -> switch (phase) {
                case CHOOSE -> null;
                case PREP -> new CoreHintHUD.Prep();
                case BATTLE -> new CoreHintHUD.Battle();
                case FINAL -> new CoreHintHUD.Final();
            };
            case INTRUDER -> switch (phase) {
                case CHOOSE -> null;
                case PREP -> new IntruderHintHUD.Prep();
                case BATTLE -> new IntruderHintHUD.Battle();
                case FINAL -> new IntruderHintHUD.Final();
            };
        };
        HudManager.add(toAdd);

        var player = context.player();
        player.level()
                .playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        switch (phase) {
                            case CHOOSE -> SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
                            case PREP -> SoundEvents.EXPERIENCE_ORB_PICKUP;
                            case BATTLE -> SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1).value();
                            case FINAL -> SoundEvents.ENDER_DRAGON_GROWL;
                        },
                        SoundSource.PLAYERS,
                        0.5F,
                        1,
                        false
                );
    }

}
