package top.bearcabbage.chainedexploration.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.bearcabbage.chainedexploration.interfaces.CEPlayerAccessor;
import top.bearcabbage.chainedexploration.player.CEPlayer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements CEPlayerAccessor {

    public ServerPlayerEntityMixin(ServerWorld world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Shadow public abstract ServerWorld getServerWorld();

    @Unique
    private CEPlayer CE;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        CE = new CEPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public CEPlayer getCE() {
        return CE;
    }

    //RTP出生点保存
//    @Inject(method = "setSpawnPoint", at = @At(value = "HEAD"), cancellable = true)
//    public void setSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
//
//    }
    @Shadow public abstract void setSpawnPoint(RegistryKey<World> dimension, BlockPos pos, float angle, boolean forced, boolean sendMessage);
    @Shadow private boolean isBedWithinRange(BlockPos pos, Direction direction){
        return true;
    }
    @Shadow private boolean isBedObstructed(BlockPos pos, Direction direction){
        return true;
    };

    @Inject(method = "trySleep", at = @At("HEAD"),cancellable = true)
    private void trySleep(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir){
        Direction direction = (Direction)this.getWorld().getBlockState(pos).get(HorizontalFacingBlock.FACING);
        if (!this.isSleeping() && this.isAlive()) {
            if (!this.getWorld().getDimension().natural()) {
                cir.setReturnValue(Either.left(SleepFailureReason.NOT_POSSIBLE_HERE));
            } else if (!this.isBedWithinRange(pos, direction)) {
                cir.setReturnValue(Either.left(SleepFailureReason.TOO_FAR_AWAY));
            } else if (this.isBedObstructed(pos, direction)) {
                cir.setReturnValue(Either.left(SleepFailureReason.OBSTRUCTED));
            } else {
                CEPlayerAccessor player = (CEPlayerAccessor) this;
                player.getCE().setSpawnPoint(this.getWorld().getRegistryKey(), pos, this.getYaw());
                this.setSpawnPoint(this.getWorld().getRegistryKey(), pos, this.getYaw(), false, false);
            }
        }
    }
//
//    @Inject(method = "getRespawnTarget", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
//    private void getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition, CallbackInfoReturnable<TeleportTarget> cir) {
//        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//        BlockPos spawnPos =  this.CE.getRtpSpawn();
//        if(spawnPos == null){
//            spawnPos = this.getServerWorld().getServer().getOverworld().getSpawnPos();
//        }
//        cir.setReturnValue(new TeleportTarget(this.getServerWorld().getServer().getOverworld(),spawnPos.toCenterPos(), Vec3d.ZERO, 0.0F, 0.0F, postDimensionTransition));
//    }


    @Shadow public final MinecraftServer server = super.getServer();
    @Shadow @Nullable public abstract BlockPos getSpawnPointPosition();
    @Shadow public abstract float getSpawnAngle();
    @Shadow public abstract boolean isSpawnForced();
    @Shadow public abstract RegistryKey<World> getSpawnPointDimension();
    @Shadow @Final private static Logger LOGGER;
    @Shadow public abstract void sendMessage(Text message);
    @Shadow @Final private ServerStatHandler statHandler;

    /**
     * @BaicaiBear
     * 和Mixin爆了！
     * @author
     */
    @Overwrite
    public TeleportTarget getRespawnTarget(boolean alive, TeleportTarget.PostDimensionTransition postDimensionTransition) {
        CEPlayerAccessor player = (CEPlayerAccessor) this;
        BlockPos blockPos = player.getCE().getSpawnPoint();
        float f = player.getCE().getSpawnAngle();
        RegistryKey<World> respawnWorld = player.getCE().getSpawnWorld();
        ServerWorld world = super.getServer().getWorld(respawnWorld);
        if(world == null){
            world = super.getServer().getOverworld();
            LOGGER.warn("找不到世界" + respawnWorld.getValue());
        }
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock && (Integer)blockState.get(RespawnAnchorBlock.CHARGES) > 0 && RespawnAnchorBlock.isNether(world)) {
            return new TeleportTarget(world, new Vec3d(blockPos.getX()+0.5,blockPos.getY()+0.5,blockPos.getZ()+0.5), Vec3d.ZERO, f, 0.0F, postDimensionTransition);
        } else if (block instanceof BedBlock && BedBlock.isBedWorking(world)) {
            world.setBlockState(blockPos, (BlockState)blockState.with(RespawnAnchorBlock.CHARGES, (Integer)blockState.get(RespawnAnchorBlock.CHARGES) - 1), 3);
            return new TeleportTarget(world, new Vec3d(blockPos.getX()+0.5,blockPos.getY()+0.5,blockPos.getZ()+0.5), Vec3d.ZERO, f, 0.0F, postDimensionTransition);
        } else {
            this.sendMessage(Text.of("您的床/重生锚已被破坏！"));
            this.sendMessage(Text.of("[CE]您的探索范围中心已重置为["+String.valueOf(player.getCE().getRtpSpawn().getX())+","+String.valueOf(player.getCE().getRtpSpawn().getY())+","+String.valueOf(player.getCE().getRtpSpawn().getZ())+"]"));
            return new TeleportTarget(super.getServer().getOverworld(), new Vec3d(player.getCE().getRtpSpawn().getX()+0.5,player.getCE().getRtpSpawn().getY()+0.5,player.getCE().getRtpSpawn().getZ()+0.5),Vec3d.ZERO,f,0.0F, postDimensionTransition);
        }
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeath(CallbackInfo ci) {
        CEPlayerAccessor player = (CEPlayerAccessor) this;
        player.getCE().onDeath();
    }

    //CEPlayer日常任务
    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {

    }
}