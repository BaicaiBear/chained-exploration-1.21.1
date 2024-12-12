package top.bearcabbage.chainedexploration.teamhor;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.network.ServerPlayerEntity;
import top.bearcabbage.chainedexploration.player.CEPlayerManager;

public class CETeamManager {
    private static final Map<ServerPlayerEntity, CETeam> teamList = new HashMap<>();

    public static boolean createOrJoinTeam(ServerPlayerEntity playerJoining, ServerPlayerEntity targetPlayer) {
        if (!teamList.containsKey(targetPlayer)) {
            //目标玩家不在线，直接返回失败
            if (!CEPlayerManager.isPlayerOnline(targetPlayer)) {
                return false;
            }
            // 目标玩家没有队伍，创建新队伍
            CETeam newCETeam = new CETeam(targetPlayer);
            teamList.put(targetPlayer, newCETeam);
            return newCETeam.addMember(playerJoining);
        } else {
            // 目标玩家已有队伍，尝试加入
            CETeam existingCETeam = teamList.get(targetPlayer);
            return existingCETeam.addMember(playerJoining);
        }
    }

    public static boolean removeMemberFromTeam(ServerPlayerEntity playerToRemove, ServerPlayerEntity teamLeader) {
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

    public static boolean quitTeam(ServerPlayerEntity playerName) {
        for (Map.Entry<ServerPlayerEntity, CETeam> entry : teamList.entrySet()) {
            CETeam team = entry.getValue();
            if (team.getMembers().contains(playerName)) {
                // 玩家存在于某个队伍中
                if (team.getLeader().equals(playerName)) {
                    // 如果玩家是队长，则不能直接退出，需要先解散队伍
                    return false; // 或者可以抛出异常，提示队长不能直接退出
                } else {
                    // 玩家是普通成员，可以直接移除
                    return team.removeMember(playerName);
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
            allTeamsInfo.append("队伍名称: ").append(team.getLeader()).append("\n")
                    .append("队长: ").append(team.getLeader()).append("\n")
                    .append("成员: ");
            for (ServerPlayerEntity member : team.getMembers()) {
                if (!team.getLeader().equals(member)) {
                    allTeamsInfo.append(member).append(", ");
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
    // 其他可能需要的方法，如列出所有队伍等...
}