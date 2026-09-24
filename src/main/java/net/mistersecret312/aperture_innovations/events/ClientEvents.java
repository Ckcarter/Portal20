package net.mistersecret312.aperture_innovations.events;

import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ServerboundOpenPortalPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundPickUpEntityPacket;
import net.mistersecret312.aperture_innovations.network.ServerboundResetPortalLinkPacket;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.utilities.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import net.mistersecret312.aperture_innovations.sounds.PortalSoundWrapper;

import java.util.Map;
import java.util.UUID;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.*;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, value = Dist.CLIENT)
public class ClientEvents
{
	@SubscribeEvent
	public static void renderPortals(RenderLevelStageEvent event) {
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		Camera camera = event.getCamera();
		PoseStack poseStack = event.getPoseStack();

		if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
		{
			for(Map.Entry<UUID, ClientPortalLink> linkEntry : LINKS.entrySet())
			{
				ClientPortalLink link = linkEntry.getValue();

				poseStack.pushPose();

				ResourceLocation texturePrimary = ClientPortalUtilities.getPortalVortexTexture(link, true);
				ResourceLocation textureSecondary = ClientPortalUtilities.getPortalVortexTexture(link, false);

				final TextureAtlasSprite primary = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															.apply(texturePrimary);
				final TextureAtlasSprite secondary = Minecraft.getInstance()
															  .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
															  .apply(textureSecondary);

				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					ResourceKey<Level> dimension = isPrimary ? link.getPrimaryPortal().getDimension() : link.getSecondaryPortal().getDimension();
					if(Minecraft.getInstance().level.dimension() != dimension) continue;

					float scale = ClientPortalUtilities.getPortalOpeningAnimationProgress(link.linkID(), isPrimary);

					Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();

					if(portalPos != null
							   && Minecraft.getInstance().level.isLoaded(BlockPos.containing(portalPos))
							   && event.getLevelRenderer().getFrustum().isVisible(new AABB(portalPos, portalPos).inflate(1)))
					{
						if(isPrimary)
							primaryRender(link, buffer, poseStack, camera, scale);
						else
							secondaryRender(link, buffer, poseStack, camera, scale);
					}

					if(isPrimary && link.getPrimaryPortal().isInWorld())
						renderPortalVortex(link, camera, primary, buffer, poseStack, true);
					else if(link.getSecondaryPortal().isInWorld())
						renderPortalVortex(link, camera, secondary, buffer, poseStack, false);
				}

				poseStack.popPose();
				buffer.endBatch();
			}
		}
	}

	@SubscribeEvent
	public static void renderBlockOverlay(RenderBlockScreenEffectEvent event)
	{
		Player player = event.getPlayer();
		Level level = player.level();

		if(event.getOverlayType().equals(RenderBlockScreenEffectEvent.OverlayType.BLOCK))
		{
			Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(player);
			UUID uuid = portal.getFirst();
			boolean isPrimary = portal.getSecond();
			if(uuid == null)
				return;

			Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);
			Vec2 rotation = PortalUtilities.getPortalRotation(level, uuid, isPrimary);

			AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, rotation.x, rotation.y);
			if(portalBox.contains(player.getEyePosition()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void mouseClicks(InputEvent.MouseButton.Pre event)
	{
		Level level = Minecraft.getInstance().level;
		Player player = Minecraft.getInstance().player;
		if(level == null || player == null || Minecraft.getInstance().screen != null)
			return;

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
		if (!hasPortalGun)
			return;

		ItemStack gunItemStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
		PortalGunItem gunItem = (PortalGunItem) gunItemStack.getItem();

		int dualityState = gunItem.getDualityState(gunItemStack);

		if(!player.isShiftKeyDown() && event.getButton() == 0 &&
				   event.getAction() == 1 && (dualityState == 2 || dualityState == 0))
		{
			NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(true));
			event.setCanceled(true);
		}
		else if(!player.isShiftKeyDown() && event.getButton() == 1 &&
						event.getAction() == 1 && (dualityState == 2 || dualityState == 1))
		{
			NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(false));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event)
	{
		if(event.phase == TickEvent.Phase.END)
		{
			Minecraft mc = Minecraft.getInstance();
			if(mc.level == null)
			{
				if(!LINKS.isEmpty())
					LINKS.clear();
			}
			if(mc.level != null && mc.player != null)
			{
				LINKS.forEach((linkID, link) ->
					{
						for(int i = 0; i < 2; i++)
						{
							boolean isPrimary = i == 0;
							Vec3 portalPos = isPrimary ? link.getPrimaryPortal()
															 .getPosition() : link.getSecondaryPortal().getPosition();

							if(portalPos != null)
							{
								float progress = ClientPortalUtilities.getPortalOpeningAnimationProgress(linkID,
										isPrimary);
								if(progress < 1F)
								{
									progress += 0.25f;
									ClientPortalUtilities.setPortalOpeningAnimationProgress(progress, linkID,
											isPrimary);
								}
							}

							if(!link.isOpen())
							{
								PortalSoundWrapper.PortalAmbient ambient = ClientPortalUtilities.getAmbientSound(linkID,
										isPrimary);
								if(ambient != null) ambient.stopSound();
							}
						}
					});

				while(ApertureInnovations.ClientModEvents.RESET_PORTAL_GUN.get().consumeClick())
				{
					NetworkInit.INSTANCE.sendToServer(new ServerboundResetPortalLinkPacket());
				}

				while(ApertureInnovations.ClientModEvents.PICK_UP.get().consumeClick())
					NetworkInit.INSTANCE.sendToServer(new ServerboundPickUpEntityPacket());

				while(ApertureInnovations.ClientModEvents.PORTAL_GUN_PRIMARY_FIRE.get().consumeClick())
				{
					NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(true));
				}
				while(ApertureInnovations.ClientModEvents.PORTAL_GUN_SECONDARY_FIRE.get().consumeClick())
				{
					NetworkInit.INSTANCE.sendToServer(new ServerboundOpenPortalPacket(false));
				}
			}
		}
	}
}
