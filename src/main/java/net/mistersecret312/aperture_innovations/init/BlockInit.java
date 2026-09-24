package net.mistersecret312.aperture_innovations.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.blocks.*;

import java.util.function.Supplier;

public class BlockInit
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ApertureInnovations.MODID);

    public static final RegistryObject<Block> METAL_SURFACE_BLOCK = registerBlock("metal_surface_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> METAL_SURFACE_TILE_BLOCK = registerBlock("metal_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> METAL_SURFACE_1x2_BLOCK = registerBlock("metal_surface_1x2_block",
            () -> new VerticalOneByTwoBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.COLOR_GRAY)));

    public static final RegistryObject<Block> CONCRETE_SURFACE_BLOCK = registerBlock("concrete_surface_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> CONCRETE_SURFACE_TILE_BLOCK = registerBlock("concrete_surface_tile_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> CONCRETE_SURFACE_1x2_BLOCK = registerBlock("concrete_surface_1x2_block",
            () -> new VerticalOneByTwoBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE).mapColor(MapColor.TERRACOTTA_WHITE)));

    public static final RegistryObject<Block> ANTLINE = registerBlock("antline",
            () -> new AntlineBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)
                                                            .isRedstoneConductor((state, getter, pos) -> true)));
    public static final RegistryObject<Block> CHECKMARK = registerBlock("antline_checkmark",
            () -> new AntlineOutputBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));
    public static final RegistryObject<Block> TIMER = registerBlock("antline_timer",
            () -> new AntlineTimerBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));

    public static final RegistryObject<Block> PEDESTAL_BUTTON = registerBlock("pedestal_button",
            () -> new PedestalButtonBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)));
    public static final RegistryObject<Block> LARGE_BUTTON = registerBlock("large_button",
            () -> new LargeButtonBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CONCRETE)));
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
    {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block)
    {
        return ItemInit.ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
    }

    public static void register(IEventBus bus)
    {
        BLOCKS.register(bus);
    }
}
