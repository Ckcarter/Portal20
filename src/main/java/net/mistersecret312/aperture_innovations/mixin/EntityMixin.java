package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public class EntityMixin
{
	@Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
	public void noSuffocation(CallbackInfoReturnable<Boolean> cir)
	{
		Entity entity = (Entity) (Object) this;

		Pair<UUID, Boolean> pair = PortalUtilities.getClosestPortal(entity);
		UUID id = pair.getFirst();
		boolean isPrimary = pair.getSecond();
		if(id == null)
			return;

		Vec3 portalPos = PortalUtilities.getPortalPos(entity.level(), id, isPrimary);
		Vec2 portalRot = PortalUtilities.getPortalRotation(entity.level(), id, isPrimary);

		AABB box = PortalUtilities.getPortalBoundingBox(portalPos, portalRot.x, portalRot.y);
		if(box.intersects(entity.getBoundingBox()))
			cir.setReturnValue(false);
	}

	@Inject(method = "tick()V", at = @At("TAIL"))
	public void tickMixin(CallbackInfo ci)
	{
		Entity entity = (Entity) (Object) this;

		entity.getCapability(CapabilityInit.HOLD).ifPresent(cap -> cap.tick(entity.level(), entity));
	}
}
