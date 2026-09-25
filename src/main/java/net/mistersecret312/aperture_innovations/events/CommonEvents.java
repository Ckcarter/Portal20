package net.mistersecret312.aperture_innovations.events;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.NearPortalDeathCriterion;
import net.mistersecret312.aperture_innovations.block_entities.AntlineBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.AntlineOutputBlockEntity;
import net.mistersecret312.aperture_innovations.block_entities.AntlineTimerBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.AntlineOutputBlock;
import net.mistersecret312.aperture_innovations.blocks.AntlineTimerBlock;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.capabilities.GenericProvider;
import net.mistersecret312.aperture_innovations.capabilities.HoldEntityCapability;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.forge_events.AntlineActivateEvent;
import net.mistersecret312.aperture_innovations.init.CapabilityInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.LongFallBootsItem;
import net.mistersecret312.aperture_innovations.items.PortalGunItem;
import net.mistersecret312.aperture_innovations.network.ClientBoundPortalSyncPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalAmbientSoundPacket;
import net.mistersecret312.aperture_innovations.data.portal.Portal;
import net.mistersecret312.aperture_innovations.data.portal.PortalLink;
import net.mistersecret312.aperture_innovations.data.PortalLinkData;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Mod.EventBusSubscriber(modid = ApertureInnovations.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents
{
	@SubscribeEvent
	public static void levelTick(TickEvent.LevelTickEvent event)
	{
		Level level = event.level;
		if(level instanceof ServerLevel)
		{
			PortalLinkData data = PortalLinkData.get(level);
			for(Map.Entry<UUID, PortalLink> entry : data.portalLinks.entrySet())
			{
				PortalLink link = entry.getValue();

				portals:
				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					Portal portal = isPrimary ? link.getPrimaryPortal() : link.getSecondaryPortal();
					if(portal.getPosition() == null)
						continue;

					ResourceKey<Level> portalDim = isPrimary ? link.getPrimaryPortal().getDimension() : link.getSecondaryPortal()
																											.getDimension();
					if(!portalDim.equals(level.dimension()))
						continue;

					List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(portal.getPosition(), portal.getPosition()).inflate(5));
					for(Entity entity : entities)
					{
						@NotNull LazyOptional<ApertureCapability> lazyCap = entity.getCapability(CapabilityInit.APERTURE);
						if(!lazyCap.isPresent())
							continue;
						Optional<ApertureCapability> optionalCap = lazyCap.resolve();
						if(optionalCap.isEmpty())
							continue;

						ApertureCapability aperture = optionalCap.get();
						if(aperture.ignorePortalsTime != 0)
							return;

						if(link.teleportLogic(entity, aperture, link, level, isPrimary))
							break portals;
					}
				}

				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					Vec3 portalPos = isPrimary ? link.getPrimaryPortal().getPosition() : link.getSecondaryPortal().getPosition();

					if(portalPos == null)
						continue;

					ResourceKey<Level> portalDim = isPrimary ? link.getPrimaryPortal().getDimension() : link.getSecondaryPortal()
																											.getDimension();
					if(!portalDim.equals(level.dimension()))
						continue;


					float xRot = isPrimary ? link.getPrimaryPortal().getXRotation() : link.getSecondaryPortal()
																						  .getXRotation();
					float yRot = isPrimary ? link.getPrimaryPortal().getYRotation() : link.getSecondaryPortal().getYRotation();

					Direction direction = Direction.fromYRot(yRot);
					if(xRot == -90)
						direction = Direction.UP;
					if(xRot == 90)
						direction = Direction.DOWN;

					if(!link.checkForValidity(level, portalPos, xRot, yRot, direction, link.linkID, isPrimary))
					{
						if(isPrimary)
							link.resetPrimary(level);
						else link.resetSecondary(level);
					}

					if(link.isOpen())
					{
						NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
										BlockPos.containing(portalPos))),
								new ClientboundPortalAmbientSoundPacket(link.linkID, isPrimary, false));

					}
				}

			}
		}
	}

	@SubscribeEvent
	public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		Player player = event.getEntity();
		if(player instanceof ServerPlayer serverPlayer)
		{
			PortalLinkData data = PortalLinkData.get(serverPlayer.level());
			data.portalLinks.forEach((uuid, link) -> {
				for(int i = 0; i < 2; i++)
				{
					boolean isPrimary = i == 0;
					if(isPrimary)
						NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
								new ClientBoundPortalSyncPacket(uuid, true,
								link.getPrimaryPortal(), link.variantKey));
					else
						NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
								new ClientBoundPortalSyncPacket(uuid, false,
										link.getSecondaryPortal(), link.variantKey));
				}
			});
		}
	}

	@SubscribeEvent
	public static void playerFall(LivingHurtEvent event)
	{
		LivingEntity living = event.getEntity();
		DamageSource source = event.getSource();
		for(ItemStack stack : living.getArmorSlots())
		{
			if((source.equals(living.damageSources().fall()) || source.equals(
					living.damageSources().flyIntoWall())) && stack.getItem() instanceof LongFallBootsItem)
			{
				if(!living.level().isClientSide())
				{
					ServerLevel level = (ServerLevel) living.level();

					level.playSound(null, living.blockPosition(), SoundInit.LONG_FALL_BOOTS_LAND.get(),
							SoundSource.PLAYERS, 0.15F, 1F);
				}
				event.setCanceled(true);
				return;
			}
		}
	}

	@SubscribeEvent
	public static void playerFallDamage(LivingFallEvent event)
	{
		LivingEntity living = event.getEntity();
		for(ItemStack stack : living.getArmorSlots())
		{
			if(stack.getItem() instanceof LongFallBootsItem)
			{

				if(!living.level().isClientSide() && event.getDistance() > 5f)
				{
					if(LongFallBootsConfig.long_fall_boots_use_energy.get() && !consumeEnergy(stack)) return;

					ServerLevel level = (ServerLevel) living.level();

					level.playSound(null, living.blockPosition(), SoundInit.LONG_FALL_BOOTS_LAND.get(),
							SoundSource.PLAYERS, 0.15F, 1F);
				}
				event.setCanceled(true);
				return;
			}
		}
	}

	public static boolean consumeEnergy(ItemStack stack)
	{
		Optional<IEnergyStorage> optionalCapability = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
		if(optionalCapability.isPresent())
		{
			IEnergyStorage storage = optionalCapability.get();
			if(storage instanceof ApertureEnergy energy)
			{
				long requiredEnergy = LongFallBootsConfig.fall_energy_consumption.get();
				if(energy.getTrueEnergyStored() >= requiredEnergy)
				{
					energy.extractLongEnergy(requiredEnergy, false);
					return true;
				} else return false;
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void playerDied(LivingDeathEvent event)
	{
		LivingEntity living = event.getEntity();
		Level level = living.level();
		if(living instanceof ServerPlayer player)
		{
			Pair<UUID, Boolean> closestPortal = PortalUtilities.getClosestPortal(player);
			UUID linkID = closestPortal.getFirst();
			if(linkID == null) return;
			boolean isPrimary = closestPortal.getSecond();

			Vec3 portalPos = PortalUtilities.getPortalPos(level, linkID, isPrimary);
			if(portalPos == null) return;

			boolean onWall = PortalUtilities.isPortalOnWall(level, linkID, isPrimary);
			boolean onCeiling = PortalUtilities.isPortalOnCeiling(level, linkID, isPrimary);

			long distance = (long) portalPos.distanceTo(player.position());
			if(distance > 16) return;

			NearPortalDeathCriterion.INSTANCE.trigger(player, distance, !onWall && !onCeiling);
		}
	}

	@SubscribeEvent
	public static void totemDeath(LivingUseTotemEvent event)
	{
		LivingEntity living = event.getEntity();
		Level level = living.level();
		if(living instanceof ServerPlayer player)
		{
			Pair<UUID, Boolean> closestPortal = PortalUtilities.getClosestPortal(player);
			UUID linkID = closestPortal.getFirst();
			if(linkID == null) return;
			boolean isPrimary = closestPortal.getSecond();

			Vec3 portalPos = PortalUtilities.getPortalPos(level, linkID, isPrimary);
			if(portalPos == null) return;

			boolean onWall = PortalUtilities.isPortalOnWall(level, linkID, isPrimary);
			boolean onCeiling = PortalUtilities.isPortalOnCeiling(level, linkID, isPrimary);

			long distance = (long) portalPos.distanceTo(player.position());
			if(distance > 16) return;

			NearPortalDeathCriterion.INSTANCE.trigger(player, distance, !onWall && !onCeiling);
		}
	}

	@SubscribeEvent
	public static void itemToss(ItemTossEvent event)
	{
		if(event.getEntity().getItem().getItem() instanceof PortalGunItem)
		{
			event.getEntity().setThrower(event.getPlayer().getUUID());
		}
	}

	@SubscribeEvent
	public static void entityTick(TickEvent.LevelTickEvent event)
	{
		Level level = event.level;

		if(level instanceof ServerLevel serverLevel)
		{
			for(Entity entity : serverLevel.getAllEntities())
			{
				entity.getCapability(CapabilityInit.APERTURE).ifPresent(cap -> cap.tick(level, entity));
			}
		}
	}

	@SubscribeEvent
	public static void antlineActivate(AntlineActivateEvent event)
	{
		Level level = event.getLevel();
		BlockPos antlinePos = event.getAntlinePos();
		BlockPos activatedBlockPos = event.getActivatedBlockPos();
		int signal = event.getSignal();

		AntlineBlockEntity antline = (AntlineBlockEntity) level.getBlockEntity(antlinePos);
		if(antline == null)
			return;

		BlockState activatedBlockState = level.getBlockState(activatedBlockPos);

		if(activatedBlockState.getBlock() instanceof AntlineOutputBlock)
		{
			activatedBlockState = activatedBlockState.setValue(AntlineOutputBlock.ACTIVE, signal != 0);
			BlockEntity blockEntity = level.getBlockEntity(activatedBlockPos);
			if(blockEntity instanceof AntlineOutputBlockEntity output)
				output.signal = signal;

			level.setBlock(activatedBlockPos, activatedBlockState, 16 | 2);
			BlockPos relativePos = activatedBlockPos.relative(activatedBlockState.getValue(AntlineOutputBlock.NORMAL).getOpposite());
			level.updateNeighborsAt(relativePos, activatedBlockState.getBlock());
		}

		if(activatedBlockState.getBlock() instanceof AntlineTimerBlock)
		{
			if(signal == 0)
				return;
			if(activatedBlockState.getValue(AntlineTimerBlock.ACTIVE))
				return;

			activatedBlockState = activatedBlockState.setValue(AntlineTimerBlock.ACTIVE, true);
			BlockEntity blockEntity = level.getBlockEntity(activatedBlockPos);
			if(blockEntity instanceof AntlineTimerBlockEntity output)
			{
				output.signal = signal;
				output.time = output.maxTime;
				output.soundTime = 20;
			}

			level.setBlock(activatedBlockPos, activatedBlockState, 16 | 2);
			BlockPos relativePos = activatedBlockPos.relative(activatedBlockState.getValue(AntlineTimerBlock.NORMAL).getOpposite());
			level.updateNeighborsAt(relativePos, activatedBlockState.getBlock());
		}
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Entity)
		{
			event.addCapability(new ResourceLocation(ApertureInnovations.MODID, "aperture"),
					new GenericProvider<>(CapabilityInit.APERTURE, new ApertureCapability()));

			event.addCapability(new ResourceLocation(ApertureInnovations.MODID, "hold_entity"),
					new GenericProvider<>(CapabilityInit.HOLD, new HoldEntityCapability()));
		}
	}
}
