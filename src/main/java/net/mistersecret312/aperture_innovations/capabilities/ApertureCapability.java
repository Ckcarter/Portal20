package net.mistersecret312.aperture_innovations.capabilities;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientboundApertureCapabilityPacket;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.UUID;

public class ApertureCapability implements INBTSerializable<CompoundTag>
{
	public Vector3d distanceVec = new Vector3d();
	public Pair<UUID, Boolean> portal;

	public double horizontalDistance;
	public double verticalDistance;

	public int frictionlessTime = 0;
	public int ignorePortalsTime = 0;

	public void tick(Level level, Entity entity)
	{
		if(level.isClientSide())
			return;

		if(entity instanceof ServerPlayer player)
			NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundApertureCapabilityPacket(frictionlessTime));

		if(frictionlessTime > 0)
			frictionlessTime--;
		if(ignorePortalsTime > 0)
			ignorePortalsTime--;

		if(entity.onGround() || (entity instanceof LivingEntity && ((LivingEntity) entity).isFallFlying()))
		{
			portal = null;
			this.distanceVec = new Vector3d();
			this.horizontalDistance = 0;
			this.verticalDistance = 0;
			this.frictionlessTime = 0;
		}
		else if(portal != null)
		{
			Vec3 speed = entity.getDeltaMovement();
			Vector3f movementVector = new Vector3f(speed.x > 0 ? (float) speed.x : (float) -speed.x,
					speed.y > 0 ? (float) speed.y : (float) -speed.y, speed.z > 0 ? (float) speed.z : (float) -speed.z);
			this.distanceVec.add(movementVector);
		}
	}

	public void updateDistance()
	{
		if(portal == null || this.distanceVec == null)
			return;

		if(distanceVec.lengthSquared() > 0)
		{
			horizontalDistance += Mth.sqrt((float) (Math.pow(distanceVec.x, 2) + Math.pow(distanceVec.z, 2)));
			verticalDistance += Math.sqrt(Math.pow(distanceVec.y, 2));
		}

		distanceVec = new Vector3d();
	}

	public void setFrictionlessTime(int frictionlessTime)
	{
		this.frictionlessTime = frictionlessTime;
	}

	public void setIgnorePortalsTime(int ignorePortalsTime)
	{
		this.ignorePortalsTime = ignorePortalsTime;
	}

	public void setPortal(Pair<UUID, Boolean> portal)
	{
		this.portal = portal;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();

		CompoundTag distanceTag = new CompoundTag();
		distanceTag.putDouble("x", distanceVec.x);
		distanceTag.putDouble("y", distanceVec.y);
		distanceTag.putDouble("z", distanceVec.z);

		tag.put("distance", distanceTag);
		tag.putDouble("horizontal", horizontalDistance);
		tag.putDouble("vertical", verticalDistance);

		tag.putInt("frictionlessTime", frictionlessTime);
		tag.putInt("ignorePortalsTime", ignorePortalsTime);

		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		Vector3d distanceVec = new Vector3d();

		CompoundTag distanceTag = nbt.getCompound("distance");
		distanceVec.x = distanceTag.getDouble("x");
		distanceVec.y = distanceTag.getDouble("y");
		distanceVec.z = distanceTag.getDouble("z");

		this.distanceVec = distanceVec;

		this.horizontalDistance = nbt.getDouble("horizontal");
		this.verticalDistance = nbt.getDouble("vertical");

		this.frictionlessTime = nbt.getInt("frictionlessTime");
		this.ignorePortalsTime = nbt.getInt("ignorePortalsTime");
	}
}
