package net.mistersecret312.aperture_innovations.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(CollisionGetter.class)
public interface CollisionMixin
{
	/**
	 * @author mistersecret312
	 * @reason Portal need removing of collisions in the blocks behind them for smooth teleportation experience
	 */
	@Overwrite
	default Iterable<VoxelShape> getBlockCollisions(Entity entity, AABB entityBox)
	{
		CollisionGetter getter = ((CollisionGetter) (Object) this);

		Iterable<VoxelShape> shapes =
				() -> new BlockCollisions<>(getter, entity, entityBox, false,
						(pos, shape) -> shape);

		List<VoxelShape> list = new ArrayList<>();
		shapes.forEach(list::add);

		if(entity == null)
		{
			return shapes;
		}
		Level level = entity.level();
		if(level == null)
		{
			return shapes;
		}

		Pair<UUID, Boolean> portal = PortalUtilities.getClosestPortal(entity);

		UUID uuid = portal.getFirst();
		boolean isPrimary = portal.getSecond();
		if(uuid == null)
		{
			return shapes;
		}

		boolean isOpen = PortalUtilities.isPortalOpen(level, uuid);

		Vec3 portalPos = PortalUtilities.getPortalPos(level, uuid, isPrimary);

		Vec2 rotation = PortalUtilities.getPortalRotation(level, uuid, isPrimary);

		AABB portalBox = PortalUtilities.getPortalBoundingBox(portalPos, rotation.x, rotation.y);
		AABB teleportBox = PortalUtilities.getPortalTeleportBox(portalPos, rotation.x, rotation.y);
		AABB floorBox = PortalUtilities.getPortalFloorBox(portalPos, rotation.x, rotation.y).inflate(0d, 0.01d, 0d);

		Direction direction = PortalUtilities.getPortalDirection(level, uuid, isPrimary);

		Vec3 logicPos = teleportBox.getCenter();
		logicPos = logicPos.add(direction.getOpposite().getStepX() * entity.getBbWidth() / 2f,
				direction.getOpposite().getStepY() * entity.getBbHeight() / 1.25f,
				direction.getOpposite().getStepZ() * entity.getBbWidth() / 2f);

		Vec3 currentPos = entity.position().add(0, entity.getBbHeight() / 2f, 0);
		Vec3 offsetFromPortal = currentPos.subtract(logicPos);

		double dotProduct = offsetFromPortal.dot(new Vec3(direction.step()));

		if(entityBox.intersects(floorBox) && isOpen)
			list.add(Shapes.create(floorBox));

		List<VoxelShape> readdVoxels = PortalUtilities.getPortalVoxels(level, uuid, isPrimary,
				portalPos, rotation.x, rotation.y);

		list.removeIf(shape -> !shape.isEmpty() && shape.bounds().intersects(portalBox)
									   && isOpen
									   && dotProduct >= 0);

		for(VoxelShape voxel : readdVoxels)
		{
			if(voxel.isEmpty() || !isOpen)
				continue;

			for(AABB aabb : voxel.toAabbs())
			{
				if(aabb.intersects(entityBox))
				{
					list.add(voxel);
				}
			}
		}

		return list;
	}
}
