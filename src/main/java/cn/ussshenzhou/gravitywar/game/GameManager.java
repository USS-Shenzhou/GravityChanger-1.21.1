package cn.ussshenzhou.gravitywar.game;

import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public abstract class GameManager {
    public static final HashMap<Direction, HashSet<UUID>> TEAM_TO_PLAYER = new HashMap<>();
    public static final HashMap<UUID, Direction> PLAYER_TO_TEAM = new HashMap<>();
    public static MatchPhase phase = MatchPhase.CHOOSE;
    public static MatchMode mode = MatchMode.CORE;
    public static int maxPlayerPerTeam = 17;
    public static int victoryScore = 25;
    public static MatchManager manager;


    public static Optional<Direction> getTeam(UUID uuid) {
        return Optional.ofNullable(PLAYER_TO_TEAM.get(uuid));
    }

    public static Optional<HashSet<UUID>> getTeamPlayers(Direction direction) {
        return Optional.ofNullable(TEAM_TO_PLAYER.get(direction));
    }

    public static void clear() {
        TEAM_TO_PLAYER.clear();
        PLAYER_TO_TEAM.clear();
        phase = MatchPhase.CHOOSE;
        mode = MatchMode.CORE;
    }

}
