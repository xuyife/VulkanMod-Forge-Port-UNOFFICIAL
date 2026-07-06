package net.vulkanmod;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.video.VideoModeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

import static net.xuyifei.VulkanModForge.MODID;

public class Initializer {
	public static final Logger LOGGER = LogManager.getLogger("VulkanMod Forge Port UNOFFICIAL");

	private static String VERSION;
	public static Config CONFIG;

	public void onInitializeClient() {
        VERSION = ModList.get()
                .getModContainerById(MODID)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");

        LOGGER.info("===========================================");
        LOGGER.info(" VulkanMod Unofficial - (Neo)Forge Edition");
        LOGGER.info(" Version: {}", VERSION);
        LOGGER.info(" Based on upstream VulkanMod: {}", VERSION.split("-")[0]);
        LOGGER.info(" Platform: {} for MC {}", "Forge" + FMLLoader.versionInfo().forgeVersion(), FMLLoader.versionInfo().mcVersion());
        LOGGER.info("===========================================");
        LOGGER.info(" This is an UNOFFICIAL community port.");
        LOGGER.info(" Original project: https://github.com/xCollateral/VulkanMod");
        // TODO:LOGGER.info(" Report issues HERE: https://github.com/NAME/VulkanMod-Forge/issues");
        LOGGER.info(" Licensed under LGPL-3.0");
        LOGGER.info("===========================================");
        LOGGER.info("== VulkanMod Unofficial (Forge) ==");

        Platform.init();
        VideoModeManager.init();
	}

	public static Config loadConfig(Path path) {
		Config config = Config.load(path);

		if(config == null) {
			config = new Config();
			config.write();
		}

		return config;
	}

	public static String getVersion() {
		return VERSION;
	}
}
