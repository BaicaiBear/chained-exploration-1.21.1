package top.bearcabbage.chainedexploration;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.bearcabbage.chainedexploration.command.ClanCommands;
import top.bearcabbage.chainedexploration.utils.CEConfig;

public class ChainedExploration implements ModInitializer {
	public static final String MOD_ID = "chained-exploration";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		// 使用CommandRegistrationCallback.EVENT注册命令

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment)->ClanCommands.registerCommands(dispatcher)); // 调用静态方法注册命令

		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
	}
}