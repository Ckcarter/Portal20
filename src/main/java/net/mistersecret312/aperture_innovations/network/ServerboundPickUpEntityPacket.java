package net.mistersecret312.aperture_innovations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.capabilities.HoldEntityCapability;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Supplier;

public class ServerboundPickUpEntityPacket
{
	public ServerboundPickUpEntityPacket()
	{}

	public ServerboundPickUpEntityPacket(FriendlyByteBuf buffer)
	{
		this();
	}

	public void encode(FriendlyByteBuf buffer)
	{}

	public boolean handle(Supplier<NetworkEvent.Context> ctx)	{
		ctx.get().enqueueWork(() ->
			{
				ServerPlayer player = (ServerPlayer) ctx.get().getSender();
				Level level = player.level();

				ItemStack main = player.getMainHandItem();
				ItemStack off = player.getOffhandItem();
				boolean hasPortalGun = main.is(ItemInit.PORTAL_GUN.get()) || off.is(ItemInit.PORTAL_GUN.get());
				if(!hasPortalGun)
					return;

				ItemStack gunStack = main.is(ItemInit.PORTAL_GUN.get()) ? main : off;
				PortalGunItem portalGun = (PortalGunItem) gunStack.getItem();

				if(portalGun.getHeldEntity(gunStack) != null)
				{
					portalGun.setHeldEntity(gunStack, null);
					NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
							new ClientboundGunZapSoundPacket(player.getUUID(), true));

					level.playSound(null, player.blockPosition(), SoundInit.PORTAL_GUN_HOLD_STOP.get(), SoundSource.PLAYERS);
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level),
							"main", "let_go");
					return;
				}
				Vec3 startPos = player.getEyePosition(1F);
				Vec3 endPos = startPos.add(player.getViewVector(1F).multiply(3f, 3f, 3f));

				AABB box = new AABB(startPos, endPos);

				EntityHitResult result = ProjectileUtil.getEntityHitResult(level, player, startPos, endPos, box, entity -> true);
				if(result == null)
				{
					portalGun.setHeldEntity(gunStack, null);
					NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
							new ClientboundGunZapSoundPacket(player.getUUID(), true));

					level.playSound(null, player.blockPosition(),
							SoundInit.PORTAL_GUN_HOLD_FAIL.get(), SoundSource.PLAYERS);
					portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level),
							"main", "reset");
					return;
				}

				if(result.getType().equals(HitResult.Type.ENTITY))
				{
					Entity entity = result.getEntity();
					if(entity.getBoundingBox().getSize() > player.getBoundingBox().getSize()*1.5f)
						return;

					entity.getCapability(CapabilityInit.HOLD).ifPresent(cap -> {
						cap.setHeld(entity, true);
						portalGun.setHeldEntity(gunStack, entity);
						portalGun.setZapSoundTick(gunStack, 0);
						portalGun.triggerAnim(player, GeoItem.getOrAssignId(gunStack, (ServerLevel) level), "main", "hold");
						level.playSound(null, player.blockPosition(), SoundInit.PORTAL_GUN_HOLD_START.get(), SoundSource.PLAYERS);
					});
				}
				else
				{
					portalGun.setHeldEntity(gunStack, null);
					NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
							new ClientboundGunZapSoundPacket(player.getUUID(), true));
				}
			});
		return true;
	}
}
