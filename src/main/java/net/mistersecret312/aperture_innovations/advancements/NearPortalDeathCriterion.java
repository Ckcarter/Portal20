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

public class NearPortalDeathCriterion extends SimpleCriterionTrigger<NearPortalDeathCriterion.NearPortalDeathTrigger>
{
	public static final NearPortalDeathCriterion INSTANCE = new NearPortalDeathCriterion();
	public static final ResourceLocation CRITERION_ID = new ResourceLocation(ApertureInnovations.MODID,
			"near_portal_death");

	@Override
	protected NearPortalDeathTrigger createInstance(JsonObject obj, ContextAwarePredicate playerPredicate, DeserializationContext predicateDeserializer)
	{
		Optional<Long> distanceToPortal = Optional.empty();
		Optional<Boolean> isFloor = Optional.empty();

		if(GsonHelper.isNumberValue(obj, "distance"))
			distanceToPortal = Optional.of(GsonHelper.getAsLong(obj, "distance"));

		if(GsonHelper.isBooleanValue(obj, "isFloor"))
			isFloor = Optional.of(GsonHelper.getAsBoolean(obj, "isFloor"));

		return new NearPortalDeathTrigger(playerPredicate, distanceToPortal, isFloor);
	}

	public void trigger(ServerPlayer player, long distanceToPortal, boolean isFloor)
	{
		this.trigger(player, (trigger -> trigger.matches(distanceToPortal, isFloor)));
	}

	@Override
	public ResourceLocation getId()
	{
		return CRITERION_ID;
	}

	public static class NearPortalDeathTrigger extends AbstractCriterionTriggerInstance
	{
		private final Optional<Long> distanceToPortal;
		private final Optional<Boolean> isFloor;

		public NearPortalDeathTrigger(ContextAwarePredicate entity,
									 Optional<Long> distanceToPortal,
								   Optional<Boolean> isFloor)
		{
			super(NearPortalDeathCriterion.CRITERION_ID, entity);
			this.isFloor = isFloor;
			this.distanceToPortal = distanceToPortal;
		}

		public boolean matches(long distanceToPortal, boolean isFloor)
		{
			if(this.distanceToPortal.isPresent())
			{
				if(this.distanceToPortal.get() < distanceToPortal)
					return false;
			}

			if(this.isFloor.isPresent())
				if(this.isFloor.get() != isFloor)
					return false;

			return true;
		}

		public JsonObject serializeToJson(SerializationContext predicateSerializer)
		{
			JsonObject jsonObject = super.serializeToJson(predicateSerializer);

			if(distanceToPortal.isPresent())
				jsonObject.add("distance", new JsonPrimitive(distanceToPortal.get()));
			if(isFloor.isPresent())
				jsonObject.add("floor", new JsonPrimitive(isFloor.get()));

			return jsonObject;
		}
	}
}
