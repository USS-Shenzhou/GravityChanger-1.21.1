package cn.ussshenzhou.gravitywar.util;

import net.minecraft.core.Direction;
import org.joml.Vector3f;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class ColorHelper {

    public static int getRGB(Direction direction) {
        return switch (direction) {
            case NORTH -> 0xEDE20C;
            case SOUTH -> 0xFA9DCF;
            case EAST -> 0xDB181F;
            case WEST -> 0x4892Ff;
            case UP -> 0x000000;
            case DOWN -> 0xF09D29;
        };
    }

    public static int getARGB(Direction direction, int alpha255) {
        return (alpha255 << 24) | getRGB(direction);
    }

    public static Vector3f getRGB3f(Direction direction) {
        int rgb = getRGB(direction);
        return new Vector3f(
                ((rgb >> 16) & 0xFF) / 255f,
                ((rgb >>  8) & 0xFF) / 255f,
                ( rgb        & 0xFF) / 255f
        );
    }
}
