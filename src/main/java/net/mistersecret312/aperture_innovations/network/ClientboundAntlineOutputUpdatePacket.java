package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

import java.util.function.Supplier;


public class ClientboundAntlineOutputUpdatePacket
{
	public BlockPos pos;
	public int color;
	public int activeColor;

	public ClientboundAntlineOutputUpdatePacket(BlockPos pos, int color, int activeColor)
	{
		this.pos = pos;
		this.color = color;
		this.activeColor = activeColor;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeBlockPos(this.pos);
		buffer.writeInt(this.color);
		buffer.writeInt(this.activeColor);
	}

	public static ClientboundAntlineOutputUpdatePacket decode(FriendlyByteBuf buffer)
	{
		return new ClientboundAntlineOutputUpdatePacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.handleAntlineOutputUpdate(this.pos, this.color, this.activeColor);
			});
		return true;
	}
}
