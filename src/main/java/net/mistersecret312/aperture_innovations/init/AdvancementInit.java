package net.mistersecret312.aperture_innovations.init;

import net.minecraft.advancements.CriteriaTriggers;
import net.mistersecret312.aperture_innovations.advancements.NearPortalDeathCriterion;
import net.mistersecret312.aperture_innovations.advancements.PortalTravelCriterion;
import net.mistersecret312.aperture_innovations.advancements.ThrownIntoFluidCriterion;

public class AdvancementInit
{
	public static void register()
	{
		CriteriaTriggers.register(PortalTravelCriterion.INSTANCE);
		CriteriaTriggers.register(ThrownIntoFluidCriterion.INSTANCE);
		CriteriaTriggers.register(NearPortalDeathCriterion.INSTANCE);
	}
}
