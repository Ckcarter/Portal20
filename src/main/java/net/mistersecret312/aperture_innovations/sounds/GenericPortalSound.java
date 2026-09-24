package net.mistersecret312.aperture_innovations.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;

public class GenericPortalSound extends PortalSound<ClientPortalLink>
{

	public GenericPortalSound(ClientPortalLink link, boolean isPrimary, SoundEvent soundEvent, int fullDistance,
							  int maxDistance, float maxVolume)
	{
		super(link, isPrimary, soundEvent, SoundSource.PLAYERS, fullDistance, maxDistance);
		this.volume =maxVolume;
	}

	@Override
	public boolean isLooping()
	{
		return false;
	}

	@Override
	public float getMaxVolume()
	{
		return this.volume;
	}
}
