package top.bearcabbage.chainedexploration.area;

import com.google.common.collect.Maps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static top.bearcabbage.chainedexploration.utils.CEMath.HorizontalDistance;

public class CEArea {
    private final boolean isPermanent;
    private final boolean isCharged;
    private final boolean isTeamArea;
    private boolean isPublic;
    private boolean isProtected;
    private boolean isWorking;

    private RegistryKey<World> world;
    private Vec2f center;
    private Double radius;
    private int rent = 0;
    private Set<ServerPlayerEntity> members = new HashSet<>();

    private int chargeTick = 0;

    public static final int CHARGE_INTERVAL_TICKS = 1200; // 60s
    public static final Map<RegistryKey<World>, Set<CEArea>> PERMANET_AREA = Maps.newHashMap();
    public static final Map<RegistryKey<World>, Set<CEArea>> PUBLIC_AREA = Maps.newHashMap();
    public static final Map<RegistryKey<World>, Set<CEArea>> PROTECTED_AREA = Maps.newHashMap();

    private CEArea(RegistryKey<World> world, Vec2f center, Double radius, int rent, boolean isPermanent, boolean isTeamArea, boolean isPublic, boolean isProtected) {
        this.world = world;
        this.center = center;
        this.radius = radius;
        this.rent = rent;
        this.isPermanent = isPermanent;
        this.isCharged = rent > 0;
        this.isTeamArea = isTeamArea;
        this.isPublic = isPublic;
        this.isProtected = isProtected;
        this.isWorking = true;
    }

    public static CEArea of(RegistryKey<World> world, Vec2f center, Double radius, int rent, boolean isPermanent ,boolean isTeamArea, boolean isPublic, boolean isProtected) {
        if((isPublic && isProtected) || (isTeamArea && isPublic) || (isTeamArea && isProtected) || (isTeamArea && world != World.OVERWORLD))
            return null;
        else {
            if(isPermanent) {
                CEArea ceArea = new CEArea(world, center, radius, 0, true, false, true, false);
                PERMANET_AREA.computeIfAbsent(world, k -> new HashSet<>()).add(ceArea);
                return ceArea;
            }
            CEArea ceArea = new CEArea(world, center, radius, rent, false, isTeamArea, isPublic, isProtected);
            if(isPublic) {
                PUBLIC_AREA.computeIfAbsent(world, k -> new HashSet<>()).add(ceArea);
            } else if(isProtected) {
                PROTECTED_AREA.computeIfAbsent(world, k -> new HashSet<>()).add(ceArea);
            }
            return ceArea;
        }
    }

    public static void remove(CEArea ceArea) {
        if(ceArea.isPublic) {
            PUBLIC_AREA.get(ceArea.world).remove(ceArea);
        } else if(ceArea.isProtected) {
            PROTECTED_AREA.get(ceArea.world).remove(ceArea);
        }
    }

    public boolean addMember(ServerPlayerEntity player) {
        if (!(this.isProtected||this.isTeamArea)){
            return false;
        }
        return members.add(player);
    }

    public boolean removeMember(ServerPlayerEntity player) {
        if (!(this.isProtected||this.isTeamArea)){
            return false;
        }
        return members.remove(player);
    }

    public boolean inside(RegistryKey<World> world,Vec3d pos) {
        Boolean insi;
        if(world == World.OVERWORLD && this.world == World.OVERWORLD) insi = HorizontalDistance(pos, center) <= radius;
        else if(world == World.OVERWORLD && this.world == World.NETHER) insi = HorizontalDistance(pos, center.multiply(8)) <= radius;
        else if(world == World.NETHER && this.world == World.OVERWORLD) insi = HorizontalDistance(pos.multiply(1/8), center) <= radius;
        else return false; //直接返回，交给末地处理机制
        if(insi && isCharged) {
            if(chargeTick==0) this.onCharge();
            chargeTick = (chargeTick + 1) % CHARGE_INTERVAL_TICKS;
        }
        return insi && isWorking;
    }

    //区域租金收费行为，无法缴纳则isWorking=false，恢复缴纳则isWorking=true
    public void onCharge() {

    }
}
