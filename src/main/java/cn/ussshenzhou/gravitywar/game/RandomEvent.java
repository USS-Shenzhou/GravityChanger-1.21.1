package cn.ussshenzhou.gravitywar.game;

import cn.ussshenzhou.t88.gui.util.ITranslatable;

/**
 * @author USS_Shenzhou
 */

public enum RandomEvent {

    FOG(60),
    RANDOM_GRAVITY(100),
    LOW_GRAVITY(60),
    RESPAWN_BEACON(60),
    FIREBALL(30),
    CORE_REVIVE(60),
    HIGH_KNOCKBACK(60),
    ULTRA_BOUNCE(60);

    public int time;

    RandomEvent(int time) {
        this.time = time;
    }
}
