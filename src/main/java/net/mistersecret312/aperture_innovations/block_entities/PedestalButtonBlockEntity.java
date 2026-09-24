package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PedestalButtonBlockEntity extends BlockEntity implements GeoBlockEntity
{
	protected static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public int color = -1;
	public int activeColor = -1;
	public int buttonColor = -1;

	public PedestalButtonBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.PEDESTAL_BUTTON.get(), pos, blockState);
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
		tag.putInt("active_color", this.activeColor);
		tag.putInt("button_color", this.buttonColor);

		super.saveAdditional(tag);
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);

		this.color = tag.getInt("color");
		this.activeColor = tag.getInt("active_color");
		this.buttonColor = tag.getInt("button_color");
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<PedestalButtonBlockEntity> controller =
				new AnimationController<>(this, "press", 0, this::pressController);
		controller.triggerableAnim("press", PRESS);
		controllers.add(controller);
	}

	private PlayState pressController(AnimationState<PedestalButtonBlockEntity> state)
	{
		return PlayState.STOP;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
