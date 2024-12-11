package top.bearcabbage.chainedexploration.rtpspawn;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import top.bearcabbage.chainedexploration.ChainedExploration;
import top.bearcabbage.chainedexploration.utils.CEConfig;

import java.nio.file.Path;
import java.util.Map;
import java.util.Vector;

public class CERtpSpawn {
    private BlockPos rtpSpawn;
    private ServerPlayerEntity player;
    private Map<String, Vector<Integer>> fullList;

    public CERtpSpawn(ServerPlayerEntity player) {
        this.player = player;
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("rtpSpawn/rtpspawn.json");
        CEConfig config = new CEConfig(configPath);
        this.fullList =config.getMap("rtpspawn");
        if(this.fullList.containsKey(player.getName().toString())) {
            Vector<Integer> pos = this.fullList.get(player.getName().toString());
            this.rtpSpawn = new BlockPos(pos.get(0), pos.get(1), pos.get(2));
        } else {
            this.rtpSpawn = player.getBlockPos();
            this.fullList.put(player.getName().toString(), new Vector<Integer>(3));
            config.set("rtpspawn", this.fullList);
            config.save();
        }
    }

    public BlockPos getRtpSpawn() {
        return this.rtpSpawn;
    }
}
