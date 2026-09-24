package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.items.*;

public class ItemInit
{
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			ApertureInnovations.MODID);

	public static final RegistryObject<Item> PORTAL_GUN = ITEMS.register("portal_gun",
			() -> new PortalGunItem(new Item.Properties().stacksTo(1).fireResistant()));
	public static final RegistryObject<Item> COLORFUL_GEL = ITEMS.register("colorful_gel",
			() -> new ColorfulGelItem(new Item.Properties().stacksTo(64)));

	public static final RegistryObject<Item> LONG_FALL_BOOTS = ITEMS.register("long_fall_boots",
			() -> new LongFallBootsItem(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1).fireResistant()));

	public static final RegistryObject<CubeItem> WEIGHTED_STORAGE_CUBE = ITEMS.register("weighted_storage_cube",
			() -> new CubeItem(new Item.Properties().stacksTo(64)));
	public static final RegistryObject<CompanionCubeItem> WEIGHTED_COMPANION_CUBE = ITEMS.register("weighted_companion_cube",
			() -> new CompanionCubeItem(new Item.Properties().stacksTo(64)));



	public static final RegistryObject<Item> SENTRY_TURRET_SPAWN_EGG = ITEMS.register("sentry_turret_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityInit.SENTRY_TURRET, 0xE8E8E8, 0xD82020, new Item.Properties()));

	public static final RegistryObject<Item> DIFFERENT_TURRET_SPAWN_EGG = ITEMS.register("different_turret_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityInit.DIFFERENT_TURRET, 0xE8E8E8, 0xFFF2A8, new Item.Properties()));



	public static void register(IEventBus bus)
	{
		ITEMS.register(bus);
	}
}
