package top.bearcabbage.chainedexploration.player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PlayerSaveHandler;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerManagerAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CEPlayerManager extends PlayerManager {

    private final List<CEPlayer> cePlayers = Lists.newArrayList();
    private final Map<UUID, CEPlayer> cePlayerMap = Maps.newHashMap();


    public CEPlayerManager(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers) {
        super(server, registryManager, saveHandler, maxPlayers);
    }


    public static boolean isPlayerOnline(ServerPlayerEntity targetPlayer) {
        if(targetPlayer.getServer().getPlayerManager() instanceof CEPlayerManagerAccessor playerManagerAccessor) {
            return playerManagerAccessor.uuid2Player(targetPlayer.getUuidAsString()) != null;
        }
        return false;
    }
}
