package cn.ussshenzhou.gravitywar.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static net.minecraft.core.Direction.*;

/**
 * @author USS_Shenzhou
 */
public class DirectionHelper {

    public static Direction getPyramidRegion(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) {
            return DOWN;
        }
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX > 50 || absY > 50 || absZ > 50) {
            return DOWN;
        }

        if (absY >= absX && absY >= absZ) {
            return y > 0 ? UP : DOWN;
        }
        if (absX >= absY && absX >= absZ) {
            return x > 0 ? WEST : EAST;
        }
        return z > 0 ? NORTH : SOUTH;
    }

    public static Direction getPyramidRegion(BlockPos pos) {
        return getPyramidRegion(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Quaternionf getRotation(Direction dir) {
        return switch (dir) {
            case DOWN -> new Quaternionf();
            case UP -> new Quaternionf().rotationX((float) Math.PI);
            case NORTH -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) Math.PI);
            case SOUTH -> new Quaternionf().rotationX((float) (Math.PI / 2));
            case WEST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2));
            case EAST -> new Quaternionf().rotationXYZ((float) (Math.PI / 2), 0.0F, (float) (-Math.PI / 2));
        };
    }

    private static final double INV_SQRT2 = 1.0 / Math.sqrt(2.0);

    public static double distanceToBoundary(double x, double y, double z) {
        if (x == 0 && y == 0 && z == 0) {
            return 0;
        }

        double absX = Math.abs(x), absY = Math.abs(y), absZ = Math.abs(z);
        double a, b, c;

        if (absY >= absX && absY >= absZ) {
            a = y;
            b = x;
            c = z;
        } else if (absX >= absY && absX >= absZ) {
            a = x;
            b = y;
            c = z;
        } else {
            a = z;
            b = x;
            c = y;
        }

        double s = Math.signum(a);
        double d1 = Math.abs(s * a - b) * INV_SQRT2;
        double d2 = Math.abs(s * a + b) * INV_SQRT2;
        double d3 = Math.abs(s * a - c) * INV_SQRT2;
        double d4 = Math.abs(s * a + c) * INV_SQRT2;

        return Math.min(Math.min(d1, d2), Math.min(d3, d4));
    }

    private static final Vec3 PIVOT = new Vec3(0.5, 0.5, 0.5);

    public static VoxelShape rotateForGravity(VoxelShape shape, Direction gravity) {
        return rotate(shape, Direction.DOWN, gravity);
    }

    public static VoxelShape rotate(VoxelShape shape, Direction fromGravity, Direction toGravity) {
        if (fromGravity == toGravity) {
            return shape;
        }

        Matrix4f mat = getGravityRotationMatrix(fromGravity, toGravity);

        VoxelShape result = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            Vec3 min = rotatePoint(new Vec3(box.minX, box.minY, box.minZ), mat);
            Vec3 max = rotatePoint(new Vec3(box.maxX, box.maxY, box.maxZ), mat);

            double x1 = Math.min(min.x, max.x);
            double y1 = Math.min(min.y, max.y);
            double z1 = Math.min(min.z, max.z);
            double x2 = Math.max(min.x, max.x);
            double y2 = Math.max(min.y, max.y);
            double z2 = Math.max(min.z, max.z);

            result = Shapes.joinUnoptimized(
                    result,
                    Shapes.box(x1, y1, z1, x2, y2, z2),
                    BooleanOp.OR
            );
        }
        return result.optimize();
    }

    private static Matrix4f getGravityRotationMatrix(Direction from, Direction to) {
        Vector3f src = new Vector3f(
                from.getNormal().getX(),
                from.getNormal().getY(),
                from.getNormal().getZ()
        );
        Vector3f dst = new Vector3f(
                to.getNormal().getX(),
                to.getNormal().getY(),
                to.getNormal().getZ()
        );
        Quaternionf q = new Quaternionf().rotationTo(src, dst);
        return new Matrix4f().rotate(q);
    }

    private static Vec3 rotatePoint(Vec3 p, Matrix4f mat) {
        Vector4f v = new Vector4f(
                (float) (p.x - PIVOT.x),
                (float) (p.y - PIVOT.y),
                (float) (p.z - PIVOT.z),
                1f
        );
        mat.transform(v);
        return new Vec3(
                v.x() + PIVOT.x,
                v.y() + PIVOT.y,
                v.z() + PIVOT.z
        );
    }
}
