package net.mistersecret312.aperture_innovations.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.PedestalButtonBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class PedestalButtonBlock extends BaseEntityBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");

	public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");

	public PedestalButtonBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
									  .setValue(NORMAL, Direction.UP)
									  .setValue(FACING, Direction.NORTH)
									  .setValue(PRESSED, false));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components,
								TooltipFlag tooltipFlag)
	{
		super.appendHoverText(stack, level, components, tooltipFlag);

		if(level instanceof Level realLevel)
		{
			Color hsbColor = Color.getHSBColor(realLevel.getTimeOfDay(1f)*50, 1f, 1f);
			components.add(Component.translatable("tooltip.aperture_innovations.is_colorable").withStyle((style -> style.withColor(
					hsbColor.getRGB()))));
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		state = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
		return state.setValue(NORMAL, rotation.rotate(state.getValue(NORMAL)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.getRotation(state.getValue(NORMAL)));
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		return getSignal(state, level, pos, state.getValue(NORMAL).getOpposite());
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		if(state.getValue(PRESSED))
			return 15;

		return super.getSignal(state, level, pos, direction);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
	{
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		Direction facing = state.getValue(FACING);
		Direction normal = state.getValue(NORMAL);

		VoxelShape shape = Shapes.empty();
		if(normal.getAxis().isVertical())
		{
			if(normal.equals(Direction.UP))
			{
				shape = Shapes.box(0.1875, 0, 0.375, 0.8125, 1.375, 1);
				if(facing.equals(Direction.NORTH)) shape = Shapes.box(0.1875, 0, 0, 0.8125, 1.375, 0.625);
				if(facing.equals(Direction.EAST)) shape = Shapes.box(0.375, 0, 0.1875, 1, 1.375, 0.8125);
				if(facing.equals(Direction.WEST)) shape = Shapes.box(0, 0, 0.1875, 0.625, 1.375, 0.8125);
			}
			else
			{
				shape = Shapes.box(0.1875, -0.375, 0.375, 0.8125, 1, 1);
				if(facing.equals(Direction.NORTH)) shape = Shapes.box(0.1875, -0.375, 0, 0.8125, 1, 0.625);
				if(facing.equals(Direction.EAST)) shape = Shapes.box(0.375, -0.375, 0.1875, 1, 1, 0.8125);
				if(facing.equals(Direction.WEST)) shape = Shapes.box(0, -0.375, 0.1875, 0.625, 1, 0.8125);
			}
		}

		if(normal.getAxis().isHorizontal())
		{
			if(facing.equals(Direction.UP))
			{
				if(normal.equals(Direction.NORTH))
					shape = Shapes.box(0.1875, 0.0, -0.375, 0.8125, 0.625, 1.0);
				if(normal.equals(Direction.SOUTH))
					shape = Shapes.box(0.1875, 0.0, 0.0, 0.8125, 0.625, 1.375);
				if(normal.equals(Direction.EAST))
					shape = Shapes.box(0.0, 0.0, 0.1875, 1.375, 0.625, 0.8125);
				if(normal.equals(Direction.WEST))
					shape = Shapes.box(-0.375, 0.0, 0.1875, 1.0, 0.625, 0.8125);
			}

			if(facing.equals(Direction.DOWN))
			{
				if(normal.equals(Direction.NORTH))
					shape = Shapes.box(0.1875, 0.375, -0.375, 0.8125, 1.0, 1.0);
				if(normal.equals(Direction.SOUTH))
					shape = Shapes.box(0.1875, 0.375, 0.0, 0.8125, 1.0, 1.375);
				if(normal.equals(Direction.EAST))
					shape = Shapes.box(0.0, 0.375, 0.1875, 1.375, 1.0, 0.8125);
				if(normal.equals(Direction.WEST))
					shape = Shapes.box(-0.375, 0.375, 0.1875, 1.0, 1.0, 0.8125);
			}
		}

		return shape;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = this.defaultBlockState();

		Direction direction = context.getClickedFace().getAxis().isHorizontal() ?
									  context.getNearestLookingVerticalDirection().getOpposite()
									  : context.getHorizontalDirection();

		state = state.setValue(NORMAL, context.getClickedFace());
		state = state.setValue(FACING, direction);
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(NORMAL).add(FACING).add(PRESSED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit)
	{
		ItemStack stack = player.getItemInHand(hand);


		BlockEntity blockEntity = level.getBlockEntity(pos);
		if(state.getValue(PRESSED))
			return InteractionResult.PASS;

		if(stack.is(ItemInit.COLORFUL_GEL.get()))
		{
			ColorfulGelItem gelItem = (ColorfulGelItem) stack.getItem();
			int color = gelItem.getColor(stack);

			if(!(blockEntity instanceof PedestalButtonBlockEntity pedestal))
				return InteractionResult.PASS;

			if(player.isCrouching())
				pedestal.buttonColor = color;
			else pedestal.color = color;

			pedestal.setChanged();
			return InteractionResult.SUCCESS;
		}

		if(blockEntity instanceof PedestalButtonBlockEntity pedestalButton)
		{
			pedestalButton.triggerAnim("press", "press");
			level.setBlock(pos, state.setValue(PRESSED, true), 3);
			level.scheduleTick(pos, this, 25);
			if(!level.isClientSide())
				level.playSound(null, pos, SoundInit.PEDESTAL_BUTTON_DOWN.get(), SoundSource.BLOCKS, 0.25f, 1f);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		level.setBlock(pos, state.setValue(PRESSED, false), 3);
		level.playSound(null, pos, SoundInit.PEDESTAL_BUTTON_UP.get(), SoundSource.BLOCKS, 0.25f, 1f);
		super.tick(state, level, pos, random);
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
	{
		return Shapes.empty();
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state)
	{
		return super.useShapeForLightOcclusion(state);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return BlockEntityInit.PEDESTAL_BUTTON.get().create(pos, state);
	}
}
