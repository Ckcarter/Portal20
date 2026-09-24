package net.mistersecret312.aperture_innovations.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.advancements.ThrownIntoFluidCriterion;
import net.mistersecret312.aperture_innovations.capabilities.ApertureEnergy;
import net.mistersecret312.aperture_innovations.capabilities.HoldEntityCapability;
import net.mistersecret312.aperture_innovations.capabilities.item.ItemEnergyProvider;
import net.mistersecret312.aperture_innovations.client.renderer.PortalGunRenderer;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;
import net.mistersecret312.aperture_innovations.data.PortalLinkData;
import net.mistersecret312.aperture_innovations.data.portal.PortalLink;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.network.ClientboundGunZapSoundPacket;
import net.mistersecret312.aperture_innovations.network.ClientboundPortalSoundsPacket;
import net.mistersecret312.aperture_innovations.utilities.PortalUtilities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PortalGunItem extends Item implements GeoItem
{
	public static final String ENERGY = "Energy";

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	protected static class Animations
	{
		protected static final String MAIN_CONTROLLER = "main";

		protected static final RawAnimation SHOOT = RawAnimation.begin().thenPlay("shoot");
		protected static final RawAnimation RESET = RawAnimation.begin().thenPlay("reset");
		protected static final RawAnimation HOLD = RawAnimation.begin().thenPlayAndHold("hold");
		protected static final RawAnimation LET_GO = RawAnimation.begin().thenPlay("let_go");

		private Animations() {}
	}

	public PortalGunItem(Properties pProperties)
	{
		super(pProperties);

		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	public static ItemStack createPortalGun(ResourceLocation variantKey)
	{
		ItemStack stack = new ItemStack(ItemInit.PORTAL_GUN.get());
		PortalGunItem item = (PortalGunItem) stack.getItem();
		item.setVariant(stack, variantKey);

		return stack;
	}

	@Override
	public boolean isBarVisible(ItemStack stack)
	{
		return PortalGunConfig.portal_gun_uses_energy.get() && getEnergy(stack) != getCapacity();
	}

	@Override
	public int getBarWidth(ItemStack stack)
	{
		return Math.round(13.0F * (float) getEnergy(stack) / getCapacity());
	}

	@Override
	public int getBarColor(ItemStack stack)
	{
		return new Color(0, 200, 255).getRGB();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components,
								TooltipFlag flag)
	{
		components.add(Component.translatable("aperture_innovations.portal_gun.variant_" + getVariant(stack).getPath()).withStyle(
						ChatFormatting.YELLOW));

		int dualityState = getDualityState(stack);

		int primaryStripeColor = getPrimaryStripeColor(stack);
		int secondaryStripeColor = getSecondaryStripeColor(stack);

		int primaryPortalColor = getPrimaryPortalColor(stack);
		int secondaryPortalColor = getSecondaryPortalColor(stack);

		if(dualityState == 0)
				components.add(Component.translatable("item.aperture_innovations.portal_gun.duality_primary").withStyle(ChatFormatting.LIGHT_PURPLE));
		if(dualityState == 1)
				components.add(Component.translatable("item.aperture_innovations.portal_gun.duality_secondary").withStyle(ChatFormatting.LIGHT_PURPLE));

		if(getPair(stack) != null && dualityState != 2)
			components.add(Component.translatable("item.aperture_innovations.portal_gun.paired").withStyle(ChatFormatting.DARK_PURPLE));

		if(PortalGunConfig.portal_gun_uses_energy.get())
		{
			components.add(Component.translatable("item.aperture_innovations.portal_gun.energy").append(ApertureEnergy.energyToString(getEnergy(stack), getCapacity())).withStyle(ChatFormatting.DARK_RED));
		}

		if(primaryPortalColor != -1 || primaryStripeColor != -1 || secondaryPortalColor != -1 || secondaryStripeColor != -1)
			components.add(Component.literal(""));

		if(primaryPortalColor != -1)
			components.add(Component.translatable("item.aperture_innovations.portal_gun.portal_primary_color", Integer.toHexString(primaryPortalColor).toUpperCase()).withStyle(style -> style.withColor(primaryPortalColor)));

		if(secondaryPortalColor != -1)
			components.add(Component.translatable("item.aperture_innovations.portal_gun.portal_secondary_color", Integer.toHexString(secondaryPortalColor).toUpperCase()).withStyle(style -> style.withColor(secondaryPortalColor)));


		if(primaryStripeColor != -1)
			components.add(Component.translatable("item.aperture_innovations.portal_gun.stripe_primary_color", Integer.toHexString(primaryStripeColor).toUpperCase()).withStyle(style -> style.withColor(primaryStripeColor)));

		if(secondaryStripeColor != -1)
			components.add(Component.translatable("item.aperture_innovations.portal_gun.stripe_secondary_color", Integer.toHexString(secondaryStripeColor).toUpperCase()).withStyle(style -> style.withColor(secondaryStripeColor)));

		if(level != null)
		{
			Color hsbColor = Color.getHSBColor(level.getTimeOfDay(1f)*50, 1f, 1f);
			components.add(Component.translatable("tooltip.aperture_innovations.is_colorable").withStyle((style -> style.withColor(
					hsbColor.getRGB()))));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected)
	{

		if(level.isClientSide() || !(entity instanceof Player player))
			return;

		if(!isInitialized(stack)) {
			setInitialized(stack, true);
			PortalLinkData data = PortalLinkData.get(level);

			UUID linkID = getUUID(stack, true);

			PortalLink link = data.getLink(linkID);
			if (link == null) {
				data.addFreshLink(linkID);

				NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(player.blockPosition())), new ClientboundPortalSoundsPacket.GunActivate(linkID, player.blockPosition()));
			}
		}
		else
		{
			PortalLink link = PortalUtilities.getPortalLinks(level).get(getUUID(stack, false));
			if(link != null && link.isOpen() && PortalGunConfig.portal_gun_uses_energy.get())
			{
				stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
					if(cap instanceof ApertureEnergy energy)
					{
						long toExtract = link.isInterdimensionalLink() ?
												 PortalGunConfig.portal_gun_passive_consumption.get() :
												 PortalGunConfig.portal_gun_interdimensional_passive_consumption.get();

						long extracted = energy.extractLongEnergy(toExtract, false);
						if(extracted < toExtract)
						{
							player.displayClientMessage(Component.translatable("item.aperture_innovations.portal_gun.not_enough_energy"), true);
							link.reset(level);
						}
					}
				});
			}

			if(getHeldEntity(stack) != null)
			{
				Integer id = getHeldEntity(stack);
				if(id != null)
				{
					Entity heldEntity = level.getEntity(id);
					if(heldEntity == null)
					{
						setHeldEntity(stack, null);
						NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
								new ClientboundGunZapSoundPacket(player.getUUID(), true));

						level.playSound(null, player.blockPosition(), SoundInit.PORTAL_GUN_HOLD_STOP.get(), SoundSource.PLAYERS);
						this.triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level),
								"main", "let_go");
					}

					if(heldEntity != null && heldEntity.getCapability(CapabilityInit.HOLD).isPresent())
					{
						HoldEntityCapability cap = heldEntity.getCapability(CapabilityInit.HOLD).resolve().get();
						if(!cap.isHeld)
						{
							setHeldEntity(stack, null);
							NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
									new ClientboundGunZapSoundPacket(player.getUUID(), true));

							level.playSound(null, player.blockPosition(), SoundInit.PORTAL_GUN_HOLD_STOP.get(),
									SoundSource.PLAYERS);
							this.triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) level), "main",
									"let_go");
						}
					}
				}

				int tick = getZapTick(stack);
				setZapTick(stack, tick+1);

				int soundTick = getZapSoundTick(stack);
				if(soundTick == 20)
					setZapSoundTick(stack, 0);

				if(soundTick == 0)
				{
					NetworkInit.INSTANCE.send(PacketDistributor.ALL.noArg(),
							new ClientboundGunZapSoundPacket(player.getUUID(), false));
				}

				setZapSoundTick(stack, soundTick+1);
			}
			else
			{
				setZapTick(stack, -1);
				setZapSoundTick(stack, -1);
			}

			if(link != null && isSelected && (getPair(stack) == null || getDualityState(stack) == 2) )
			{
				link.updateColors(level, getPrimaryPortalColor(stack), getSecondaryPortalColor(stack));
				link.updateVariant(level, getVariant(stack));
			}
		}
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity)
	{
		if(entity.isInFluidType())
		{
			Entity owner = entity.getOwner();
			if(owner instanceof Player player)
				ThrownIntoFluidCriterion.INSTANCE.trigger((ServerPlayer) (player), stack, entity.blockPosition());
		}
		return super.onEntityItemUpdate(stack, entity);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return slotChanged;
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
	{
		return getDualityState(stack) != 2;
	}

	public static long getEnergy(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();

		if(tag.contains(ENERGY, Tag.TAG_LONG))
			return tag.getLong(ENERGY);
		else tag.putLong(ENERGY, getCapacity());
		return 0;
	}

	public static long getCapacity()
	{
		return PortalGunConfig.portal_gun_max_energy_stored.get();
	}

	@Override
	public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag)
	{
		return new ItemEnergyProvider(stack)
		{
			@Override
			public long capacity()
			{
				return getCapacity();
			}

			@Override
			public long maxReceive()
			{
				return 10000L;
			}

			@Override
			public long maxExtract()
			{
				return 10000L;
			}

			@Override
			public boolean canReceiveEnergy()
			{
				return true;
			}
		};
	}

	public static BlockHitResult rayTrace(Level level, Player player, double range) {
		float xRot = player.getXRot();
		float yRot = player.getYRot();
		Vec3 eyePos = player.getEyePosition();

		float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
		float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
		float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
		float f5 = Mth.sin(-xRot * ((float)Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;

		Vec3 lookVec = new Vec3(f6, f5, f7);
		Vec3 endPos = eyePos.add(lookVec.scale(range));

		ClipContext context = new ClipContext(eyePos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player)
		{
			@Override
			public VoxelShape getBlockShape(BlockState state, BlockGetter level, BlockPos pos)
			{
				if(state.is(TagInit.Blocks.SHOOT_THROUGH))
					return Shapes.empty();

				return super.getBlockShape(state, level, pos);
			}
		};

		return level.clip(context);
	}

	public boolean isLookingAtMoon(Player player, Level level)
	{
		HitResult hit = player.pick(PortalGunConfig.portal_gun_shoot_range.get(), 0.0F, false);

		if (hit.getType() != HitResult.Type.MISS)
			return false;

		float timeAngle = level.getSunAngle(0.0F);

		double moonX = Math.sin(timeAngle);
		double moonY = -Math.cos(timeAngle);

		Vec3 moonVector = new Vec3(moonX, moonY, 0);

		Vec3 lookVector = player.getLookAngle();

		double dot = lookVector.dot(moonVector);

		return dot > 0.995;
	}

	public UUID getUUID(ItemStack stack, boolean generateIfEmpty)
	{
		CompoundTag tag = stack.getOrCreateTag();
		int dualityState = getDualityState(stack);
		if(dualityState != 2)
		{
			if(tag.contains("pair"))
				return tag.getUUID("pair");
		}

		if(tag.contains("link"))
			return tag.getUUID("link");
		else if(generateIfEmpty)
		{
			UUID uuid = UUID.randomUUID();
			setUUID(stack, uuid);
			return uuid;
		}

		return null;
	}

	public void setUUID(ItemStack stack, UUID uuid)
	{
		stack.getOrCreateTag().putUUID("link", uuid);
	}

	public UUID getPair(ItemStack stack)
	{
		CompoundTag tag = stack.getTag();
		if(tag != null && tag.contains("pair"))
			return tag.getUUID("pair");
		else return null;
	}

	public void setPair(ItemStack stack, UUID pairID)
	{
		if(pairID == null)
		{
			stack.getOrCreateTag().remove("pair");
			return;
		}

		stack.getOrCreateTag().putUUID("pair", pairID);
	}

	public int getDualityState(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("duality"))
			return tag.getInt("duality");
		else return 2;
	}


	public int getPrimaryStripeColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("primaryStripeColor"))
			return tag.getInt("primaryStripeColor");
		else return -1;
	}

	public int getSecondaryStripeColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("secondaryStripeColor"))
			return tag.getInt("secondaryStripeColor");
		else return -1;
	}

	public int getPrimaryPortalColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("primaryPortalColor"))
			return tag.getInt("primaryPortalColor");
		else return -1;
	}

	public int getSecondaryPortalColor(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("secondaryPortalColor"))
			return tag.getInt("secondaryPortalColor");
		else return -1;
	}

	public ResourceLocation getVariant(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("variant"))
			return new ResourceLocation(tag.getString("variant"));
		else return new ResourceLocation(ApertureInnovations.MODID, "chell");
	}

	@Nullable
	public Integer getHeldEntity(ItemStack stack)
	{
		if(stack.getTag() != null && stack.getTag().contains("held_entity"))
			return stack.getTag().getInt("held_entity");

		return null;
	}

	public void setHeldEntity(ItemStack stack, Entity entity)
	{
		if(entity == null && stack.getTag() != null && stack.getTag().contains("held_entity"))
		{
			stack.getTag().remove("held_entity");
		}

		if(entity != null)
			stack.getOrCreateTag().putInt("held_entity", entity.getId());
	}

	public int getZapSoundTick(ItemStack stack)
	{
		if(stack.getTag() != null && stack.getTag().contains("zap_sound_tick"))
			return stack.getTag().getInt("zap_sound_tick");

		return -1;
	}

	public void setZapSoundTick(ItemStack stack, int tick)
	{
		if(tick > 40)
			tick = 0;

		stack.getOrCreateTag().putInt("zap_sound_tick", tick);
	}

	public void setZapTick(ItemStack stack, int tick)
	{
		if(tick > 8)
			tick = 0;

		if(tick < 0 && stack.getTag() != null && stack.getTag().contains("zap_tick"))
			stack.getTag().putInt("zap_tick", tick);
		else stack.getOrCreateTag().putInt("zap_tick", tick);
	}

	public int getZapTick(ItemStack stack)
	{
		if(stack.getTag() != null && stack.getTag().contains("zap_tick"))
			return stack.getTag().getInt("zap_tick");

		return -1;
	}

	public void setDualityState(ItemStack stack, int state)
	{
		stack.getOrCreateTag().putInt("duality", state);
	}

	public void setPrimaryStripeColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("primaryStripeColor", color);
	}

	public void setSecondaryStripeColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("secondaryStripeColor", color);
	}

	public void setPrimaryPortalColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("primaryPortalColor", color);
	}

	public void setSecondaryPortalColor(ItemStack stack, int color)
	{
		stack.getOrCreateTag().putInt("secondaryPortalColor", color);
	}

	public void setVariant(ItemStack stack, ResourceLocation variantKey)
	{
		stack.getOrCreateTag().putString("variant", variantKey.toString());
	}

	public int getLastShotPortal(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("lastPortal"))
			return tag.getInt("lastPortal");
		else return -1;
	}

	public void setLastShotPortal(ItemStack stack, int portal)
	{
		stack.getOrCreateTag().putInt("lastPortal", portal);
	}

	public void setInitialized(ItemStack stack, boolean value)
	{
		stack.getOrCreateTag().putBoolean("init", value);
	}

	public boolean isInitialized(ItemStack stack)
	{
		CompoundTag tag = stack.getOrCreateTag();
		if(tag.contains("init"))
			return tag.getBoolean("init");
		else return false;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private PortalGunRenderer renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new PortalGunRenderer();

				return this.renderer;
			}

			@Override
			public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand,
															  ItemStack itemStack)
			{
				return HumanoidModel.ArmPose.CROSSBOW_HOLD;
			}
		});
	}

	private <T extends PortalGunItem> PlayState handleAnimationState(AnimationState<T> state) {
		return PlayState.STOP;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		AnimationController<PortalGunItem> controller = new AnimationController<>(this,
				Animations.MAIN_CONTROLLER, 0, this::handleAnimationState);
		controller.triggerableAnim("shoot", Animations.SHOOT);
		controller.triggerableAnim("reset", Animations.RESET);
		controller.triggerableAnim("hold", Animations.HOLD);
		controller.triggerableAnim("let_go", Animations.LET_GO);
		controllers.add(controller);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return this.cache;
	}
}
