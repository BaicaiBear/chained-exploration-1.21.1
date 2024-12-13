package top.bearcabbage.chainedexploration.player;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import top.bearcabbage.chainedexploration.ChainedExploration;
import top.bearcabbage.chainedexploration.area.CEArea;
import top.bearcabbage.chainedexploration.team.CETeam;

import java.util.HashSet;
import java.util.Set;

import static top.bearcabbage.chainedexploration.utils.CEMath.HorizontalDistance;

public class CEPlayer {

    private ServerPlayerEntity player;
    private BlockPos rtpSpawn;
    private BlockPos spawnPoint;
    private RegistryKey<World> spawnWorld;
    private int level;
    private boolean isTeamed;
    private CETeam team;
    private CEArea selfArea;
    private Set<CEArea> protectedAreas = new HashSet<>();

    private static final int TICK_INTERVAL = 20;
    private static final int GRACE_TICK = 100;
    private static final int DAMAGE_INTERVAL = 10;
    private static final float DAMAGE = 2.0F;
    private int CETick;
    private int unsafeTick;
    private int damageTick;

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
        } else {
            this.spawnPoint = player.getServerWorld().getSpawnPos();
        }
        boolean spawnInOverworld = data.getBoolean("spawn-in-overworld");
        this.spawnWorld = spawnInOverworld ? World.OVERWORLD : World.NETHER;
        this.level = data.getInt("level");
        Vec2f pos = new Vec2f(spawnPoint.getX(), spawnPoint.getZ());
        if(spawnWorld==World.NETHER) pos.multiply(8);
        this.selfArea = CEArea.of(World.OVERWORLD, pos, CELevel.RADIUS.get(this.level), 0, false, false, false, false);
        CETick = 0;
        unsafeTick = 0;
    }

    public boolean onTick() {
        if (CETick == 0) {
            return true;
        }
        CETick = (CETick + 1) % TICK_INTERVAL;
        return false;
    }

    public void onUnsafeTick() {
        this.player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 3*TICK_INTERVAL, 0, false, true));
        if(++unsafeTick>GRACE_TICK){
            if(damageTick++%DAMAGE_INTERVAL==0){
                this.player.damage(player.getDamageSources().genericKill(),DAMAGE);
            }
        }
    }

    public void onSafeTick() {
        if(--unsafeTick==0){
            this.player.removeStatusEffect(StatusEffects.BLINDNESS);
            damageTick = 0;
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
        Vec2f pos = new Vec2f(spawnPoint.getX(), spawnPoint.getZ());
        if(spawnWorld==World.NETHER) pos.multiply(8);
        this.selfArea = CEArea.of(World.OVERWORLD, pos, CELevel.RADIUS.get(this.level), 0, false, false, false, false);
        return true;
    }

    public void levelUP() {
        if (this.level < CELevel.LEVELS.size() - 1) {
            this.setCELevel(this.level+1);
        }
    }

    public BlockPos getRtpSpawn() {
        if(this.rtpSpawn == null) {
            return spawnPoint;
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
        Vec2f poss = new Vec2f(spawnPoint.getX(), spawnPoint.getZ());
        if(spawnWorld==World.NETHER) poss.multiply(8);
        this.selfArea = CEArea.of(World.OVERWORLD, poss, CELevel.RADIUS.get(this.level), 0, false, false, false, false);
        return true;
    }

    public boolean setSpawnPoint(RegistryKey<World> worldRegistryKey, BlockPos pos, float Yaw) {
        if(rtpSpawn == null){
            rtpSpawn = player.getServerWorld().getSpawnPos();
        }
        if (worldRegistryKey == World.OVERWORLD) {
            if (HorizontalDistance(rtpSpawn, pos) <= CELevel.RADIUS.get(this.level)) {
                this.spawnWorld = worldRegistryKey;
                this.spawnPoint = pos;
                NbtCompound data = new NbtCompound();
                data.putIntArray("spawnpoint", new int[]{pos.getX(), pos.getY(), pos.getZ()});
                data.putBoolean("spawn-in-overworld", true);
                PlayerDataApi.setCustomDataFor(player, ChainedExploration.CEData, data);
                Vec2f poss = new Vec2f(spawnPoint.getX(), spawnPoint.getZ());
                this.selfArea = CEArea.of(World.OVERWORLD, poss, CELevel.RADIUS.get(this.level), 0, false, false, false, false);
                this.player.sendMessage(Text.of("成功设置重生点！"));
                this.player.sendMessage(Text.of("[CE]您的探索范围中心已更新为[" + String.valueOf(poss.x)  + "," + String.valueOf(poss.y) + "]"));
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
                Vec2f poss = new Vec2f(spawnPoint.getX(), spawnPoint.getZ()).multiply(8);
                this.selfArea = CEArea.of(World.OVERWORLD, poss, CELevel.RADIUS.get(this.level), 0, false, false, false, false);
                this.player.sendMessage(Text.of("成功设置重生点！"));
                this.player.sendMessage(Text.of("[CE]您的探索范围中心已更新为[" + String.valueOf(poss.x) + "," + String.valueOf(poss.y) + "]"));
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

    public boolean joinProtectedArea(CEArea area){
        return this.protectedAreas.add(area);
    }

    public boolean quitProtectedArea(CEArea area){
        return this.protectedAreas.remove(area);
    }

    public Set<CEArea> getProtectedAreas(){
        return this.protectedAreas;
    }

    public CEArea getSelfArea(){
        return this.selfArea;
    }

    public float getSpawnAngle() {
        return player.getYaw();
    }

    public RegistryKey<World> getSpawnPointDimension() {
        return this.spawnWorld;
    }

    public double getRadiusForTeam(){
        if(this.level==CELevel.LEVELS.getLast()){
            return CELevel.RADIUS.get(CELevel.RADIUS.size()-2);
        }
        return CELevel.RADIUS.get(this.level);
    }
    public void onDeath() {
        CETick = damageTick = unsafeTick = 0;
    }

    public int getUnsafeTick() {
        return unsafeTick;
    }


}
