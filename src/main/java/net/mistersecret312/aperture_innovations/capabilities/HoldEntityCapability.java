package net.mistersecret312.aperture_innovations.capabilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityHeldUpdatePacket;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityPortalLerpPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;

import java.util.ArrayList;
import java.util.List;

public class HoldEntityCapability implements INBTSerializable<CompoundTag>
{
	public boolean isHeld = false;

	public void tick(Level level, Entity entity)
	{
		if(!isHeld)
			return;

		Player player = findHoldingPlayer(level, entity);
		if(player == null)
		{
			this.setHeld(entity, false);
			entity.setNoGravity(false);
			return;
		}

		entity.setNoGravity(true);
		entity.resetFallDistance();

		Vec3 targetPos = player.getEyePosition().add(player.getViewVector(1F).multiply(3f, 3f, 3f));

		BlockHitResult result = level.clip(new ClipContext(player.getEyePosition(), targetPos, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, entity));
		targetPos = result.getLocation();
		Vec3 offset = entity.getBoundingBox().getCenter().vectorTo(targetPos);

		entity.setOldPosAndRot();
		entity.setYRot(-player.getYRot());
		entity.setDeltaMovement(offset);

		if(!level.isClientSide() && !(entity instanceof LivingEntity))
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new ClientboundEntityPortalLerpPacket(entity.getId(),
					entity.position().toVector3f(), offset.toVector3f(), entity.getXRot(), entity.getYRot()));

		}
	}

	public Player findHoldingPlayer(Level level, Entity entity)
	{
		AABB box = new AABB(entity.blockPosition()).inflate(4);
		List<Player> players = new ArrayList<>();
		for(Player player : level.players())
		{
			if(box.contains(player.position()))
				players.add(player);
		}

		Player holdingPlayer = null;
		for(Player player : players)
		{
			ItemStack main = player.getMainHandItem();
			ItemStack off = player.getOffhandItem();
			boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
			if(!hasPortalGun)
				continue;

			ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
			PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

			Integer id = portalGun.getHeldEntity(gunStack);
			if(id == null)
				continue;

			if(id.equals(entity.getId()))
			{
				holdingPlayer = player;
				break;
			}
		}

		return holdingPlayer;
	}

	public void setHeld(Entity entity, boolean held)
	{
		if(!entity.level().isClientSide())
		{
			this.isHeld = held;
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
					new ClientboundEntityHeldUpdatePacket(entity.getId(), held));
		}
		else this.isHeld = held;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		tag.putBoolean("isHeld", this.isHeld);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		this.isHeld = nbt.getBoolean("isHeld");
	}
}
