package cn.ussshenzhou.gravitywar.util;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

/**
 * @author USS_Shenzhou
 */
public class RotationUtil {
    private static final Direction[][] DIR_WORLD_TO_PLAYER = new Direction[6][6];
    private static final Direction[][] DIR_PLAYER_TO_WORLD = new Direction[6][6];

    // === Quaternions for each gravity direction ===

    private static final Quaternionf[] WORLD_ROT = new Quaternionf[6];
    private static final Quaternionf[] CAMERA_ROT = new Quaternionf[6];

    static {
        // DOWN = default
        WORLD_ROT[Direction.DOWN.get3DDataValue()] = new Quaternionf();
        // UP   = flip around Z
        WORLD_ROT[Direction.UP.get3DDataValue()] = Axis.ZP.rotationDegrees(-180);
        // NORTH = tilt forward 90° around X
        WORLD_ROT[Direction.NORTH.get3DDataValue()] = Axis.XP.rotationDegrees(-90);
        // SOUTH = NORTH + flip 180° around Y
        WORLD_ROT[Direction.SOUTH.get3DDataValue()] = new Quaternionf(WORLD_ROT[Direction.NORTH.get3DDataValue()])
                .mul(Axis.YP.rotationDegrees(180));
        // WEST  = tilt forward 90° + turn left 90°
        WORLD_ROT[Direction.WEST.get3DDataValue()] = new Quaternionf(WORLD_ROT[Direction.NORTH.get3DDataValue()])
                .mul(Axis.YP.rotationDegrees(-90));
        // EAST  = tilt forward 90° + turn right 90°
        WORLD_ROT[Direction.EAST.get3DDataValue()] = new Quaternionf(WORLD_ROT[Direction.NORTH.get3DDataValue()])
                .mul(Axis.YP.rotationDegrees(90));

        // Camera (entity) rotation is inverse of world rotation
        for (int i = 0; i < 6; i++) {
            CAMERA_ROT[i] = new Quaternionf(WORLD_ROT[i]).conjugate();
        }
    }

    static {
        for (Direction gravity : Direction.values()) {
            int g = gravity.get3DDataValue();
            for (Direction dir : Direction.values()) {
                // world → player
                Vec3 worldVec = Vec3.atLowerCornerOf(dir.getNormal());
                Vec3 playerVec = vecWorldToPlayer(worldVec, gravity);
                DIR_WORLD_TO_PLAYER[g][dir.get3DDataValue()] =
                        Direction.getNearest((float) playerVec.x, (float) playerVec.y, (float) playerVec.z);

                // player → world
                Vec3 localVec = Vec3.atLowerCornerOf(dir.getNormal());
                Vec3 worldVec2 = vecPlayerToWorld(localVec, gravity);
                DIR_PLAYER_TO_WORLD[g][dir.get3DDataValue()] =
                        Direction.getNearest((float) worldVec2.x, (float) worldVec2.y, (float) worldVec2.z);
            }
        }
    }

    /** Maps a world‐space Direction to the player‐local Direction under given gravity. */
    public static Direction dirWorldToPlayer(Direction worldDir, Direction gravity) {
        return DIR_WORLD_TO_PLAYER[gravity.get3DDataValue()][worldDir.get3DDataValue()];
    }

    /** Maps a player‐local Direction to world‐space Direction under given gravity. */
    public static Direction dirPlayerToWorld(Direction localDir, Direction gravity) {
        return DIR_PLAYER_TO_WORLD[gravity.get3DDataValue()][localDir.get3DDataValue()];
    }

    // === Vector conversion using quaternions ===

    /** World→player: rotate by the camera (entity) quaternion */
    public static Vec3 vecWorldToPlayer(Vec3 v, Direction gravity) {
        return rotateVec(v, getCameraRotationQuaternion(gravity));
    }

    public static Vec3 vecWorldToPlayer(double x, double y, double z, Direction gravity) {
        return vecWorldToPlayer(new Vec3(x, y, z), gravity);
    }

    /** Player→world: rotate by the world quaternion */
    public static Vec3 vecPlayerToWorld(Vec3 v, Direction gravity) {
        return rotateVec(v, getWorldRotationQuaternion(gravity));
    }

    public static Vec3 vecPlayerToWorld(double x, double y, double z, Direction gravity) {
        return vecPlayerToWorld(new Vec3(x, y, z), gravity);
    }

    /** World→player for JOML Vector3f */
    public static Vector3f vecWorldToPlayer(Vector3f v, Direction gravity) {
        Quaternionfc q = getCameraRotationQuaternion(gravity);
        Vector3f out = new Vector3f(v);
        q.transform(out);
        return out;
    }

    public static Vector3f vecWorldToPlayer(float x, float y, float z, Direction gravity) {
        return vecWorldToPlayer(new Vector3f(x, y, z), gravity);
    }

    /** Player→world for JOML Vector3f */
    public static Vector3f vecPlayerToWorld(Vector3f v, Direction gravity) {
        Quaternionfc q = getWorldRotationQuaternion(gravity);
        Vector3f out = new Vector3f(v);
        q.transform(out);
        return out;
    }

