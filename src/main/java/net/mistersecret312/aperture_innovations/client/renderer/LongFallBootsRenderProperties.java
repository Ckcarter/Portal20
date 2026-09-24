package net.mistersecret312.aperture_innovations.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.mistersecret312.aperture_innovations.client.model.LongFallBootsModel;
import org.jetbrains.annotations.Nullable;

public class LongFallBootsRenderProperties implements IClientItemExtensions
{
	public static final LongFallBootsRenderProperties INSTANCE = new LongFallBootsRenderProperties();

	private LongFallBootsRenderProperties()
	{

	}

	@Override
	public @Nullable HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> defaultModel)
	{
		return LongFallBootsModel.INSTANCE;
	}
}
