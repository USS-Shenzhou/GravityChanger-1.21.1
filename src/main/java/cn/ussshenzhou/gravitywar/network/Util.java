package cn.ussshenzhou.gravitywar.network;

import cn.ussshenzhou.gravitywar.game.ClientGameManager;
import cn.ussshenzhou.gravitywar.game.GameManager;
import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.gravitywar.gui.*;
import cn.ussshenzhou.gravitywar.network.s2c.*;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.gui.widegt.TComponent;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

    public static void handleOpAllPlayerChosenPacket(OpAllPlayerChosenPacket opAllPlayerChosenPacket) {
        GameManager.TEAM_TO_PLAYER.clear();
        GameManager.TEAM_TO_PLAYER.putAll(opAllPlayerChosenPacket.team2Players);
        if (Minecraft.getInstance().screen instanceof OpScreen opScreen) {
            opScreen.update();
        }
    }

    public static void handleChangePhasePacket(ChangePhasePacket changePhasePacket, IPayloadContext context) {
        AutoCloseHintHUD toAdd = switch (GameManager.mode) {
            case SIEGE -> switch (changePhasePacket.phase) {
                case CHOOSE -> null;
                case PREP -> new SiegeHintHUD.Prep();
                case BATTLE -> new SiegeHintHUD.Battle();
                case FINAL -> new SiegeHintHUD.Final();
            };
            case CORE -> switch (changePhasePacket.phase) {
                case CHOOSE -> null;
                case PREP -> new CoreHintHUD.Prep();
                case BATTLE -> new CoreHintHUD.Battle();
                case FINAL -> new CoreHintHUD.Final();
            };
            case INTRUDER -> switch (changePhasePacket.phase) {
                case CHOOSE -> null;
                case PREP -> new IntruderHintHUD.Prep();
                case BATTLE -> new IntruderHintHUD.Battle();
                case FINAL -> new IntruderHintHUD.Final();
            };
        };
        if (toAdd != null) {
            HudManager.add(toAdd);
        }
        GameManager.phase = changePhasePacket.phase;

        var player = context.player();
        player.level()
                .playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        switch (changePhasePacket.phase) {
                            case CHOOSE,PREP -> SoundEvents.EXPERIENCE_ORB_PICKUP;
                            case BATTLE -> SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(1).value();
                            case FINAL -> SoundEvents.ENDER_DRAGON_GROWL;
                        },
                        SoundSource.PLAYERS,
                        0.5F,
                        1,
                        false
                );
    }

    public static void handleStartCPacket(StartCPacket startCPacket) {
        ClientGameManager.clear();
        HudManager.removeInstanceOf(TComponent.class);
        GameManager.PLAYER_TO_TEAM.putAll(startCPacket.playerToTeam);
        startCPacket.playerToTeam.forEach((uuid, direction) ->
                GameManager.TEAM_TO_PLAYER.computeIfAbsent(direction, d -> new HashSet<>()).add(uuid));
        GameManager.phase = startCPacket.phase;
        GameManager.mode = startCPacket.mode;
        GameManager.maxPlayerPerTeam = startCPacket.maxPlayerPerTeam;
        GameManager.victoryScore = startCPacket.victoryScore;
        ConfigHelper.getConfigWrite(GravityWarConfig.class, c -> c.preparePhase = startCPacket.preparePhase);
        ConfigHelper.getConfigWrite(GravityWarConfig.class, c -> c.battlePhase = startCPacket.battlePhase);
        ConfigHelper.getConfigWrite(GravityWarConfig.class, c -> c.finalPhase = startCPacket.finalPhase);
        ClientGameManager.start();
    }

    public static void handleTeamFailPacket(TeamFailPacket teamFailPacket, IPayloadContext context) {
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
        Minecraft.getInstance().gui.setSubtitle(Component.literal(teamFailPacket.message));
    }

    public static void handleTimeCheckPacket(TimeCheckPacket timeCheckPacket) {
        HudManager.getChildren()
                .forEach(t -> {
                    if (t instanceof CoreModeHUD coreModeHUD) {
                        coreModeHUD.timer.setStartMs(timeCheckPacket.start);
                    } else if (t instanceof IntruderModeHUD intruderModeHUD) {
                        intruderModeHUD.timer.setStartMs(timeCheckPacket.start);
                    }
                });
    }

    public static void handleVictoryPacket(VictoryPacket victoryPacket, IPayloadContext context) {
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
        HudManager.add(new VictoryHUD(victoryPacket.victory));
        TaskHelper.addClientTask(ClientGameManager::end, 200);
    }
}
