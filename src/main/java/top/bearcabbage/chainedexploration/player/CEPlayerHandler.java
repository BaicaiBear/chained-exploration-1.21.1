package top.bearcabbage.chainedexploration.player;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PlayerSaveHandler;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.team.CETeam;

public abstract class CEPlayerHandler extends PlayerManager {


    public CEPlayerHandler(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers) {
        super(server, registryManager, saveHandler, maxPlayers);
    }

    // 处理玩家每个tick的事件
    public static void onTick(ServerPlayerEntity player) {
        if (player instanceof CEPlayerAccessor cePlayer &&
                cePlayer.getCE().getCELevel() != CELevel.LEVELS.getLast() &&
                cePlayer.getCE().onTick()){ //排除掉满级玩家
            if (!checkSafety(player, cePlayer.getCE().isTeamed() && cePlayer.getCE().getTeam().wasSetOut())){
                cePlayer.getCE().onUnsafeTick();//不安全的状态
            } else if (cePlayer.getCE().getUnsafeTick() > 0){
                cePlayer.getCE().onSafeTick();//刚刚回到安全区的状态
            }
        }
    }

    //检查安全状态
    private static boolean checkSafety(ServerPlayerEntity player, boolean inTeam){
        return inTeam ? checkSafety(player, ((CEPlayerAccessor)player).getCE().getTeam()) : checkSafety(player);
    }
    //检查非组队探险玩家的安全状态
    private static boolean checkSafety(ServerPlayerEntity player){
        //还没写
        return true;
    }
    //检查组队探险玩家的安全状态
    private static boolean checkSafety(ServerPlayerEntity player, CETeam team){
        //还没写
        return true;
    }
}


