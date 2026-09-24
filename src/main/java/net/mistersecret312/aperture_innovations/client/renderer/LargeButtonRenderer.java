package net.mistersecret312.aperture_innovations.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.LargeButtonBlock;
import net.mistersecret312.aperture_innovations.client.PortalRenderTypes;
import net.mistersecret312.aperture_innovations.client.model.LargeButtonModel;
import net.mistersecret312.aperture_innovations.client.renderer.geckolib.DynamicGeoBlockRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.awt.*;

import static net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderer.GLOWING_RENDER_TYPE;

public class LargeButtonRenderer extends DynamicGeoBlockRenderer<LargeButtonBlockEntity>
{
	public LargeButtonRenderer(BlockEntityRendererProvider.Context context)
	{
		super(new LargeButtonModel());
	}

	@Override
	protected boolean boneRenderOverride(PoseStack poseStack, GeoBone bone, MultiBufferSource bufferSource,
										 VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay,
										 float red, float green, float blue, float alpha)
	{
		if(bone.getName().equals("ColoredLines"))
		{
			boolean active = animatable.getBlockState().getValue(LargeButtonBlock.PRESSED);
			int intColor = active ? this.animatable.activeColor : this.animatable.color;

			Color color = new Color(intColor, false);
			red *= color.getRed()/255f;
			green *= color.getGreen()/255f;
			blue *= color.getBlue()/255f;

			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			return true;
		}
		if(bone.getName().equals("Button"))
		{
			Color color = new Color(this.animatable.buttonColor, false);
			red *= color.getRed()/255f;
			green *= color.getGreen()/255f;
			blue *= color.getBlue()/255f;

			renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			return true;
		}

		return super.boneRenderOverride(poseStack, bone, bufferSource, buffer, partialTick, packedLight, packedOverlay,
				red, green, blue, alpha);
	}

	@Override
	protected @Nullable ResourceLocation getTextureOverrideForBone(GeoBone bone, LargeButtonBlockEntity animatable,
																   float partialTick)
	{
		if(bone.getName().equals("ColoredLines"))
		{
			boolean active = animatable.getBlockState().getValue(LargeButtonBlock.PRESSED);
			int color = active ? animatable.activeColor : animatable.color;
			if(color == -1)
			{
				if(active)
					return new ResourceLocation(ApertureInnovations.MODID,
							"textures/block/large_button/large_button_lines_active.png");
				else return new ResourceLocation(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_lines_inactive.png");
			}
			else return new ResourceLocation(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_lines_generic.png");

		}
		if(bone.getName().equals("Button"))
		{
			int color = animatable.buttonColor;
			if(color != -1)
				return new ResourceLocation(ApertureInnovations.MODID,
						"textures/block/large_button/large_button_button_generic.png");

			return new ResourceLocation(ApertureInnovations.MODID,
					"textures/block/large_button/large_button_button.png");
		}
		return super.getTextureOverrideForBone(bone, animatable, partialTick);
	}

	@Override
	protected @Nullable RenderType getRenderTypeOverrideForBone(GeoBone bone, LargeButtonBlockEntity animatable,
																ResourceLocation texturePath,
																MultiBufferSource bufferSource, float partialTick)
	{
		if(bone.getName().equals("Button"))
			return GLOWING_RENDER_TYPE.apply(getTextureOverrideForBone(bone, animatable, partialTick), false);

		if(bone.getName().equals("ColoredLines"))
			return PortalRenderTypes.APERTURE_GLOW.apply(getTextureOverrideForBone(bone, animatable, partialTick));

		return super.getRenderTypeOverrideForBone(bone, animatable, texturePath, bufferSource, partialTick);
	}

	@Override
	public void preRender(PoseStack poseStack, LargeButtonBlockEntity animatable, BakedGeoModel model,
						  @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender,
						  float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha)
	{
		super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
				packedOverlay, red, green, blue, alpha);

		if(!isReRender)
			poseStack.translate(0.5, 0, 0.5);

		Direction facing = animatable.getBlockState().getValue(LargeButtonBlock.FACING);
		Direction normal = animatable.getBlockState().getValue(LargeButtonBlock.NORMAL);
		boolean positive = normal.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);
		boolean facingPos = facing.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);

		if(normal.getAxis().isVertical())
		{
			if(!positive)
			{
				poseStack.translate(0f, 1f, 0f);
				poseStack.mulPose(Axis.ZP.rotationDegrees(180));
				if(facing.getAxis().equals(Direction.Axis.X))
				{
					poseStack.translate(0f, 0f, 1f);
					poseStack.mulPose(Axis.YP.rotationDegrees(180));
				}
				else
					poseStack.translate(1f, 0f, 0f);
			}

			if(facing.getAxis().equals(Direction.Axis.X))
			{
				if(!facingPos)
					poseStack.translate(0f, 0f, 1f);
				else poseStack.translate(-1f, 0f, 0f);
			}
			else if(!facingPos)
				poseStack.translate(-1f, 0f, 1f);
		}
		if(normal.getAxis().isHorizontal())
		{
			if(normal.getAxis().equals(Direction.Axis.X))
			{
				poseStack.translate(positive ? -0.5f : 0.5f, 0.5f, positive ? 0f: 1f);

				poseStack.mulPose(Axis.ZP.rotationDegrees(normal.toYRot()));
				poseStack.mulPose(Axis.YP.rotationDegrees(positive ? 180 : 0));
			}
			if(normal.getAxis().equals(Direction.Axis.Z))
			{
				poseStack.translate(positive ? 0f : -1f, 0.5f, positive ? -0.5f : 0.5f);
				poseStack.mulPose(Axis.ZP.rotationDegrees(90));
				poseStack.mulPose(Axis.XP.rotationDegrees(positive ? 90 : -90));
			}

			if(facing.getAxis().isVertical())
			{
				boolean posFace = facing.getAxisDirection().equals(Direction.AxisDirection.POSITIVE);
				poseStack.mulPose(Axis.YP.rotationDegrees(posFace ? 0 :180));
			}
		}

		poseStack.mulPose(Axis.YP.rotationDegrees(animatable.getBlockState().getValue(LargeButtonBlock.FACING).toYRot()));
		if(facing.getAxis().equals(Direction.Axis.X))
			poseStack.mulPose(Axis.YP.rotationDegrees(180));

		poseStack.translate(-0.5, 0, -0.5);
	}

	@Override
	protected Direction getFacing(LargeButtonBlockEntity block)
	{
		return Direction.NORTH;
	}

	@Override
	public boolean shouldRenderOffScreen(LargeButtonBlockEntity pBlockEntity)
	{
		return true;
	}
}
