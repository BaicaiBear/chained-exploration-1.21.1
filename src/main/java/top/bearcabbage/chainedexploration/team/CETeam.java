package top.bearcabbage.chainedexploration.team;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.text.Text;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;

public class CETeam {
    private final ServerPlayerEntity leader;
    private final Set<ServerPlayerEntity> members;
    private double radius;
    private boolean wasSetOut;


    public CETeam(ServerPlayerEntity leader) {
        this.wasSetOut = false;
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
            //向队伍所有成员发送玩家加入队伍的消息
            Text joinMessage = Text.of(player.getName().getLiteralString() + " 已经加入队伍！");
            for(ServerPlayerEntity member : members) {
                if (!member.equals(player)) { // 确保不给新加入的玩家自己发送消息
                    member.sendMessage(joinMessage, true);
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
            if (members.size() == 1 && members.contains(getLeader())) {
                // 只剩队长一人，自动解散队伍
                this.disbandTeam();
                return true;
            }
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

    //完成组队，开始探险！
    public void setOut(){
        //玩家RTP到集合点
        //禁止队伍接纳新成员
    }

    public boolean wasSetOut() {
        return wasSetOut;
    }

    public ServerPlayerEntity getLeader() {
        return leader;
    }

    public Set<ServerPlayerEntity> getMembers() {
        return members;
    }

    public double getRadius() {
        return radius;
    }
}