package net.mistersecret312.aperture_innovations.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.mistersecret312.aperture_innovations.sounds.SoundAccess;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class ClientboundPortalSoundsPacket
{
	public UUID linkID;
	public boolean isPrimary;

	public ClientboundPortalSoundsPacket(UUID linkID, boolean isPrimary)
	{
		this.linkID = linkID;
		this.isPrimary = isPrimary;
	}

	public ClientboundPortalSoundsPacket(FriendlyByteBuf buffer)
	{
		this(buffer.readUUID(), buffer.readBoolean());
	}

	public void encode(FriendlyByteBuf buffer)
	{
		buffer.writeUUID(linkID);
		buffer.writeBoolean(isPrimary);
	}


	public abstract boolean handle(Supplier<NetworkEvent.Context> ctx);

	public static class OpenPortal extends ClientboundPortalSoundsPacket
	{
		public OpenPortal(UUID linkID, boolean isPrimary)
		{
			super(linkID, isPrimary);
		}

		public OpenPortal(FriendlyByteBuf buffer)
		{
			super(buffer);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playOpenPortalSound(linkID, isPrimary);
				});
			return true;
		}
	}

	public static class EnterPortal extends ClientboundPortalSoundsPacket
	{
		public EnterPortal(UUID linkID, boolean isPrimary)
		{
			super(linkID, isPrimary);
		}

		public EnterPortal(FriendlyByteBuf buffer)
		{
			super(buffer);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playEnterPortalSound(linkID, isPrimary);
				});
			return true;
		}
	}

	public static class InvalidSurface extends ClientboundPortalSoundsPacket
	{
		public BlockPos pos;
		public InvalidSurface(UUID linkID, BlockPos pos, boolean isPrimary)
		{
			super(linkID, isPrimary);
			this.pos = pos;
		}

		public InvalidSurface(FriendlyByteBuf buffer)
		{
			this(buffer.readUUID(), buffer.readBlockPos(), buffer.readBoolean());
		}

		@Override
		public void encode(FriendlyByteBuf buffer)
		{
			buffer.writeUUID(linkID);
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(isPrimary);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playInvalidSurfaceSound(linkID, pos, isPrimary);
				});
			return true;
		}
	}

	public static class FizzlePortal extends ClientboundPortalSoundsPacket
	{
		public FizzlePortal(UUID linkID, boolean isPrimary)
		{
			super(linkID, isPrimary);
		}

		public FizzlePortal(FriendlyByteBuf buffer)
		{
			super(buffer);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playFizzlePortalSound(linkID, isPrimary);
				});
			return true;
		}
	}

	public static class GunActivate extends ClientboundPortalSoundsPacket
	{
		public BlockPos pos;
		public GunActivate(UUID linkID, BlockPos pos)
		{
			super(linkID, true);
			this.pos = pos;
		}

		@Override
		public void encode(FriendlyByteBuf buffer)
		{
			buffer.writeUUID(linkID);
			buffer.writeBlockPos(pos);
		}

		public GunActivate(FriendlyByteBuf buffer)
		{
			this(buffer.readUUID(), buffer.readBlockPos());
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playGunActivateSound(linkID, pos, isPrimary);
				});
			return true;
		}
	}

	public static class ShootPortal extends ClientboundPortalSoundsPacket
	{
		public BlockPos pos;
		public ShootPortal(UUID linkID, BlockPos pos, boolean isPrimary)
		{
			super(linkID, isPrimary);
			this.pos = pos;
		}

		public ShootPortal(FriendlyByteBuf buffer)
		{
			this(buffer.readUUID(), buffer.readBlockPos(), buffer.readBoolean());
		}

		@Override
		public void encode(FriendlyByteBuf buffer)
		{
			buffer.writeUUID(linkID);
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(isPrimary);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playShootPortalSound(linkID, pos, isPrimary);
				});
			return true;
		}
	}

	public static class ResetPortal extends ClientboundPortalSoundsPacket
	{
		public BlockPos pos;
		public ResetPortal(UUID linkID, BlockPos pos)
		{
			super(linkID, true);
			this.pos = pos;
		}

		public ResetPortal(FriendlyByteBuf buffer)
		{
			this(buffer.readUUID(), buffer.readBlockPos());
		}

		@Override
		public void encode(FriendlyByteBuf buffer)
		{
			buffer.writeUUID(linkID);
			buffer.writeBlockPos(pos);
			buffer.writeBoolean(isPrimary);
		}

		@Override
		public boolean handle(Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() ->
				{
					SoundAccess.playResetPortalSound(linkID, pos, isPrimary);
				});
			return true;
		}
	}
}
