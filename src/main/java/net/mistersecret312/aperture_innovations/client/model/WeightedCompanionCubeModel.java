package net.mistersecret312.aperture_innovations.client.model;

import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.entities.WeightedCompanionCubeEntity;
import software.bernie.geckolib.model.GeoModel;

public class WeightedCompanionCubeModel extends GeoModel<WeightedCompanionCubeEntity>
{
	private final ResourceLocation model = new ResourceLocation(ApertureInnovations.MODID,
			"geo/entity/weighted_cube.geo.json");
	private final ResourceLocation texture = new ResourceLocation(ApertureInnovations.MODID,
			"textures/entity/weighted_cube_companion.png");

	@Override
	public ResourceLocation getModelResource(WeightedCompanionCubeEntity animatable)
	{
		return model;
	}

	@Override
	public ResourceLocation getTextureResource(WeightedCompanionCubeEntity animatable)
	{
		return texture;
	}

	@Override
	public ResourceLocation getAnimationResource(WeightedCompanionCubeEntity animatable)
	{
		return null;
	}
}
