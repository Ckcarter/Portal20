package net.mistersecret312.aperture_innovations.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.sounds.SoundAccess;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundGunZapSoundPacket
{
	public UUID player;
	public boolean stop;

	public ClientboundGunZapSoundPacket(UUID player, boolean stop)
	{
		this.player = player;
		this.stop = stop;
	}

	public ClientboundGunZapSoundPacket(FriendlyByteBuf buffer)
	{
		this(buffer.readUUID(), buffer.readBoolean());
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeUUID(player);
		buffer.writeBoolean(stop);
	}


	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				SoundAccess.playGunZap(player, stop);
			});
		return true;
	}
}
