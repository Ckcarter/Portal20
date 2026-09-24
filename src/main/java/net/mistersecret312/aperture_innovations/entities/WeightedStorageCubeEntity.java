package net.mistersecret312.aperture_innovations.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.blocks.LargeButtonBlock;
import net.mistersecret312.aperture_innovations.init.EntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.network.ClientboundEntityPortalLerpPacket;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WeightedStorageCubeEntity extends Entity implements GeoEntity
{
	private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(WeightedStorageCubeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WeightedStorageCubeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> ACTIVE_COLOR = SynchedEntityData.defineId(
			WeightedStorageCubeEntity.class, EntityDataSerializers.INT);

	private SimpleContainer container = new SimpleContainer(9);

	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

	public WeightedStorageCubeEntity(EntityType<?> type, Level level)
	{
		super(type, level);
	}

	public WeightedStorageCubeEntity(Level level)
	{
		super(EntityInit.WEIGHTED_STORAGE_CUBE.get(), level);
	}

	@Override
	public void tick()
	{
		super.tick();
		this.tickLerp();

		this.xo = getX();
		this.yo = getY();
		this.zo = getZ();

		if(!isNoGravity())
		{
			this.addDeltaMovement(new Vec3(0f, -0.08f, 0f));
		}

		if(level().getBlockState(this.blockPosition()).getBlock() instanceof LargeButtonBlock button)
		{
			BlockState state = level().getBlockState(blockPosition());

			this.setActive(state.getValue(LargeButtonBlock.PRESSED));
			BlockPos masterPos = button.getMasterPos(blockPosition(), state);
			if(level().getBlockEntity(masterPos) instanceof LargeButtonBlockEntity blockEntity)
			{
				this.setActiveColor(blockEntity.activeColor);
			}
		} else this.setActive(false);

		float friction = 0.85f;
		if(!this.onGround()) friction = 0.95f;

		this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 1f, friction));
		this.move(MoverType.SELF, this.getDeltaMovement());

		if(!level().isClientSide && level().getGameTime() % 4 == 0)
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
					new ClientboundEntityPortalLerpPacket(this.getId(), this.position().toVector3f(),
							this.getDeltaMovement().toVector3f(), this.getXRot(), this.getYRot()));
		}
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source)
	{
		Level level = this.level();

		if(fallDistance > 5F)
		{
			AABB impactZone = this.getBoundingBox().expandTowards(0f, -0.25f, 0f);
			for(Entity entity : level.getEntities(this, impactZone))
			{
				entity.hurt(entity.damageSources().fallingBlock(this), fallDistance/10);
			}
		}

		if(!level.isClientSide())
			level.playSound(null, blockPosition(), SoundInit.CUBE_IMPACT.get(), SoundSource.NEUTRAL, 0.5f, 1f);
		return false;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}

	@Override
	public boolean isPickable()
	{
		return true;
	}

	@Override
	public boolean isPushable()
	{
		return true;
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		return false;
	}

	@Override
	public boolean skipAttackInteraction(Entity entity)
	{
		return true;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand)
	{
		Level level = player.level();
		ItemStack stack = player.getItemInHand(hand);

		if(stack.isEmpty() && !player.isCrouching())
		{
			player.openMenu(new SimpleMenuProvider(
					(id, playerInv, playerEntity) ->  new ChestMenu(MenuType.GENERIC_9x1, id, playerInv, container, 1),
					Component.translatable("container.aperture_innovations.weighted_storage_cube")));
			return InteractionResult.SUCCESS;
		}
		if(stack.isEmpty() && player.isCrouching())
		{
			this.kill();
			ItemEntity item = new ItemEntity(player.level(), this.getX(), this.getY(), this.getZ(),
					ItemInit.WEIGHTED_STORAGE_CUBE.get().getDefaultInstance());
			level.addFreshEntity(item);
			return InteractionResult.SUCCESS;
		}
		if(stack.getItem() instanceof ColorfulGelItem gelItem)
		{
			int color = gelItem.getColor(stack);
			setColor(color);

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.CONSUME;
	}

	private void tickLerp() {
		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}

		if (this.lerpSteps > 0) {
			this.lerpTo(this.lerpX, this.lerpY, this.lerpZ, (float) this.lerpYRot, (float) this.lerpXRot, this.lerpSteps, false);
			this.lerpSteps--;
		}
	}

	public void fizzle()
	{
		this.setNoGravity(true);

		//TODO - Fizzling

		super.kill();
	}

	@Override
	public void kill()
	{
		for(int i = 0; i < this.container.getContainerSize(); i++)
		{
			ItemStack stack = this.container.getItem(i);
			ItemEntity item = new ItemEntity(level(), position().x, position().y, position().z, stack);
			level().addFreshEntity(item);
		}

		super.kill();
	}

	@Override
	public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport)
	{
		this.lerpX = pX;
		this.lerpY = pY;
		this.lerpZ = pZ;
		this.lerpYRot = (double)pYRot;
		this.lerpXRot = (double)pXRot;
		this.lerpSteps = pLerpSteps;
	}

	public int getColor()
	{
		return this.entityData.get(COLOR);
	}

	public int getActiveColor()
	{
		return this.entityData.get(ACTIVE_COLOR);
	}

	public boolean isActive()
	{
		return this.entityData.get(ACTIVE);
	}

	public void setActive(boolean active)
	{
		this.entityData.set(ACTIVE, active);
	}

	public void setColor(int color)
	{
		this.entityData.set(COLOR, color);
	}

	public void setActiveColor(int color)
	{
		this.entityData.set(ACTIVE_COLOR, color);
	}


	@Override
	protected void defineSynchedData()
	{
		this.entityData.define(ACTIVE, false);
		this.entityData.define(COLOR, -1);
		this.entityData.define(ACTIVE_COLOR, -1);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound)
	{
		if (compound.contains("Inventory")) {
			this.container.fromTag(compound.getList("Inventory", Tag.TAG_COMPOUND));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound)
	{
		compound.put("Inventory", this.container.createTag());
	}

	@Override
	public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers)
	{

	}

	@Override
	public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return this.geoCache;
	}
}
