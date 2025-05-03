package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.t88.config.TConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class IntruderModeConfig implements TConfig {
    public Map<Direction, List<BlockPos>> spawnPos = new HashMap<>();
    public Map<Direction, List<BlockPos>> spots = new HashMap<>();
}
