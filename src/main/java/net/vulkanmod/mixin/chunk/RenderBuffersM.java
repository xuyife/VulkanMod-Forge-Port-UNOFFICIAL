package net.vulkanmod.mixin.chunk;

import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderBuffers.class)
public class RenderBuffersM {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/BufferBuilder;<init>(I)V"
            ),
            index = 0
    )
    private int modifyBufferBuilderSize(int originalSize) {
        return originalSize;
    }
}
