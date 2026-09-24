package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

import static net.mistersecret312.aperture_innovations.ApertureInnovations.MODID;

public class TagInit
{
	public class Blocks
	{
		public static final TagKey<Block> SHOOT_THROUGH = TagKey.create(
				ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MODID, "shoot_through"));
		public static final TagKey<Block> IMPORTALABLE = TagKey.create(
				ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MODID, "importalable"));
		public static final TagKey<Block> PORTALABLE = TagKey.create(
				ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(MODID, "portalable"));

		public static final TagKey<Block> CONNECTS_TO_ANTLINE = TagKey.create(
				BuiltInRegistries.BLOCK.key(), new ResourceLocation(MODID, "connects_to_antline"));
	}
}
