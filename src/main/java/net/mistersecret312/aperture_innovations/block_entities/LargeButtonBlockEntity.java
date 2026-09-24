package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.blocks.LargeButtonBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class LargeButtonBlockEntity extends BlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("down");
	protected static final RawAnimation UNPRESS = RawAnimation.begin().thenPlay("up");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public int color = -1;
	public int activeColor = -1;
	public int buttonColor = -1;

	public LargeButtonBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.LARGE_BUTTON.get(), pos, blockState);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag()
	{
		return this.saveWithoutMetadata();
	}

	@Override
	public void saveAdditional(CompoundTag tag)
	{
		tag.putInt("color", this.color);
		tag.putInt("button_color", this.buttonColor);
		tag.putInt("active_color", this.activeColor);

		super.saveAdditional(tag);
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);

		this.color = tag.getInt("color");
		this.buttonColor = tag.getInt("button_color");
		this.activeColor = tag.getInt("active_color");
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<LargeButtonBlockEntity> controller =
				new AnimationController<>(this, "press", 0, this::pressController);
		controller.triggerableAnim("down", PRESS);
		controller.triggerableAnim("up", UNPRESS);
		controllers.add(controller);
	}

	private PlayState pressController(AnimationState<LargeButtonBlockEntity> state)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	public static void tick(Level level, BlockPos pos, BlockState blockState,
							LargeButtonBlockEntity button)
	{
		if(!level.isClientSide())
		{
			if(blockState.getValue(LargeButtonBlock.UPDATE))
			{
				level.setBlock(pos, blockState.setValue(LargeButtonBlock.UPDATE, false), 1 | 2);
				blockState.getBlock().setPlacedBy(level, pos, blockState, null, ItemStack.EMPTY);
			}

		}

		Vec3 centerPos = Vec3.atLowerCornerOf(button.getBlockPos()).add(0.5f, 0f, 0.5f);

		Direction normal = blockState.getValue(LargeButtonBlock.NORMAL);
		AABB box = new AABB(centerPos, centerPos);

		if(normal.equals(Direction.UP))
			box = new AABB(centerPos.x, centerPos.y+0.25, centerPos.z,
					centerPos.x-1f, centerPos.y+0.5f, centerPos.z+1f);
		if(normal.equals(Direction.DOWN))
			box = new AABB(centerPos.x, centerPos.y+0.5, centerPos.z,
					centerPos.x-1f, centerPos.y+0.75f, centerPos.z+1f);

		if(normal.equals(Direction.SOUTH))
			box = new AABB(centerPos.x, centerPos.y-0.5f, centerPos.z-0.25F,
					centerPos.x-1f, centerPos.y+0.5f, centerPos.z);
		if(normal.equals(Direction.NORTH))
			box = new AABB(centerPos.x, centerPos.y-0.5f, centerPos.z+0.25F,
					centerPos.x-1f, centerPos.y+0.5f, centerPos.z);

		if(normal.equals(Direction.WEST))
			box = new AABB(centerPos.x+0.25f, centerPos.y-0.5f, centerPos.z,
					centerPos.x, centerPos.y+0.5f, centerPos.z+1f);
		if(normal.equals(Direction.EAST))
			box = new AABB(centerPos.x-0.25f, centerPos.y-0.5f, centerPos.z,
					centerPos.x, centerPos.y+0.5f, centerPos.z+1f);

		List<Entity> entities = level.getEntities((Entity) null, box, entity -> entity.getBoundingBox().getSize() > 0.25);

		boolean isPressed = blockState.getValue(LargeButtonBlock.PRESSED);
		if(level.isClientSide())
			return;

		if(isPressed)
		{
			if(entities.isEmpty())
			{
				level.setBlock(pos, blockState.setValue(LargeButtonBlock.PRESSED, false), 3);
				button.triggerAnim("press", "up");
				level.playSound(null, pos, SoundInit.LARGE_BUTTON_UP.get(), SoundSource.BLOCKS, 0.25f, 1f);
			}
		}

		if(!isPressed)
		{
			if(!entities.isEmpty())
			{
				level.setBlock(pos, blockState.setValue(LargeButtonBlock.PRESSED, true), 3);
				button.triggerAnim("press", "down");
				level.playSound(null, pos, SoundInit.LARGE_BUTTON_DOWN.get(), SoundSource.BLOCKS, 0.25f, 1f);
			}
		}
	}
}
