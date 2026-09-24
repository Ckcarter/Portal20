package net.mistersecret312.aperture_innovations.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.mistersecret312.aperture_innovations.ApertureInnovations;

public class ThrownIntoFluidCriterion extends SimpleCriterionTrigger<ThrownIntoFluidCriterion.ThrownIntoLavaTrigger>
{
	public static final ThrownIntoFluidCriterion INSTANCE = new ThrownIntoFluidCriterion();
	public static final ResourceLocation CRITERION_ID = new ResourceLocation(ApertureInnovations.MODID,
			"thrown_into_fluid");

	@Override
	protected ThrownIntoLavaTrigger createInstance(JsonObject obj, ContextAwarePredicate playerPredicate,
												   DeserializationContext predicateDeserializer)
	{
		ItemPredicate itemPredicate = ItemPredicate.fromJson(obj.get("item"));
		FluidPredicate fluidPredicate = FluidPredicate.fromJson(obj.get("fluid"));

		return new ThrownIntoLavaTrigger(playerPredicate, itemPredicate, fluidPredicate);
	}

	public void trigger(ServerPlayer player, ItemStack stack, BlockPos pos)
	{
		this.trigger(player, (trigger -> trigger.matches(stack, player.level(), pos)));
	}

	@Override
	public ResourceLocation getId()
	{
		return CRITERION_ID;
	}

	public static class ThrownIntoLavaTrigger extends AbstractCriterionTriggerInstance
	{
		private final ItemPredicate item;
		private final FluidPredicate fluid;
		public ThrownIntoLavaTrigger(ContextAwarePredicate entity,
									 ItemPredicate item, FluidPredicate fluid)
		{
			super(ThrownIntoFluidCriterion.CRITERION_ID, entity);
			this.item = item;
			this.fluid = fluid;
		}

		public boolean matches(ItemStack stack, Level level, BlockPos blockPos)
		{
			if(!this.item.matches(stack))
				return false;

			if(!this.fluid.matches((ServerLevel) level, blockPos))
				return false;

			return true;
		}

		public JsonObject serializeToJson(SerializationContext predicateSerializer)
		{
			JsonObject jsonObject = super.serializeToJson(predicateSerializer);

			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("fluid", this.fluid.serializeToJson());

			return jsonObject;
		}
	}
}
