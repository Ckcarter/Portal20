package net.mistersecret312.aperture_innovations.items;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ColorfulGelItemProperty implements ItemPropertyFunction
{

	@Override
	public float call(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed)
	{
		ColorfulGelItem item = (ColorfulGelItem) stack.getItem();
		return item.getColor(stack) != -1 ? 1 : 0;
	}
}
