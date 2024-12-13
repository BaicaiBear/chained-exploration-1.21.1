package top.bearcabbage.chainedexploration.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class CEMath {
    public static double HorizontalDistance(BlockPos pos1, BlockPos pos2){
        return HorizontalDistance(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()), new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ()));
    }

    public static double HorizontalDistance(BlockPos pos1, Vec2f pos2){
        return HorizontalDistance(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()), new Vec3d(pos2.x, pos1.getY(), pos2.y));
    }

    public static double HorizontalDistance(Vec3d pos1, Vec2f pos2){
        return HorizontalDistance(pos1, new Vec3d(pos2.x, pos1.getY(), pos2.y));
    }

    public static double HorizontalDistance(Vec3d pos1, Vec3d pos2){
        return Math.sqrt(Math.pow(pos1.getX()-pos2.getX(), 2) + Math.pow(pos1.getZ()-pos2.getZ(), 2));
    }

}
