package net.mistersecret312.aperture_innovations.future;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;

public class LaserRenderThingie
{
	public void laserRender(PoseStack poseStack, MultiBufferSource buffer, Level level)
	{
		poseStack.pushPose();
		//						LevelRenderer.renderLineBox(poseStack, buffer.getBuffer(RenderType.lines()), new AABB(BlockPos.ZERO), 1f, 0f, 0f, 1f);
		VertexConsumer consumer = buffer.getBuffer(PortalRenderTypes.laserTest(
				new ResourceLocation(ApertureInnovations.MODID, "textures/block/laser_test.png")));

		poseStack.translate(0.5f, 0.5f, 0.5f);
		float length = 0;
		float yRot = 90;
		BlockHitResult result = level.clip(new ClipContext(new Vec3(0.5d, 0.5d, 0.5d),
				new Vec3(-100.5d, 0.5d, 0.5d).yRot((float) Math.toRadians(yRot)), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, null));

		length = (float) result.getLocation().distanceTo(new Vec3(0, 0, 0)) - 0.9f;
		//						level.addParticle(ParticleTypes.CRIT, result.getLocation().x, result.getLocation().y, result.getLocation().z,
		//								0, 0, 0);

		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

		float uMin = 0.0f;
		float uMax = (1.0f + length);

		// We use 0.0 to 1.0 for V so the 3-pixel width fills the quad height exactly once
		float vMin = 0.0f;
		float vMax = 1f;

		poseStack.mulPose(Axis.XP.rotationDegrees(45));
		for(int i = 1; i <= 4; i++)
		{
			poseStack.pushPose();
			poseStack.mulPose(Axis.XP.rotationDegrees(90 * i));

			consumer.vertex(poseStack.last().pose(), -0.5f - length, -0.09375f, 0)
					.color(1f, 1f, 1f, 1f)
					.uv(uMax, vMax);
			consumer.vertex(poseStack.last().pose(), 0.5f, -0.09375f, 0)
					.color(1f, 1f, 1f, 1f)
					.uv(uMin, vMax);
			consumer.vertex(poseStack.last().pose(), 0.5f, 0.09375f, 0)
					.color(1f, 1f, 1f, 1f)
					.uv(uMin, vMin);
			consumer.vertex(poseStack.last().pose(), -0.5f - length, 0.09375f, 0)
					.color(1f, 1f, 1f, 1f)
					.uv(uMax, vMin);

			poseStack.popPose();

		}
	}
}
