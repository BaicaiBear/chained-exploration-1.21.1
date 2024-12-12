package top.bearcabbage.chainedexploration.teamhor;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;

public class CETeam {
    private ServerPlayerEntity leader;
    private Set<ServerPlayerEntity> members;

    public CETeam(ServerPlayerEntity leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        members.add(leader);
    }

    public boolean addMember(ServerPlayerEntity player) {
        if (members.add(player)) {
            if (player instanceof CEPlayerAccessor cePlayerAccessor) {
                cePlayerAccessor.getCE().joinTeam(this); // 更新玩家的isTeamed状态
            }
            return true;
        }
        return false; // 玩家已经在这个队伍中或者加入失败
    }

    public boolean removeMember(ServerPlayerEntity player) {
        if (!members.remove(player)) {
            return false; // 玩家不在队伍中
        }
        if (player instanceof CEPlayerAccessor cePlayerAccessor) {
            cePlayerAccessor.getCE().quitTeam(); // 确保玩家离开队伍时更新isTeamed状态
        }
        return true;
    }

    // 解散队伍，仅队长可以执行此操作
    public void disbandTeam() {
        if (members.isEmpty()) {
            return; // 队伍已为空，无需操作
        }
        for (ServerPlayerEntity member : new HashSet<>(members)) { // 使用副本遍历以避免修改集合时的并发修改异常
            if (member instanceof CEPlayerAccessor cePlayerAccessor) {
                cePlayerAccessor.getCE().quitTeam();
            }
        }
        members.clear();
    }

    public ServerPlayerEntity getLeader() {
        return leader;
    }

    public Set<ServerPlayerEntity> getMembers() {
        return members;
    }
}