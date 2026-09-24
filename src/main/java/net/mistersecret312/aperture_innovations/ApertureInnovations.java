package net.mistersecret312.aperture_innovations;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataPackRegistryEvent;
import net.mistersecret312.aperture_innovations.client.Layers;
import net.mistersecret312.aperture_innovations.client.overlay.CrosshairOverlay;
import net.mistersecret312.aperture_innovations.client.renderer.*;
import net.mistersecret312.aperture_innovations.client.resourcepack.ResourcePackReloadListener;
import net.mistersecret312.aperture_innovations.datapack.PortalGunVariant;
import net.mistersecret312.aperture_innovations.init.*;
import net.mistersecret312.aperture_innovations.init.ItemInit;
import net.mistersecret312.aperture_innovations.init.ItemTabInit;
import net.mistersecret312.aperture_innovations.init.NetworkInit;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItem;
import net.mistersecret312.aperture_innovations.items.ColorfulGelItemProperty;
import net.mistersecret312.aperture_innovations.items.CompanionCubeItem;
import net.mistersecret312.aperture_innovations.items.CubeItem;
import net.mistersecret312.aperture_innovations.entities.SentryTurretEntity;
import net.mistersecret312.aperture_innovations.client.model.SentryTurretModel;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(ApertureInnovations.MODID)
public class ApertureInnovations
{
	public static final String MODID = "aperture_innovations";
	public static final Logger LOGGER = LogUtils.getLogger();

	public ApertureInnovations()
	{
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::entityAttributes);

		MinecraftForge.EVENT_BUS.register(this);

		EntityInit.register(modEventBus);
		ItemInit.register(modEventBus);
		BlockInit.register(modEventBus);
		ItemTabInit.register(modEventBus);
		SoundInit.register(modEventBus);
		StatisticsInit.register(modEventBus);
		RecipeInit.register(modEventBus);
		BlockEntityInit.register(modEventBus);

		AdvancementInit.register();

		modEventBus.addListener(Layers::registerLayers);

		modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) ->
			{
				event.dataPackRegistry(PortalGunVariant.REGISTRY_KEY, PortalGunVariant.CODEC, PortalGunVariant.CODEC);
			});

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "aperture_innovations-common.toml");
		NetworkInit.register();
	}




	private void entityAttributes(EntityAttributeCreationEvent event)
	{
		event.put(EntityInit.SENTRY_TURRET.get(), SentryTurretEntity.createAttributes().build());
		event.put(EntityInit.DIFFERENT_TURRET.get(), SentryTurretEntity.createAttributes().build());
	}

	private void commonSetup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(() ->
			{
				DispenserBlock.registerBehavior(ItemInit.WEIGHTED_COMPANION_CUBE.get(), CompanionCubeItem.getDispenserBehaviour());
				DispenserBlock.registerBehavior(ItemInit.WEIGHTED_STORAGE_CUBE.get(), CubeItem.getDispenserBehaviour());
			});
	}

	// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents
	{
		public static ShaderInstance portalCorridorShaderInstance;
		public static ShaderInstance portalFizzleShaderInstance;

		public static final Lazy<KeyMapping> RESET_PORTAL_GUN = Lazy.of(() -> new KeyMapping("aperture_innovations.portal_gun.reset",
				KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category.aperture_innovations"));
		public static final Lazy<KeyMapping> PICK_UP = Lazy.of(() -> new KeyMapping("aperture_innovations.portal_gun.pick_up",
				KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.category.aperture_innovations"));

		public static final Lazy<KeyMapping> PORTAL_GUN_PRIMARY_FIRE = Lazy.of(() -> new KeyMapping("aperture_innovations.portal_gun.primary_fire",
				KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, "key.category.aperture_innovations"));
		public static final Lazy<KeyMapping> PORTAL_GUN_SECONDARY_FIRE = Lazy.of(() -> new KeyMapping("aperture_innovations.portal_gun.secondary_fire",
				KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "key.category.aperture_innovations"));


		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event)
		{
			event.enqueueWork(()
			-> {
					ItemProperties.register(ItemInit.COLORFUL_GEL.get(), new ResourceLocation(MODID, "colored"), new ColorfulGelItemProperty());
				});
		}

		@SubscribeEvent
		public static void registerShaders(RegisterShadersEvent event) throws IOException
		{
//			event.registerShader(new ShaderInstance(event.getResourceProvider(),
//							new ResourceLocation(MODID, "portal_corridor"),
//							DefaultVertexFormat.POSITION_TEX_COLOR),
//					shaderInstance -> portalCorridorShaderInstance = shaderInstance);

			event.registerShader(new ShaderInstance(event.getResourceProvider(),
							new ResourceLocation(MODID, "portal_fizzler"),
							DefaultVertexFormat.POSITION_TEX_COLOR),
					shaderInstance -> portalFizzleShaderInstance = shaderInstance);
		}


		@SubscribeEvent
		public static void registerTurretLayer(EntityRenderersEvent.RegisterLayerDefinitions event)
		{
			event.registerLayerDefinition(SentryTurretRenderer.LAYER, SentryTurretModel::createBodyLayer);
		}

		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
		{
			event.registerBlockEntityRenderer(BlockEntityInit.ANTLINE.get(), AntlineRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.CHECKMARK.get(), AntlineOutputRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.TIMER.get(), AntlineTimerRenderer::new);

			event.registerBlockEntityRenderer(BlockEntityInit.PEDESTAL_BUTTON.get(), PedestalButtonRenderer::new);
			event.registerBlockEntityRenderer(BlockEntityInit.LARGE_BUTTON.get(), LargeButtonRenderer::new);

			event.registerEntityRenderer(EntityInit.WEIGHTED_STORAGE_CUBE.get(), WeightedStorageCubeRenderer::new);
			event.registerEntityRenderer(EntityInit.WEIGHTED_COMPANION_CUBE.get(), WeightedCompanionCubeRenderer::new);
			event.registerEntityRenderer(EntityInit.SENTRY_TURRET.get(), SentryTurretRenderer::new);
			event.registerEntityRenderer(EntityInit.DIFFERENT_TURRET.get(), SentryTurretRenderer::new);
			event.registerEntityRenderer(EntityInit.TURRET_BULLET.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);

		}

		@SubscribeEvent
		public static void registerClientReload(RegisterClientReloadListenersEvent event)
		{
			ResourcePackReloadListener.ReloadListener.registerReloadListener(event);
		}

		@SubscribeEvent
		public static void registerItemColors(RegisterColorHandlersEvent.Item event)
		{
			ColorfulGelItem gelItem = (ColorfulGelItem) ItemInit.COLORFUL_GEL.get();
			event.register(((stack, color) -> color != 1 ? -1 : gelItem.getColor(stack)), ItemInit.COLORFUL_GEL.get());
		}

		@SubscribeEvent
		public static void registerKeybindings(RegisterKeyMappingsEvent event)
		{
			event.register(RESET_PORTAL_GUN.get());
			event.register(PICK_UP.get());
			event.register(PORTAL_GUN_PRIMARY_FIRE.get());
			event.register(PORTAL_GUN_SECONDARY_FIRE.get());
		}

		@SubscribeEvent
		public static void registerOverlays(RegisterGuiOverlaysEvent event)
		{
			event.registerAboveAll("portal_crosshair", CrosshairOverlay.OVERLAY);
		}
	}
}
