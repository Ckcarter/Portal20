package net.mistersecret312.aperture_innovations.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class ClientboundAntlineUpdatePacket
{
	public BlockPos pos;
	public boolean active;
	public int color;
	public int activeColor;

	public ClientboundAntlineUpdatePacket(BlockPos pos, boolean active, int color, int activeColor)
	{
		this.pos = pos;
		this.active = active;
		this.color = color;
		this.activeColor = activeColor;
	}

	public ClientboundAntlineUpdatePacket(FriendlyByteBuf buffer)
	{
		this(buffer.readBlockPos(), buffer.readBoolean(), buffer.readInt(), buffer.readInt());
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBlockPos(pos);
		buffer.writeBoolean(active);
		buffer.writeInt(color);
		buffer.writeInt(activeColor);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handleAntlineUpdate(pos, active, color, activeColor);
			});
		return true;
	}
}
