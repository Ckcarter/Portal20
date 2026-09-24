package net.mistersecret312.aperture_innovations.init;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.capabilities.HoldEntityCapability;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityInit
{
    public static final Capability<ApertureCapability> APERTURE = CapabilityManager.get(
            new CapabilityToken<ApertureCapability>() {});

    public static final Capability<HoldEntityCapability> HOLD = CapabilityManager.get(
            new CapabilityToken<HoldEntityCapability>() {});


    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(ApertureCapability.class);
    }
}
