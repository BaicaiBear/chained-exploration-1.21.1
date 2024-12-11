package top.bearcabbage.chainedexploration.team;

import java.util.HashMap;
import java.util.Map;

public class TeamManager {
    private static final Map<String, Team> teamList = new HashMap<>();

    public static boolean createOrJoinTeam(String playerJoining, String targetPlayer) {
        if (!teamList.containsKey(targetPlayer)) {
            // 目标玩家没有队伍，创建新队伍
            Team newTeam = new Team(targetPlayer);
            teamList.put(targetPlayer, newTeam);
            return newTeam.addMember(playerJoining);
        } else {
            // 目标玩家已有队伍，尝试加入
            Team existingTeam = teamList.get(targetPlayer);
            return existingTeam.addMember(playerJoining);
        }
    }

    public static boolean removeMemberFromTeam(String playerNameToRemove, String teamLeader) {
        if (teamList.containsKey(teamLeader)) {
            Team team = teamList.get(teamLeader);
            return team.removeMember(playerNameToRemove);
        }
        return false;
    }

    public static boolean disbandTeam(String teamLeader) {
        if (!teamList.containsKey(teamLeader)) {
            return false;
        }

        Team team = teamList.get(teamLeader);
        if (!team.getLeader().equals(teamLeader)) {
            return false; // 尝试解散的人不是队长
        }

        team.disbandTeam();
        teamList.remove(teamLeader);
        return true;
    }

    // 其他可能需要的方法，如列出所有队伍等...
}