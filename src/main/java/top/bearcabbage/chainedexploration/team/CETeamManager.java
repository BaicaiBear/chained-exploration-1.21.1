package top.bearcabbage.chainedexploration.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import net.minecraft.server.network.ServerPlayerEntity;

//满足CECommands的调用 进行权限判断和聊天框报错反馈
public class CETeamManager {
    private static final Map<ServerPlayerEntity, CETeam> teamList = new HashMap<>();//创建一个teamList

    public record TeamInvite(ServerPlayerEntity sender, ServerPlayerEntity recipient) {
    }
    // 添加一个邀请列表，用来存储未处理的邀请
    public static final Map<UUID, TeamInvite> pendingInvitations = new HashMap<>();

    // 发送邀请
    public static boolean sendInvitation(ServerPlayerEntity sender, ServerPlayerEntity recipient) {
        // 确保被邀请玩家不在任何队伍中
        if (((CEPlayerAccessor) recipient).getCE().isTeamed()) {
            recipient.sendMessage(Text.literal("您已经在队伍中，不能接受新的队伍邀请！"), true);
            return false;
        }
        if (pendingInvitations.values().stream().noneMatch(invite -> invite.recipient().equals(recipient))) {
            pendingInvitations.put(recipient.getUuid(), new TeamInvite(sender, recipient));
            recipient.sendMessage(Text.literal(sender.getName().getLiteralString() + " 邀请您加入队伍！"), true);// 发送邀请消息给recipient
            return true;
        }
        return false; // 如果已经有未处理的邀请，则不再发送新的邀请
    }

    // 接受邀请
    public static boolean acceptInvitation(ServerPlayerEntity player) {
        TeamInvite invitation = pendingInvitations.get(player.getUuid());
        if (invitation != null) {
            // 检查是否有多个邀请
            long multipleInvitationsCount = pendingInvitations.values().stream()
                    .filter(inv -> inv.recipient().equals(player))
                    .count();
            if (multipleInvitationsCount > 1) {
                return false; // 存在多个邀请，返回 false 表示此情况
            }
            // 确保被邀请玩家没有已经在队伍中
            if (((CEPlayerAccessor) player).getCE().isTeamed()) {
                player.sendMessage(Text.of("您已经在队伍中，不能加入其他队伍！"), true);
                return false;
            }
            // 处理接受逻辑，例如加入队伍
            if (createOrJoinTeam(player, invitation.sender())) {
                pendingInvitations.remove(player.getUuid());
                return true;
            }
        }
        return false;
    }

    // 用于接受指定玩家的邀请
    public static boolean acceptInvitation(ServerPlayerEntity player, ServerPlayerEntity sender) {
        // 确保邀请存在并且来自指定的玩家
        TeamInvite invitation = pendingInvitations.values().stream()
                .filter(inv -> inv.sender().equals(sender) && inv.recipient().equals(player))
                .findFirst()
                .orElse(null);

        if (invitation != null) {
            // 处理接受逻辑
            if (createOrJoinTeam(player, invitation.sender())) {
                pendingInvitations.remove(player.getUuid());
                return true;
            }
        }
        return false; // 邀请不存在或处理失败
    }

    // 拒绝邀请
    public static boolean denyInvitation(ServerPlayerEntity player) {
        return pendingInvitations.remove(player.getUuid()) != null;
    }

    public static boolean createOrJoinTeam(ServerPlayerEntity playerJoining, ServerPlayerEntity targetPlayer) {
        if (!teamList.containsKey(targetPlayer)) {
            // 使用 CEPlayerAccessor 直接获取 isTeamed 状态
            CEPlayerAccessor cePlayerAccessorJoining = (CEPlayerAccessor) playerJoining;
            if (cePlayerAccessorJoining.getCE().isTeamed()) {
                // 如果playerJoining已在队伍中，则返回失败
                return false;
            }
            CETeam newCETeam = new CETeam(targetPlayer);
            teamList.put(targetPlayer, newCETeam);
            cePlayerAccessorJoining.getCE().joinTeam(newCETeam);
            ((CEPlayerAccessor) targetPlayer).getCE().joinTeam(newCETeam);
            return newCETeam.addMember(playerJoining);
        } else {
            // 目标玩家已有队伍，尝试加入
            CETeam existingCETeam = teamList.get(targetPlayer);
            return existingCETeam.addMember(playerJoining);
        }
    }

