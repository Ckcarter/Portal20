package net.mistersecret312.aperture_innovations.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.client.model.LongFallBootsModel;

public class Layers
{
	// Armor
	public static final ModelLayerLocation LONG_FALL_BOOTS = new ModelLayerLocation(new ResourceLocation(
			ApertureInnovations.MODID, "long_fall_boots"), "main");


	public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
	{
		event.registerLayerDefinition(LONG_FALL_BOOTS, LongFallBootsModel::createBodyLayer);
	}
}
