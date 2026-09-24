package net.mistersecret312.aperture_innovations.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.mistersecret312.aperture_innovations.blocks.enums.OneByTwoStatesEnum;

public class VerticalOneByTwoBlock extends Block {
    public static final EnumProperty<OneByTwoStatesEnum> STATE = EnumProperty.create("state", OneByTwoStatesEnum.class);

    public VerticalOneByTwoBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(STATE, OneByTwoStatesEnum.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(STATE);
        super.createBlockStateDefinition(pBuilder);
    }

    // 1 - Single; 2 - Upper; 3 - Lower

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        Block blockSelf = pState.getBlock();
        BlockState blockAbove = pLevel.getBlockState(pPos.above());
        BlockState blockBelow = pLevel.getBlockState(pPos.below());

        //If single, check if can merge with block above or below
        if (pState.getValue(STATE) == OneByTwoStatesEnum.SINGLE) {
            if (blockAbove.getBlock() == blockSelf && blockAbove.getValue(STATE) == OneByTwoStatesEnum.SINGLE) {
                pLevel.setBlock(pPos.above(), blockAbove.setValue(STATE, OneByTwoStatesEnum.UPPER), 2);
                pLevel.setBlock(pPos, pState.setValue(STATE, OneByTwoStatesEnum.LOWER), 2);
            } else if (blockBelow.getBlock() == blockSelf && blockBelow.getValue(STATE) == OneByTwoStatesEnum.SINGLE) {
                pLevel.setBlock(pPos.below(), blockBelow.setValue(STATE, OneByTwoStatesEnum.LOWER), 2);
                pLevel.setBlock(pPos, pState.setValue(STATE, OneByTwoStatesEnum.UPPER), 2);
            }
            //If merged, check if the merged block is still there
        } else {
            if ((pState.getValue(STATE) == OneByTwoStatesEnum.UPPER && blockBelow.getBlock() != blockSelf) || (pState.getValue(STATE) == OneByTwoStatesEnum.LOWER && blockAbove.getBlock() != blockSelf)) {
                pLevel.setBlock(pPos, pState.setValue(STATE, OneByTwoStatesEnum.SINGLE), 2);
            }
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }
}
