package cn.ussshenzhou.gravitywar.util;

import cn.ussshenzhou.gravitywar.game.GravityWarConfig;
import cn.ussshenzhou.gravitywar.game.ServerGameManager;
import cn.ussshenzhou.t88.config.ConfigHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.ArrayList;

/**
 * @author USS_Shenzhou
 */
public class ModCommand {

    public ModCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("gw")
                        .requires(c -> c.hasPermission(2))
                        .then(
                                Commands.literal("prepTime")
                                        .then(
                                                Commands.argument("sec", IntegerArgumentType.integer(0))
                                                        .executes(ct -> {
                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg -> cfg.preparePhase = IntegerArgumentType.getInteger(ct, "sec"));
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("battleTime")
                                        .then(
                                                Commands.argument("sec", IntegerArgumentType.integer(0))
                                                        .executes(ct -> {
                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg -> cfg.battlePhase = IntegerArgumentType.getInteger(ct, "sec"));
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("finalTime")
                                        .then(
                                                Commands.argument("sec", IntegerArgumentType.integer(0))
                                                        .executes(ct -> {
                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg -> cfg.finalPhase = IntegerArgumentType.getInteger(ct, "sec"));
                                                            return 1;
                                                        })
                                        )
                        )
                        .then(
                                Commands.literal("forceTeam")
                                        .then(
                                                Commands.argument("player", EntityArgument.player())
                                                        .then(
                                                                Commands.argument("team", EnumArgument.enumArgument(Direction.class))
                                                                        .executes(ct -> {
                                                                            ServerGameManager.pickTeam(EntityArgument.getPlayer(ct, "player"), ct.getArgument("team", Direction.class));
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("corePos")
                                        .then(
                                                Commands.argument("team", EnumArgument.enumArgument(Direction.class))
                                                        .then(
                                                                Commands.argument("pos", Vec3Argument.vec3())
                                                                        .executes(ct -> {
                                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg ->
                                                                                    cfg.corePos.computeIfAbsent(ct.getArgument("team", Direction.class), d -> new ArrayList<>())
                                                                                            .add(BlockPos.containing(Vec3Argument.getVec3(ct, "pos")))
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("spawnPos")
                                        .then(
                                                Commands.argument("team", EnumArgument.enumArgument(Direction.class))
                                                        .then(
                                                                Commands.argument("pos", Vec3Argument.vec3())
                                                                        .executes(ct -> {
                                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg ->
                                                                                    cfg.spawnPos.computeIfAbsent(ct.getArgument("team", Direction.class), d -> new ArrayList<>())
                                                                                            .add(BlockPos.containing(Vec3Argument.getVec3(ct, "pos")))
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("spotPos")
                                        .then(
                                                Commands.argument("team", EnumArgument.enumArgument(Direction.class))
                                                        .then(
                                                                Commands.argument("pos", Vec3Argument.vec3())
                                                                        .executes(ct -> {
                                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg ->
                                                                                    cfg.spotPos.computeIfAbsent(ct.getArgument("team", Direction.class), d -> new ArrayList<>())
                                                                                            .add(BlockPos.containing(Vec3Argument.getVec3(ct, "pos")))
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("waitPos")
                                        .then(
                                                Commands.argument("team", EnumArgument.enumArgument(Direction.class))
                                                        .then(
                                                                Commands.argument("pos", Vec3Argument.vec3())
                                                                        .executes(ct -> {
                                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg ->
                                                                                    cfg.waitingPos.put(ct.getArgument("team", Direction.class), BlockPos.containing(Vec3Argument.getVec3(ct, "pos")))
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("victoryPoints")
                                        .then(
                                                Commands.argument("score", IntegerArgumentType.integer(0))
                                                        .executes(ct -> {
                                                            ConfigHelper.getConfigWrite(GravityWarConfig.class, cfg ->
                                                                    cfg.victoryPoints = IntegerArgumentType.getInteger(ct, "score")
                                                            );
                                                            return 1;
                                                        })
                                        )
                        )
        );
    }
}
