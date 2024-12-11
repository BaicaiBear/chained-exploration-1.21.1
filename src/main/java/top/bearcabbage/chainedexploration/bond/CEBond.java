package top.bearcabbage.chainedexploration.bond;

/*
    这个类是CE中两个玩家绑定之后描述这个绑定小队的类
 */

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import top.bearcabbage.chainedexploration.player.CEPlayer;
import top.bearcabbage.chainedexploration.ChainedExploration;

public class CEBond extends ChainedExploration {

    private CEPlayer player1;
    private CEPlayer player2;
    private BlockPos bondCenter;
    private int bondRadius;

    public CEBond(CEPlayer player1, CEPlayer player2, int bondRadius) {
        this.player1 = player1;
        this.player2 = player2;
        this.bondCenter = bondCenter;
        this.bondRadius = player1.getRadius() + player2.getRadius();
    }

    public void create() {
        player1.joinBond(this);
        player2.joinBond(this);
        new CEDistanceChecker(this);
    }

    public BlockPos getBondCenter() {
        return bondCenter;
    }

    public int getBondRadius() {
        return bondRadius;
    }

    public CEPlayer getPlayer1() {
        return player1;
    }

    public CEPlayer getPlayer2() {
        return player2;
    }
}
