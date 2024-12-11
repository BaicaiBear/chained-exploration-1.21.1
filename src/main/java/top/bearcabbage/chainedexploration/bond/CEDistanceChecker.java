package top.bearcabbage.chainedexploration.bond;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import top.bearcabbage.chainedexploration.player.CEPlayer;

public class CEDistanceChecker {

    private final CEBond bond;

    public CEDistanceChecker(CEBond bond) {
        this.bond = bond;
        registerTickEvent();
    }

    private void registerTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        CEPlayer player1 = bond.getPlayer1();
        CEPlayer player2 = bond.getPlayer2();
        BlockPos bondCenter = bond.getBondCenter();
        double distance1;

        if(player1.getWorld() == player1.getServer().getWorld(World.OVERWORLD)) {
            distance1 = player1.getPos().distanceTo(Vec3d.of(bondCenter));
        } else if(player1.getWorld() == player1.getServer().getWorld(World.NETHER)) {
            distance1 = player2.getPos().multiply(8).distanceTo(Vec3d.of(bondCenter));
        } else if(player1.getWorld() == player1.getServer().getWorld(World.END)) {
            //相互牵制
        }
        double distance2 = player2.getPos().distanceTo(Vec3d.of(bondCenter));

        // Add your logic here to handle the distances
//        System.out.println("Player 1 distance: " + distance1);
//        System.out.println("Player 2 distance: " + distance2);
    }
}