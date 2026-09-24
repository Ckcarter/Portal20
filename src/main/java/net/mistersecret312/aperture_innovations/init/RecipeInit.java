package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.recipes.ColorfulGelRecipe;
import net.mistersecret312.aperture_innovations.recipes.PortalGunColoringRecipe;

public class RecipeInit
{
	public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,
			ApertureInnovations.MODID);

	public static RegistryObject<RecipeSerializer<ColorfulGelRecipe>> GEL_COLORING = RECIPES.register("gel_coloring",
			() -> new SimpleCraftingRecipeSerializer<>(ColorfulGelRecipe::new));
	public static RegistryObject<RecipeSerializer<PortalGunColoringRecipe>> GUN_COLORING = RECIPES.register("gun_coloring",
			() -> new SimpleCraftingRecipeSerializer<>(PortalGunColoringRecipe::new));

	public static void register(IEventBus bus)
	{
		RECIPES.register(bus);
	}
}
