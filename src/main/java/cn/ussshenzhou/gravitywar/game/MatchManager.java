package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.gravitywar.network.s2c.CoreModeConfigPacket;
import cn.ussshenzhou.gravitywar.network.s2c.IntruderModeConfigPacket;
import cn.ussshenzhou.gravitywar.util.DirectionHelper;
import cn.ussshenzhou.gravitywar.util.GravityChangerAPIProxy;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import static cn.ussshenzhou.gravitywar.game.GameManager.*;
import static cn.ussshenzhou.gravitywar.game.ServerGameManager.*;
import static cn.ussshenzhou.gravitywar.game.ClientGameManager.*;

/**
 * @author USS_Shenzhou
 */
public abstract class MatchManager {

    public abstract void start();

    public void serverTick() {
        autoGravityDirection();
    }

    protected void autoGravityDirection() {
        getLevel().getAllEntities().forEach(entity -> {
            if (entity instanceof Player player) {
                if (!PLAYER_TO_TEAM.containsKey(player.getUUID())) {
                    return;
                }
                var tags = entity.getTags();
                //remove, add
                String[] tagTo = {null, null};
                tags.stream()
                        .filter(s -> s.startsWith("gw_rot_cd_"))
                        .findAny()
                        .ifPresentOrElse(tag -> {
                            int t = Integer.parseInt(tag.replace("gw_rot_cd_", ""));
                            t--;
                            tagTo[0] = tag;
                            if (t > 0) {
                                tagTo[1] = "gw_rot_cd_" + t;
                            }
                        }, () -> {
                            var currentG = GravityChangerAPIProxy.getGravityDirection(entity);
                            var correctG = /*phase == MatchPhase.CHOOSE ? Direction.DOWN :*/ DirectionHelper.getPyramidRegion(entity.getEyePosition());
                            if (currentG != correctG) {
                                GravityChangerAPIProxy.setBaseGravityDirection(entity, correctG);
                                entity.addTag("gw_rot_cd_" + 20);
                            }
                        });
                if (tagTo[0] != null) {
                    entity.removeTag(tagTo[0]);
                }
                if (tagTo[1] != null) {
                    entity.addTag(tagTo[1]);
                }
            } else {
                var currentG = GravityChangerAPIProxy.getGravityDirection(entity);
                var correctG = DirectionHelper.getPyramidRegion(entity.getEyePosition());
                if (currentG != correctG) {
                    GravityChangerAPIProxy.setBaseGravityDirection(entity, correctG);
                }
            }
        });
    }

    public abstract void clientTick();

    public static MatchManager create(MatchMode mode) {
        return switch (mode) {
            case CORE -> new Core();
            case INTRUDER -> new Intruder();
        };
    }

    public static class Core extends MatchManager {

        @Override
        public void start() {
            NetworkHelper.sendToAllPlayers(new CoreModeConfigPacket(ConfigHelper.getConfigRead(CoreModeConfig.class).cores));
        }

        @Override
        public void serverTick() {
            super.serverTick();
        }

        @Override
        public void clientTick() {

        }
    }

    public static class Intruder extends MatchManager {

        @Override
        public void start() {
            var cfg = ConfigHelper.getConfigRead(IntruderModeConfig.class);
            NetworkHelper.sendToAllPlayers(new IntruderModeConfigPacket(cfg.spawnPos, cfg.spots));
        }

        @Override
        public void serverTick() {
            super.serverTick();
        }

        @Override
        public void clientTick() {

        }
    }
}
