package top.bearcabbage.chainedexploration.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class RespawnPositionMixin  {

    @Shadow public abstract ServerWorld getServerWorld();

    @Unique
    private BlockPos rtpSpawn;

    @Inject(method="<init>", at=@At("RETURN"))
    private void init(CallbackInfo ci) {
        this.rtpSpawn = new BlockPos(0,100,0);
        // 默认rtpSpawn设置为世界生成点
        //this.rtpSpawn = getWorldSpawnPos(getServerWorld(), getServerWorld().getSpawnPos());
    }


    @Unique
    public BlockPos setRtpSpawn(BlockPos pos) {
        this.rtpSpawn = pos;
        return this.rtpSpawn;
    }

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        System.out.println("你的床没了QWQ!");
        cir.setReturnValue(new TeleportTarget(getServerWorld(), this.rtpSpawn.toCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
    }
}