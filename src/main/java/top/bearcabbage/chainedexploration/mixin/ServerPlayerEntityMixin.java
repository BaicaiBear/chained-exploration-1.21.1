package top.bearcabbage.chainedexploration.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.player.CEPlayer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements CEPlayerAccessor {

    @Unique
    private CEPlayer CE;

    @Shadow public abstract ServerWorld getServerWorld();


    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        CE = new CEPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public CEPlayer getCE() {
        return CE;
    }

    //RTP出生点保存
    @Inject(method = "setSpawnPoint", at = @At(value = "HEAD"), cancellable = true)
    private void setSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {

    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        BlockPos spawnPos =  this.CE.getRtpSpawn();
        if(spawnPos == null){
            spawnPos = this.getServerWorld().getSpawnPos();
        }
        cir.setReturnValue(new TeleportTarget(getServerWorld(),spawnPos.toCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
    }

    //CEPlayer日常任务
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {

    }
}