    public static boolean removeMember(ServerPlayerEntity playerToRemove, ServerPlayerEntity teamLeader) {
        if (teamList.containsKey(teamLeader)) {
            CETeam CETeam = teamList.get(teamLeader);
            if (CETeam.getMembers().size() == 2) {
                // 如果移除玩家后队伍将只剩队长一人，则解散队伍
                CETeamManager.disbandTeam(teamLeader);
                return true;
            } else {
                // 否则正常尝试移除成员
                return CETeam.removeMember(playerToRemove);
            }
        }
        return false;
    }

    public static boolean disbandTeam(ServerPlayerEntity teamLeader) {
        if (!teamList.containsKey(teamLeader)) {
            return false;
        }
        CETeam CETeam = teamList.get(teamLeader);
        if (!CETeam.getLeader().equals(teamLeader)) {
            return false; // 尝试解散的人不是队长
        }
        CETeam.disbandTeam();
        teamList.remove(teamLeader);
        return true;
    }

    public static boolean LeaveTeam(ServerPlayerEntity player) {
        CEPlayerAccessor cePlayerAccessor = (CEPlayerAccessor) player;
        if (!cePlayerAccessor.getCE().isTeamed()) {
            player.sendMessage(Text.literal("您当前没有加入任何队伍！"), true);
            return false;
        }// 玩家不在任何队伍中
        for (Map.Entry<ServerPlayerEntity, CETeam> entry : teamList.entrySet()) {
            CETeam team = entry.getValue();
            if (team.getMembers().contains(player)) {
                // 玩家存在于某个队伍中
                if (team.getMembers().size() == 2) {
                    // 如果队伍只剩下队长（即当前玩家）且没有其他成员，直接解散队伍
                    CETeamManager.disbandTeam(team.getLeader());
                    return true;
                } else {
                    // 玩家是普通成员，可以直接移除
                    return team.removeMember(player);
                }
            }
        }
        player.sendMessage(Text.literal("错误：您不在已知的队伍列表中，但标记却表示您已组队。"), true);
        return false;
    }
    public static String listAllTeams() {
        StringBuilder allTeamsInfo = new StringBuilder("当前所有队伍列表:\n");
        for (Map.Entry<ServerPlayerEntity, CETeam> entry : teamList.entrySet()) {
            CETeam team = entry.getValue();
            allTeamsInfo.append("队伍名称: ").append(team.getLeader().getName().getLiteralString()).append("\n")
                    .append("队长: ").append(team.getLeader().getName().getLiteralString()).append("\n")
                    .append("成员: ");
            for (ServerPlayerEntity member : team.getMembers()) {
                if (!team.getLeader().equals(member)) {
                    allTeamsInfo.append(member.getName().getLiteralString()).append(", ");
                }
            }
            if (team.getMembers().size() > 1) {
                // 移除最后一个逗号和空格
                allTeamsInfo.setLength(allTeamsInfo.length() - 2);
            }
            allTeamsInfo.append("\n\n");
        }
        return allTeamsInfo.toString().trim(); // 去除最后的换行符
    }

    public static String listMyTeam(ServerPlayerEntity player) {
        // 初始化返回信息
        StringBuilder myTeamInfo = new StringBuilder();
        CEPlayerAccessor cePlayer = (CEPlayerAccessor) player;
        // 使用CEPlayer的getTeam方法直接获取玩家所在的队伍
        CETeam team = cePlayer.getCE().getTeam();
        // 检查玩家是否有队伍
        if (team != null) {
            // 构建队伍信息
            myTeamInfo.append("队伍名称: ").append(team.getLeader().getName().getLiteralString()).append("\n")
                    .append("队长: ").append(team.getLeader().getName().getLiteralString()).append("\n")
                    .append("成员: ");

            // 添加队员信息
            for (ServerPlayerEntity member : team.getMembers()) {
                if (!team.getLeader().equals(member)) {
                    myTeamInfo.append(member.getName().getLiteralString()).append(", ");
                }
            }

            // 移除最后一个逗号和空格
            if (team.getMembers().size() > 1) {
                myTeamInfo.setLength(myTeamInfo.length() - 2);
            }
        } else {
            // 如果玩家没有队伍，返回提示信息
            return "您当前没有加入任何队伍。";
        }

        return myTeamInfo.toString();
    }
    // 其他方法
}