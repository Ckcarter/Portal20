package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.model.SentryTurretModel;
import net.mistersecret312.aperture_innovations.entities.SentryTurretEntity;

public class SentryTurretRenderer
        extends MobRenderer<SentryTurretEntity, SentryTurretModel>
{
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(ApertureInnovations.MODID, "sentry_turret"),
                    "main"
            );

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ApertureInnovations.MODID,
                    "textures/entity/legacy_portalgun/skin_turret.png"
            );

    public SentryTurretRenderer(EntityRendererProvider.Context context)
    {
        super(
                context,
                new SentryTurretModel(context.bakeLayer(LAYER)),
                0.35F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(SentryTurretEntity entity)
    {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(
            SentryTurretEntity entity,
            PoseStack poseStack,
            float ageInTicks,
            float rotationYaw,
            float partialTick)
    {
        super.setupRotations(
                entity,
                poseStack,
                ageInTicks,
                rotationYaw,
                partialTick
        );

        // Portal turrets face the opposite direction from Minecraft's
        // default living-entity model orientation.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }
}
