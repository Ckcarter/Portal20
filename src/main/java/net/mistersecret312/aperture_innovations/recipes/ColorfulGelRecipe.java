package net.mistersecret312.aperture_innovations.recipes;

import com.google.common.collect.Lists;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.RecipeInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;

import java.util.List;

public class ColorfulGelRecipe extends CustomRecipe
{

	public ColorfulGelRecipe(ResourceLocation pId, CraftingBookCategory pCategory)
	{
		super(pId, pCategory);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() instanceof ColorfulGelItem) {
					if (!itemstack.isEmpty()) {
						return false;
					}

					itemstack = itemstack1;
				} else {
					if (!(itemstack1.getItem() instanceof DyeItem)) {
						return false;
					}

					list.add(itemstack1);
				}
			}
		}

		return !itemstack.isEmpty();
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess)
	{
		List<DyeItem> list = Lists.newArrayList();
		ItemStack itemstack = ItemStack.EMPTY;

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				Item item = itemstack1.getItem();
				if (item instanceof ColorfulGelItem) {
					if (!itemstack.isEmpty()) {
						return ItemStack.EMPTY;
					}

					itemstack = itemstack1.copy();
				} else {
					if (!(item instanceof DyeItem)) {
						return ItemStack.EMPTY;
					}

					list.add((DyeItem)item);
				}
			}
		}

		return !itemstack.isEmpty() && !list.isEmpty() ? ColorfulGelItem.dyeGel(itemstack, list) :
					   new ItemStack(ItemInit.COLORFUL_GEL.get());
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeInit.GEL_COLORING.get();
	}
}
