package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class LargeButtonModel extends GeoModel<LargeButtonBlockEntity>
{

	@Override
	public ResourceLocation getModelResource(LargeButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "geo/block/large_button.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(LargeButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "textures/block/large_button/large_button.png");
	}

	@Override
	public ResourceLocation getAnimationResource(LargeButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "animations/block/large_button.animation.json");
	}
}
