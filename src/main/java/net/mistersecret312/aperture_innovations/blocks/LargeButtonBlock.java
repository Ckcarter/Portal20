package net.mistersecret312.aperture_innovations.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.mistersecret312.aperture_innovations.block_entities.LargeButtonBlockEntity;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class LargeButtonBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final DirectionProperty NORMAL = DirectionProperty.create("normal");
	public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");

	public static final IntegerProperty PART = IntegerProperty.create("part", 0, 3);
	public static final BooleanProperty UPDATE = BooleanProperty.create("update");

	public LargeButtonBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState()
									  .setValue(NORMAL, Direction.UP)
									  .setValue(FACING, Direction.NORTH)
									  .setValue(PRESSED, false)
									  .setValue(UPDATE, false)
									  .setValue(PART, 0));
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

	public static BlockPos[] getMultiblockOffsets(BlockPos pos, Direction normal) {
		int dx1 = 0, dy1 = 0, dz1 = 0;
		int dx2 = 0, dy2 = 0, dz2 = 0;

		if (normal.getAxis() == Direction.Axis.Y)
		{
			dx1 = -1;
			dz2 = 1;
		}
		else if (normal.getAxis() == Direction.Axis.X)
		{
			dy1 = -1;
			dz2 = 1;
		}
		else { dx1 = -1; dy2 = -1; }

		return new BlockPos[]
		{
				pos.offset(dx1, dy1, dz1),
				pos.offset(dx2, dy2, dz2),
				pos.offset(dx1 + dx2, dy1 + dy2, dz1 + dz2)
		};
	}

	public BlockPos getMasterPos(BlockPos pos, BlockState state)
	{
		int part = state.getValue(PART);
		if (part == 0) return pos;

		Direction normal = state.getValue(NORMAL);
		int dx1 = 0, dy1 = 0, dz1 = 0;
		int dx2 = 0, dy2 = 0, dz2 = 0;

		if (normal.getAxis() == Direction.Axis.Y) { dx1 = -1; dz2 = 1; }
		else if (normal.getAxis() == Direction.Axis.X) { dy1 = -1; dz2 = 1; }
		else { dx1 = -1; dy2 = -1; }

		if (part == 1) return pos.offset(-dx1, -dy1, -dz1);
		if (part == 2) return pos.offset(-dx2, -dy2, -dz2);
		if (part == 3) return pos.offset(-dx1 - dx2, -dy1 - dy2, -dz1 - dz2);

		return pos;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
	{
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction normal = context.getClickedFace();

		BlockPos[] offsets = getMultiblockOffsets(pos, normal);
		for (BlockPos offsetPos : offsets)
		{
			if (!level.getBlockState(offsetPos).canBeReplaced(context))
				return null;
		}

		BlockState state = this.defaultBlockState();
		Direction direction = context.getClickedFace().getAxis().isHorizontal() ? Direction.UP : context.getHorizontalDirection();

		return state.setValue(NORMAL, normal).setValue(FACING, direction).setValue(PART, 0);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation)
	{
		if(state.getValue(PART) != 0)
			return Blocks.AIR.defaultBlockState();

		state = state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
		state = state.setValue(NORMAL, rotation.rotate(state.getValue(NORMAL)));
		return state.setValue(UPDATE, true);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.getRotation(state.getValue(NORMAL)));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, state, placer, stack);

		BlockPos[] offsets = getMultiblockOffsets(pos, state.getValue(NORMAL));
		BlockState dummyState = state.setValue(PRESSED, false);

		level.setBlock(offsets[0], dummyState.setValue(PART, 1), 1 | 2);
		level.updateNeighborsAt(offsets[0], dummyState.getBlock());
		level.scheduleTick(offsets[0], dummyState.getBlock(), 2);

		level.setBlock(offsets[1], dummyState.setValue(PART, 2), 1 | 2);
		level.updateNeighborsAt(offsets[1], dummyState.getBlock());
		level.scheduleTick(offsets[1], dummyState.getBlock(), 2);

		level.setBlock(offsets[2], dummyState.setValue(PART, 3), 1 | 2);
		level.updateNeighborsAt(offsets[2], dummyState.getBlock());
		level.scheduleTick(offsets[2], dummyState.getBlock(), 2);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
		if (state.getValue(PART) != 0)
		{
			BlockPos masterPos = getMasterPos(currentPos, state);
			BlockState masterState = level.getBlockState(masterPos);

			if (!masterState.is(this))
				return Blocks.AIR.defaultBlockState();

			if (state.getValue(PRESSED) != masterState.getValue(PRESSED))
			{
				BlockState newState = state.setValue(PRESSED, masterState.getValue(PRESSED));
				level.setBlock(currentPos, newState, 1 | 2);
				return newState;
			}
		}
		else
		{
			BlockPos[] offsets = getMultiblockOffsets(currentPos, state.getValue(NORMAL));
			for (BlockPos pos : offsets)
			{
				if(state.getValue(UPDATE) && level instanceof Level lvl)
				{
					state.getBlock().setPlacedBy(lvl, currentPos, state, null, ItemStack.EMPTY);
					return state;
				}
				if (!level.getBlockState(pos).is(this))
					return Blocks.AIR.defaultBlockState();
			}
		}
		return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
	{
		BlockPos masterPos = getMasterPos(pos, state);
		BlockState masterState = level.getBlockState(masterPos);

		if(masterState.is(this) && masterState.getValue(PART) == 0)
		{
			BlockPos[] offsets = getMultiblockOffsets(masterPos, masterState.getValue(NORMAL));
			BlockPos[] allParts = new BlockPos[]{masterPos, offsets[0], offsets[1], offsets[2]};

			if(!player.isCreative())
			{
				BlockEntity be = level.getBlockEntity(masterPos);
				Block.dropResources(masterState, level, masterPos, be, player, player.getMainHandItem());
			}

			for(BlockPos part : allParts)
			{
				if(level.getBlockState(part).is(this))
				{
					level.setBlock(part, Blocks.AIR.defaultBlockState(), 3);
					level.levelEvent(player, 2001, part, Block.getId(masterState));
				}
			}

			for(BlockPos p : allParts)
			{
				level.updateNeighborsAt(p, Blocks.AIR.defaultBlockState().getBlock());
			}
		}
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		return getSignal(state, level, pos, state.getValue(NORMAL).getOpposite());
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction)
	{
		if (state.getValue(PART) != 0)
		{
			BlockPos masterPos = getMasterPos(pos, state);
			BlockState masterState = level.getBlockState(masterPos);
			if (masterState.is(this) && masterState.getValue(PART) == 0)
				return this.getSignal(masterState, level, masterPos, direction);

			return 0;
		}
		if (state.getValue(PRESSED))
			return 15;

		return super.getSignal(state, level, pos, direction);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult hit)
	{
		ItemStack stack = player.getItemInHand(hand);
		if (state.getValue(PART) != 0)
		{
			BlockPos masterPos = getMasterPos(pos, state);
			BlockState masterState = level.getBlockState(masterPos);
			if (masterState.is(this) && masterState.getValue(PART) == 0)
				return this.use(masterState, level, masterPos, player, hand, hit.withPosition(masterPos));

			return InteractionResult.PASS;
		}

		if (stack.is(ItemInit.COLORFUL_GEL.get()))
		{
			ColorfulGelItem gelItem = (ColorfulGelItem) stack.getItem();
			int color = gelItem.getColor(stack);

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof LargeButtonBlockEntity button))
				return InteractionResult.PASS;

			if (player.isCrouching())
				button.activeColor = color;
			else
			{
				Direction normal = state.getValue(LargeButtonBlock.NORMAL);
				Vec3 centerPos = Vec3.atLowerCornerOf(button.getBlockPos()).add(0.5f, 0f, 0.5f);
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

				if(box.inflate(0.1f).contains(hit.getLocation()))
				{
					button.buttonColor = color;
					button.setChanged();
					return InteractionResult.SUCCESS;
				}
				else
					button.color = color;
			}
			button.setChanged();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
	{
		if (state.getValue(PART) != 0)
		{
			BlockPos masterPos = getMasterPos(pos, state);
			BlockState masterState = level.getBlockState(masterPos);
			if (masterState.is(this))
				return getMasterShape(masterState).move(masterPos.getX() - pos.getX(), masterPos.getY() - pos.getY(), masterPos.getZ() - pos.getZ());

			return Shapes.empty();
		}
		return getMasterShape(state);
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	private VoxelShape getMasterShape(BlockState state)
	{
		Direction normal = state.getValue(NORMAL);
		VoxelShape shape = Shapes.box(-0.6875, 0, 0.3125, 0.6875, 0.1875, 1.6875);

		if (normal.equals(Direction.DOWN)) shape = Shapes.box(-0.6875, 0.8125, 0.3125, 0.6875, 1, 1.6875);
		if (normal.equals(Direction.EAST)) shape = Shapes.box(0, -0.6875, 0.3125, 0.1875, 0.6875, 1.6875);
		if (normal.equals(Direction.WEST)) shape = Shapes.box(0.8125, -0.6875, 0.3125, 1.0, 0.6875, 1.6875);
		if (normal.equals(Direction.NORTH)) shape = Shapes.box(-0.6875, -0.6875, 0.8125, 0.6875, 0.6875, 1.0);
		if (normal.equals(Direction.SOUTH)) shape = Shapes.box(-0.6875, -0.6875, 0, 0.6875, 0.6875, 0.1875);

		return shape;
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return state.getValue(PART) == 0 ? RenderShape.ENTITYBLOCK_ANIMATED : RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos)
	{
		return Shapes.empty();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(NORMAL, FACING, PRESSED, PART, UPDATE);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if (state.getValue(PART) == 0)
			return BlockEntityInit.LARGE_BUTTON.get().create(pos, state);

		return null;
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		if (state.getValue(PART) != 0)
			return null;
		return createTickerHelper(blockEntityType, BlockEntityInit.LARGE_BUTTON.get(), LargeButtonBlockEntity::tick);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
		return typeB == typeA ? (BlockEntityTicker<A>)ticker : null;
	}
}