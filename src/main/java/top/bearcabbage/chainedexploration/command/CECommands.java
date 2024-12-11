package top.bearcabbage.chainedexploration.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import top.bearcabbage.chainedexploration.team.TeamManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CECommands {
    public void Initialize(){}

    private static int sendSuccessFeedback(ServerCommandSource source, String key, Object... args) {
 //       source.sendFeedback(() -> Text.translatable(key, args), true);
        return 1;
    }

    private static int sendErrorFeedback(ServerCommandSource source, String key, Object... args) {
//        source.sendError(() -> Text.translatable(key, args));
        return 0;
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 新增命令: 加入队伍
        dispatcher.register(literal("clan")
                .then(argument("targetPlayer", StringArgumentType.word())
                        .requires(source -> source.hasPermissionLevel(0)) // 玩家权限调整为0，即所有玩家都可以使用
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                String targetPlayer = StringArgumentType.getString(context, "targetPlayer");
                                if (TeamManager.createOrJoinTeam(player.getName().getString(), targetPlayer)) {
                                    return sendSuccessFeedback(source, "command.clan.join.success", targetPlayer);
                                } else {
                                    sendErrorFeedback(source, "command.clan.join.fail", targetPlayer);
                                }
                            }
                            return 0;
                        })
                )
        );
        // 移除队员命令
        dispatcher.register(literal("clanRemove")
                .requires(source -> source.hasPermissionLevel(0))
                .then(argument("playerName", StringArgumentType.word())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                String targetPlayer = StringArgumentType.getString(context, "playerName");
                                if (TeamManager.removeMemberFromTeam(targetPlayer, player.getName().getString())) {
                                    return sendSuccessFeedback(source, "command.clan.remove.success", targetPlayer);
                                } else {
                                    sendErrorFeedback(source, "command.clan.remove.notInTeam", targetPlayer);
                                }
                            }
                            return 0;
                        })
                ));

        // 解散队伍命令
        dispatcher.register(literal("clanDisband")
                .requires(source -> source.hasPermissionLevel(0))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        if (TeamManager.disbandTeam(player.getName().getString())) {
                            return sendSuccessFeedback(source, "command.clan.disband.success");
                        } else {
                            sendErrorFeedback(source, "command.clan.disband.notLeader");
                        }
                    }
                    return 0;
                })
        );
    }
}