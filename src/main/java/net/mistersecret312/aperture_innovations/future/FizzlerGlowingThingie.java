package net.mistersecret312.aperture_innovations.future;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;

public class FizzlerGlowingThingie
{
	public void renderGlowingField(PoseStack poseStack, MultiBufferSource buffer, boolean isPrimary, ClientPortalLink link)
	{
		poseStack.translate(0f, 0f, 1f);
		poseStack.scale(2f, 1f, 1f);
		ShaderInstance shader = ApertureInnovations.ClientModEvents.portalFizzleShaderInstance;
		if(shader != null)
		{
			shader.safeGetUniform("u_resolution").set(1.0f, 2.0f);

			shader.safeGetUniform("pointAmount").set(1);
			shader.safeGetUniform("brighten").set(5f);
			shader.safeGetUniform("radius").set(0.5f);
			float[] f = {0.25f, 0.5f, 0.25f, 0f, 0f, 0f, 0f, 0f, 0f
					, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
					, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f
					, 0f, 0f, 0f};
			shader.safeGetUniform("Points").set(f);
		}
		VertexConsumer consumerC = buffer.getBuffer(PortalRenderTypes.fizzler(isPrimary ?
																					  link.getVariant().primaryPortal.getClosedTexture() :
																					  link.getVariant().secondaryPortal.getClosedTexture()));
		consumerC.vertex(poseStack.last().pose(), -0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(0, 1);

		consumerC.vertex(poseStack.last().pose(), 0.5f, -0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(1, 1);

		consumerC.vertex(poseStack.last().pose(), 0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(1, 0);

		consumerC.vertex(poseStack.last().pose(), -0.5f, 0.5f, 0)
				 .color(FastColor.ABGR32.color(255, 255, 255, 255))
				 .uv(0, 0);
	}
}
