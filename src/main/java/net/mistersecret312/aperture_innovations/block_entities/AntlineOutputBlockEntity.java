package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.mistersecret312.aperture_innovations.blocks.AntlineOutputBlock;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.network.ClientboundAntlineOutputUpdatePacket;

public class AntlineOutputBlockEntity extends BlockEntity
{
	public int color = -1;
	public int activeColor = -1;

	public int signal = 0;

	public AntlineOutputBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.CHECKMARK.get(), pos, blockState);
	}

	@Override
	public void setChanged()
	{
		if(getLevel() != null && !getLevel().isClientSide())
		{
			NetworkInit.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> getLevel().getChunkAt(getBlockPos())),
					new ClientboundAntlineOutputUpdatePacket(getBlockPos(), color, activeColor));
		}
		super.setChanged();
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

		tag.putInt("signal", this.signal);

		super.saveAdditional(tag);
	}

	@Override
	public void load(CompoundTag tag)
	{
		super.load(tag);

		this.color = tag.getInt("color");
		this.activeColor = tag.getInt("active_color");

		this.signal = tag.getInt("signal");
	}

	public Direction getNormal()
	{
		return getBlockState().getValue(AntlineOutputBlock.NORMAL);
	}

	public Direction getFacing()
	{
		return getBlockState().getValue(AntlineOutputBlock.FACING);
	}

}
