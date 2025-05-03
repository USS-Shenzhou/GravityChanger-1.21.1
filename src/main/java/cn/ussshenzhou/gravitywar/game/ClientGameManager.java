package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.gui.CoreModeHUD;
import cn.ussshenzhou.gravitywar.gui.IntruderModeHUD;
import cn.ussshenzhou.gravitywar.gui.MainScreen;
import cn.ussshenzhou.t88.gui.HudManager;
import cn.ussshenzhou.t88.gui.widegt.TComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientGameManager extends GameManager {
    private static int[] playerNumber = new int[6];

    private static Minecraft getMC() {
        return Minecraft.getInstance();
    }

    public static void setPlayerNumber(int[] playerNumber) {
        ClientGameManager.playerNumber = playerNumber;
        if (getMC().screen instanceof MainScreen mainScreen) {
            mainScreen.update();
        }
    }

    public static int[] getPlayerNumber() {
        return playerNumber;
    }

    public static Optional<Direction> getMyTeam() {
        return getMC().player == null ? Optional.empty() : Optional.ofNullable(PLAYER_TO_TEAM.get(getMC().player.getUUID()));
    }

    public static Optional<Player> getPlayerC(UUID uuid) {
        return getMC().level == null ? Optional.empty() : Optional.ofNullable(getMC().level.getPlayerByUUID(uuid));
    }

    public static void clear() {
        GameManager.clear();
    }

    public static void start() {
        manager = MatchManager.create(mode);
        manager.start();
        HudManager.add(mode == MatchMode.CORE ? new CoreModeHUD() : new IntruderModeHUD());
    }

    public static void end() {
        HudManager.removeInstanceOf(TComponent.class);
        manager = null;
    }


    @SubscribeEvent
    public static void matchTick(ClientTickEvent.Pre event) {
        if (manager != null) {
            manager.clientTick();
        }
    }

}
