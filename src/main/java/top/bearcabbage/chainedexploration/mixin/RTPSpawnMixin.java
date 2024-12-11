package top.bearcabbage.chainedexploration.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SpawnPointCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bearcabbage.chainedexploration.rtpspawn.CERtpSpawn;

import java.util.Collection;
import java.util.Iterator;


@Mixin(SpawnPointCommand.class)
public class RTPSpawnMixin {



    @Inject(method = "execute", at = @At("TAIL"))
    private static void execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos, float angle, CallbackInfoReturnable<Integer> cir) {
        Iterator<ServerPlayerEntity> iterator = targets.iterator();
        while (iterator.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = iterator.next();
            new CERtpSpawn(serverPlayerEntity.getName()).setRtpSpawn(pos);
        }
    }
}