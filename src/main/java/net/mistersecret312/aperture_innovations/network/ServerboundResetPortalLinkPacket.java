package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.data.portal.PortalLink;
import net.mistersecret312.aperture_innovations.data.PortalLinkData;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.UUID;
import java.util.function.Supplier;

public class ServerboundResetPortalLinkPacket
{
	public ServerboundResetPortalLinkPacket()
	{}

	public ServerboundResetPortalLinkPacket(FriendlyByteBuf buffer)
	{}

	public void encode(FriendlyByteBuf buffer)
	{}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
			{
				ServerPlayer player = ctx.get().getSender();
				Level level = player.level();

				ItemStack main = player.getMainHandItem();
				ItemStack off = player.getOffhandItem();
				boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
				if(!hasPortalGun)
					return;

				ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
				PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

				if(portalGun.getHeldEntity(gunStack) != null)
					return;

				int dualityState = portalGun.getDualityState(gunStack);

				UUID linkID = portalGun.getUUID(gunStack, true);

				PortalLinkData linkData = PortalLinkData.get(level);
				PortalLink link = linkData.getLink(gunStack);
				PortalLink pairLink = null;

				UUID pairID = portalGun.getPair(gunStack);
				if(pairID != null)
					pairLink = linkData.getLink(pairID);

				if(link == null)
				{
					linkData.addFreshLink(linkID);
					link = linkData.getLink(linkID);
				}
				if(!link.getPrimaryPortal().isOpen() && !link.getSecondaryPortal().isOpen())
					return;

				if(pairLink != null && dualityState != 2)
				{
					if(dualityState == 1 && !pairLink.getSecondaryPortal().isOpen())
						return;
					if(dualityState == 0 && !pairLink.getPrimaryPortal().isOpen())
						return;
				}

				if(dualityState == 2)
					link.reset(level);
				if(dualityState == 1)
					link.resetSecondary(level);
				if(dualityState == 0)
					link.resetPrimary(level);

				portalGun.setLastShotPortal(gunStack, -1);
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())),
						new ClientboundPortalSoundsPacket.ResetPortal(linkID, player.blockPosition()));

				portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "reset");
			});
		return true;
	}
}
