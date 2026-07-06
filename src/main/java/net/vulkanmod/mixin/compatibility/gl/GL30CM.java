package net.vulkanmod.mixin.compatibility.gl;

import net.vulkanmod.gl.GlFramebuffer;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GL30C.class)
public class GL30CM {
    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public static void glDeleteFramebuffers(@NativeType("GLuint const *") int framebuffers) {
        GlFramebuffer.deleteFramebuffer(framebuffers);
    }
}
