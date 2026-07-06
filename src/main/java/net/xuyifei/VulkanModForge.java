package net.xuyifei;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.gui.VOptionScreen;

@Mod(VulkanModForge.MODID)
public class VulkanModForge {

    public static final String MODID = "vulkanmod_forge_port_unofficial";

    static {
        new Initializer().onInitializeClient();
    }

    public VulkanModForge(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((mcClient, modsScreen) -> new VOptionScreen(Component.literal("Video Setting"), modsScreen)));
    }
}
