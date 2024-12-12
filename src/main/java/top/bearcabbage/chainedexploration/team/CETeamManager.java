package top.bearcabbage.chainedexploration.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import net.minecraft.server.network.ServerPlayerEntity;

public class CETeamManager {
    private static final Map<ServerPlayerEntity, CETeam> teamList = new HashMap<>();//创建一个teamList

    public record TeamInvite(ServerPlayerEntity sender, ServerPlayerEntity recipient) {
    }
    // 添加一个邀请列表，用来存储未处理的邀请
    private static final Map<UUID, TeamInvite> pendingInvitations = new HashMap<>();

    // 发送邀请
    public static boolean sendInvitation(ServerPlayerEntity sender, ServerPlayerEntity recipient) {
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
            // 处理接受逻辑，例如加入队伍
            if (createOrJoinTeam(player, invitation.sender())) {
                pendingInvitations.remove(player.getUuid());
                return true;
            }
        }
        return false;
    }

    // 拒绝邀请
    public static boolean denyInvitation(ServerPlayerEntity player) {
        return pendingInvitations.remove(player.getUuid()) != null;
    }

    public static boolean createOrJoinTeam(ServerPlayerEntity playerJoining, ServerPlayerEntity targetPlayer) {
        if (!teamList.containsKey(targetPlayer)) {
            // 检查playerJoining是否已在队伍中
            if (!(playerJoining instanceof CEPlayerAccessor cePlayerAccessor) || cePlayerAccessor.getCE().isTeamed()) {
                // 如果playerJoining已在队伍中，则返回失败
                return false;
            }

            CETeam newCETeam = new CETeam(targetPlayer);
            teamList.put(targetPlayer, newCETeam);
            cePlayerAccessor.getCE().joinTeam(newCETeam);
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
            return CETeam.removeMember(playerToRemove);
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
        for (Map.Entry<ServerPlayerEntity, CETeam> entry : teamList.entrySet()) {
            CETeam team = entry.getValue();
            if (team.getMembers().contains(player)) {
                // 玩家存在于某个队伍中
                if (team.getLeader().equals(player)) {
                    // 如果玩家是队长，则解散队伍
                    CETeamManager.disbandTeam(player);
                } else {
                    // 玩家是普通成员，可以直接移除

                    return team.removeMember(player);
                }
            }
        }
        // 玩家不在任何队伍中
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
    // 其他方法
}