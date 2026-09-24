package net.mistersecret312.aperture_innovations.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

public class PortalGunVariant
{
	public static final ResourceLocation PORTAL_GUN_VARIANT_LOCATION = new ResourceLocation(
			ApertureInnovations.MODID, "portal_gun_variant");
	public static final ResourceKey<Registry<PortalGunVariant>> REGISTRY_KEY = ResourceKey.createRegistryKey(
			PORTAL_GUN_VARIANT_LOCATION);
	public static final Codec<ResourceKey<PortalGunVariant>> RESOURCE_KEY_CODEC = ResourceKey.codec(REGISTRY_KEY);

	public static final Codec<PortalGunVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("client_variant").forGetter(PortalGunVariant::getClientVariant)
	).apply(instance, PortalGunVariant::new));

	private final ResourceLocation clientVariant;

	private boolean isFound = false;
	private boolean isMissing = false;

	public PortalGunVariant(ResourceLocation clientVariant)
	{
		this.clientVariant = clientVariant;
	}

	public ResourceLocation getClientVariant()
	{
		return clientVariant;
	}

	public boolean isFound()
	{
		return isFound;
	}

	public boolean isMissing()
	{
		return isMissing;
	}

	public void handleLocation(boolean isLocated)
	{
		if(isLocated) this.isFound = true;
		else if(!isMissing)
		{
			isMissing = true;
			ApertureInnovations.LOGGER.error(
					"Could not locate Portal Gun variant [" + getClientVariant().getNamespace() + ':' + getClientVariant().getPath() + ']');
		}
	}

	public void resetMissing()
	{
		isFound = false;
		isMissing = false;
	}
}
