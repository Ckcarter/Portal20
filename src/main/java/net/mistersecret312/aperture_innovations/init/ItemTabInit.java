package net.mistersecret312.aperture_innovations.init;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;

public class ItemTabInit
{
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
			ApertureInnovations.MODID);

	public static final RegistryObject<CreativeModeTab> APERTURE_INNOVATIONS = TABS.register("aperture_innovations",
			() -> CreativeModeTab.builder()
						  .icon(() -> new ItemStack(ItemInit.PORTAL_GUN.get()))
						  .title(Component.translatable("tabs.aperture_innovations"))
						  .displayItems((parameters, output) ->
							  {
								  output.accept(PortalGunItem.createPortalGun(
										  new ResourceLocation(ApertureInnovations.MODID, "chell")));
								  output.accept(PortalGunItem.createPortalGun(
										  new ResourceLocation(ApertureInnovations.MODID, "atlas")));
								  output.accept(PortalGunItem.createPortalGun(
										  new ResourceLocation(ApertureInnovations.MODID, "pbody")));
								  output.accept(PortalGunItem.createPortalGun(
										  new ResourceLocation(ApertureInnovations.MODID,
												  "reloaded")));

								  output.accept(ItemInit.LONG_FALL_BOOTS.get());

								  output.accept(ItemInit.COLORFUL_GEL.get());

								  output.accept(BlockInit.CONCRETE_SURFACE_BLOCK.get());
								  output.accept(BlockInit.CONCRETE_SURFACE_TILE_BLOCK.get());
								  output.accept(BlockInit.CONCRETE_SURFACE_1x2_BLOCK.get());

								  output.accept(BlockInit.METAL_SURFACE_BLOCK.get());
								  output.accept(BlockInit.METAL_SURFACE_TILE_BLOCK.get());
								  output.accept(BlockInit.METAL_SURFACE_1x2_BLOCK.get());

								  output.accept(BlockInit.ANTLINE.get());
								  output.accept(BlockInit.CHECKMARK.get());
								  output.accept(BlockInit.TIMER.get());

								  output.accept(ItemInit.WEIGHTED_STORAGE_CUBE.get());
								  output.accept(ItemInit.WEIGHTED_COMPANION_CUBE.get());
								  output.accept(ItemInit.SENTRY_TURRET_SPAWN_EGG.get());
								  output.accept(ItemInit.DIFFERENT_TURRET_SPAWN_EGG.get());


								  output.accept(BlockInit.PEDESTAL_BUTTON.get());
								  output.accept(BlockInit.LARGE_BUTTON.get());
							  }
						  ).build());

	public static void register(IEventBus bus)
	{
		TABS.register(bus);
	}
}
