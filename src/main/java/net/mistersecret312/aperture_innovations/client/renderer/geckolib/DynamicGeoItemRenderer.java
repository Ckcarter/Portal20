package net.mistersecret312.aperture_innovations.client.renderer.geckolib;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Extended special-entity renderer for more dynamic entity rendering.<br>
 * Primarily handles per-bone textures and/or {@link RenderType RenderTypes}
 * Because of the extra performance cost of this renderer, it is advised to avoid using it unnecessarily,
 * and consider whether the benefits are worth the cost for your needs.
 */
public abstract class DynamicGeoItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T>
{
	protected static Map<ResourceLocation, IntIntPair> TEXTURE_DIMENSIONS_CACHE = new Object2ObjectOpenHashMap<>();

	protected ResourceLocation textureOverride = null;

	public DynamicGeoItemRenderer(GeoModel<T> model) {
		super(model);
	}

	/**
	 * For each bone rendered, this method is called
	 * <p>
	 * If a ResourceLocation is returned, the renderer will render the bone using that texture instead of the default
	 * This can be useful for custom rendering  on a per-bone basis
	 * <p>
	 * There is a somewhat significant performance cost involved in this however, so only use as needed
	 *
	 * @return The specified ResourceLocation, or null if no override
	 */
	@Nullable
	protected ResourceLocation getTextureOverrideForBone(GeoBone bone, T animatable, float partialTick) {
		return null;
	}

	/**
	 * For each bone rendered, this method is called
	 * <p>
	 * If a RenderType is returned, the renderer will render the bone using that RenderType instead of the default
	 * This can be useful for custom rendering operations on a per-bone basis
	 * <p>
	 * There is a somewhat significant performance cost involved in this however, so only use as needed
	 *
	 * @return The specified RenderType, or null if no override
	 */
	@Nullable
	protected RenderType getRenderTypeOverrideForBone(GeoBone bone, T animatable, ResourceLocation texturePath, MultiBufferSource bufferSource, float partialTick) {
		return null;
	}

	/**
	 * Override this to handle a given {@link GeoBone GeoBone's} rendering in a particular way
	 *
	 * @return Whether the renderer should skip rendering the {@link GeoCube cubes} of the given GeoBone or not
	 */
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource, VertexConsumer buffer,
										 float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		return false;
	}

	/**
	 * Renders the provided {@link GeoBone} and its associated child bones
	 */
	@Override
	public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType,
								  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
								  float partialTick, int packedLight, int packedOverlay, float red, float green,
								  float blue, float alpha) {
		poseStack.pushPose();
		RenderUtils.translateMatrixToBone(poseStack, bone);
		RenderUtils.translateToPivotPoint(poseStack, bone);
		RenderUtils.rotateMatrixAroundBone(poseStack, bone);
		RenderUtils.scaleMatrixForBone(poseStack, bone);

		if (bone.isTrackingMatrices()) {
			Matrix4f poseState = new Matrix4f(poseStack.last().pose());

			bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
			bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations));
		}

		RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

		this.textureOverride = getTextureOverrideForBone(bone, this.animatable, partialTick);
		ResourceLocation texture = this.textureOverride == null ? getTextureLocation(this.animatable) : this.textureOverride;
		RenderType renderTypeOverride = getRenderTypeOverrideForBone(bone, this.animatable, texture, bufferSource, partialTick);

		if (texture != null && renderTypeOverride == null)
			renderTypeOverride = getRenderType(this.animatable, texture, bufferSource, partialTick);

		if (renderTypeOverride != null)
			buffer = bufferSource.getBuffer(renderTypeOverride);

		if (!boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha))
			super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);

		if (renderTypeOverride != null)
			buffer = bufferSource.getBuffer(renderTypeOverride);

		if (!isReRender)
			applyRenderLayersForBone(poseStack, animatable, bone, renderTypeOverride, bufferSource, buffer, partialTick, packedLight, packedOverlay);

		super.renderChildBones(poseStack, animatable, bone, renderTypeOverride, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

		poseStack.popPose();
	}

	/**
	 * Called after rendering the model to buffer. Post-render modifications should be performed here
	 * <p>
	 * {@link PoseStack} transformations will be unused and lost once this method ends
	 */
	@Override
	public void postRender(PoseStack poseStack, T animatable, BakedGeoModel model, MultiBufferSource bufferSource,
						   @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
						   float red, float green, float blue, float alpha) {
		this.textureOverride = null;
		super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link VertexConsumer buffer} for rendering
	 * <p>
	 * Custom override to handle custom non-baked textures for DynamicGeoItemRenderer
	 */
	@Override
	public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer,
									 int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		if (this.textureOverride == null) {
			super.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay,
					red, green, blue, alpha);

			return;
		}

		IntIntPair boneTextureSize = computeTextureSize(this.textureOverride);
		IntIntPair itemTextureSize = computeTextureSize(getTextureLocation(this.animatable));

		if (boneTextureSize == null || itemTextureSize == null) {
			super.createVerticesOfQuad(quad, poseState, normal, buffer, packedLight, packedOverlay,
					red, green, blue, alpha);

			return;
		}

		for (GeoVertex vertex : quad.vertices()) {
			Vector4f vector4f = poseState.transform(new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0f));
			float texU = (vertex.texU() * itemTextureSize.firstInt()) / boneTextureSize.firstInt();
			float texV = (vertex.texV() * itemTextureSize.secondInt()) / boneTextureSize.secondInt();

			buffer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, texU, texV,
					packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
		}
	}

	/**
	 * Retrieve or compute the height and width of a given texture from its {@link ResourceLocation}
	 * <p>
	 * This is used for dynamically mapping vertices on a given quad
	 * <p>
	 * This is inefficient however, and should only be used where required
	 */
	protected IntIntPair computeTextureSize(ResourceLocation texture) {
		return TEXTURE_DIMENSIONS_CACHE.computeIfAbsent(texture, RenderUtils::getTextureDimensions);
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
	public static final Function<ResourceLocation, RenderType> GLOWING_FUNCTION = Util.memoize(texture -> {
		RenderStateShard.TextureStateShard textureState = new RenderStateShard.TextureStateShard(texture, false, false);

		return RenderType.create("geo_glowing_layer", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
				RenderType.CompositeState.builder()
										 .setShaderState(SHADER_STATE)
										 .setTextureState(textureState)
										 .setTransparencyState(TRANSPARENCY_STATE)
										 .setWriteMaskState(WRITE_MASK).createCompositeState(false));
	});
}