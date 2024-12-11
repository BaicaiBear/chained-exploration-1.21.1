package top.bearcabbage.chainedexploration.teamhor;

import java.util.HashSet;
import java.util.Set;

public class CETeam {
    private String leader;
    private Set<String> members;

    public CETeam(String leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        members.add(leader);
    }

    public boolean addMember(String playerName) {
        return members.add(playerName);
    }

    public boolean removeMember(String playerName) {
        return members.remove(playerName);
    }

    // 解散队伍，仅队长可以执行此操作
    public boolean disbandTeam() {
        if (members.contains(leader)) {
            members.clear();
            return true;
        }
        return false;
    }

    public String getLeader() {
        return leader;
    }

    public Set<String> getMembers() {
        return members;
    }
}