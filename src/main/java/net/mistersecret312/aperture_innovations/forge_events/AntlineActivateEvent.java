package net.mistersecret312.aperture_innovations.forge_events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

public class AntlineActivateEvent extends Event
{
	private final Level level;
	private final BlockPos antlinePos;
	private final BlockPos activatedBlockPos;
	private final int signal;

	public AntlineActivateEvent(Level level, BlockPos antlinePos, BlockPos activatedBlockPos, int signal)
	{
		this.level = level;
		this.antlinePos = antlinePos;
		this.activatedBlockPos = activatedBlockPos;
		this.signal = signal;
	}

	public BlockPos getActivatedBlockPos()
	{
		return activatedBlockPos;
	}

	public BlockPos getAntlinePos()
	{
		return antlinePos;
	}

	public int getSignal()
	{
		return signal;
	}

	public Level getLevel()
	{
		return level;
	}
}
