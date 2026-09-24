package net.mistersecret312.aperture_innovations.client.resourcepack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.mistersecret312.aperture_innovations.client.ColorUtil.RGBA;

import static net.mistersecret312.aperture_innovations.ApertureInnovations.MODID;

public class ClientPortalGunVariant
{
	public static final RGBA FULL_COLOR = new RGBA(1F,1F,1F,1F);

	public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(MODID,
			"textures/item/portal_gun.png");
	public static final ResourceLocation DEFAULT_IDLE_CORE_TEXTURE = new ResourceLocation(MODID,
			"textures/item/idle_core.png");
	public static final ResourceLocation DEFAULT_CROSSHAIR_TEXTURE = new ResourceLocation(MODID,
			"textures/item/chell/chell_crosshair.png");

	public static final GenericPortal DEFAULT_GENERIC = new GenericPortal(
			new ResourceLocation(MODID, "textures/block/portal/generic/portal_closed.png"),
			new ResourceLocation(MODID, "textures/block/portal/generic/portal_highlight.png"),
			new ResourceLocation(MODID, "textures/block/portal/generic/portal_vortex.png"),
			new ResourceLocation(MODID, "textures/block/portal/generic/gun_core.png"),
			new ResourceLocation(MODID, "textures/gui/generic_crosshair.png")
			);

	public static final Portal DEFAULT_PRIMARY = new Portal(
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_blue_closed.png"),
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_blue_highlight.png"),
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_blue_vortex.png"),
			new ResourceLocation(MODID, "textures/item/chell/chell_blue_core.png"),
			new ResourceLocation(MODID, "textures/block/portal/portal_mask.png"),
			FULL_COLOR, true,
			new ResourceLocation(MODID, "portal_open_primary"),
			new ResourceLocation(MODID, "portal_gun_fire_primary"),
			new ResourceLocation(MODID, "portal_enter"),
			new ResourceLocation(MODID, "portal_ambient"),
			new ResourceLocation(MODID, "portal_invalid_surface"),
			new ResourceLocation(MODID, "portal_fizzle")
	);

	public static final Portal DEFAULT_SECONDARY = new Portal(
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_orange_closed.png"),
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_orange_highlight.png"),
			new ResourceLocation(MODID, "textures/block/portal/chell/portal_orange_vortex.png"),
			new ResourceLocation(MODID, "textures/item/chell/chell_orange_core.png"),
			new ResourceLocation(MODID, "textures/block/portal/portal_mask.png"),
			FULL_COLOR, true,
			new ResourceLocation(MODID, "portal_open_secondary"),
			new ResourceLocation(MODID, "portal_gun_fire_secondary"),
			new ResourceLocation(MODID, "portal_enter"),
			new ResourceLocation(MODID, "portal_ambient"),
			new ResourceLocation(MODID, "portal_invalid_surface"),
			new ResourceLocation(MODID, "portal_fizzle")
	);

	public static final ClientPortalGunVariant DEFAULT_VARIANT = new ClientPortalGunVariant(
			DEFAULT_TEXTURE, DEFAULT_IDLE_CORE_TEXTURE, DEFAULT_CROSSHAIR_TEXTURE,
			DEFAULT_GENERIC, DEFAULT_PRIMARY, DEFAULT_SECONDARY,
			FULL_COLOR, FULL_COLOR,
			new ResourceLocation(MODID, "portal_gun_activation"),
			new ResourceLocation(MODID, "portal_gun_reset")
	);

	public static final String TEXTURE = "texture";

	public static final String IDLE_CORE_TEXTURE = "idle_core_texture";
	public static final String CROSSHAIR_TEXTURE = "crosshair_texture";

	public static final String GENERIC_PORTAL = "generic_portal";

	public static final String PRIMARY_PORTAL = "primary_portal";
	public static final String SECONDARY_PORTAL = "secondary_portal";

	public static final String PRIMARY_STRIPE_COLOR = "primary_stripe_color";
	public static final String SECONDARY_STRIPE_COLOR = "secondary_stripe_color";

	public static final String ACTIVATION_SOUND = "activation_sound";
	public static final String RESET_SOUND = "reset_sound";

