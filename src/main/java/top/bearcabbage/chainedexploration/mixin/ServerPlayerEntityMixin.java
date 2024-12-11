package top.bearcabbage.chainedexploration.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bearcabbage.chainedexploration.player.CEPlayer;
import top.bearcabbage.chainedexploration.rtpspawn.CERtpSpawn;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    //RTP出生点保存
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        BlockPos spawnPos =  new CERtpSpawn(this.getName()).getRtpSpawn();
        if(spawnPos == null){
            spawnPos = this.getServerWorld().getSpawnPos();
        }
        cir.setReturnValue(new TeleportTarget(getServerWorld(),spawnPos.toCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
    }

    //CEPlayer日常任务
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        CEPlayer.onTick((ServerPlayerEntity) (Object) this);
    }
}