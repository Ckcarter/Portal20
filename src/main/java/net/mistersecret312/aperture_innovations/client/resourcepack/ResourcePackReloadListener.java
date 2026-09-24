package net.mistersecret312.aperture_innovations.client.resourcepack;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;

import java.util.Map;

public class ResourcePackReloadListener
{
	public static final String PATH = ApertureInnovations.MODID;

	public static final String GUN_VARIANT = "portal_gun_variant";
	private static Minecraft minecraft = Minecraft.getInstance();

	@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ReloadListener extends SimpleJsonResourceReloadListener
	{
		public ReloadListener()
		{
			super(new GsonBuilder().create(), PATH);
		}

		@Override
		protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler)
		{
			ClientPortalGunVariants.clear();

			ClientPacketListener clientPacketListener = minecraft.getConnection();

			if(clientPacketListener != null)
			{
				RegistryAccess registries = clientPacketListener.registryAccess();
				Registry<PortalGunVariant> variantRegistry = registries.registryOrThrow(PortalGunVariant.REGISTRY_KEY);

				for(Map.Entry<ResourceKey<PortalGunVariant>, PortalGunVariant> stargateVariantEntry : variantRegistry.entrySet())
				{
					stargateVariantEntry.getValue().resetMissing();
				}
			}

			for(Map.Entry<ResourceLocation, JsonElement> jsonEntry : jsonMap.entrySet())
			{
				ResourceLocation location = jsonEntry.getKey();
				JsonElement element = jsonEntry.getValue();

				if(canShortenPath(location, GUN_VARIANT))
				{
					location = shortenPath(location, GUN_VARIANT);
					addPortalGunVariant(location, element);
				}
			}
		}

		private static void addPortalGunVariant(ResourceLocation location, JsonElement element)
		{
			try
			{
				JsonObject json = GsonHelper.convertToJsonObject(element, GUN_VARIANT);
				ClientPortalGunVariant stargateVariant = ClientPortalGunVariant.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, msg -> new DecoderException("Failed to parse Portal Gun Variant "+ msg));

				ClientPortalGunVariants.addPortalGunVariant(location, stargateVariant);
			}
			catch(RuntimeException e)
			{
				ApertureInnovations.LOGGER.error("Could not load Portal Gun Variant: " + location.toString());
				ApertureInnovations.LOGGER.error(e.getMessage());
			}
		}



		@SubscribeEvent
		public static void registerReloadListener(RegisterClientReloadListenersEvent event)
		{
			event.registerReloadListener(new ReloadListener());
		}

		private static boolean canShortenPath(ResourceLocation location, String shortenBy)
		{
			return location.getPath().startsWith(shortenBy) && location.getPath().length() > shortenBy.length(); // If it starts with the string and isn't empty after getting shortened
		}

		private static ResourceLocation shortenPath(ResourceLocation location, String shortenBy)
		{
			return location.withPath(location.getPath().substring(shortenBy.length() + 1)); // Magical 1 because there's also the / symbol
		}
	}
}
