package cn.ussshenzhou.gravitywar.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;

/**
 * @author USS_Shenzhou
 */
public class DirectionHelper {

    public static Direction getPyramidRegion(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) {
            return UP;
        }
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absY >= absX && absY >= absZ) {
            return y > 0 ? DOWN : UP;
        }
        if (absX >= absY && absX >= absZ) {
            return x > 0 ? WEST : EAST;
        }
        return z > 0 ? NORTH : SOUTH;
    }

    public static Direction getPyramidRegion(BlockPos pos) {
        return getPyramidRegion(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    private static final double INV_SQRT2 = 1.0 / Math.sqrt(2.0);

    public static double distanceToBoundary(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) {
            return 0;
        }

        double absX = Math.abs(x), absY = Math.abs(y), absZ = Math.abs(z);
        double a, b, c;

        if (absY >= absX && absY >= absZ) {
            a = y; b = x; c = z;
        }
        else if (absX >= absY && absX >= absZ) {
            a = x; b = y; c = z;
        }
        else {
            a = z; b = x; c = y;
        }

        double s = Math.signum(a);
        double d1 = Math.abs(s * a - b) * INV_SQRT2;
        double d2 = Math.abs(s * a + b) * INV_SQRT2;
        double d3 = Math.abs(s * a - c) * INV_SQRT2;
        double d4 = Math.abs(s * a + c) * INV_SQRT2;

        return Math.min(Math.min(d1, d2), Math.min(d3, d4));
    }

}
