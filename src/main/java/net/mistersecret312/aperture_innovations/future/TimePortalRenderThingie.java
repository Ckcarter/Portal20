package net.mistersecret312.aperture_innovations.future;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;

public class TimePortalRenderThingie
{
	public void renderPortalCorridorStuff(Vec3 pos, float yRot, float xRot, Direction direction,
										  Camera camera, PoseStack poseStack, boolean isPrimary, ClientPortalLink link)
	{
		// outer half extents
		float hx = 1.0f, hy = 1.5f;
		float thick = 0.10f;
		// inner opening half extents
		float innerHx = hx - thick;
		float innerHy = hy - thick;

		// Portal transform in world space
		final double px = pos.x, py = pos.y, pz = pos.z;

		Vec3i normal = direction.getNormal();
		if(xRot == -90)
			normal = Direction.UP.getNormal();
		else if(xRot == 90)
			normal = Direction.DOWN.getNormal();

		// Portal normal
		final float nX = normal.getX();
		final float nY = normal.getY();
		final float nZ = normal.getZ();

		// Portal basis vectors
		float upX = 0f, upY = 0f, upZ = 0f;
		if(xRot == 0)
			upY = 1f;
		else if(direction.getAxis().equals(Direction.Axis.X))
			upX = direction.getAxisDirection().getStep();
		else if(direction.getAxis().equals(Direction.Axis.Z))
			upZ = direction.getAxisDirection().getStep();

		// right = up x normal
		float rX =  nZ, rY = nY, rZ = -nX;

		float rLen = (float) Math.sqrt(rX*rX + rY*rY + rZ*rZ);
		if (rLen > 1e-6f) { rX /= rLen; rY /= rLen; rZ /= rLen; }

		// Camera world position (trying to undo the vanilla view bobbing)
		var mc  = Minecraft.getInstance();
		// camera position before bob
		Vec3 camWP = Minecraft.getInstance().player.getPosition(Minecraft.getInstance().getPartialTick()).add(0, 0.5, 0);

		// Camera relative to portal
		double toCamX = camWP.x - px;
		double toCamY = camWP.y - py;
		double toCamZ = camWP.z - pz;

		// Signed distance along portal normal
		double distSigned = toCamX * nX + toCamY * nY + toCamZ * nZ;

		float halfScale = Math.max(Math.min(innerHx, innerHy), 1e-4f);
		float eyeDistPU = Math.max((float) Math.abs(distSigned) / halfScale, 5e-3f);

		// Project camera onto portal plane
		double inPX = toCamX - distSigned * nX;
		double inPY = toCamY - distSigned * nY;
		double inPZ = toCamZ - distSigned * nZ;

		// Convert to portal-local parallax coords
		float parallaxX = (float) ((inPX * rX + inPY * rY + inPZ * rZ) / innerHx);
		float parallaxY = (float) ((inPX * upX + inPY * upY + inPZ * upZ) / innerHy);

		ShaderInstance sh = ApertureInnovations.ClientModEvents.portalCorridorShaderInstance;
		if (sh != null) {
			float timeSec = System.nanoTime() / 1_000_000_000f;

			// Animation / layers
			sh.safeGetUniform("GameTime").set(timeSec);
			sh.safeGetUniform("Layers").set(16);

			// View dependent uniforms
			sh.safeGetUniform("Parallax").set(parallaxX, parallaxY);
			sh.safeGetUniform("EyeDist").set(eyeDistPU);

			// Corridor tuning
			sh.safeGetUniform("Segment").set(1f);
			sh.safeGetUniform("MaxFrames").set(18);
			sh.safeGetUniform("EdgeThickness").set(0.05f);
			sh.safeGetUniform("EdgeSoftness").set(0.25f);
			sh.safeGetUniform("DepthFade").set(0.15f);

			// Textures
			sh.safeGetUniform("Sampler0").set(0);
			sh.safeGetUniform("Sampler1").set(1);
			RenderSystem.setShaderTexture(1, isPrimary ?
													 link.getVariant().getPrimaryPortal().getMaskTexture() :
													 link.getVariant().getSecondaryPortal().getMaskTexture());

			sh.apply();
		}
	}
}
