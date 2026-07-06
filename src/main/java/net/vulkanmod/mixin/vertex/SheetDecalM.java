package net.vulkanmod.mixin.vertex;

import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.render.vertex.VertexUtil;
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

@Mixin(SheetedDecalTextureGenerator.class)
public abstract class SheetDecalM implements ExtendedVertexBuilder {
    @Shadow
    @Final
    private VertexConsumer delegate;
    @Shadow @Final private Matrix3f normalInversePose;
    @Shadow @Final private Matrix4f cameraInversePose;
    @Shadow @Final private float textureScale;

    @Unique
    private boolean canUseFastVertex = false;

    private Vector3f normal = new Vector3f();
    private Vector4f position = new Vector4f();

    @Override
    public boolean canUseFastVertex() {
        return this.canUseFastVertex;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void checkDelegates(VertexConsumer pDelegate, Matrix4f pCameraPose, Matrix3f pNormalPose, float pTextureScale, CallbackInfo ci) {
        this.canUseFastVertex = (ExtendedVertexBuilder.of(this.delegate) != null);
    }

    @Override
    public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
        float nx = VertexUtil.unpackN1(packedNormal);
        float ny = VertexUtil.unpackN2(packedNormal);
        float nz = VertexUtil.unpackN3(packedNormal);

        Vector3f vector3f = this.normalInversePose.transform(new Vector3f(nx, ny, nz));
        Direction direction = Direction.getNearest(vector3f.x(), vector3f.y(), vector3f.z());
        Vector4f vector4f = this.cameraInversePose.transform(new Vector4f(x, y, z, 1.0F));
        vector4f.rotateY(3.1415927F);
        vector4f.rotateX(-1.5707964F);
        vector4f.rotate(direction.getRotation());
        float finalU = (u != 0 || v != 0) ? u : -vector4f.x() * this.textureScale;
        float finalV = (u != 0 || v != 0) ? v : -vector4f.y() * this.textureScale;

        this.delegate.vertex(x, y, z, 1.0F, 1.0F, 1.0F, 1.0F, finalU, finalV, overlay, light, nx, ny,nz);
    }
}
