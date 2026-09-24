package net.mistersecret312.aperture_innovations.forge_events;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.mistersecret312.aperture_innovations.data.portal.Portal;
import net.mistersecret312.aperture_innovations.data.portal.PortalLink;

public abstract class PortalTravelEvent extends Event
{
	private final PortalLink link;
	private final Portal entrancePortal;
	private final boolean isPrimary;

	private final Level level;
	private final Level targetLevel;

	private final Vec3 pos;
	private final Vec3 targetPos;

	private final boolean moonshot;

	protected PortalTravelEvent(PortalLink link, Portal portal, boolean isPrimary,
								Level level, Level targetLevel,
								Vec3 pos, Vec3 targetPos, boolean moonshot)
	{
		this.link = link;
		this.entrancePortal = portal;
		this.isPrimary = isPrimary;

		this.level = level;
		this.targetLevel = targetLevel;

		this.pos = pos;
		this.targetPos = targetPos;

		this.moonshot = moonshot;
	}

	public Level getEntranceLevel()
	{
		return level;
	}

	public Level getTargetLevel()
	{
		return targetLevel;
	}

	public Vec3 getEntrancePos()
	{
		return pos;
	}

	public Vec3 getTargetPos()
	{
		return targetPos;
	}

	public PortalLink getTargetLink()
	{
		return link;
	}

	public Portal getEntrancePortal()
	{
		return entrancePortal;
	}

	public boolean isEntrancePortalPrimary()
	{
		return isPrimary;
	}

	public boolean isTravellingToTheMoon()
	{
		return moonshot;
	}

	@net.minecraftforge.eventbus.api.Cancelable
	public static class Pre extends PortalTravelEvent
	{
		public Pre(PortalLink link, Portal portal, boolean isPrimary,
				   Level level, Level targetLevel,
				   Vec3 pos, Vec3 targetPos, boolean moonshot)
		{
			super(link, portal, isPrimary,
					level, targetLevel,
					pos, targetPos,
					moonshot);
		}
	}

	public static class Post extends PortalTravelEvent
	{
		public Post(PortalLink link, Portal portal, boolean isPrimary,
				   Level level, Level targetLevel,
				   Vec3 pos, Vec3 targetPos, boolean moonshot)
		{
			super(link, portal, isPrimary,
					level, targetLevel,
					pos, targetPos,
					moonshot);
		}
	}
}
