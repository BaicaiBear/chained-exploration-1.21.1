package top.bearcabbage.chainedexploration.interfaces;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.bearcabbage.chainedexploration.player.CEPlayer;

public interface CEPlayerAccessor {

    public CEPlayer getCE();
}
