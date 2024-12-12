package top.bearcabbage.chainedexploration.teamhor;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;

public class CETeam {
    private ServerPlayerEntity leader;
    private Set<ServerPlayerEntity> members;
    private double radius;


    public CETeam(ServerPlayerEntity leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        members.add(leader);
        CEPlayerAccessor celeader = (CEPlayerAccessor) leader;
        radius = celeader.getCE().getRadiusForTeam()/10;
    }

    public boolean addMember(ServerPlayerEntity player) {
        if (members.add(player)) {
            if (player instanceof CEPlayerAccessor cePlayerAccessor) {
                cePlayerAccessor.getCE().joinTeam(this); // 更新玩家的isTeamed状态
                this.radius += cePlayerAccessor.getCE().getRadiusForTeam()/10/this.members.size();
                for(ServerPlayerEntity member : members){
                    member.sendMessage(Text.of(player.getName().getLiteralString()+"加入了队伍，现在队伍的半径变为"+String.valueOf(this.radius)));
                }
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
            this.radius = this.radius*(this.members.size()+1)/(this.members.size()) - cePlayerAccessor.getCE().getRadiusForTeam()/10/this.members.size();
            for(ServerPlayerEntity member : members){
                member.sendMessage(Text.of(player.getName().getLiteralString()+"离开了队伍，现在队伍的半径变为"+String.valueOf(this.radius)));
            }
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