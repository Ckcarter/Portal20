package net.mistersecret312.aperture_innovations.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.entities.WeightedCompanionCubeEntity;
import net.mistersecret312.aperture_innovations.entities.WeightedStorageCubeEntity;
import net.mistersecret312.aperture_innovations.entities.SentryTurretEntity;
import net.mistersecret312.aperture_innovations.entities.TurretBulletEntity;

public class EntityInit
{
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
			DeferredRegister.create(Registries.ENTITY_TYPE, ApertureInnovations.MODID);

	public static final RegistryObject<EntityType<WeightedStorageCubeEntity>> WEIGHTED_STORAGE_CUBE =
			ENTITY_TYPES.register("weighted_storage_cube",
					() -> EntityType.Builder.<WeightedStorageCubeEntity>of(WeightedStorageCubeEntity::new, MobCategory.MISC)
								  .sized(0.75f, 0.75f)
								  .build(new ResourceLocation(ApertureInnovations.MODID,
										  "weighted_storage_cube").toString()));


	public static final RegistryObject<EntityType<TurretBulletEntity>> TURRET_BULLET =
            ENTITY_TYPES.register("turret_bullet", () -> EntityType.Builder.<TurretBulletEntity>of(TurretBulletEntity::new, MobCategory.MISC)
                    .sized(0.0625F, 0.03125F).clientTrackingRange(8).updateInterval(1)
                    .build(new ResourceLocation(ApertureInnovations.MODID, "turret_bullet").toString()));

	public static final RegistryObject<EntityType<SentryTurretEntity>> SENTRY_TURRET =
            ENTITY_TYPES.register("sentry_turret", () -> EntityType.Builder.<SentryTurretEntity>of(SentryTurretEntity::new, MobCategory.MONSTER)
                    .sized(0.75F, 1.8F).clientTrackingRange(10)
                    .build(new ResourceLocation(ApertureInnovations.MODID, "sentry_turret").toString()));

    public static final RegistryObject<EntityType<SentryTurretEntity>> DIFFERENT_TURRET =
            ENTITY_TYPES.register("different_turret", () -> EntityType.Builder.<SentryTurretEntity>of((type, level) -> {
                        SentryTurretEntity turret = new SentryTurretEntity(type, level);
                        turret.setDifferent(true);
                        return turret;
                    }, MobCategory.MISC)
                    .sized(0.75F, 1.8F).clientTrackingRange(10)
                    .build(new ResourceLocation(ApertureInnovations.MODID, "different_turret").toString()));

	public static final RegistryObject<EntityType<WeightedCompanionCubeEntity>> WEIGHTED_COMPANION_CUBE =
			ENTITY_TYPES.register("weighted_companion_cube",
					() -> EntityType.Builder.<WeightedCompanionCubeEntity>of(WeightedCompanionCubeEntity::new, MobCategory.MISC)
											.sized(0.75f, 0.75f)
											.build(new ResourceLocation(ApertureInnovations.MODID,
													"weighted_companion_cube").toString()));

	public static void register(IEventBus bus)
	{
		ENTITY_TYPES.register(bus);
	}
}
