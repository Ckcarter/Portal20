package net.mistersecret312.aperture_innovations.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.data.portal.ClientPortalLink;

public class PortalSound<T extends ClientPortalLink> extends AbstractTickableSoundInstance
{
	protected T link;
	protected Vec3 portalPos;
	protected Minecraft minecraft = Minecraft.getInstance();
	protected int fullDistance;
	protected int maxDistance;

	public PortalSound(T link, boolean isPrimary, SoundEvent soundEvent, SoundSource source, int fullDistance, int maxDistance) {
		super(soundEvent, source, SoundInstance.createUnseededRandom());

		this.link = link;
		this.portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();
		if(portalPos == null && minecraft.player != null)
			portalPos = minecraft.player.position();
		this.relative = true;
		this.fullDistance = fullDistance;
		this.maxDistance = maxDistance;
	}

	@Override
	public void tick()
	{
		if(link == null || portalPos == null)
			this.stop();
	}

	@Override
	public boolean canStartSilent()
	{
		return true;
	}

	public void stopSound()
	{
		this.stop();
	}

	public double getDistanceFromSource()
	{
		LocalPlayer player = minecraft.player;
		Vec3 playerPos = player.position();
		return portalPos.distanceTo(playerPos);
	}

	public float getVolume()
	{
		float localVolume = 0.0F;
		double distanceFromSource = getDistanceFromSource();

		float fullDistance = this.fullDistance;
		float maxDistance = this.maxDistance;

		if(fullDistance >= maxDistance)
			maxDistance = fullDistance + 1;

		if(fullDistance >= maxDistance)
			maxDistance = fullDistance + 1;

		if(distanceFromSource <= fullDistance)
			localVolume = getMaxVolume();
		else if(distanceFromSource <= maxDistance)
			localVolume = (float) (getMaxVolume() - (distanceFromSource - fullDistance) / (maxDistance - fullDistance));
		else
			localVolume = getMinVolume();

		return super.getVolume() * localVolume;
	}

	public float getMaxVolume()
	{
		return 1.0F;
	}

	public float getMinVolume()
	{
		return 0.0F;
	}
}