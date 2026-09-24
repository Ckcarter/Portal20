package net.mistersecret312.aperture_innovations.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class LongFallBootsConfig
{
	public static ForgeConfigSpec.BooleanValue long_fall_boots_use_energy;
	public static ForgeConfigSpec.LongValue long_fall_boots_max_energy_stored;
	public static ForgeConfigSpec.LongValue fall_energy_consumption;

	public static void init(ForgeConfigSpec.Builder server)
	{
		long_fall_boots_use_energy = server
			.comment("If true, the Long Fall Boots will require energy to dampen fall/impact damage")
			.define("long_fall_boots_use_energy", false);

		long_fall_boots_max_energy_stored = server
			.comment("The maximum amount of energy the Long Fall Boots can store in itself")
			.defineInRange("long_fall_boots_max_energy_stored", 128000L, 0L, Long.MAX_VALUE);

		fall_energy_consumption = server
			.comment("The amount of energy the Long Fall Boots use when dampening the damage")
			.defineInRange("fall_energy_consumption", 250L, 0L, Long.MAX_VALUE);
	}
}
