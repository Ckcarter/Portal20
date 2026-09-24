package net.mistersecret312.aperture_innovations.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
	@ModifyExpressionValue(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
	at = @At(value = "CONSTANT", args = "floatValue=0.91"))
	public float dontAir(float original)
	{
		LivingEntity living = (LivingEntity) (Object) this;
		Optional<ApertureCapability> capabilityOptional = living.getCapability(CapabilityInit.APERTURE).resolve();
		if(capabilityOptional.isPresent())
		{
			ApertureCapability capability = capabilityOptional.get();
			if(capability.frictionlessTime > 0)
				return 0.95F;
		}
		return original;
	}

	@ModifyArg(method = "travel(Lnet/minecraft/world/phys/Vec3;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;handleRelativeFrictionAndCalculateMovement(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
	private float modifyFrictionValue(float friction)
	{
		LivingEntity living = (LivingEntity) (Object) this;
		Optional<ApertureCapability> capabilityOptional = living.getCapability(CapabilityInit.APERTURE).resolve();
		if(capabilityOptional.isPresent())
		{
			ApertureCapability capability = capabilityOptional.get();
			if(capability.frictionlessTime > 0)
				return 0.98F;
		}
		return friction;
	}
}
