package top.bearcabbage.chainedexploration.player;

import eu.pb4.playerdata.api.PlayerDataApi;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import top.bearcabbage.chainedexploration.ChainedExploration;

public class CEPlayer {

    public static void onTick(ServerPlayerEntity player) {

    }

    public static int getCELevel(ServerPlayerEntity player) {
        NbtCompound data = PlayerDataApi.getCustomDataFor(player, ChainedExploration.CE_LEVEL);
        return data != null ? data.getInt("ce_level") : 0;
    }

    public static boolean setCELevel(ServerPlayerEntity player, int level) {
        if(level<0||level>4) return false;
        NbtCompound data = new NbtCompound();
        data.putInt("ce_level", level);
        PlayerDataApi.setCustomDataFor(player, ChainedExploration.CE_LEVEL, data);
        return true;
    }

    public void levelUP(ServerPlayerEntity player) {
        int level = getCELevel(player);
        if (level < CELevel.LEVELS.size() - 1) {
            setCELevel(player,level + 1);
        }
    }

    public boolean isTeamed(ServerPlayerEntity player) {
        NbtCompound data = PlayerDataApi.getCustomDataFor(player, ChainedExploration.CE_LEVEL);
        return data != null && data.getBoolean("ce_isteamed");
    }

    public boolean joinTeam(ServerPlayerEntity player) {
        if(!isTeamed(player)) {
            return false;
        }
        NbtCompound data = new NbtCompound();
        data.putBoolean("ce_isteamed", true);
        PlayerDataApi.setCustomDataFor(player, ChainedExploration.CE_LEVEL, data);
        return true;
    }
}
