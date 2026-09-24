package net.mistersecret312.aperture_innovations.data.portal;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class Portal
{
	private Vec3 position = null;
	private ResourceKey<Level> dimension = null;

	private float xRot = 0;
	private float yRot = 0;

	private Direction direction = null;
	private Direction facing = null;

	private boolean moonshot = false;
	private int color = -1;

	private List<VoxelShape> replaceShapes = new ArrayList<>();

	public Portal()
	{}

	public CompoundTag save()
	{
		CompoundTag tag = new CompoundTag();
		if(position == null)
		{
			tag.putBoolean("moonshot", moonshot);
			return tag;
		}

		CompoundTag posTag = new CompoundTag();
		posTag.putDouble("x", position.x());
		posTag.putDouble("y", position.y());
		posTag.putDouble("z", position.z());
		tag.put("position", posTag);

		tag.putString("dimension", dimension.location().toString());
		tag.putFloat("xRot", xRot);
		tag.putFloat("yRot", yRot);

		tag.putInt("direction", direction.get3DDataValue());
		tag.putInt("facing", facing.get3DDataValue());

		tag.putBoolean("moonshot", moonshot);
		tag.putInt("color", color);

		return tag;
	}

	public void load(CompoundTag tag)
	{
		if(!tag.contains("position"))
		{
			this.setMoonshot(tag.getBoolean("moonshot"));
			return;
		}

		CompoundTag posTag = tag.getCompound("position");
		double x = posTag.getDouble("x");
		double y = posTag.getDouble("y");
		double z = posTag.getDouble("z");
		Vec3 position = new Vec3(x, y, z);
		ResourceKey<Level> dimension = stringToDimension(tag.getString("dimension"));

		float xRot = tag.getFloat("xRot");
		float yRot = tag.getFloat("yRot");

		Direction direction = Direction.from3DDataValue(tag.getInt("direction"));
		Direction facing = Direction.from3DDataValue(tag.getInt("facing"));

		boolean moonshot = tag.getBoolean("moonshot");
		int color = tag.getInt("color");

		this.setPosition(position);
		this.setDimension(dimension);
		this.setXRotation(xRot);
		this.setYRotation(yRot);
		this.setDirection(direction);
		this.setFacing(facing);
		this.setMoonshot(moonshot);
		this.setColor(color);
	}

	public void encode(FriendlyByteBuf buffer)
	{
		if(position == null)
		{
			buffer.writeBoolean(false);
			buffer.writeBoolean(moonshot);
			buffer.writeInt(color);
			return;
		}
		buffer.writeBoolean(true);

		buffer.writeDouble(position.x);
		buffer.writeDouble(position.y);
		buffer.writeDouble(position.z);

		buffer.writeUtf(dimension.location().toString());
		buffer.writeFloat(xRot);
		buffer.writeFloat(yRot);

		buffer.writeInt(direction.get3DDataValue());
		buffer.writeInt(facing.get3DDataValue());

		buffer.writeBoolean(moonshot);
		buffer.writeInt(color);
	}

	public static Portal decode(FriendlyByteBuf buffer)
	{
		Portal portal = new Portal();

		boolean exists = buffer.readBoolean();
		if(!exists)
		{
			portal.setMoonshot(buffer.readBoolean());
			portal.setColor(buffer.readInt());
			return portal;
		}

		double x = buffer.readDouble();
		double y = buffer.readDouble();
		double z = buffer.readDouble();
		Vec3 position = new Vec3(x, y, z);
		ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
			new ResourceLocation(buffer.readUtf()));

		float xRot = buffer.readFloat();
		float yRot = buffer.readFloat();

		Direction direction = Direction.from3DDataValue(buffer.readInt());
		Direction facing = Direction.from3DDataValue(buffer.readInt());

		boolean moonshot = buffer.readBoolean();
		int color = buffer.readInt();

		portal.setPosition(position);
		portal.setDimension(dimension);
		portal.setXRotation(xRot);
		portal.setYRotation(yRot);
		portal.setDirection(direction);
		portal.setFacing(facing);
		portal.setMoonshot(moonshot);
		portal.setColor(color);
		return portal;
	}

	public boolean isOpen()
	{
		return position != null || moonshot;
	}

	public boolean isOnWall()
	{
		return xRot == 0;
	}

	public boolean isOnCeiling()
	{
		return xRot == 90;
	}

	public boolean isOnFloor()
	{
		return xRot == -90;
	}

	public boolean isInWorld()
	{
		return position != null && !moonshot;
	}

	public Vec3 getPosition()
	{
		return position;
	}

	public float getXRotation()
	{
		return xRot;
	}

	public float getYRotation()
	{
		return yRot;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public Direction getFacing()
	{
		return facing;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}

	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	public ResourceKey<Level> getDimension()
	{
		return dimension;
	}

	public int getColor()
	{
		return color;
	}

	public boolean isMoonshot()
	{
		return moonshot;
	}

	public void setPosition(Vec3 position)
	{
		this.position = position;
	}

	public void setDimension(ResourceKey<Level> dimension)
	{
		this.dimension = dimension;
	}

	public void setMoonshot(boolean moonshot)
	{
		this.moonshot = moonshot;
	}

	public void setColor(int color)
	{
		this.color = color;
	}

	public void setXRotation(float xRot)
	{
		this.xRot = xRot;
	}

	public void setYRotation(float yRot)
	{
		this.yRot = yRot;
	}

	public void setReplaceShapes(List<VoxelShape> replaceShapes)
	{
		this.replaceShapes = replaceShapes;
	}

	public List<VoxelShape> getReplaceShapes()
	{
		return replaceShapes;
	}

	public static ResourceKey<Level> stringToDimension(String dimensionString)
	{
		String[] split = dimensionString.split(":");

		if(split.length > 1)
			return ResourceKey.create(ResourceKey.createRegistryKey(
							new ResourceLocation("minecraft", "dimension")),
					new ResourceLocation(split[0], split[1]));

		return null;
	}
}

