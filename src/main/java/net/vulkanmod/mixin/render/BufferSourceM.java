package net.vulkanmod.mixin.render;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.xuyifei.forged.render.GuiEntityRenderState;
import net.xuyifei.forged.render.GuiItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiBufferSource.BufferSource.class)
public class BufferSourceM {
    @Unique
    private boolean endingFullBatch;

    @Inject(method = "endBatch()V", at = @At("HEAD"))
    private void beginGuiEntityFullBatch(CallbackInfo ci) {
        this.endingFullBatch = hasDeferredGuiDrawState();
        if (this.endingFullBatch) {
            prepareDeferredGuiDrawState();
        }
    }

    @Inject(method = "endBatch()V", at = @At("RETURN"))
    private void endGuiEntityFullBatch(CallbackInfo ci) {
        if (this.endingFullBatch) {
            this.endingFullBatch = false;
            restoreDeferredGuiDrawState();
        }
    }

    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"))
    private void beginGuiEntityRenderTypeBatch(RenderType renderType, CallbackInfo ci) {
        if (!this.endingFullBatch && hasDeferredGuiDrawState()) {
            prepareDeferredGuiDrawState();
        }
    }

    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("RETURN"))
    private void endGuiEntityRenderTypeBatch(RenderType renderType, CallbackInfo ci) {
        if (!this.endingFullBatch) {
            restoreDeferredGuiDrawState();
        }
    }

    @Unique
    private static boolean hasDeferredGuiDrawState() {
        return GuiEntityRenderState.hasDeferredDrawState() || GuiItemRenderState.hasDeferredDrawState();
    }

    @Unique
    private static void prepareDeferredGuiDrawState() {
        if (GuiEntityRenderState.hasDeferredDrawState()) {
            GuiEntityRenderState.prepareDeferredDraw();
        }

        if (GuiItemRenderState.hasDeferredDrawState()) {
            GuiItemRenderState.prepareDeferredDraw();
        }
    }

    @Unique
    private static void restoreDeferredGuiDrawState() {
        GuiItemRenderState.restoreDeferredDrawState();
        GuiEntityRenderState.restoreDeferredDrawState();
    }
}
