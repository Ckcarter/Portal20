package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundTeleportMomentumPacket
{
	public Vec3 speed;

	public ClientboundTeleportMomentumPacket(Vec3 speed)
	{
		this.speed = speed;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeDouble(speed.x);
		buffer.writeDouble(speed.y);
		buffer.writeDouble(speed.z);
	}

	public static ClientboundTeleportMomentumPacket decode(FriendlyByteBuf buffer)
	{
		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();

		Vec3 speed = new Vec3(x,y,z);

		return new ClientboundTeleportMomentumPacket(speed);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handleTeleportMomentumPacket(this.speed);
			});
		return true;
	}
}
