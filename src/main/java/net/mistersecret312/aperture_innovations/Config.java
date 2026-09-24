package net.mistersecret312.aperture_innovations;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.mistersecret312.aperture_innovations.config.LongFallBootsConfig;
import net.mistersecret312.aperture_innovations.config.PortalGunConfig;

import java.io.File;

public class Config
{
	private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec COMMON_CONFIG;

	static
	{
		COMMON_BUILDER.push("portal_gun");
		PortalGunConfig.init(COMMON_BUILDER);
		COMMON_BUILDER.pop();

		COMMON_BUILDER.push("long_fall_boots");
		LongFallBootsConfig.init(COMMON_BUILDER);
		COMMON_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
	}

	public static void loadConfig(ForgeConfigSpec config, String path)
	{
		ApertureInnovations.LOGGER.info("Loading Config: " + path);
		final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(
				WritingMode.REPLACE).build();
		ApertureInnovations.LOGGER.info("Built config: " + path);
		file.load();
		ApertureInnovations.LOGGER.info("Loaded Config: " + path);
		config.setConfig(file);
	}

}
