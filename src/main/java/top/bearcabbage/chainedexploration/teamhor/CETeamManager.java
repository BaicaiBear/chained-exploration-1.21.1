package top.bearcabbage.chainedexploration.teamhor;

import java.util.HashMap;
import java.util.Map;
import top.bearcabbage.chainedexploration.player.CEPlayerManager;

public class CETeamManager {
    private static final Map<String, CETeam> teamList = new HashMap<>();

    public static boolean createOrJoinTeam(String playerJoining, String targetPlayer) {
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

    public static boolean removeMemberFromTeam(String playerNameToRemove, String teamLeader) {
        if (teamList.containsKey(teamLeader)) {
            CETeam CETeam = teamList.get(teamLeader);
            return CETeam.removeMember(playerNameToRemove);
        }
        return false;
    }

    public static boolean disbandTeam(String teamLeader) {
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

    // 其他可能需要的方法，如列出所有队伍等...
}