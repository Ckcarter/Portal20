package net.mistersecret312.aperture_innovations.recipes;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.RecipeInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import oshi.util.tuples.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PortalGunColoringRecipe extends CustomRecipe
{

	public PortalGunColoringRecipe(ResourceLocation pId, CraftingBookCategory pCategory)
	{
		super(pId, pCategory);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		ItemStack itemStack2 = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() instanceof PortalGunItem) {
					if (!itemstack.isEmpty())
					{
						if(!itemStack2.isEmpty())
							return false;
						itemStack2 = itemstack1;
					}

					itemstack = itemstack1;
				} else {
					if (!(itemstack1.getItem() instanceof ColorfulGelItem)) {
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
		List<ItemStack> list = Lists.newArrayList();
		ItemStack gunItemStack = ItemStack.EMPTY;
		ItemStack secondGunStack = ItemStack.EMPTY;
		int gunStack = -1;

		for(int i = 0; i < container.getContainerSize(); ++i) {
			ItemStack itemstack1 = container.getItem(i);
			if (!itemstack1.isEmpty()) {
				Item item = itemstack1.getItem();
				if (item instanceof PortalGunItem)
				{
					if (!gunItemStack.isEmpty())
					{
						if(!secondGunStack.isEmpty())
							return ItemStack.EMPTY;
						secondGunStack = itemstack1.copy();
						continue;
					}

					gunItemStack = itemstack1.copy();
					gunStack = i;
				} else {
					if (!(item instanceof ColorfulGelItem))
						return ItemStack.EMPTY;

					list.add(itemstack1);
				}
			}
		}

		PortalGunItem gunItem = (PortalGunItem) gunItemStack.getItem();

		for(int i = 0; i < list.size(); i++)
		{
			ItemStack gelStack = list.get(i);
			ColorfulGelItem gel = (ColorfulGelItem) gelStack.getItem();

			if(i == 0)
				gunItem.setPrimaryStripeColor(gunItemStack, gel.getColor(gelStack));
			if(i == 1)
				gunItem.setSecondaryStripeColor(gunItemStack, gel.getColor(gelStack));
			if(i == 2)
				gunItem.setPrimaryPortalColor(gunItemStack, gel.getColor(gelStack));
			if(i == 3)
				gunItem.setSecondaryPortalColor(gunItemStack, gel.getColor(gelStack));
		}

		if(list.isEmpty())
		{
			int dualityState = gunItem.getDualityState(gunItemStack);
			dualityState++;
			dualityState %= 3;

			if(gunItem.getPair(gunItemStack) == null && secondGunStack.isEmpty())
				gunItem.setDualityState(gunItemStack, dualityState);

			gunItem.setPair(gunItemStack, null);
		}

		return gunItemStack;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer pContainer)
	{
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(pContainer.getContainerSize(), ItemStack.EMPTY);
		ItemStack portalGunStack = ItemStack.EMPTY;

		for(int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack item = pContainer.getItem(i);
			if(item.getItem() instanceof PortalGunItem gun)
			{
				if(portalGunStack.isEmpty())
				{
					portalGunStack = item;
					continue;
				}
				UUID pairID = gun.getUUID(portalGunStack, true);
				gun.setPair(item, pairID);
				nonnulllist.set(i, item.copy());
				continue;
			}
			if (item.hasCraftingRemainingItem()) {
				nonnulllist.set(i, item.getCraftingRemainingItem());
			}
		}

		return nonnulllist;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeInit.GUN_COLORING.get();
	}
}
