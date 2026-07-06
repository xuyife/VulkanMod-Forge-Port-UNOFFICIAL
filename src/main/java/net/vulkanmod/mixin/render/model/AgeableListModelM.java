package net.vulkanmod.mixin.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.xuyifei.forged.render.GuiEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableListModel.class)
public class AgeableListModelM {
    @Inject(method = "renderToBuffer", at = @At("HEAD"))
    private void deferDirectGuiPlayerModelStateBoundary(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha, CallbackInfo ci) {
        if (!GuiEntityRenderState.isGuiEntityPreview(pPackedLight)) {
            return;
        }

        GuiEntityRenderState.prepareDeferredDraw();
    }
}
