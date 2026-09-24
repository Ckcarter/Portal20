package net.mistersecret312.aperture_innovations.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class PortalGunConfig
{
	public static ForgeConfigSpec.BooleanValue use_portalable_tag;
	public static ForgeConfigSpec.IntValue portal_gun_shoot_range;

	public static ForgeConfigSpec.BooleanValue portal_gun_uses_energy;
	public static ForgeConfigSpec.LongValue portal_gun_max_energy_stored;
	public static ForgeConfigSpec.LongValue portal_gun_shoot_consumption;
	public static ForgeConfigSpec.LongValue portal_gun_passive_consumption;
	public static ForgeConfigSpec.LongValue portal_gun_interdimensional_passive_consumption;
	public static ForgeConfigSpec.BooleanValue portal_gun_consume_on_shot;

	public static void init(ForgeConfigSpec.Builder server)
	{
		use_portalable_tag = server
			.comment("If true, portals can be placed only on blocks in the Portalable block tag")
			.define("portalable_tag", false);

		portal_gun_shoot_range = server
			.comment("How far the Portal Gun can shoot and open a portal")
			.defineInRange("portal_gun_shoot_range", 256, 0, Integer.MAX_VALUE);

		portal_gun_uses_energy = server
			.comment("If true, the Portal Gun will require energy to create and upkeep portals")
			.define("portal_gun_uses_energy", false);

		portal_gun_max_energy_stored = server
			.comment("The maximum amount of energy the Portal Gun can store in itself")
			.defineInRange("portal_gun_max_energy_stored", 1000000L, 0L, Long.MAX_VALUE);

		portal_gun_consume_on_shot = server
			.comment("If true, the Portal Gun will consume energy when shooting the portal, not when it actually opens")
			.define("portal_gun_consume_on_shot", false);

		portal_gun_shoot_consumption = server
			.comment("The amount of energy the Portal Gun uses when shooting/opening the portal")
			.defineInRange("portal_gun_shoot_consumption", 2500L, 0L, Long.MAX_VALUE);

		portal_gun_passive_consumption = server
			.comment("The amount of energy the Portal Gun uses when two portals are open")
			.defineInRange("portal_gun_passive_consumption", 10, 0L, Long.MAX_VALUE);

		portal_gun_interdimensional_passive_consumption = server
			.comment("The amount of energy the Portal Gun uses when two portals are open")
			.defineInRange("portal_gun_interdimensional_passive_consumption", 100, 0L, Long.MAX_VALUE);
	}
}
