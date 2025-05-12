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
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author USS_Shenzhou
 */
public class UtilC {
    public static void openTradeScreen() {
        Minecraft.getInstance().setScreen(new TradeScreen());
    }

    public static void handleOpAllPlayerChosenPacket(OpAllPlayerChosenPacket opAllPlayerChosenPacket) {
        GameManager.TEAM_TO_PLAYER.clear();
        GameManager.TEAM_TO_PLAYER.putAll(opAllPlayerChosenPacket.team2Players);
        GameManager.PLAYER_TO_TEAM.clear();
        opAllPlayerChosenPacket.team2Players.forEach((direction, uuids) -> uuids.forEach(uuid -> GameManager.PLAYER_TO_TEAM.put(uuid, direction)));
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
            HudManager.addOrReplaceIfSameClassExist(toAdd);
        }
        GameManager.phase = changePhasePacket.phase;

        var player = context.player();
        player.level()
                .playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        switch (changePhasePacket.phase) {
                            case CHOOSE, PREP -> SoundEvents.EXPERIENCE_ORB_PICKUP;
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
        HudManager.addOrReplaceIfSameClassExist(new SubtitleHUD(teamFailPacket.message));
    }

    public static void handleSubtitlePacket(SubtitlePacket subtitlePacket, IPayloadContext context) {
        HudManager.addOrReplaceIfSameClassExist(new SubtitleHUD(subtitlePacket.message));
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
        UtilC.delay(ClientGameManager::end, 10);
    }

    public static void handleRandomEventPacket(RandomEventPacket randomEventPacket, IPayloadContext context) {
        GameManager.event = randomEventPacket.event;
        UtilC.delay(() -> {
            GameManager.event = null;
        }, randomEventPacket.event.time);
        HudManager.addOrReplaceIfSameClassExist(new AutoCloseHintHUD("出现扰动！",
                switch (randomEventPacket.event) {
                    case FOG -> "迷雾重重";
                    case RANDOM_GRAVITY -> "重力紊乱";
                    case LOW_GRAVITY -> "低功率模式";
                    case FIREBALL -> "太阳约束失控";
                    case CORE_REVIVE -> "核心修复包已生成";
                    case ULTRA_BOUNCE -> "史莱姆感染";
                    case HIGH_KNOCKBACK -> "强作用力";
                    case RESPAWN_BEACON -> "重生信标已投放";
                },
                switch (randomEventPacket.event) {
                    case FOG -> "核心将会在周围生成迷雾来保护自己（60秒）";
                    case RANDOM_GRAVITY -> {
                        var dir = Direction.values()[Mth.abs(Minecraft.getInstance().player.getUUID().hashCode()) / 6];
                        yield "重力即将转向 " + switch (dir) {
                            case DOWN -> "下（黑）";
                            case UP -> "上（橙）";
                            case NORTH -> "北（粉）";
                            case SOUTH -> "南（黄）";
                            case WEST -> "西（红）";
                            case EAST -> "东（蓝）";
                        } + "(持续90秒)";
                    }
                    case LOW_GRAVITY -> "六分之一重力（60秒）";
                    case FIREBALL -> "避开从太阳飞来的火球！（30秒）";
                    case CORE_REVIVE -> "跟着粒子指引找到备用核心，带回己方区域并放置";
                    case ULTRA_BOUNCE -> "没有摔落伤害，弹！弹！弹！（60秒）";
                    case HIGH_KNOCKBACK -> "击退增强500%（60秒）";
                    case RESPAWN_BEACON -> "跟着粒子找到重生信标，放置在你想要的地方";
                }));
    }

    public static void delay(Runnable runnable, int delay) {
        CompletableFuture
                .runAsync(
                        () -> Minecraft.getInstance().execute(runnable),
                        CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS)
                );
    }
}
