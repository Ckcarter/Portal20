package net.mistersecret312.aperture_innovations.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.utilities.ClientPortalUtilities;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalRenderer.LINKS;

public class CrosshairOverlay
{
	public static final IGuiOverlay OVERLAY = ((gui, guiGraphics, partialTick, screenWidth, screenHeight)
													   -> {
			LocalPlayer player = Minecraft.getInstance().player;

			if (Minecraft.getInstance().options.hideGui) return;

			if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

			if (player == null) return;

			PoseStack poseStack = guiGraphics.pose();

			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if (!hasPortalGun) return;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			int dualityState = portalGun.getDualityState(gunStack);

			ClientPortalLink link = LINKS.get(portalGun.getUUID(gunStack, false));
			if(link != null)
			{
				boolean hasPrimary = link.getPrimaryPortal().isOpen();
				boolean hasSecondary = link.getSecondaryPortal().isOpen();

				ColorUtil.RGBA primaryColor = ClientPortalUtilities.getPortalColor(link, true);
				ColorUtil.RGBA secondaryColor = ClientPortalUtilities.getPortalColor(link, false);

				ResourceLocation primaryCrosshairTexture = ClientPortalUtilities.getCrosshairTexture(link, true);
				ResourceLocation secondaryCrosshairTexture = ClientPortalUtilities.getCrosshairTexture(link, false);

				int uOffsetPrimary = hasPrimary ? 0 : 51;
				int uOffsetSecondary = hasSecondary ? 34 : 17;

				poseStack.pushPose();
				int x = (screenWidth - 17) / 2;
				int y = (screenHeight - 33) / 2;
				poseStack.translate(x, y, 0);

				poseStack.pushPose();

				if(dualityState == 1)
				{
					primaryCrosshairTexture = secondaryCrosshairTexture;
					primaryColor = secondaryColor;
					poseStack.translate(17, 33, 0);
					poseStack.mulPose(Axis.ZP.rotationDegrees(180));
					uOffsetPrimary = uOffsetSecondary;
				}
				RenderSystem.setShaderColor(primaryColor.red(), primaryColor.green(), primaryColor.blue(),
						primaryColor.alpha());

				guiGraphics.blit(primaryCrosshairTexture, 0, 0,
						uOffsetPrimary, 0,
						17, 33,
						68, 33);

				poseStack.popPose();

				poseStack.pushPose();
				if(dualityState == 0)
				{
					secondaryCrosshairTexture = primaryCrosshairTexture;
					secondaryColor = primaryColor;
					poseStack.translate(17, 33, 0);
					poseStack.mulPose(Axis.ZP.rotationDegrees(180));
					uOffsetSecondary = uOffsetPrimary;
				}

				RenderSystem.setShaderColor(secondaryColor.red(), secondaryColor.green(), secondaryColor.blue(),
						secondaryColor.alpha());

				guiGraphics.blit(secondaryCrosshairTexture, 0, 0,
						uOffsetSecondary, 0,
						17, 33,
						68, 33);

				poseStack.popPose();

				poseStack.popPose();

				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			}

		});
}
