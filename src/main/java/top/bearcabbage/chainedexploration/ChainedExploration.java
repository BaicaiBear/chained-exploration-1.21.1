package top.bearcabbage.chainedexploration;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.NbtDataStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bearcabbage.chainedexploration.command.ClanCommands;
import top.bearcabbage.chainedexploration.utils.CEConfig;

public class ChainedExploration implements ModInitializer {
	public static final String MOD_ID = "chained-exploration";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final NbtDataStorage CE_LEVEL = new NbtDataStorage("ce_level");
	public static final NbtDataStorage CE_ISTEAMED = new NbtDataStorage("ce_isteamed");
	@Override
	public void onInitialize() {

		// 获取配置文件
		PlayerDataApi.register(CE_LEVEL);
		PlayerDataApi.register(CE_ISTEAMED);
		// 使用CommandRegistrationCallback.EVENT注册命令
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment)->ClanCommands.registerCommands(dispatcher)); // 调用静态方法注册命令

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
	}
}