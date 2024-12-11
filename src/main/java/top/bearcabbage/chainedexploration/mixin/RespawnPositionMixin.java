package top.bearcabbage.chainedexploration.mixin;

import com.jcraft.jorbis.Block;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bearcabbage.chainedexploration.rtpspawn.CERtpSpawn;

@Mixin(ServerPlayerEntity.class)
public abstract class RespawnPositionMixin extends PlayerEntity {

    public RespawnPositionMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();


    @Shadow @Nullable public abstract Text getPlayerListName();

    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
        BlockPos spawnPos =  new CERtpSpawn(this.getName()).getRtpSpawn();
        if(spawnPos == null){
            spawnPos = this.getServerWorld().getSpawnPos();
        }
        cir.setReturnValue(new TeleportTarget(getServerWorld(),spawnPos.toCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
    }
}