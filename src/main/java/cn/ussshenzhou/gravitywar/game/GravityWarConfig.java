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
public class GravityWarConfig implements TConfig {
    public int preparePhase = 10 * 60;
    public int battlePhase = 10 * 60;
    public int finalPhase = 10 * 60;
    public Map<Direction, BlockPos> waitingPos = new HashMap<>();

    public Map<Direction, List<BlockPos>> corePos = new HashMap<>();


    public Map<Direction, List<BlockPos>> spawnPos = new HashMap<>();
    public Map<Direction, List<BlockPos>> spotPos = new HashMap<>();
    public int victoryPoints = 50;


}
