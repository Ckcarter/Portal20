package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundApertureCapabilityPacket
{
	public int frictionlessTime;

	public ClientboundApertureCapabilityPacket(int frictionlessTime)
	{
		this.frictionlessTime = frictionlessTime;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(this.frictionlessTime);
	}

	public static ClientboundApertureCapabilityPacket decode(FriendlyByteBuf buffer)
	{
		return new ClientboundApertureCapabilityPacket(buffer.readInt());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handlePlayerCapabilityPacket(this.frictionlessTime);
			});
		return true;
	}
}
