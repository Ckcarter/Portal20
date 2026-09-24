package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

import java.util.ArrayList;
import java.util.List;

public class StatisticsInit
{
	public static final DeferredRegister<ResourceLocation> STATISTICS = DeferredRegister.create(Registries.CUSTOM_STAT,
			ApertureInnovations.MODID);
	private static final List<Runnable> STATISTIC_SETUP = new ArrayList<>();

	public static final RegistryObject<ResourceLocation> TIMES_USED_PORTALS =
			STATISTICS.register("times_used_portals", () -> registerDefaultStatistic("times_used_portals"));

	private static ResourceLocation registerDefaultStatistic(String key)
	{
		ResourceLocation resourceLocation = new ResourceLocation(ApertureInnovations.MODID, key);
		STATISTIC_SETUP.add(() -> Stats.CUSTOM.get(resourceLocation, StatFormatter.DEFAULT));
		return resourceLocation;
	}

	private static ResourceLocation registerDistanceStatistic(String key)
	{
		ResourceLocation resourceLocation = new ResourceLocation(ApertureInnovations.MODID, key);
		STATISTIC_SETUP.add(() -> Stats.CUSTOM.get(resourceLocation, StatFormatter.DISTANCE));
		return resourceLocation;
	}

	public static void register()
	{
		STATISTIC_SETUP.forEach(Runnable::run);
	}

	public static void register(IEventBus bus)
	{
		STATISTICS.register(bus);
	}
}
