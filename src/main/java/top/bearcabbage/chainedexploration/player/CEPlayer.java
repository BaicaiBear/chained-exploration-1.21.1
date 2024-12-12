package top.bearcabbage.chainedexploration.player;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import top.bearcabbage.chainedexploration.ChainedExploration;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.teamhor.CETeam;
import top.bearcabbage.chainedexploration.utils.CEConfig;
import top.bearcabbage.chainedexploration.utils.CEVec3d;

import java.nio.file.Path;

public class CEPlayer {

    private ServerPlayerEntity player;
    private BlockPos rtpSpawn;
    private int level;
    private boolean isTeamed;
    private CETeam team;

    public CEPlayer(ServerPlayerEntity player) {
        this.player = player;
        NbtCompound data = PlayerDataApi.getCustomDataFor(player, ChainedExploration.CEData);
        if(data == null){
            level = 0;
            isTeamed = false;
            data = new NbtCompound();
            data.putInt("level", 0);
            data.putIntArray("rtpspawn", new int[]{-1});
            PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
        }

        int[] posVec = data.getIntArray("rtpspawn");
        if (posVec.length == 3) {
            this.rtpSpawn = new BlockPos(posVec[0], posVec[1], posVec[2]);
        }
    }

    public void onTick() {
        if(!this.isTeamed){

        }
    }

    public int getCELevel() {
        return this.level;
    }

    public  boolean setCELevel(int level) {
        if(level<0||level>4) return false;
        this.level = level;
        NbtCompound data = new NbtCompound();
        data.putInt("level", level);
        PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
        return true;
    }

    public void levelUP() {
        if (this.level < CELevel.LEVELS.size() - 1) {
            this.setCELevel(this.level+1);
        }
    }

    public BlockPos getRtpSpawn() {
        if(this.rtpSpawn == null) {
            return null;
        }
        return this.rtpSpawn;
    }

    public boolean setRtpSpawn(BlockPos pos) {
        if(rtpSpawn != null){
            return false;
        }
        this.rtpSpawn = pos;
        NbtCompound data = new NbtCompound();
        data.putIntArray("rtpspawn", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
        return true;
    }

    public boolean isTeamed() {
        return isTeamed;
    }

    public boolean joinTeam(CETeam newTeam) {
        if(this.isTeamed) {
            return false;
        }
        isTeamed = true;
        this.team = newTeam;
        return true;
    }


    public CETeam getTeam(){
        return this.team;
    }


}
