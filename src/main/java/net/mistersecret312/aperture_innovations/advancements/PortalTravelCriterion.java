package net.mistersecret312.aperture_innovations.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

import java.util.Objects;
import java.util.Optional;

public class PortalTravelCriterion extends SimpleCriterionTrigger<PortalTravelCriterion.PortalTravelTrigger>
{
	public static final PortalTravelCriterion INSTANCE = new PortalTravelCriterion();
	public static final ResourceLocation CRITERION_ID = new ResourceLocation(ApertureInnovations.MODID,
			"portal_travel");

	@Override
	protected PortalTravelTrigger createInstance(JsonObject obj, ContextAwarePredicate playerPredicate, DeserializationContext predicateDeserializer)
	{
		Optional<ResourceLocation> initialDimension = Optional.empty();
		Optional<ResourceLocation> destinationDimension = Optional.empty();
		Optional<Long> teleportDistance = Optional.empty();
		Optional<Long> verticalFlight = Optional.empty();
		Optional<Long> horizontalJump = Optional.empty();
		Optional<Boolean> lunacy = Optional.empty();

		if(GsonHelper.isStringValue(obj, "from"))
			initialDimension = Optional.of(ResourceLocation.tryParse(GsonHelper.getAsString(obj, "from")));

		if(GsonHelper.isStringValue(obj, "to"))
			destinationDimension = Optional.of(ResourceLocation.tryParse(GsonHelper.getAsString(obj, "to")));

		if(GsonHelper.isNumberValue(obj, "distance"))
			teleportDistance = Optional.of(GsonHelper.getAsLong(obj, "distance"));

		if(GsonHelper.isNumberValue(obj, "flight"))
			verticalFlight = Optional.of(GsonHelper.getAsLong(obj, "flight"));

		if(GsonHelper.isNumberValue(obj, "jump"))
			horizontalJump = Optional.of(GsonHelper.getAsLong(obj, "jump"));

		if(GsonHelper.isBooleanValue(obj, "lunacy"))
			lunacy = Optional.of(GsonHelper.getAsBoolean(obj, "lunacy"));

		return new PortalTravelTrigger(playerPredicate, initialDimension, destinationDimension, teleportDistance,
				horizontalJump, verticalFlight, lunacy);
	}

	public void trigger(ServerPlayer player, ResourceLocation initialDimension, ResourceLocation destinationDimension,
						long teleportDistance, long verticalFlight, long horizontalJump, boolean lunacy)
	{
		this.trigger(player, (trigger -> trigger.matches(initialDimension, destinationDimension,
				teleportDistance, horizontalJump, verticalFlight, lunacy)));
	}

	@Override
	public ResourceLocation getId()
	{
		return CRITERION_ID;
	}

	public static class PortalTravelTrigger extends AbstractCriterionTriggerInstance
	{
		private final Optional<ResourceLocation> initialDimension;
		private final Optional<ResourceLocation> destinationDimension;
		private final Optional<Long> teleportDistance;
		private final Optional<Long> horizontalJump;
		private final Optional<Long> verticalFlight;
		private final Optional<Boolean> lunacy;

		public PortalTravelTrigger(ContextAwarePredicate entity,
									 Optional<ResourceLocation> initialDimension, Optional<ResourceLocation> destinationDimension,
									 Optional<Long> teleportDistance, Optional<Long> horizontalJump, Optional<Long> verticalFlight,
								   Optional<Boolean> lunacy)
		{
			super(PortalTravelCriterion.CRITERION_ID, entity);
			this.initialDimension = initialDimension;
			this.destinationDimension = destinationDimension;
			this.teleportDistance = teleportDistance;
			this.horizontalJump = horizontalJump;
			this.verticalFlight = verticalFlight;
			this.lunacy = lunacy;
		}

		public boolean matches(ResourceLocation initialDimension, ResourceLocation destinationDimension,
							   long teleportDistance, long horizontalJump, long verticalFlight, boolean lunacy)
		{
			if(this.initialDimension.isPresent())
			{
				if(!Objects.equals(this.initialDimension.get(), initialDimension))
					return false;
			}

			if(this.destinationDimension.isPresent())
			{
				if(!Objects.equals(this.destinationDimension.get(), destinationDimension))
					return false;
			}

			if(this.teleportDistance.isPresent())
			{
				if(this.teleportDistance.get() > teleportDistance)
					return false;
			}

			if(this.horizontalJump.isPresent())
			{
				if(this.horizontalJump.get() > horizontalJump)
					return false;
			}

			if(this.verticalFlight.isPresent())
			{
				if(this.verticalFlight.get() > verticalFlight)
					return false;
			}

			if(this.lunacy.isPresent())
				if(this.lunacy.get() != lunacy)
					return false;

			return true;
		}

		public JsonObject serializeToJson(SerializationContext predicateSerializer)
		{
			JsonObject jsonObject = super.serializeToJson(predicateSerializer);

			if(initialDimension.isPresent())
				jsonObject.add("from", new JsonPrimitive(initialDimension.get().toString()));
			if(destinationDimension.isPresent())
				jsonObject.add("to", new JsonPrimitive(destinationDimension.get().toString()));
			if(teleportDistance.isPresent())
				jsonObject.add("distance", new JsonPrimitive(teleportDistance.get()));
			if(horizontalJump.isPresent())
				jsonObject.add("distance", new JsonPrimitive(horizontalJump.get()));
			if(verticalFlight.isPresent())
				jsonObject.add("distance", new JsonPrimitive(verticalFlight.get()));
			if(lunacy.isPresent())
				jsonObject.add("lunacy", new JsonPrimitive(lunacy.get()));

			return jsonObject;
		}
	}
}
