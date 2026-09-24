package net.mistersecret312.aperture_innovations.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import net.mistersecret312.aperture_innovations.init.EntityInit;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class CubeItem extends Item
{
	public CubeItem(Properties properties)
	{
		super(properties);
	}

	public static DispenseItemBehavior getDispenserBehaviour()
	{
		return (blockSource, item) ->
			{
				Level level = blockSource.getLevel();
				Vec3 dirVec = Vec3.atLowerCornerOf(blockSource.getBlockState().getValue(DispenserBlock.FACING).getNormal());
				EntityType<?> entitytype = EntityInit.WEIGHTED_STORAGE_CUBE.get();
				Entity entity = entitytype.spawn((ServerLevel)level, item, null,
						BlockPos.containing(blockSource.getPos().getCenter().add(dirVec)), MobSpawnType.SPAWN_EGG,
						true, false);
				if (entity instanceof WeightedStorageCubeEntity cube)
				{
					cube.setDeltaMovement(
							dirVec.multiply(0.5, dirVec.y < 0 ? 0 : 0.5, 0.5));
					item.shrink(1);
					return item;
				}
				return item;
			};
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components,
								TooltipFlag tooltipFlag)
	{
		super.appendHoverText(stack, level, components, tooltipFlag);

		if(level != null)
		{
			Color hsbColor = Color.getHSBColor(level.getTimeOfDay(1f)*50, 1f, 1f);
			components.add(Component.translatable("tooltip.aperture_innovations.is_colorable").withStyle((style -> style.withColor(
					hsbColor.getRGB()))));
		}
	}


	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Level level = context.getLevel();
		if(level.isClientSide())
			return InteractionResult.SUCCESS;

		ItemStack itemstack = context.getItemInHand();
		BlockPos blockpos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockState blockstate = level.getBlockState(blockpos);
		BlockPos blockpos1;
		if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
			blockpos1 = blockpos;
		} else {
			blockpos1 = blockpos.relative(direction);
		}


		EntityType<?> entitytype = EntityInit.WEIGHTED_STORAGE_CUBE.get();
		if (entitytype.spawn(
				(ServerLevel)level,
				itemstack,
				context.getPlayer(),
				blockpos1,
				MobSpawnType.SPAWN_EGG,
				true,
				false
		) != null)
		{
			itemstack.shrink(1);
			level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
		}

		return InteractionResult.CONSUME;
	}
}
