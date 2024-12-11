package top.bearcabbage.chainedexploration.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import top.bearcabbage.chainedexploration.bond.CEBond;

public class CEPlayer extends ServerPlayerEntity {

    private int CELevel;

    public CEPlayer(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions) {
        super(server, world, profile, clientOptions);
    }


    public int getRadius(){
        return 0;
    }

    public void joinBond(CEBond ceBond) {
    }
}
