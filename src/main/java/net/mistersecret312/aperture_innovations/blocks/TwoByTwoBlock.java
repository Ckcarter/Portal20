package net.mistersecret312.aperture_innovations.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class TwoByTwoBlock extends Block {
    public final static IntegerProperty STATE = IntegerProperty.create("state", 1, 5);

    public TwoByTwoBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(STATE, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STATE);
        super.createBlockStateDefinition(pBuilder);
    }

    // 1 - Single; 2 - Upper Right; 3 - Upper Left; 4 - Lower Right; 5 - Lower Left

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        Block blockSelf = pState.getBlock();
        BlockState blockAbove = pLevel.getBlockState(pPos.above());
        BlockState blockBelow = pLevel.getBlockState(pPos.below());

        //If single, check if can merge with block above or below
        if (pState.getValue(STATE) == 1) {
            if (blockAbove.getBlock() == blockSelf && blockAbove.getValue(STATE) == 1) {
                pLevel.setBlock(pPos.above(), blockAbove.setValue(STATE, 2), 2);
                pLevel.setBlock(pPos, pState.setValue(STATE, 3), 2);
            } else if (blockBelow.getBlock() == blockSelf && blockBelow.getValue(STATE) == 1) {
                pLevel.setBlock(pPos.below(), blockBelow.setValue(STATE, 3), 2);
                pLevel.setBlock(pPos, pState.setValue(STATE, 2), 2);
            }
            //If merged, check if the merged block is still there
        } else {
            if ((pState.getValue(STATE) == 2 && blockBelow.getBlock() != blockSelf) || (pState.getValue(STATE) == 3 && blockAbove.getBlock() != blockSelf)) {
                pLevel.setBlock(pPos, pState.setValue(STATE, 1), 2);
            }
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }
}