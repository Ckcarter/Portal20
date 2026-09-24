package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.block_entities.PedestalButtonBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class PedestalButtonModel extends GeoModel<PedestalButtonBlockEntity>
{

	@Override
	public ResourceLocation getModelResource(PedestalButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "geo/block/pedestal_button.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(PedestalButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "textures/block/pedestal_button/pedestal_button.png");
	}

	@Override
	public ResourceLocation getAnimationResource(PedestalButtonBlockEntity animatable)
	{
		return new ResourceLocation(ApertureInnovations.MODID, "animations/block/pedestal_button.animation.json");
	}
}
