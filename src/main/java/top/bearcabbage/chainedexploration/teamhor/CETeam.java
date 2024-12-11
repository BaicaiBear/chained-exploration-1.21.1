package top.bearcabbage.chainedexploration.teamhor;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class CETeam {
    private ServerPlayerEntity leader;
    private Set<ServerPlayerEntity> members;

    public CETeam(ServerPlayerEntity leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        members.add(leader);
    }

    public boolean addMember(ServerPlayerEntity player) {
        return members.add(player);
    }

    public boolean removeMember(ServerPlayerEntity player) {
        return members.remove(player);
    }

    // 解散队伍，仅队长可以执行此操作
    public boolean disbandTeam() {
        if (members.contains(leader)) {
            members.clear();
            return true;
        }
        return false;
    }

    public ServerPlayerEntity getLeader() {
        return leader;
    }

    public Set<ServerPlayerEntity> getMembers() {
        return members;
    }
}