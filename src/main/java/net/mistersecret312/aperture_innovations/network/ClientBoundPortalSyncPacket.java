
package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.data.portal.Portal;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientBoundPortalSyncPacket
{
	public UUID linkID;
	public boolean isPrimary;
	public Portal portal;
	public ResourceLocation variant;
	
	public ClientBoundPortalSyncPacket(UUID linkID, boolean isPrimary, Portal portal, ResourceLocation variant)
	{
		this.linkID = linkID;
		this.isPrimary = isPrimary;
		this.portal = portal;
		this.variant = variant;
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeUUID(this.linkID);
		buffer.writeBoolean(isPrimary);
		portal.encode(buffer);
		buffer.writeUtf(variant.toString());
	}

	public static ClientBoundPortalSyncPacket decode(FriendlyByteBuf buffer)
	{
		UUID uuid = buffer.readUUID();
		boolean isPrimary = buffer.readBoolean();
		Portal portal = Portal.decode(buffer);
		
		ResourceLocation variant = new ResourceLocation(buffer.readUtf());
		
		return new ClientBoundPortalSyncPacket(uuid, isPrimary, portal, variant);
	}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ClientPacketHandler.syncPortalData(linkID, isPrimary, portal, variant);
			});
		return true;
	}
}