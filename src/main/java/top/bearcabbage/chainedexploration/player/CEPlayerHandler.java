package top.bearcabbage.chainedexploration.player;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PlayerSaveHandler;
import top.bearcabbage.chainedexploration.area.CEArea;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.team.CETeam;

import java.util.HashSet;

public abstract class CEPlayerHandler extends PlayerManager {


    public CEPlayerHandler(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers) {
        super(server, registryManager, saveHandler, maxPlayers);
    }

    // 处理玩家每个tick的事件
    public static void onTick(ServerPlayerEntity player) {
        if (player instanceof CEPlayerAccessor cePlayer &&
                cePlayer.getCE().getCELevel() != CELevel.LEVELS.getLast() &&
                cePlayer.getCE().onTick()){ //排除掉满级玩家
            if (!checkSafety(player)){
                cePlayer.getCE().onUnsafeTick();//不安全的状态
            } else if (cePlayer.getCE().getUnsafeTick() > 0){
                cePlayer.getCE().onSafeTick();//刚刚回到安全区的状态
            }
        }
    }

    //检查安全状态
    private static boolean checkSafety(ServerPlayerEntity player){
        CEPlayerAccessor ceplayer = (CEPlayerAccessor) player;
        //检查顺序：组队>个人>永久>公共>保护
        if (ceplayer.getCE().isTeamed() && ceplayer.getCE().getTeam().wasSetOut()) {
            if (ceplayer.getCE().getTeam().getTeamArea().inside(player.getServerWorld().getRegistryKey(), player.getPos()))
                return true;
        }
        else if (ceplayer.getCE().getSelfArea().inside(player.getServerWorld().getRegistryKey(),player.getPos())) return true;
        else if (CEArea.PERMANET_AREA.computeIfAbsent(player.getServerWorld().getRegistryKey(), k -> new HashSet<>()).stream().anyMatch(area -> area.inside(player.getServerWorld().getRegistryKey(), player.getPos()))) return true;
        else if (CEArea.PUBLIC_AREA.computeIfAbsent(player.getServerWorld().getRegistryKey(), k -> new HashSet<>()).stream().anyMatch(area -> area.inside(player.getServerWorld().getRegistryKey(), player.getPos()))) return true;
        else if (CEArea.PROTECTED_AREA.computeIfAbsent(player.getServerWorld().getRegistryKey(), k -> new HashSet<>()).stream().anyMatch(area -> area.inside(player.getServerWorld().getRegistryKey(), player.getPos()))) return true;
        return false;
    }
}


