package top.bearcabbage.chainedexploration;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.NbtDataStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bearcabbage.chainedexploration.command.CECommands;


public class ChainedExploration implements ModInitializer {
	public static final String MOD_ID = "chained-exploration";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final NbtDataStorage CEData = new NbtDataStorage("CE_Data");
	@Override
	public void onInitialize() {
		// 获取配置文件
		PlayerDataApi.register(CEData);
		// 使用CommandRegistrationCallback.EVENT注册命令
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment)->CECommands.registerCommands(dispatcher)); // 调用静态方法注册命令


		UseBlockCallback.EVENT.register((player, world, hand, hitResult)->{
			// 拦截玩家点击床/重生锚的事件
			if(world.isClient){
				return ActionResult.PASS;
			}
			BlockState blockState = world.getBlockState(hitResult.getBlockPos());
			if(blockState.getBlock() instanceof RespawnAnchorBlock && (Integer)blockState.get(RespawnAnchorBlock.CHARGES) >0 && ((!player.getMainHandStack().isOf(Items.GLOWSTONE))||(blockState.get(RespawnAnchorBlock.CHARGES)==4))){
				LOGGER.info("玩家" + player.getName().getLiteralString() + "右键了重生锚");
				return ActionResult.FAIL;
			}
			if(blockState.getBlock() instanceof BedBlock && BedBlock.isBedWorking(world) && !blockState.get(BedBlock.OCCUPIED)){
				LOGGER.info("玩家" + player.getName().getLiteralString() + "右键了床");
				player.trySleep(hitResult.getBlockPos());
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

	}
}