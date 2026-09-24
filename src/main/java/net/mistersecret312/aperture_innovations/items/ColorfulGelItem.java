package net.mistersecret312.aperture_innovations.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ColorfulGelItem extends Item
{

	public ColorfulGelItem(Properties pProperties)
	{
		super(pProperties);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components,
								TooltipFlag flag)
	{
		components.add(Component.translatable("item.aperture_innovations.colorful_gel.tooltip").withStyle(ChatFormatting.DARK_PURPLE));

		int gelColor = getColor(stack);
		if(gelColor != -1)
			components.add(Component.translatable("item.aperture_innovations.colorful_gel.color", Integer.toHexString(gelColor).toUpperCase()).withStyle(style -> style.withColor(gelColor)));
	}

	@Override
	public int getMaxStackSize(ItemStack stack)
	{
		return getColor(stack) != -1 ? 16 : 64;
	}

	public void setColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("color", color);
	}

	public int getColor(ItemStack stack)
	{
		if(stack.getTag() != null && stack.getTag().contains("color"))
		{
			return stack.getTag().getInt("color");
		}

		return -1;
	}

	public static ItemStack dyeGel(ItemStack pStack, List<DyeItem> pDyes) {
		ItemStack itemstack = ItemStack.EMPTY;
		int[] aint = new int[3];
		int i = 0;
		int j = 0;
		Item item = pStack.getItem();
		ColorfulGelItem gel = null;
		if (item instanceof ColorfulGelItem) {
			gel = (ColorfulGelItem) item;
			itemstack = pStack.copyWithCount(1);
			if (gel.getColor(pStack) != -1) {
				int k = gel.getColor(itemstack);
				float f = (float)(k >> 16 & 255) / 255.0F;
				float f1 = (float)(k >> 8 & 255) / 255.0F;
				float f2 = (float)(k & 255) / 255.0F;
				i += (int)(Math.max(f, Math.max(f1, f2)) * 255.0F);
				aint[0] += (int)(f * 255.0F);
				aint[1] += (int)(f1 * 255.0F);
				aint[2] += (int)(f2 * 255.0F);
				++j;
			}

			for(DyeItem dyeitem : pDyes) {
				float[] afloat = dyeitem.getDyeColor().getTextureDiffuseColors();
				int i2 = (int)(afloat[0] * 255.0F);
				int l = (int)(afloat[1] * 255.0F);
				int i1 = (int)(afloat[2] * 255.0F);
				i += Math.max(i2, Math.max(l, i1));
				aint[0] += i2;
				aint[1] += l;
				aint[2] += i1;
				++j;
			}
		}

		if (gel == null) {
			return ItemStack.EMPTY;
		} else {
			int j1 = aint[0] / j;
			int k1 = aint[1] / j;
			int l1 = aint[2] / j;
			float f3 = (float)i / (float)j;
			float f4 = (float)Math.max(j1, Math.max(k1, l1));
			j1 = (int)((float)j1 * f3 / f4);
			k1 = (int)((float)k1 * f3 / f4);
			l1 = (int)((float)l1 * f3 / f4);
			int j2 = (j1 << 8) + k1;
			j2 = (j2 << 8) + l1;
			gel.setColor(itemstack, j2);
			return itemstack;
		}
	}
}
