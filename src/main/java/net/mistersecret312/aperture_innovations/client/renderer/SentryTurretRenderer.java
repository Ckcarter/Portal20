package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
    public void render(SentryTurretEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);

        // Modern port of PortalGun 1.7.10 RenderTurret's red aiming line.
        // Unlike the failed particle version, this is a real continuous rendered line.
        if (entity.isAlive() && !entity.isFallen()) {
            Vec3 dir = entity.getWeaponForwardVector().normalize();
            Vec3 start = new Vec3(0.0D, 1.02D, 0.0D).add(dir.scale(0.45D));
            Vec3 worldStart = entity.position().add(start).add(0.0D, -0.0625D, 0.0D);
            // PortalGun 1.7.10-style laser: the beam is driven by the turret's
            // actual weapon yaw/pitch. Because the gun tracks the target, the laser
            // naturally follows the same aim instead of being independently snapped
            // to target.getEyePosition().
            Vec3 worldEnd = worldStart.add(dir.scale(24.0D));

            net.minecraft.world.phys.HitResult hit = entity.level().clip(
                    new net.minecraft.world.level.ClipContext(
                            worldStart, worldEnd,
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE,
                            entity));
            if (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                worldEnd = hit.getLocation();
            }

            Vec3 localEnd = worldEnd.subtract(entity.position());
            Vec3 delta = localEnd.subtract(start);
            float len = (float)delta.length();
            if (len > 0.0001F) {
                float nx = (float)(delta.x / len);
                float ny = (float)(delta.y / len);
                float nz = (float)(delta.z / len);

                PoseStack.Pose pose = poseStack.last();
                Matrix4f matrix = pose.pose();
                Matrix3f normal = pose.normal();
                VertexConsumer line = buffer.getBuffer(RenderType.lines());

                line.vertex(matrix, (float)start.x, (float)start.y, (float)start.z)
                        .color(255, 0, 0, 220)
                        .normal(normal, nx, ny, nz)
                        .endVertex();
                line.vertex(matrix, (float)localEnd.x, (float)localEnd.y, (float)localEnd.z)
                        .color(255, 0, 0, 220)
                        .normal(normal, nx, ny, nz)
                        .endVertex();
            }
        }
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
