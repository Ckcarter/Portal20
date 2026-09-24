package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.sounds.SoundAccess;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundPortalAmbientSoundPacket
{
	public UUID linkID;
	public boolean isPrimary;
	public boolean stop;

	public ClientboundPortalAmbientSoundPacket(UUID linkID, boolean isPrimary, boolean stop)
	{
		this.linkID = linkID;
		this.isPrimary = isPrimary;
		this.stop = stop;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeUUID(linkID);
		buffer.writeBoolean(isPrimary);
		buffer.writeBoolean(stop);
	}

	public static ClientboundPortalAmbientSoundPacket decode(FriendlyByteBuf buffer)
	{
		return new ClientboundPortalAmbientSoundPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readBoolean());
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				SoundAccess.playPortalAmbient(linkID, isPrimary, stop);
			});
		return true;
	}
}
