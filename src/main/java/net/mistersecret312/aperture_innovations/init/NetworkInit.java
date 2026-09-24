package net.mistersecret312.aperture_innovations.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.network.*;

public class NetworkInit
{
	private static final String PROTOCOL_VERSION = "1";

	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(
					ApertureInnovations.MODID, "main_network"),
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public NetworkInit()
	{

	}

	public static void register()
	{
		int index = 0;

		INSTANCE.messageBuilder(ClientBoundPortalSyncPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientBoundPortalSyncPacket::encode)
				.decoder(ClientBoundPortalSyncPacket::decode)
				.consumerMainThread(ClientBoundPortalSyncPacket::handle).add();

		INSTANCE.messageBuilder(ServerboundOpenPortalPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerboundOpenPortalPacket::encode)
				.decoder(ServerboundOpenPortalPacket::new)
				.consumerMainThread(ServerboundOpenPortalPacket::handle).add();

		INSTANCE.messageBuilder(ServerboundResetPortalLinkPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerboundResetPortalLinkPacket::encode)
				.decoder(ServerboundResetPortalLinkPacket::new)
				.consumerMainThread(ServerboundResetPortalLinkPacket::handle).add();

		INSTANCE.messageBuilder(ClientboundTeleportMomentumPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundTeleportMomentumPacket::encode)
				.decoder(ClientboundTeleportMomentumPacket::decode)
				.consumerMainThread(ClientboundTeleportMomentumPacket::handle).add();

		INSTANCE.messageBuilder(ClientboundApertureCapabilityPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundApertureCapabilityPacket::encode)
				.decoder(ClientboundApertureCapabilityPacket::decode)
				.consumerMainThread(ClientboundApertureCapabilityPacket::handle).add();

		//Sounds

		INSTANCE.messageBuilder(ClientboundPortalAmbientSoundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalAmbientSoundPacket::encode)
				.decoder(ClientboundPortalAmbientSoundPacket::decode)
				.consumerMainThread(ClientboundPortalAmbientSoundPacket::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.FizzlePortal.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.FizzlePortal::encode)
				.decoder(ClientboundPortalSoundsPacket.FizzlePortal::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.FizzlePortal::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.EnterPortal.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.EnterPortal::encode)
				.decoder(ClientboundPortalSoundsPacket.EnterPortal::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.EnterPortal::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.OpenPortal.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.OpenPortal::encode)
				.decoder(ClientboundPortalSoundsPacket.OpenPortal::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.OpenPortal::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.InvalidSurface.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.InvalidSurface::encode)
				.decoder(ClientboundPortalSoundsPacket.InvalidSurface::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.InvalidSurface::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.ResetPortal.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.ResetPortal::encode)
				.decoder(ClientboundPortalSoundsPacket.ResetPortal::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.ResetPortal::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.ShootPortal.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.ShootPortal::encode)
				.decoder(ClientboundPortalSoundsPacket.ShootPortal::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.ShootPortal::handle).add();

		INSTANCE.messageBuilder(ClientboundPortalSoundsPacket.GunActivate.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundPortalSoundsPacket.GunActivate::encode)
				.decoder(ClientboundPortalSoundsPacket.GunActivate::new)
				.consumerMainThread(ClientboundPortalSoundsPacket.GunActivate::handle).add();

		INSTANCE.messageBuilder(ClientboundEntityPortalLerpPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundEntityPortalLerpPacket::encode)
				.decoder(ClientboundEntityPortalLerpPacket::decode)
				.consumerMainThread(ClientboundEntityPortalLerpPacket::handle).add();

		INSTANCE.messageBuilder(ClientboundAntlineOutputUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundAntlineOutputUpdatePacket::encode)
				.decoder(ClientboundAntlineOutputUpdatePacket::decode)
				.consumerMainThread(ClientboundAntlineOutputUpdatePacket::handle).add();

		INSTANCE.messageBuilder(ClientboundAntlineUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundAntlineUpdatePacket::encode)
				.decoder(ClientboundAntlineUpdatePacket::new)
				.consumerMainThread(ClientboundAntlineUpdatePacket::handle).add();

		INSTANCE.messageBuilder(ClientboundEntityHeldUpdatePacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundEntityHeldUpdatePacket::encode)
				.decoder(ClientboundEntityHeldUpdatePacket::new)
				.consumerMainThread(ClientboundEntityHeldUpdatePacket::handle).add();

		INSTANCE.messageBuilder(ClientboundGunZapSoundPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientboundGunZapSoundPacket::encode)
				.decoder(ClientboundGunZapSoundPacket::new)
				.consumerMainThread(ClientboundGunZapSoundPacket::handle).add();

		INSTANCE.messageBuilder(ServerboundPickUpEntityPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerboundPickUpEntityPacket::encode)
				.decoder(ServerboundPickUpEntityPacket::new)
				.consumerMainThread(ServerboundPickUpEntityPacket::handle).add();
	}
}
