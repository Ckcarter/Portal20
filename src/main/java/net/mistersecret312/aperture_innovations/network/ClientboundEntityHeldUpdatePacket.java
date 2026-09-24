package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundEntityHeldUpdatePacket
{
	public int id;
	public boolean held;

	public ClientboundEntityHeldUpdatePacket(int id, boolean held)
	{
		this.id = id;
		this.held = held;
	}

	public ClientboundEntityHeldUpdatePacket(FriendlyByteBuf buffer)
	{
		this(buffer.readInt(), buffer.readBoolean());
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeInt(id);
		buffer.writeBoolean(held);
	}


	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handleEntityHeldUpdate(id, held);
			});

		return true;
	}
}
