package net.mistersecret312.aperture_innovations.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.capabilities.item.ItemEnergyProvider;
import net.mistersecret312.aperture_innovations.client.renderer.LongFallBootsRenderProperties;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class LongFallBootsItem extends ArmorItem
{
	public static final String ENERGY = "Energy";

	public LongFallBootsItem(ArmorMaterial material, Type type, Properties properties)
	{
		super(material, type, properties);
	}


	@Override
	public boolean isBarVisible(ItemStack stack)
	{
		return LongFallBootsConfig.long_fall_boots_use_energy.get() && getEnergy(stack) != getCapacity();
	}

	@Override
	public int getBarWidth(ItemStack stack)
	{
		return Math.round(13.0F * (float) getEnergy(stack) / getCapacity());
	}

	@Override
	public int getBarColor(ItemStack stack)
	{
		return new Color(0, 200, 255).getRGB();
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer)
	{
		consumer.accept(LongFallBootsRenderProperties.INSTANCE);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components,
								TooltipFlag flag)
	{
		if(LongFallBootsConfig.long_fall_boots_use_energy.get())
		{
			components.add(Component.translatable("item.aperture_innovations.long_fall_boots.energy").append(
					ApertureEnergy.energyToString(getEnergy(stack), getCapacity())).withStyle(ChatFormatting.DARK_RED));
		}
	}

	@Override
	public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		return ApertureInnovations.MODID + ":textures/models/armor/long_fall_boots.png";
	}

	public static long getEnergy(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();

		if(tag.contains(ENERGY, Tag.TAG_LONG))
			return tag.getLong(ENERGY);
		else tag.putLong(ENERGY, getCapacity());

		return 0;
	}

	public static long getCapacity()
	{
		return LongFallBootsConfig.long_fall_boots_max_energy_stored.get();
	}

	@Override
	public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag)
	{
		return new ItemEnergyProvider(stack)
		{
			@Override
			public long capacity()
			{
				return getCapacity();
			}

			@Override
			public long maxReceive()
			{
				return 1000L;
			}

			@Override
			public long maxExtract()
			{
				return 1000L;
			}
		};
	}
}
