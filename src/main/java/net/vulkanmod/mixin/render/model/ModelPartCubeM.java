package net.vulkanmod.mixin.render.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.vulkanmod.interfaces.ModelPartCubeMixed;
import net.vulkanmod.render.model.CubeModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelPart.Cube.class)
public class ModelPartCubeM implements ModelPartCubeMixed {

    @Unique
    CubeModel cube;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;size()I"
            )
    )
    private int onSetSize(Set<Direction> set,
                          int pTexCoordU, int pTexCoordV,
                          float pOriginX, float pOriginY, float pOriginZ,
                          float pDimensionX, float pDimensionY, float pDimensionZ,
                          float pGrowX, float pGrowY, float pGrowZ,
                          boolean pMirror,
                          float pTexScaleU, float pTexScaleV,
                          Set<Direction> pVisibleFaces) {

        CubeModel cubeModel = new CubeModel();
        cubeModel.setVertices(
                pTexCoordU, pTexCoordV, pOriginX, pOriginY, pOriginZ,
                pDimensionX, pDimensionY, pDimensionZ, pGrowX, pGrowY, pGrowZ,
                pMirror, pTexScaleU, pTexScaleV, pVisibleFaces
        );

        this.cube = cubeModel;

        return pVisibleFaces.size();
    }

    @Override
    public CubeModel getCubeModel() {
        return this.cube;
    }
}
