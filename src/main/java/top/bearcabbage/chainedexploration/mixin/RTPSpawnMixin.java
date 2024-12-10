package top.bearcabbage.chainedexploration.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SpawnPointCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Iterator;

@Mixin(SpawnPointCommand.class)
public class RTPSpawnMixin {


    @Inject(at = @At("TAIL"), method = "execute")
	private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos, float angle, CallbackInfoReturnable<Integer> cir) {
		RegistryKey<World> registryKey = source.getWorld().getRegistryKey();
		Iterator var5 = targets.iterator();
		while(var5.hasNext()) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)var5.next();
			RespawnPositionMixin.setRtpSpawn() = pos;

		}
	}
}