    public static Vector3f vecPlayerToWorld(float x, float y, float z, Direction gravity) {
        return vecPlayerToWorld(new Vector3f(x, y, z), gravity);
    }

    // Internal helper: apply quaternion to a Vec3
    private static Vec3 rotateVec(Vec3 v, Quaternionfc q) {
        Vector3f tmp = new Vector3f((float) v.x, (float) v.y, (float) v.z);
        q.transform(tmp);
        return new Vec3(tmp.x(), tmp.y(), tmp.z());
    }

    // === Rotation of look‐vector ↔ yaw,pitch ===

    public static Vec2 rotWorldToPlayer(float yaw, float pitch, Direction gravity) {
        Vec3 dir = rotToVec(yaw, pitch);
        Vec3 conv = vecWorldToPlayer(dir, gravity);
        return vecToRot(conv.x, conv.y, conv.z);
    }

    public static Vec2 rotWorldToPlayer(Vec2 rot, Direction gravity) {
        return rotWorldToPlayer(rot.x, rot.y, gravity);
    }

    public static Vec2 rotPlayerToWorld(float yaw, float pitch, Direction gravity) {
        Vec3 dir = rotToVec(yaw, pitch);
        Vec3 conv = vecPlayerToWorld(dir, gravity);
        return vecToRot(conv.x, conv.y, conv.z);
    }

    public static Vec2 rotPlayerToWorld(Vec2 rot, Direction gravity) {
        return rotPlayerToWorld(rot.x, rot.y, gravity);
    }

    /** Yaw/Pitch → unit look vector */
    public static Vec3 rotToVec(float yaw, float pitch) {
        double rPitch = Math.toRadians(pitch);
        double rYaw = Math.toRadians(-yaw);
        double cYaw = Math.cos(rYaw), sYaw = Math.sin(rYaw);
        double cPitch = Math.cos(rPitch), sPitch = Math.sin(rPitch);
        return new Vec3(sYaw * cPitch, -sPitch, cYaw * cPitch);
    }

    /** unit look vector → (yaw, pitch) */
    public static Vec2 vecToRot(double x, double y, double z) {
        double sinP = -y;
        double rP = Math.asin(sinP);
        double cP = Math.cos(rP);
        double sNY = x / cP;
        double cNY = Mth.clamp(z / cP, -1, 1);
        double rNY = Math.acos(cNY);
        if (sNY < 0) rNY = Math.PI * 2 - rNY;
        float yaw = (float) Math.toDegrees(-rNY);
        float pitch = (float) Math.toDegrees(rP);
        return new Vec2(yaw, pitch);
    }

    // === Optional: mask helpers (axis‐swapping) ===

    public static Vec3 maskWorldToPlayer(double x, double y, double z, Direction gravity) {
        return switch (gravity) {
            case DOWN, UP -> new Vec3(x, y, z);
            case NORTH, SOUTH -> new Vec3(x, z, y);
            case WEST, EAST -> new Vec3(z, x, y);
        };
    }

    public static Vec3 maskWorldToPlayer(Vec3 v, Direction gravity) {
        return maskWorldToPlayer(v.x, v.y, v.z, gravity);
    }

    public static Vec3 maskPlayerToWorld(double x, double y, double z, Direction gravity) {
        return switch (gravity) {
            case DOWN, UP -> new Vec3(x, y, z);
            case NORTH, SOUTH -> new Vec3(x, z, y);
            case WEST, EAST -> new Vec3(y, z, x);
        };
    }

    public static Vec3 maskPlayerToWorld(Vec3 v, Direction gravity) {
        return maskPlayerToWorld(v.x, v.y, v.z, gravity);
    }

    // === AABB helpers ===

    public static AABB boxWorldToPlayer(AABB box, Direction gravity) {
        Vec3 mn = vecWorldToPlayer(box.minX, box.minY, box.minZ, gravity);
        Vec3 mx = vecWorldToPlayer(box.maxX, box.maxY, box.maxZ, gravity);
        return new AABB(mn, mx);
    }

    public static AABB boxPlayerToWorld(AABB box, Direction gravity) {
        Vec3 mn = vecPlayerToWorld(box.minX, box.minY, box.minZ, gravity);
        Vec3 mx = vecPlayerToWorld(box.maxX, box.maxY, box.maxZ, gravity);
        return new AABB(mn, mx);
    }

    /** Builds an entity‐aligned bounding box under arbitrary gravity */
    public static AABB makeBoxFromDimensions(EntityDimensions dims, Direction gravity, Vec3 pos) {
        AABB raw = dims.makeBoundingBox(0, 0, 0);
        return boxPlayerToWorld(raw, gravity).move(pos);
    }

    /** Quaternion to rotate player→world under given gravity */
    public static Quaternionf getWorldRotationQuaternion(Direction gravity) {
        return WORLD_ROT[gravity.get3DDataValue()];
    }

    /** Quaternion to rotate world→player under given gravity */
    public static Quaternionf getCameraRotationQuaternion(Direction gravity) {
        return CAMERA_ROT[gravity.get3DDataValue()];
    }
}
