package top.bearcabbage.chainedexploration.mixin;

import com.google.common.collect.Maps;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerManagerAccessor;

import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManagerMixin.class)
public class PlayerManagerMixin implements CEPlayerManagerAccessor {
    @Shadow private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();

    @Override
    public ServerPlayerEntity uuid2Player(String uuid) {
        UUID uUID = UUID.fromString(uuid);
        return (ServerPlayerEntity) this.playerMap.get(uUID);
    }

}
