package top.bearcabbage.chainedexploration.interfaces;

import net.minecraft.server.network.ServerPlayerEntity;

public interface CEPlayerManagerAccessor {
    ServerPlayerEntity uuid2Player(String uuid);
}