	public static final Codec<ClientPortalGunVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf(TEXTURE).forGetter(ClientPortalGunVariant::getTexture),
			ResourceLocation.CODEC.fieldOf(IDLE_CORE_TEXTURE).forGetter(ClientPortalGunVariant::getIdleCoreTexture),
			ResourceLocation.CODEC.fieldOf(CROSSHAIR_TEXTURE).forGetter(ClientPortalGunVariant::getCrosshairTexture),
			GenericPortal.CODEC.optionalFieldOf(GENERIC_PORTAL, DEFAULT_GENERIC).forGetter(ClientPortalGunVariant::getGenericPortal),
			Portal.CODEC.fieldOf(PRIMARY_PORTAL).forGetter(ClientPortalGunVariant::getPrimaryPortal),
			Portal.CODEC.fieldOf(SECONDARY_PORTAL).forGetter(ClientPortalGunVariant::getSecondaryPortal),
			RGBA.CODEC.fieldOf(PRIMARY_STRIPE_COLOR).forGetter(ClientPortalGunVariant::getPrimaryStripeColor),
			RGBA.CODEC.fieldOf(SECONDARY_STRIPE_COLOR).forGetter(ClientPortalGunVariant::getSecondaryStripeColor),
			ResourceLocation.CODEC.fieldOf(ACTIVATION_SOUND).forGetter(ClientPortalGunVariant::getActivationSound),
			ResourceLocation.CODEC.fieldOf(RESET_SOUND).forGetter(ClientPortalGunVariant::getResetSound)
	).apply(instance, ClientPortalGunVariant::new));

	public final ResourceLocation texture;

	public final ResourceLocation idleCoreTexture;
	public final ResourceLocation crosshairTexture;

	public final GenericPortal genericPortal;

	public final Portal primaryPortal;
	public final Portal secondaryPortal;

	public final RGBA primaryStripeColor;
	public final RGBA secondaryStripeColor;

	public final ResourceLocation activationSound;
	public final ResourceLocation resetSound;

	public ClientPortalGunVariant(ResourceLocation texture, ResourceLocation idleCoreTexture,
								  ResourceLocation crosshairTexture, GenericPortal genericPortal,
								  Portal primaryPortal, Portal secondaryPortal,
								  RGBA primaryStripeColor, RGBA secondaryStripeColor,
								  ResourceLocation activationSound, ResourceLocation resetSound)
	{
		this.texture = texture;
		this.idleCoreTexture = idleCoreTexture;
		this.crosshairTexture = crosshairTexture;

		this.genericPortal = genericPortal;

		this.primaryPortal = primaryPortal;
		this.secondaryPortal = secondaryPortal;

		this.primaryStripeColor = primaryStripeColor;
		this.secondaryStripeColor = secondaryStripeColor;

		this.activationSound = activationSound;
		this.resetSound = resetSound;
	}

	public ResourceLocation getTexture()
	{
		return texture;
	}

	public ResourceLocation getIdleCoreTexture()
	{
		return idleCoreTexture;
	}

	public ResourceLocation getCrosshairTexture()
	{
		return crosshairTexture;
	}

	public GenericPortal getGenericPortal()
	{
		return genericPortal;
	}

	public Portal getPrimaryPortal()
	{
		return primaryPortal;
	}

	public Portal getSecondaryPortal()
	{
		return secondaryPortal;
	}

	public RGBA getPrimaryStripeColor()
	{
		return primaryStripeColor;
	}

	public RGBA getSecondaryStripeColor()
	{
		return secondaryStripeColor;
	}

	public ResourceLocation getActivationSound()
	{
		return activationSound;
	}

	public ResourceLocation getResetSound()
	{
		return resetSound;
	}

	public static class Portal
	{
		public static final String CLOSED_TEXTURE = "closed_texture";
		public static final String HIGHLIGHT_TEXTURE = "highlight_texture";
		public static final String VORTEX_TEXTURE = "vortex_texture";
		public static final String CORE_TEXTURE = "core_texture";

		public static final String MASK_TEXTURE = "portal_mask_texture";

		public static final String COLOR = "color";
		public static final String GENERIC_COLORING = "generic_coloring";

		public static final String OPENING_SOUND = "opening_sound";
		public static final String SHOT_SOUND = "shot_sound";
		public static final String ENTER_SOUND = "enter_sound";
		public static final String AMBIENT_SOUND = "ambient_sound";
		public static final String INVALID_SURFACE_SOUND = "invalid_surface_sound";
		public static final String FIZZLE_SOUND = "fizzle_sound";

		public static final Codec<Portal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf(CLOSED_TEXTURE).forGetter(Portal::getClosedTexture),
				ResourceLocation.CODEC.fieldOf(HIGHLIGHT_TEXTURE).forGetter(Portal::getHighlightTexture),
				ResourceLocation.CODEC.fieldOf(VORTEX_TEXTURE).forGetter(Portal::getVortexTexture),
				ResourceLocation.CODEC.fieldOf(CORE_TEXTURE).forGetter(Portal::getCoreTexture),
				ResourceLocation.CODEC.fieldOf(MASK_TEXTURE).forGetter(Portal::getMaskTexture),

				RGBA.CODEC.optionalFieldOf(COLOR, FULL_COLOR).forGetter(Portal::getColor),
				Codec.BOOL.optionalFieldOf(GENERIC_COLORING, true).forGetter(Portal::isGenericColoring),

				ResourceLocation.CODEC.fieldOf(OPENING_SOUND).forGetter(Portal::getOpeningSound),
				ResourceLocation.CODEC.fieldOf(SHOT_SOUND).forGetter(Portal::getShotSound),
				ResourceLocation.CODEC.fieldOf(ENTER_SOUND).forGetter(Portal::getEnterSound),
				ResourceLocation.CODEC.fieldOf(AMBIENT_SOUND).forGetter(Portal::getAmbientSound),
				ResourceLocation.CODEC.fieldOf(INVALID_SURFACE_SOUND).forGetter(Portal::getInvalidSurfaceSound),
				ResourceLocation.CODEC.fieldOf(FIZZLE_SOUND).forGetter(Portal::getFizzleSound)
		).apply(instance, Portal::new));

		private final ResourceLocation closedTexture;
		private final ResourceLocation highlightTexture;
		private final ResourceLocation vortexTexture;
		private final ResourceLocation coreTexture;

		private final ResourceLocation maskTexture;

		private final RGBA color;
		private final boolean genericColoring;

		private final ResourceLocation openingSound;
		private final ResourceLocation shotSound;
		private final ResourceLocation enterSound;
		private final ResourceLocation ambientSound;
		private final ResourceLocation invalidSurfaceSound;
		private final ResourceLocation fizzleSound;

		public Portal(ResourceLocation closedTexture, ResourceLocation highlightTexture,
					  ResourceLocation vortexTexture, ResourceLocation coreTexture,
					  ResourceLocation maskTexture,
					  RGBA color, boolean genericColoring,
					  ResourceLocation openingSound, ResourceLocation shotSound,
					  ResourceLocation enterSound, ResourceLocation ambientSound,
					  ResourceLocation invalidSurfaceSound, ResourceLocation fizzleSound)
		{
			this.closedTexture = closedTexture;
			this.highlightTexture = highlightTexture;
			this.vortexTexture = vortexTexture;
			this.coreTexture = coreTexture;

			this.maskTexture = maskTexture;

			this.color = color;
			this.genericColoring = genericColoring;

			this.openingSound = openingSound;
			this.shotSound = shotSound;
			this.enterSound = enterSound;
			this.ambientSound = ambientSound;
			this.invalidSurfaceSound = invalidSurfaceSound;
			this.fizzleSound = fizzleSound;
		}

		public ResourceLocation getClosedTexture()
		{
			return closedTexture;
		}

		public ResourceLocation getHighlightTexture()
		{
			return highlightTexture;
		}

		public ResourceLocation getVortexTexture()
		{
			return vortexTexture;
		}

		public ResourceLocation getCoreTexture()
		{
			return coreTexture;
		}

		public ResourceLocation getMaskTexture()
		{
			return maskTexture;
		}

		public RGBA getColor()
		{
			return color;
		}

		public boolean isGenericColoring()
		{
			return genericColoring;
		}

		public ResourceLocation getOpeningSound()
		{
			return openingSound;
		}

		public ResourceLocation getShotSound()
		{
			return shotSound;
		}

		public ResourceLocation getEnterSound()
		{
			return enterSound;
		}

		public ResourceLocation getAmbientSound()
		{
			return ambientSound;
		}

		public ResourceLocation getInvalidSurfaceSound()
		{
			return invalidSurfaceSound;
		}

		public ResourceLocation getFizzleSound()
		{
			return fizzleSound;
		}
	}

	public static class GenericPortal
	{
		public static final String CLOSED_TEXTURE = "closed_texture";
		public static final String HIGHLIGHT_TEXTURE = "highlight_texture";
		public static final String VORTEX_TEXTURE = "vortex_texture";
		public static final String CORE_TEXTURE = "core_texture";

		public static final String CROSSHAIR_TEXTURE = "crosshair_texture";

		public static final Codec<GenericPortal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf(CLOSED_TEXTURE).forGetter(GenericPortal::getClosedTexture),
				ResourceLocation.CODEC.fieldOf(HIGHLIGHT_TEXTURE).forGetter(GenericPortal::getHighlightTexture),
				ResourceLocation.CODEC.fieldOf(VORTEX_TEXTURE).forGetter(GenericPortal::getVortexTexture),
				ResourceLocation.CODEC.fieldOf(CORE_TEXTURE).forGetter(GenericPortal::getCoreTexture),
				ResourceLocation.CODEC.fieldOf(CROSSHAIR_TEXTURE).forGetter(GenericPortal::getCrosshairTexture)
		).apply(instance, GenericPortal::new));

		public final ResourceLocation closedTexture;
		public final ResourceLocation highlightTexture;
		public final ResourceLocation vortexTexture;
		public final ResourceLocation coreTexture;

		public final ResourceLocation crosshairTexture;

		public GenericPortal(ResourceLocation closedTexture, ResourceLocation highlightTexture,
							 ResourceLocation vortexTexture, ResourceLocation coreTexture,
							 ResourceLocation crosshairTexture)
		{
			this.closedTexture = closedTexture;
			this.highlightTexture = highlightTexture;
			this.vortexTexture = vortexTexture;
			this.coreTexture = coreTexture;
			this.crosshairTexture = crosshairTexture;
		}

		public ResourceLocation getVortexTexture()
		{
			return vortexTexture;
		}

		public ResourceLocation getClosedTexture()
		{
			return closedTexture;
		}

		public ResourceLocation getHighlightTexture()
		{
			return highlightTexture;
		}

		public ResourceLocation getCoreTexture()
		{
			return coreTexture;
		}

		public ResourceLocation getCrosshairTexture()
		{
			return crosshairTexture;
		}
	}
}
