package net.mistersecret312.aperture_innovations.data.portal;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.PortalTravelCriterion;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.mistersecret312.aperture_innovations.data.PortalLinkData;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.forge_events.PortalTravelEvent;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.StatisticsInit;
import net.mistersecret312.aperture_innovations.init.TagInit;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityPortalLerpPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundTeleportMomentumPacket;
import net.mistersecret312.aperture_innovations.utilities.CoordUtil;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PortalLink
{
	public UUID linkID;

	private Portal primaryPortal;
	private Portal secondaryPortal;

	public ResourceLocation variantKey = null;

	public PortalLink(UUID linkID, ResourceLocation variantKey)
	{
		this.linkID = linkID;
		this.variantKey = variantKey;

		this.primaryPortal = new Portal();
		this.secondaryPortal = new Portal();
	}

	public PortalLink(UUID linkID, Portal primaryPortal, Portal secondaryPortal,
					  ResourceLocation variantKey)
	{
		this.linkID = linkID;

		this.primaryPortal = primaryPortal;
		this.secondaryPortal = secondaryPortal;

		this.variantKey = variantKey;
	}

	public void createPrimaryPortal(Level level, Vec3 pos, ResourceKey<Level> dimension, Direction direction, Direction facing)
	{
		float xRot = 0;
		float yRot = direction.toYRot();
		if(direction.equals(Direction.UP))
		{
			xRot = -90;
			yRot = facing.toYRot()+(facing.getAxis().equals(Direction.Axis.X) ? 0 : 180);
		}
		if(direction.equals(Direction.DOWN))
		{
			xRot = 90;
			yRot = facing.toYRot();
		}

		this.primaryPortal.setPosition(pos);
		this.primaryPortal.setYRotation(yRot);
		this.primaryPortal.setXRotation(xRot);
		this.primaryPortal.setDirection(direction);
		this.primaryPortal.setFacing(facing);
		this.primaryPortal.setDimension(dimension);
		this.primaryPortal.setMoonshot(false);

		this.primaryPortal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(level, pos, xRot, yRot));

		ServerLevel portalLevel = level.getServer().getLevel(dimension);

		if(portalLevel != null)
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
							BlockPos.containing(pos))),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, true));
		}

		PortalLinkData.get(level).setDirty(linkID, true);
	}

	public void createSecondaryPortal(Level level, Vec3 pos, ResourceKey<Level> dimension,
									  Direction direction, Direction facing)
	{
		float xRot = 0;
		float yRot = direction.toYRot();
		if(direction.equals(Direction.UP))
		{
			xRot = -90;
			yRot = facing.toYRot()+(facing.getAxis().equals(Direction.Axis.X) ? 0 : 180);
		}
		if(direction.equals(Direction.DOWN))
		{
			xRot = 90;
			yRot = facing.toYRot();
		}

		this.secondaryPortal.setPosition(pos);
		this.secondaryPortal.setYRotation(yRot);
		this.secondaryPortal.setXRotation(xRot);
		this.secondaryPortal.setDirection(direction);
		this.secondaryPortal.setFacing(facing);
		this.secondaryPortal.setDimension(dimension);
		this.secondaryPortal.setMoonshot(false);

		this.secondaryPortal.setReplaceShapes(PortalUtilities.calculatePortalVoxels(level, pos, xRot, yRot));

		ServerLevel portalLevel = level.getServer().getLevel(dimension);

		if(portalLevel != null)
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
							BlockPos.containing(pos))),
					new ClientboundPortalSoundsPacket.OpenPortal(linkID, false));
		}

		PortalLinkData.get(level).setDirty(linkID, false);
	}

	public boolean checkForValidity(Level level, Vec3 position, float xRot, float yRot, Direction direction, UUID id, boolean isPrimary)
	{
		AABB portalBox = PortalUtilities.getPortalBoundingBox(position, xRot, yRot).inflate(0.025);
		AABB placementBox = PortalUtilities.getPortalPlacementBox(position, xRot, yRot).inflate(0.025);

		Portal self;
		if(level.isClientSide())
		{
			ClientPortalLink link =  PortalUtilities.getPortalLinks().get(id);
			if(isPrimary)
				self = link.getPrimaryPortal();
			else self = link.getSecondaryPortal();
		}
		else
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(id);
			if(isPrimary)
				self = link.getPrimaryPortal();
			else self = link.getSecondaryPortal();
		}
		Pair<UUID, Boolean> closestPortalPair;
		if(self.getPosition() == null)
			closestPortalPair = PortalUtilities.getClosestPortal(level, position, id, isPrimary);
		else closestPortalPair = PortalUtilities.getClosestPortal(level, self);

		Portal closestPortal;
		if(level.isClientSide() && closestPortalPair.getFirst() != null)
		{
			ClientPortalLink link = PortalUtilities.getPortalLinks().get(closestPortalPair.getFirst());
			if(closestPortalPair.getSecond())
				closestPortal = link.getPrimaryPortal();
			else closestPortal = link.getSecondaryPortal();
		}
		else if(closestPortalPair.getFirst() != null)
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(closestPortalPair.getFirst());
			if(closestPortalPair.getSecond())
				closestPortal = link.getPrimaryPortal();
			else closestPortal = link.getSecondaryPortal();
		}
		else
			closestPortal = null;

		if(self.equals(closestPortal))
			closestPortal = null;

		VoxelShape placementShape = Shapes.create(placementBox);

		AABB portalBoxCopy = PortalUtilities.getPortalPlacementBox(position, xRot, yRot).inflate(0.025).deflate(0.025)
											.move(Vec3.directionFromRotation(xRot+(direction.getAxis().isVertical() ? 180 : 0), yRot+180)
													  .multiply(0.15, 0.15, 0.15));

		AtomicBoolean hasAir = new AtomicBoolean();
		BlockPos.betweenClosedStream(portalBoxCopy).forEach(pos ->
			{
				BlockState state = level.getBlockState(pos);
				if(state.isAir())
					hasAir.set(true);
			});

		if(hasAir.get())
			return false;

		AtomicReference<VoxelShape> placementReference = new AtomicReference<>(placementShape);
		Portal finalClosestPortal = closestPortal;
		BlockPos.betweenClosedStream(portalBox).forEach(pos ->
			{
				BlockState state = level.getBlockState(pos);
				if(finalClosestPortal != null)
				{
					VoxelShape shape = Shapes.create(PortalUtilities.getPortalPlacementBox(finalClosestPortal.getPosition(),
							finalClosestPortal.getXRotation(), finalClosestPortal.getYRotation()).inflate(0.05));

					if(!placementReference.get().isEmpty())
						placementReference.set(Shapes.join(placementReference.get(), shape, BooleanOp.ONLY_FIRST));
				}
				if(!state.isAir())
				{
					VoxelShape shape = state.getCollisionShape(level, pos)
											.move(pos.getX(), pos.getY(), pos.getZ());

					if(state.is(TagInit.Blocks.IMPORTALABLE))
						shape = Shapes.create(shape.bounds().inflate(0.025));

					if(!placementReference.get().isEmpty())
						placementReference.set(Shapes.join(placementReference.get(), shape, BooleanOp.ONLY_FIRST));
				}
			});

		placementShape = placementReference.get();
		List<AABB> shapeList = placementShape.toAabbs();

		if(!shapeList.isEmpty())
		{
			AABB firstPart = shapeList.get(0);

			boolean wall = direction.getAxis().isHorizontal();

			boolean equal = false;
			if(!wall)
			{
				equal = firstPart.maxX == placementBox.maxX && firstPart.minX == placementBox.minX
								&& firstPart.maxZ == placementBox.maxZ && firstPart.minZ == placementBox.minZ;
			}
			if(wall)
			{
				if(direction.getAxis().equals(Direction.Axis.Z))
				{
					equal = firstPart.maxX == placementBox.maxX && firstPart.minX == placementBox.minX
									&& firstPart.maxY == placementBox.maxY && firstPart.minY == placementBox.minY;

				}
				if(direction.getAxis().equals(Direction.Axis.X))
				{
					equal = firstPart.maxY == placementBox.maxY && firstPart.minY == placementBox.minY
									&& firstPart.maxZ == placementBox.maxZ && firstPart.minZ == placementBox.minZ;
				}
			}

			return equal && shapeList.size() == 1;
		}

		return false;
	}

	public void updateColors(Level level, int primaryPortalColor, int secondaryPortalColor)
	{
		if(this.primaryPortal.getColor() != primaryPortalColor)
		{
			this.primaryPortal.setColor(primaryPortalColor);
			PortalLinkData.get(level).setDirty(linkID, true);
		}

		if(this.secondaryPortal.getColor() != secondaryPortalColor)
		{
			this.secondaryPortal.setColor(secondaryPortalColor);
			PortalLinkData.get(level).setDirty(linkID, false);
		}
	}

	public void updateVariant(Level level, ResourceLocation variantKey)
	{
		if(!this.variantKey.equals(variantKey))
		{
			this.variantKey = variantKey;
			PortalLinkData.get(level).setDirty(linkID, true);
			PortalLinkData.get(level).setDirty(linkID, false);
		}
	}

	public void reset(Level level)
	{
		if(primaryPortal.isOpen())
			resetPrimary(level);

		if(secondaryPortal.isOpen())
			resetSecondary(level);
	}

	public void resetPrimary(Level level)
	{
		if(primaryPortal.isInWorld())
		{
			ServerLevel portalLevel = level.getServer().getLevel(primaryPortal.getDimension());
			if(portalLevel != null)
			{
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
								BlockPos.containing(primaryPortal.getPosition()))),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, true));
			}

			if(isOpen())
			{
				AABB box = PortalUtilities.getPortalBoundingBox(primaryPortal.getPosition(),
						primaryPortal.getXRotation(), primaryPortal.getYRotation()).deflate(0.05);
				for(Entity entity : level.getEntitiesOfClass(Entity.class, box))
				{
					Direction portalDirection = PortalUtilities.getPortalDirection(level, linkID, true);
					Vec3 vector = Vec3.atLowerCornerOf(portalDirection.getNormal()).multiply(0.75f, 0.75f, 0.75f);
					entity.push(vector.x, vector.y, vector.z);
					if(entity instanceof ServerPlayer player)
						NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
								new ClientboundTeleportMomentumPacket(player.getDeltaMovement()));
				}
			}
		}
		int color = primaryPortal.getColor();

		this.primaryPortal = new Portal();
		this.primaryPortal.setColor(color);
		this.primaryPortal.setReplaceShapes(new ArrayList<>());

		PortalLinkData.get(level).setDirty(linkID, true);
	}

	public void resetSecondary(Level level)
	{
		if(secondaryPortal.isInWorld())
		{
			ServerLevel portalLevel = level.getServer().getLevel(secondaryPortal.getDimension());
			if(portalLevel != null)
			{
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(
								BlockPos.containing(secondaryPortal.getPosition()))),
						new ClientboundPortalSoundsPacket.FizzlePortal(linkID, true));
			}

			if(isOpen())
			{
				AABB box = PortalUtilities.getPortalBoundingBox(secondaryPortal.getPosition(),
						secondaryPortal.getXRotation(), secondaryPortal.getYRotation()).deflate(0.05);
				for(Entity entity : level.getEntitiesOfClass(Entity.class, box))
				{
					Direction portalDirection = PortalUtilities.getPortalDirection(level, linkID, true);
					Vec3 vector = Vec3.atLowerCornerOf(portalDirection.getNormal()).multiply(0.75f, 0.75f, 0.75f);
					entity.push(vector.x, vector.y, vector.z);
					if(entity instanceof ServerPlayer player)
						NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
							new ClientboundTeleportMomentumPacket(player.getDeltaMovement()));
				}
			}
		}
		int color = secondaryPortal.getColor();

		this.secondaryPortal = new Portal();
		this.secondaryPortal.setColor(color);
		this.secondaryPortal.setReplaceShapes(new ArrayList<>());

		PortalLinkData.get(level).setDirty(linkID, false);
	}

	public void setMoonshot(boolean isPrimary, boolean moonshot, Level level)
	{
		if(isPrimary)
		{
			primaryPortal.setPosition(null);
			primaryPortal.setMoonshot(moonshot);
		}
		else
		{
			secondaryPortal.setPosition(null);
			secondaryPortal.setMoonshot(moonshot);
		}

		PortalLinkData.get(level).setDirty(linkID, isPrimary);
	}

	public boolean isOpen()
	{
		return primaryPortal.isOpen() && secondaryPortal.isOpen();
	}

	public boolean isInWorld()
	{
		return primaryPortal.isInWorld() && secondaryPortal.isInWorld();
	}

	public boolean isInterdimensionalLink()
	{
		if(isOpen())
		{
			return !primaryPortal.getDimension().equals(secondaryPortal.getDimension());
		}
		return false;
	}

	public boolean teleportLogic(Entity entity, ApertureCapability aperture,
								 PortalLink link, Level level, boolean isPrimary)
	{
		UUID linkID = link.linkID;
		if(level.isClientSide())
			return false;
		if(aperture.ignorePortalsTime != 0)
			return false;

		Vec3 currentPos = entity.position().add(0, entity.getBbHeight() / 2f, 0);

		Vec3 speed = entity.getDeltaMovement();
		Vec3 nextPos = currentPos.add(speed.multiply(2f, 2f, 2f));

		AABB movementBox = entity.getBoundingBox().expandTowards(speed);
		if(link == null || !link.isOpen())
			return false;

		Portal portal = isPrimary ? link.getPrimaryPortal() : link.getSecondaryPortal();
		Portal otherPortal = isPrimary ? link.getSecondaryPortal() : link.getPrimaryPortal();

		double distance = portal.getPosition().distanceTo(entity.position());
		if(distance < 6 && otherPortal.isMoonshot() && !(entity instanceof ServerPlayer && ((ServerPlayer) entity).getAbilities().instabuild))
		{
			Direction direction = PortalUtilities.getPortalDirection(level, linkID, isPrimary);

			Vec3 portalPos = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
					portal.getYRotation()).getCenter();
			portalPos = portalPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
					direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
					direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

			Vec3 pushVector = portalPos.subtract(entity.position()).multiply(0.08, 0.08, 0.08);
			entity.push(pushVector.x, pushVector.y, pushVector.z);
			if(entity instanceof ServerPlayer player)
				NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new ClientboundTeleportMomentumPacket(player.getDeltaMovement()));
		}
		else if(distance < 0.5D)
		{
			aperture.setFrictionlessTime(2);
		}

		AABB teleportBox = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
				portal.getYRotation());

		if(movementBox.inflate(0.15f).intersects(teleportBox))
		{
			Direction direction = PortalUtilities.getPortalDirection(level, linkID, isPrimary);
			Vector3f normal = direction.step();

			Vec3 portalPos = PortalUtilities.getPortalTeleportBox(portal.getPosition(), portal.getXRotation(),
					portal.getYRotation()).getCenter();
			portalPos = portalPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
					direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
					direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

			Vec3 offsetFromPortal = currentPos.subtract(portalPos);
			Vec3 nextOffsetFromPortal = nextPos.subtract(portalPos);

			double relativePos = offsetFromPortal.dot(new Vec3(normal));
			double nextRelativePos = nextOffsetFromPortal.dot(new Vec3(normal));

			boolean slow = portalPos.closerThan(currentPos, 0.5f) && relativePos >= 0;
			boolean fast = relativePos > 0 && nextRelativePos <= 0;

			AABB boundingBox = PortalUtilities.getPortalBoundingBox(portal.getPosition(), portal.getXRotation(), portal.getYRotation());
			boolean full = portal.getDirection().equals(Direction.UP) && boundingBox.contains(entity.getBoundingBox().getCenter().add(0, entity.getBbHeight()/2f, 0));
			if(slow || fast || full)
			{

				if(direction.getAxis().isHorizontal())
					aperture.setIgnorePortalsTime(5);
				if(link.isInWorld() && link.isInterdimensionalLink())
					aperture.setIgnorePortalsTime(20);

				double xSpeed = entity.position().x()-entity.xOld;
				double ySpeed = entity.position().y()-entity.yOld;
				double zSpeed = entity.position().z()-entity.zOld;

				entity.resetFallDistance();

				ServerLevel targetLevel;
				if(otherPortal.isMoonshot()) targetLevel = (ServerLevel) level;
				else targetLevel = level.getServer().getLevel(otherPortal.getDimension());
				if(targetLevel == null)
					return false;

				boolean smallEntity = entity.getBoundingBox().getYsize() < 1.5f;
				Vec3 relativePosition = new Vec3(otherPortal.isOnWall() ? -0.15 : portal.isOnWall() ? 0 : smallEntity ? -entity.getBoundingBox().getYsize() :
																												  otherPortal.isOnCeiling() ? -entity.getBoundingBox().getYsize()/2 : entity.getBoundingBox().getYsize()/1.25f,
						otherPortal.isOnWall() ? -entity.getBoundingBox().getYsize()/2 : 0, 0);

				//				relativePosition = new Vec3(otherPortal.isOnWall() ? -0.15 : -0.15,
				//						otherPortal.isOnWall() ? -entity.getBoundingBox().getYsize()/2 : 0, 0);

				Vec3 relativeMomentum;
				if(direction.getAxis().isHorizontal())
					relativeMomentum = CoordUtil.toPortalCoords(portal, new Vec3(xSpeed, 0, zSpeed));
				else relativeMomentum = CoordUtil.toPortalCoords(portal, new Vec3(0, entity.getDeltaMovement().y(), 0));

				Vec3 relativeLookAngle = CoordUtil.toPortalCoords(portal, entity.getLookAngle());

				Vec3 destinationPosition;
				Vec3 destinationMomentum;
				Vec3 destinationLookAngle;
				if(!otherPortal.isMoonshot())
				{
					destinationPosition = CoordUtil.fromPortalCoords(otherPortal, relativePosition, true).add(otherPortal.getPosition());
					destinationMomentum = CoordUtil.fromPortalCoords(otherPortal, relativeMomentum, true);
					destinationLookAngle = CoordUtil.fromPortalCoords(otherPortal, relativeLookAngle, true);
				}
				else
				{
					destinationPosition = portal.getPosition().add(0, 1000, 0);
					destinationMomentum = new Vec3(0, 0, 0);
					destinationLookAngle = entity.getLookAngle();
				}

				PortalTravelEvent.Pre event = new PortalTravelEvent.Pre(link, portal, isPrimary, level,
						targetLevel, currentPos, destinationPosition, otherPortal.isMoonshot());

				boolean cancelled = MinecraftForge.EVENT_BUS.post(event);

				targetLevel = (ServerLevel) event.getTargetLevel();
				destinationPosition = event.getTargetPos();

				if(cancelled)
					return false;

				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(
						() -> level.getChunkAt(BlockPos.containing(portal.getPosition()))),
						new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, isPrimary));


				entity.hasImpulse = false;
				entity.teleportTo(targetLevel, destinationPosition.x(), destinationPosition.y(), destinationPosition.z(),
						Set.of(),
						CoordUtil.CoordinateSystems.lookAngleY(destinationLookAngle), entity.getXRot());
				entity.setDeltaMovement(destinationMomentum);
				entity.setOldPosAndRot();

				BlockPos targetBlock = BlockPos.containing(destinationPosition);
				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(targetBlock)),
						new ClientboundPortalSoundsPacket.EnterPortal(link.linkID, !isPrimary));

				aperture.setFrictionlessTime(100 * 20);
				MinecraftForge.EVENT_BUS.post(new PortalTravelEvent.Post(link, portal, isPrimary, level,
						targetLevel, currentPos, destinationPosition, otherPortal.isMoonshot()));

				if(entity instanceof ServerPlayer player)
				{
					player.awardStat(StatisticsInit.TIMES_USED_PORTALS.get(), 1);
					player.connection.send(new ClientboundSetEntityMotionPacket(player));
					aperture.setPortal(Pair.of(linkID, isPrimary));
					aperture.updateDistance();
					PortalTravelCriterion.INSTANCE.trigger(player, portal.getDimension().location(),
							targetLevel.dimension().location(),
							(long) portal.getPosition().distanceToSqr(destinationPosition),
							(long) aperture.verticalDistance, (long) aperture.horizontalDistance, otherPortal.isMoonshot());
				}
				else
				{
					NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity),
							new ClientboundEntityPortalLerpPacket(entity.getId(), destinationPosition.toVector3f(),
									entity.getDeltaMovement().toVector3f(),
									entity.getXRot(),
									CoordUtil.CoordinateSystems.lookAngleY(destinationLookAngle)));
				}

				return true;
			}
		}
		return false;
	}

	public static PortalLink load(CompoundTag tag)
	{
		UUID linkID = tag.getUUID("link");
		Portal primaryPortal = new Portal();
		Portal secondaryPortal = new Portal();

		primaryPortal.load(tag.getCompound("primaryPortal"));
		secondaryPortal.load(tag.getCompound("secondaryPortal"));

		ResourceLocation variantKey = new ResourceLocation(tag.getString("variantKey"));

		PortalLink link = new PortalLink(linkID, variantKey);
		link.setPrimaryPortal(primaryPortal);
		link.setSecondaryPortal(secondaryPortal);

		return link;
	}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();

		tag.putUUID("link", linkID);

		tag.put("primaryPortal", this.primaryPortal.save());
		tag.put("secondaryPortal", this.secondaryPortal.save());

		if(variantKey == null)
			variantKey = new ResourceLocation(ApertureInnovations.MODID, "chell");
		tag.putString("variantKey", variantKey.toString());

		return tag;
	}

	public PortalGunVariant getGunVariant(Level level)
	{
		Registry<PortalGunVariant> registry =  level.getServer().registryAccess()
													.registryOrThrow(PortalGunVariant.REGISTRY_KEY);
		return registry.get(variantKey);
	}

	public Portal getSecondaryPortal()
	{
		return secondaryPortal;
	}

	public void setSecondaryPortal(Portal secondaryPortal)
	{
		this.secondaryPortal = secondaryPortal;
	}

	public Portal getPrimaryPortal()
	{
		return primaryPortal;
	}

	public void setPrimaryPortal(Portal primaryPortal)
	{
		this.primaryPortal = primaryPortal;
	}
}
