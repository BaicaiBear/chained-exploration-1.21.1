package top.bearcabbage.chainedexploration.rtpspawn;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import top.bearcabbage.chainedexploration.ChainedExploration;
import top.bearcabbage.chainedexploration.utils.CEConfig;
import top.bearcabbage.chainedexploration.utils.CEVec3d;

import java.nio.file.Path;
import java.util.Map;
import java.util.Vector;

public class CERtpSpawn {
    private BlockPos rtpSpawn;
    private String player;

    public CERtpSpawn(Text player) {
        this.player = player.toString();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("rtpSpawn/rtpspawn.json");
        CEConfig config = new CEConfig(configPath);
        CEVec3d posVec = config.getCEVec3d(this.player);
        if (posVec != null) {
            this.rtpSpawn = new BlockPos(posVec.x, posVec.y, posVec.z);
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
        CEVec3d newPos = new CEVec3d();
        newPos.x = pos.getX();
        newPos.y = pos.getY();
        newPos.z = pos.getZ();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("rtpSpawn/rtpspawn.json");
        CEConfig config = new CEConfig(configPath);
        config.set(this.player, newPos);
        config.save();
        return true;
    }
}
