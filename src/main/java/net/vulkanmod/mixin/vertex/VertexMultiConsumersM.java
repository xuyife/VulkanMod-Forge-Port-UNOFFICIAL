package net.vulkanmod.mixin.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.render.vertex.VertexUtil;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class VertexMultiConsumersM {

    @Mixin(targets = "com/mojang/blaze3d/vertex/VertexMultiConsumer$Double")
    public static class DoubleM implements ExtendedVertexBuilder {

        @Shadow @Final private VertexConsumer first;
        @Shadow @Final private VertexConsumer second;

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            ExtendedVertexBuilder firstExt = (ExtendedVertexBuilder) this.first;
            ExtendedVertexBuilder secondExt = (ExtendedVertexBuilder) this.second;

            firstExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            secondExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
        }
    }

    @Mixin(targets = "com/mojang/blaze3d/vertex/VertexMultiConsumer$Multiple")
    public static class MultipleM implements ExtendedVertexBuilder {
        @Shadow @Final private VertexConsumer[] delegates;

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            for (VertexConsumer vertexConsumer : this.delegates) {
                ExtendedVertexBuilder extendedVertexBuilder = (ExtendedVertexBuilder) vertexConsumer;

                extendedVertexBuilder.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            }
        }
    }
}
