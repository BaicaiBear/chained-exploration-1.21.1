package top.bearcabbage.chainedexploration.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.player.CEPlayer;
import top.bearcabbage.chainedexploration.teamhor.TeamManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.text.Texts.toText;

public class CECommands {
    public void Initialize(){}
    private static int sendSuccessFeedback(ServerCommandSource source, String message) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            player.sendMessage(toText(new LiteralMessage(message)));
        } else {
            System.out.println(message);
        }
        return 1;
    }

    private static int sendErrorFeedback(ServerCommandSource source, String errorMessage) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            player.sendMessage(toText(new LiteralMessage(errorMessage)));
        } else {
            System.out.println(errorMessage);
        }
        return 0;
    }
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 新增命令: 加入队伍
        dispatcher.register(literal("clan")
                .then(argument("targetPlayer", StringArgumentType.word())
                        .requires(source -> source.hasPermissionLevel(0))
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                String targetPlayer = StringArgumentType.getString(context, "targetPlayer");
                                if (TeamManager.createOrJoinTeam(player.getName().getString(), targetPlayer)) {
                                    return sendSuccessFeedback(source, "成功加入队伍: " + targetPlayer);
                                } else {
                                    sendErrorFeedback(source, "无法加入队伍: 目标玩家不存在或已达到队伍人数限制");
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
                                    return sendSuccessFeedback(source, "成功移除队员: " + targetPlayer);
                                } else {
                                    sendErrorFeedback(source, "无法移除队员: 玩家不在队伍中");
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
                            return sendSuccessFeedback(source, "成功解散队伍");
                        } else {
                            sendErrorFeedback(source, "无法解散队伍: 您不是队伍的领导者");
                        }
                    }
                    return 0;
                })
        );

        //ce指令
        dispatcher.register(literal("ce")
                // 查询玩家等级的子命令
                .then(literal("getlevel")
                        .requires(source -> source.hasPermissionLevel(0))
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                int level = CEPlayer.getCELevel(player);
                                sendSuccessFeedback(source,"您的等级为: " + level);
                                return level; // 返回等级信息
                            }
                            return 0;
                        })
                )

                // 设置玩家等级的子命令
                .then(literal("setlevel")
                        .requires(source -> source.hasPermissionLevel(0))
                        .then(argument("targetPlayer", EntityArgumentType.player())
                                .then(argument("level", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerCommandSource source = context.getSource();

                                            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "targetPlayer");
                                            int newLevel = IntegerArgumentType.getInteger(context, "level");

                                            // 假设存在一个方法来设置玩家等级
                                            if (CEPlayer.setCELevel(targetPlayer, newLevel)) {
                                                source.sendFeedback(() -> Text.literal("成功设置 " + targetPlayer.getName().getLiteralString() + " 的等级为: " + newLevel), false);
                                                return sendSuccessFeedback(source, "等级设置成功");
                                            } else {
                                                sendErrorFeedback(source,"无法设置等级: 请确保等级在0-4之间且玩家在线");
                                                return sendErrorFeedback(source, "等级设置失败");
                                            }
                                        })
                                )
                        )
                )
        );
    }
}