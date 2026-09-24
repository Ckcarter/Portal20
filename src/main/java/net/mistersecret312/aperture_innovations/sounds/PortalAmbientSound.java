package net.mistersecret312.aperture_innovations.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;

public class PortalAmbientSound extends PortalSound<ClientPortalLink>
{
	private static final float VOLUME_MIN = 0.0F;
	private static final float VOLUME_MAX = 0.2F;

	public PortalAmbientSound(ClientPortalLink fountain, boolean isPrimary, SoundEvent soundEvent) {
		super(fountain, isPrimary, soundEvent, SoundSource.AMBIENT,2, 4);
		this.looping = true;
		this.volume = VOLUME_MIN;
	}

	@Override
	public void tick()
	{
		if(getDistanceFromSource() <= 4)
			fadeIn();
		else
			fadeOut();

		super.tick();
	}

	@Override
	public boolean canStartSilent()
	{
		return true;
	}

	private void fadeIn()
	{
		if(this.volume < VOLUME_MAX)
			this.volume += 0.01F;
	}

	private void fadeOut()
	{
		if(this.volume > VOLUME_MIN)
			this.volume -= 0.01F;
	}
}
