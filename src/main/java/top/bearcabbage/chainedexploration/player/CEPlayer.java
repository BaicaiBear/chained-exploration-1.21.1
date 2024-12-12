package top.bearcabbage.chainedexploration.player;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.entity.feature.WolfArmorFeatureRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.bearcabbage.chainedexploration.ChainedExploration;
import top.bearcabbage.chainedexploration.teamhor.CETeam;

import java.nio.file.Path;

import static top.bearcabbage.chainedexploration.utils.CEMath.HorizontalDistance;

public class CEPlayer {

    private ServerPlayerEntity player;
    private BlockPos rtpSpawn;
    private BlockPos spawnPoint;
    private RegistryKey<World> spawnWorld;
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
            data.putIntArray("spawnpoint", new int[]{-1});
            data.putBoolean("spawn-in-overworld", true);
            PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
        }
        int[] posVec = data.getIntArray("rtpspawn");
        if (posVec.length == 3) {
            this.rtpSpawn = new BlockPos(posVec[0], posVec[1], posVec[2]);
        }
        int[] spawnVec = data.getIntArray("spawnpoint");
        if (spawnVec.length == 3) {
            this.spawnPoint = new BlockPos(spawnVec[0], spawnVec[1], spawnVec[2]);
        }
        Boolean spawnInOverworld = data.getBoolean("spawn-in-overworld");
        this.spawnWorld = spawnInOverworld ? World.OVERWORLD : World.NETHER;
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

    public BlockPos getSpawnPoint(){
        if(this.spawnPoint == null){
            return player.getServerWorld().getSpawnPos();
        }
        return this.spawnPoint;
    }

    public RegistryKey<World> getSpawnWorld(){
        if(this.spawnWorld == null){
            return World.OVERWORLD;
        }
        return this.spawnWorld;
    }

    public boolean setRtpSpawn(BlockPos pos) {
        if(rtpSpawn != null){
            return false;
        }
        this.rtpSpawn = pos;
        this.spawnWorld = World.OVERWORLD;
        this.spawnPoint = pos;
        NbtCompound data = new NbtCompound();
        data.putIntArray("rtpspawn", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        data.putIntArray("spawnpoint", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
        return true;
    }

    public boolean setSpawnPoint(RegistryKey<World> worldRegistryKey, BlockPos pos, float Yaw) {
        if (worldRegistryKey == World.OVERWORLD) {
            if (HorizontalDistance(rtpSpawn, pos) <= CELevel.RADIUS.get(this.level)) {
                this.spawnWorld = worldRegistryKey;
                this.spawnPoint = pos;
                NbtCompound data = new NbtCompound();
                data.putIntArray("spawnpoint", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                data.putBoolean("spawn-in-overworld", true);
                PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
                this.player.sendMessage(Text.of("成功设置重生点！"));
                this.player.sendMessage(Text.of("[CE]您的探索范围中心已更新为[" + String.valueOf(pos.getX()) + "," + String.valueOf(pos.getY()) + "," + String.valueOf(pos.getZ()) + "]"));
                return true;
            }
            this.player.sendMessage(Text.of("[CE]重生点设置失败！重生点超出原始探索范围！"));
            return false;
        } else if (worldRegistryKey == World.NETHER) {
            if (HorizontalDistance(rtpSpawn, pos.multiply(8)) <= CELevel.RADIUS.get(this.level)) {
                this.spawnWorld = worldRegistryKey;
                this.spawnPoint = pos;
                NbtCompound data = new NbtCompound();
                data.putIntArray("spawnpoint", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                data.putBoolean("spawn-in-overworld", false);
                PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
                this.player.sendMessage(Text.of("成功设置重生点！"));
                this.player.sendMessage(Text.of("[CE]您的探索范围中心已更新为[" + String.valueOf(8 * pos.getX()) + "," + String.valueOf(8 * pos.getY()) + "," + String.valueOf(8 * pos.getZ()) + "]"));
                return true;
            }
            this.player.sendMessage(Text.of("[CE]重生点设置失败！重生点超出原始探索范围！"));
            return false;
        }
        return false;
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
    public boolean quitTeam() {
        if(!this.isTeamed) {
            return false;
        }
        isTeamed = false;
        this.team = null;
        return true;
    }

    public CETeam getTeam(){
        return this.team;
    }

    public float getSpawnAngle() {
        return player.getYaw();
    }

    public RegistryKey<World> getSpawnPointDimension() {
        return this.spawnWorld;
    }
}
