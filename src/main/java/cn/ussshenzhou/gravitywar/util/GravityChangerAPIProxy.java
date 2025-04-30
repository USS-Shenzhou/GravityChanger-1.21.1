package cn.ussshenzhou.gravitywar.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

/**
 * @author USS_Shenzhou
 */
public class GravityChangerAPIProxy {
    private static final Class<?> API_CLASS;
    private static final Method
            M_GET_GRAVITY_DIRECTION,
            M_GET_GRAVITY_STRENGTH,
            M_GET_BASE_GRAVITY_STRENGTH,
            M_SET_BASE_GRAVITY_STRENGTH,
            M_GET_DIMENSION_GRAVITY_STRENGTH,
            M_SET_DIMENSION_GRAVITY_STRENGTH,
            M_RESET_GRAVITY,
            M_GET_BASE_GRAVITY_DIRECTION,
            M_SET_BASE_GRAVITY_DIRECTION,
            M_INSTANT_SET_CLIENT_BASE_GRAVITY_DIRECTION,
            M_GET_WORLD_VELOCITY,
            M_SET_WORLD_VELOCITY,
            M_GET_EYE_OFFSET,
            M_CAN_CHANGE_GRAVITY;

    static {
        try {
            API_CLASS = Class.forName("gravity_changer.api.GravityChangerAPI");

            M_GET_GRAVITY_DIRECTION                 = API_CLASS.getMethod("getGravityDirection", Entity.class);
            M_GET_GRAVITY_STRENGTH                  = API_CLASS.getMethod("getGravityStrength", Entity.class);
            M_GET_BASE_GRAVITY_STRENGTH             = API_CLASS.getMethod("getBaseGravityStrength", Entity.class);
            M_SET_BASE_GRAVITY_STRENGTH             = API_CLASS.getMethod("setBaseGravityStrength", Entity.class, double.class);
            M_GET_DIMENSION_GRAVITY_STRENGTH        = API_CLASS.getMethod("getDimensionGravityStrength", Level.class);
            M_SET_DIMENSION_GRAVITY_STRENGTH        = API_CLASS.getMethod("setDimensionGravityStrength", Level.class, double.class);
            M_RESET_GRAVITY                         = API_CLASS.getMethod("resetGravity", Entity.class);
            M_GET_BASE_GRAVITY_DIRECTION            = API_CLASS.getMethod("getBaseGravityDirection", Entity.class);
            M_SET_BASE_GRAVITY_DIRECTION            = API_CLASS.getMethod("setBaseGravityDirection", Entity.class, Direction.class);
            M_INSTANT_SET_CLIENT_BASE_GRAVITY_DIRECTION =
                    API_CLASS.getMethod(
                            "instantlySetClientBaseGravityDirection",
                            Entity.class, Direction.class
                    );
            M_GET_WORLD_VELOCITY                    = API_CLASS.getMethod("getWorldVelocity", Entity.class);
            M_SET_WORLD_VELOCITY                    = API_CLASS.getMethod("setWorldVelocity", Entity.class, Vec3.class);
            M_GET_EYE_OFFSET                        = API_CLASS.getMethod("getEyeOffset", Entity.class);
            M_CAN_CHANGE_GRAVITY                    = API_CLASS.getMethod("canChangeGravity", Entity.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize GravityChangerAPIProxy", e);
        }
    }

    private GravityChangerAPIProxy() { /* no instantiation */ }

    public static Direction getGravityDirection(Entity entity) {
        try {
            return (Direction) M_GET_GRAVITY_DIRECTION.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double getGravityStrength(Entity entity) {
        try {
            return (double) M_GET_GRAVITY_STRENGTH.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double getBaseGravityStrength(Entity entity) {
        try {
            return (double) M_GET_BASE_GRAVITY_STRENGTH.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setBaseGravityStrength(Entity entity, double strength) {
        try {
            M_SET_BASE_GRAVITY_STRENGTH.invoke(null, entity, strength);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double getDimensionGravityStrength(Level world) {
        try {
            return (double) M_GET_DIMENSION_GRAVITY_STRENGTH.invoke(null, world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setDimensionGravityStrength(Level world, double strength) {
        try {
            M_SET_DIMENSION_GRAVITY_STRENGTH.invoke(null, world, strength);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void resetGravity(Entity entity) {
        try {
            M_RESET_GRAVITY.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Direction getBaseGravityDirection(Entity entity) {
        try {
            return (Direction) M_GET_BASE_GRAVITY_DIRECTION.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setBaseGravityDirection(Entity entity, Direction gravityDirection) {
        try {
            M_SET_BASE_GRAVITY_DIRECTION.invoke(null, entity, gravityDirection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void instantlySetClientBaseGravityDirection(Entity entity, Direction direction) {
        try {
            M_INSTANT_SET_CLIENT_BASE_GRAVITY_DIRECTION.invoke(null, entity, direction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Vec3 getWorldVelocity(Entity entity) {
        try {
            return (Vec3) M_GET_WORLD_VELOCITY.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setWorldVelocity(Entity entity, Vec3 worldVelocity) {
        try {
            M_SET_WORLD_VELOCITY.invoke(null, entity, worldVelocity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Vec3 getEyeOffset(Entity entity) {
        try {
            return (Vec3) M_GET_EYE_OFFSET.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean canChangeGravity(Entity entity) {
        try {
            return (boolean) M_CAN_CHANGE_GRAVITY.invoke(null, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
