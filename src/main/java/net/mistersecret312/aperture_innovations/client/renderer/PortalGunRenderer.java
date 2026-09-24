package net.mistersecret312.aperture_innovations.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.ColorUtil;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.renderer.geckolib.DynamicGeoItemRenderer;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariant;
import net.mistersecret312.aperture_innovations.client.resourcepack.ClientPortalGunVariants;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;
import net.mistersecret312.aperture_innovations.utilities.ClientPortalUtilities;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

import java.awt.*;
import java.util.List;
import java.util.function.BiFunction;

public class PortalGunRenderer extends DynamicGeoItemRenderer<PortalGunItem>
{
	public PortalGunRenderer()
	{
		super(new DefaultedItemGeoModel<>(new ResourceLocation(ApertureInnovations.MODID, "portal_gun")));
	}

	@Override
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource,
										 VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay,
										 float red, float green, float blue, float alpha)
	{
		List<String> gunCore = Lists.newArrayList("CoreOuter", "CoreInner", "PortalLight", "Muzzle");
		if(gunCore.contains(bone.getName()))
		{
			int portal = this.getAnimatable().getLastShotPortal(this.currentItemStack);
			ClientPortalLink link = PortalUtilities.getPortalLinks().get(this.getAnimatable().getUUID(this.currentItemStack, false));

			if(portal == -1 || link == null)
				return super.boneRenderOverride(poseStack, bone, bufferSource, buffer,
						partialTick, packedLight, packedOverlay, red, green, blue, alpha);

			ColorUtil.RGBA color = ClientPortalUtilities.getPortalColor(link, portal == 0);

			red *= color.red();
			green *= color.green();
			blue *= color.blue();
			alpha *= color.alpha();

			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			return true;
		}

		if(bone.getName().equals("StripePrimary") || bone.getName().equals("StripeSecondary"))
		{
			boolean isPrimary = bone.getName().equals("StripePrimary");
			int stripeColor = isPrimary ? this.getAnimatable().getPrimaryStripeColor(this.currentItemStack) :
					this.getAnimatable().getSecondaryStripeColor(this.currentItemStack);
			if(stripeColor == -1)
			{
				ClientPortalLink link = PortalUtilities.getPortalLinks().get(this.getAnimatable().getUUID(this.currentItemStack, false));
				ClientPortalGunVariant variant = ClientPortalGunVariant.DEFAULT_VARIANT;

				if(animatable.getVariant(currentItemStack) != null)
					variant = ClientPortalGunVariants.getPortalGunVariant(animatable.getVariant(currentItemStack));

				if(link != null)
					variant = link.getVariant();

				ColorUtil.RGBA color = isPrimary ? variant.getPrimaryStripeColor() : variant.getSecondaryStripeColor();

				if(color.red() == 1F && color.green() == 1F && color.blue() == 1F && color.alpha() == 1F)
					return true;

				red *= color.red();
				green *= color.green();
				blue *= color.blue();
				alpha *= color.alpha();

				renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
				return true;
			}
			else
			{
				Color color = new Color(stripeColor, false);
				if(color.getRGB() == -1)
					return true;

				red *= color.getRed()/255f;
				green *= color.getGreen()/255f;
				blue *= color.getBlue()/255f;

				renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
				return true;
			}
		}

		return super.boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay,
				red, green, blue, alpha);
	}

	@Override
	protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, PortalGunItem animatable,
																ResourceLocation texturePath,
																MultiBufferSource bufferSource, float partialTick)
	{
		List<String> gunCore = Lists.newArrayList("CoreOuter", "CoreInner", "PortalLight", "Muzzle");
		if(bone.getName().equals("Zap"))
			return PortalRenderTypes.entityTranslucentCull(getTextureOverrideForBone(bone, animatable, partialTick));
		if(gunCore.contains(bone.getName()))
		{
			return DynamicGeoItemRenderer.GLOWING_FUNCTION.apply(getTextureOverrideForBone(bone, animatable, partialTick));
		}
		return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
	}

	@Override
	protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, PortalGunItem animatable,
																   float partialTick)
	{
		int portal = this.getAnimatable().getLastShotPortal(this.currentItemStack);
		ClientPortalLink link = PortalUtilities.getPortalLinks().get(this.getAnimatable().getUUID(this.currentItemStack, false));

		if(bone.getName().equals("Zap") && animatable.getZapTick(currentItemStack) != -1)
		{
			return new ResourceLocation(ApertureInnovations.MODID, "textures/portal_gun/zap/portal_gun_zap_" + animatable.getZapTick(this.currentItemStack) + ".png");
		}

		if(link != null)
		{
			List<String> gunCore = Lists.newArrayList("CoreOuter", "CoreInner", "PortalLight", "Muzzle");
			if(gunCore.contains(bone.getName()))
				return ClientPortalUtilities.getPortalGunCoreTexture(link, portal);
			else return ClientPortalUtilities.getPortalGunTexture(link);
		}
		return new ResourceLocation(ApertureInnovations.MODID, "textures/item/portal_gun.png");
	}

	@Override
	public RenderType getRenderType(PortalGunItem animatable, ResourceLocation texture,
									@Nullable MultiBufferSource bufferSource, float partialTick)
	{
		return RenderType.entityTranslucent(texture);
	}

	private static final RenderStateShard.ShaderStateShard SHADER_STATE = new RenderStateShard.ShaderStateShard(
			GameRenderer::getRendertypeEntityTranslucentEmissiveShader);
	private static final RenderStateShard.TransparencyStateShard TRANSPARENCY_STATE = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});
	private static final RenderStateShard.WriteMaskStateShard WRITE_MASK = new RenderStateShard.WriteMaskStateShard(true, true);
	public static final BiFunction<ResourceLocation, Boolean, RenderType> GLOWING_RENDER_TYPE = Util.memoize((texture, isGlowing) -> {
		RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);

		return RenderType.create("geo_glowing_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
										 .setShaderState(SHADER_STATE)
										 .setTextureState(textureState)
										 .setTransparencyState(TRANSPARENCY_STATE)
										 .setOverlayState(new RenderStateShard.OverlayStateShard(true))
										 .setWriteMaskState(WRITE_MASK).createCompositeState(isGlowing));
	});
}
