package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import software.bernie.geckolib.model.GeoModel;

public class WeightedStorageCubeModel extends GeoModel<WeightedStorageCubeEntity>
{
	private final ResourceLocation model = new ResourceLocation(ApertureInnovations.MODID,
			"geo/entity/weighted_cube.geo.json");
	private final ResourceLocation texture = new ResourceLocation(ApertureInnovations.MODID,
			"textures/entity/weighted_cube.png");

	@Override
	public ResourceLocation getModelResource(WeightedStorageCubeEntity animatable)
	{
		return model;
	}

	@Override
	public ResourceLocation getTextureResource(WeightedStorageCubeEntity animatable)
	{
		return texture;
	}

	@Override
	public ResourceLocation getAnimationResource(WeightedStorageCubeEntity animatable)
	{
		return null;
	}
}
