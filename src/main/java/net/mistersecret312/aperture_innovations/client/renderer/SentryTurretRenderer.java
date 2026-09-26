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

        // IMPORTANT: this runs AFTER super.render(). LivingEntityRenderer has already
        // popped its model/body rotations, so this PoseStack is entity-translated but
        // WORLD-ALIGNED. Do not rotate the laser by entityYaw here.
        //
        // The beam therefore uses the exact synchronized shot vector in world space.
        if (entity.isAlive() && !entity.isFallen() && !entity.isDifferent()) {
            Vec3 worldStart = entity.getLaserMuzzlePosition();
            Vec3 worldDir = entity.getCombatLineDirection().normalize();
            Vec3 worldEnd = worldStart.add(worldDir.scale(20.0D));

            net.minecraft.world.phys.HitResult hit = entity.level().clip(
                    new net.minecraft.world.level.ClipContext(
                            worldStart, worldEnd,
                            net.minecraft.world.level.ClipContext.Block.COLLIDER,
                            net.minecraft.world.level.ClipContext.Fluid.NONE, entity));
            if (hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                worldEnd = hit.getLocation();
            }

            // Renderer origin is already at entity.position(); subtract only translation.
            // No yaw/pitch/body/model conversion belongs here.
            Vec3 localStart = worldStart.subtract(entity.position());
            Vec3 localEnd = worldEnd.subtract(entity.position());
            Vec3 delta = localEnd.subtract(localStart);
            float len = (float)delta.length();

            if (len > 0.0001F) {
                float nx=(float)(delta.x/len), ny=(float)(delta.y/len), nz=(float)(delta.z/len);
                PoseStack.Pose pose=poseStack.last();
                Matrix4f matrix=pose.pose();
                Matrix3f normal=pose.normal();
                VertexConsumer line=buffer.getBuffer(RenderType.lines());

                line.vertex(matrix,(float)localStart.x,(float)localStart.y,(float)localStart.z)
                        .color(255,0,0,220).normal(normal,nx,ny,nz).endVertex();
                line.vertex(matrix,(float)localEnd.x,(float)localEnd.y,(float)localEnd.z)
                        .color(255,0,0,220).normal(normal,nx,ny,nz).endVertex();
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
