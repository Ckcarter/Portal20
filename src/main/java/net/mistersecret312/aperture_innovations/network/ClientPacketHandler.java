package net.mistersecret312.aperture_innovations.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.block_entities.AntlineBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.AntlineOutputBlockEntity;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.utilities.ClientAntlineUtilities;
import net.mistersecret312.aperture_innovations.utilities.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.data.portal.Portal;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.joml.Vector3f;

import java.util.UUID;

public class ClientPacketHandler
{
	public static void syncPortalData(UUID linkID, boolean isPrimary, Portal portal, ResourceLocation variant)
	{
		ClientPortalLink link = PortalRenderer.LINKS.getOrDefault(linkID, new ClientPortalLink());
		link.variantKey = variant;
		link.linkID = linkID;
		if(isPrimary)
		{
			if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
			{
				portal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(Minecraft.getInstance().level,
						portal.getPosition(), portal.getXRotation(), portal.getYRotation()));
				ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, true);
			}
			link.setPrimaryPortal(portal);
		}
		else
		{
			if(portal.getPosition() != null && !portal.getPosition().equals(link.getPrimaryPortal().getPosition()))
			{
				portal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(Minecraft.getInstance().level,
						portal.getPosition(), portal.getXRotation(), portal.getYRotation()));
				ClientPortalUtilities.setPortalOpeningAnimationProgress(0F, linkID, false);
			}
			link.setSecondaryPortal(portal);
		}
		PortalRenderer.LINKS.put(linkID, link);
	}

	public static void handleTeleportMomentumPacket(Vec3 speed)
	{
		Player player = Minecraft.getInstance().player;
		if(player != null)
		{
			player.setDeltaMovement(speed);
		}
	}

	public static void handleEntityPortalLerp(int id, Vector3f pos, Vector3f delta, float xRot, float yRot)
	{
		Entity entity = Minecraft.getInstance().level.getEntity(id);
		if(entity != null)
		{
			entity.setPos(pos.x, pos.y, pos.z);

			entity.xOld = pos.x;
			entity.xo = pos.x;
			entity.xRotO = xRot;

			entity.yOld = pos.y;
			entity.yo = pos.y;
			entity.yRotO = yRot;

			entity.zOld = pos.z;
			entity.zo = pos.z;

			entity.setOldPosAndRot();
			entity.lerpTo(pos.x, pos.y, pos.z, yRot, xRot, 0, true);
			entity.setDeltaMovement(new Vec3(delta));
		}
	}

	public static void handleAntlineUpdate(BlockPos pos, boolean active, int color, int activeColor)
	{
		BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
		if(blockEntity != null)
		{
			if(blockEntity instanceof AntlineBlockEntity antlineBlockEntity)
			{
				antlineBlockEntity.updateConnections();
				antlineBlockEntity.trimConnections();
				antlineBlockEntity.active = active;

				antlineBlockEntity.color = color;
				antlineBlockEntity.activeColor = activeColor;

				ClientAntlineUtilities.setActive(antlineBlockEntity.getNetworkID(), active);

				antlineBlockEntity.setChanged();
			}
		}
	}

	public static void handleAntlineOutputUpdate(BlockPos pos, int color, int activeColor)
	{
		BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
		if(blockEntity != null)
		{
			if(blockEntity instanceof AntlineOutputBlockEntity output)
			{
				output.color = color;
				output.activeColor = activeColor;

				output.setChanged();
			}
		}
	}

	public static void handleEntityHeldUpdate(int id, boolean held)
	{
		Entity entity = Minecraft.getInstance().level.getEntity(id);
		if(entity != null)
		{
			entity.getCapability(CapabilityInit.HOLD).ifPresent(cap -> cap.setHeld(entity, held));
			entity.setNoGravity(held);
		}
	}

	public static void handlePlayerCapabilityPacket(int frictionlessTime)
	{
		Player player = Minecraft.getInstance().player;
		if(player == null)
			return;

		player.getCapability(CapabilityInit.APERTURE).ifPresent(cap ->
			{
				cap.frictionlessTime = frictionlessTime;
			});
	}
}
