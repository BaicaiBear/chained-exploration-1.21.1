package top.bearcabbage.chainedexploration.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.team.CETeamManager;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//注册命令并引用对应的CETeamManager方法 包括一些简单的使用者判断
public class CECommands {

    private static int sendSuccessFeedback(ServerCommandSource source, String message) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.of(message));
        } else {
            System.out.println(message);
        }
        return 1;
    }

    private static int sendErrorFeedback(ServerCommandSource source, String errorMessage) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.of(errorMessage));
        } else {
            System.out.println(errorMessage);
        }
        return 0;
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        // CETeam相关命令整合
        LiteralArgumentBuilder<ServerCommandSource> cetRoot = literal("cet")
                .requires(source -> source.hasPermissionLevel(0));

        // 加入队伍子命令
        cetRoot.then(literal("invite")
                .then(argument("targetPlayer", EntityArgumentType.player())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "targetPlayer");
                                if (targetPlayer.getUuid().equals(player.getUuid())) {
                                    return sendErrorFeedback(source, "不能向自己发送队伍邀请");
                                }//检测自己
                                if (CETeamManager.sendInvitation(player, targetPlayer)) {
                                    return sendSuccessFeedback(source, "已向 " + targetPlayer.getName().getLiteralString() + " 发送队伍邀请");
                                } else {
                                    return sendErrorFeedback(source, "无法发送邀请: 目标玩家已有未处理的邀请或已达队伍人数限制");
                                }
                            }
                            return 0;
                        })
                ));

        cetRoot.then(literal("accept")
                .then(argument("sender", EntityArgumentType.player())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayer();
                            ServerPlayerEntity sender = context.getArgument("sender", ServerPlayerEntity.class);

                            if (CETeamManager.acceptInvitation(player, sender)) {
                                return sendSuccessFeedback(source, "成功接受来自 " + sender.getName().getString() + " 的邀请并加入队伍");
                            } else {
                                return sendErrorFeedback(source, "没有来自 " + sender.getName().getString() + " 的未处理邀请或接受邀请失败");
                            }
                        })
                )
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        // 检查是否存在多个邀请
                        long multipleInvitationsCount = CETeamManager.pendingInvitations.values().stream()
                                .filter(inv -> inv.recipient().equals(player))
                                .count();
                        if (multipleInvitationsCount > 1) {
                            return sendErrorFeedback(source, "您有多个未处理的邀请，请先使用 /cet list invite 来查看并管理它们。");
                        }
                        if (CETeamManager.acceptInvitation(player)) {
                            return sendSuccessFeedback(source, "成功接受邀请并加入队伍");
                        } else {
                            return sendErrorFeedback(source, "没有未处理的邀请或接受邀请失败");
                        }
                    }
                    return 0;
                })
        );

        // 拒绝邀请子命令
        cetRoot.then(literal("deny")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        if (CETeamManager.denyInvitation(player)) {
                            return sendSuccessFeedback(source, "成功清空邀请");
                        } else {
                            return sendErrorFeedback(source, "没有未处理的邀请");
                        }
                    }
                    return 0;
                })
        );

        // 退出队伍子命令
        cetRoot.then(literal("quit")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        if (CETeamManager.LeaveTeam(player)) {
                            return sendSuccessFeedback(source, "成功退出队伍");
                        } else {
                            return sendErrorFeedback(source, "无法退出队伍: 您没有在任何队伍中");
                        }
                    }
                    return 0;
                })
        );

        // 移除队员子命令
        cetRoot.then(literal("remove")
                .then(argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            if (source.getEntity() instanceof ServerPlayerEntity player) {
                                ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                if (player.getUuid().equals(targetPlayer.getUuid())) {
                                    return sendErrorFeedback(source, "您不能移除自己");
                                }//检测自己
                                if (CETeamManager.removeMember(targetPlayer, player)) {
                                    return sendSuccessFeedback(source, "成功移除队员: " + targetPlayer.getName().getLiteralString());
                                } else {
                                    return sendErrorFeedback(source, "无法移除队员: 玩家不在队伍中或您不是队伍的发起者");
                                }
                            }
                            return 0;
                        })
                ));

        // 解散队伍子命令
        cetRoot.then(literal("disband")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        if (CETeamManager.disbandTeam(player)) {
                            return sendSuccessFeedback(source, "成功解散队伍");
                        } else {
                            return sendErrorFeedback(source, "无法解散队伍: 您不是队伍的发起者");
                        }
                    }
                    return 0;
                })
        );

        // 列出发出命令的玩家的队伍及队长和成员
        cetRoot.then(literal("list")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        String myTeamInfo = CETeamManager.listMyTeam(player);
                        if (myTeamInfo.isEmpty()) {
                            sendErrorFeedback(source, "您当前没有在任何队伍中");
                        } else {
                            sendSuccessFeedback(source, myTeamInfo);
                        }
                    } else {
                        sendErrorFeedback(source, "该命令只能由玩家执行");
                    }
                    return 1;
                })
        );

        dispatcher.register(cetRoot);

        // ce 命令及其子命令
        LiteralArgumentBuilder<ServerCommandSource> ceRoot = literal("ce")
                .requires(source -> source.hasPermissionLevel(0));

        // 查询level
        ceRoot.then(literal("level")
                .executes(context -> {
                    // 如果没有提供targetPlayer参数，获取命令发出者作为默认查询对象
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity targetPlayer = (ServerPlayerEntity) source.getEntity();
                    if (targetPlayer == null) {
                        return sendErrorFeedback(source, "该命令只能由玩家执行");
                    }
                    int level = (targetPlayer instanceof CEPlayerAccessor cePlayerAccessor) ? cePlayerAccessor.getCE().getCELevel() : -1;
                    return sendSuccessFeedback(source, "您的CE等级为: " + level);
                })
                .then(argument("targetPlayer", EntityArgumentType.player())
                        .executes(context -> {
                            // 如果提供了targetPlayer参数，使用参数指定的玩家
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "targetPlayer");
                            int level = (targetPlayer instanceof CEPlayerAccessor cePlayerAccessor) ? cePlayerAccessor.getCE().getCELevel() : -1;
                            return sendSuccessFeedback(source, targetPlayer.getName().getLiteralString() + " 的CE等级为: " + level);
                        })
                ));

        ceRoot.then(literal("list")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String allTeamsInfo = CETeamManager.listAllTeams();
                    if (allTeamsInfo.equals("当前所有队伍列表:")) {
                        sendErrorFeedback(source, "当前没有队伍存在");
                    } else {
                        sendSuccessFeedback(source, allTeamsInfo);
                    }
                    return 1;
                })
        );

        // 设置level子命令
        ceRoot.then(argument("targetPlayer", EntityArgumentType.player())
                .then(literal("set")
                        .then(argument("level", IntegerArgumentType.integer(0))
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "targetPlayer");
                                    int newLevel = IntegerArgumentType.getInteger(context, "level");
                                    if (targetPlayer instanceof CEPlayerAccessor cePlayerAccessor && cePlayerAccessor.getCE().setCELevel(newLevel)) {
                                        return sendSuccessFeedback(source, "成功设置 " + targetPlayer.getName().getLiteralString() + " 的等级为: " + newLevel);
                                    } else {
                                        return sendErrorFeedback(source, "无法设置等级: 请确保等级在0-4之间且玩家在线");
                                    }
                                })
                        )
                )
                .requires(source -> source.hasPermissionLevel(0))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if (source.getEntity() instanceof ServerPlayerEntity player) {
                        int level = (player instanceof CEPlayerAccessor cePlayerAccessor) ? cePlayerAccessor.getCE().getCELevel() : -1;
                        sendSuccessFeedback(source, "您的CE等级为: " + level);
                        return level; // 返回等级信息
                    }
                    return 0;
                })
        );

        dispatcher.register(ceRoot);
    }
}