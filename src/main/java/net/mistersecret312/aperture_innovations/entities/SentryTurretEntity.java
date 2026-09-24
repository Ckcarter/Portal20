package net.mistersecret312.aperture_innovations.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.mistersecret312.aperture_innovations.init.SoundInit;
import net.mistersecret312.aperture_innovations.init.EntityInit;

/**
 * Forge 1.20.1 port of the classic PortalGun EntityTurret state machine.
 * The original 4.0.0 beta turret was used as the behavior reference: deployment/retraction,
 * target search sweep, alternating gun fire, bouncy/knock-over state and persistent turret data.
 */
public class SentryTurretEntity extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FALLEN = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOUNCY = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEFECTIVE = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DIFFERENT = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SINGING = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RETRACTION = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.INT);
    private boolean fireFromLeft = true;
    private int barrel = 1;
    private int searchTimer = 0;
    private int lostTargetTicks = 0;
    private int fireTime = 0;
    private float homeYaw;
    private int singTime = 0;
    private int singCheckTime = 0;

    public SentryTurretEntity(EntityType<? extends SentryTurretEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OPEN, false);
        entityData.define(FALLEN, false);
        entityData.define(BOUNCY, true);
        entityData.define(DEFECTIVE, false);
        entityData.define(DIFFERENT, false);
        entityData.define(SINGING, false);
        entityData.define(RETRACTION, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0D, 4, 24.0F));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 24.0F));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                entity -> entity instanceof Mob && !(entity instanceof SentryTurretEntity)));
    }

    @Override
    public void tick() {
        super.tick();

        // Portal-style sentry turrets are stationary once placed. Keep all target,
        // search, deploy/retract and serenade logic, but never let AI/pathing move the entity.
        getNavigation().stop();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        hasImpulse = false;
        if (tickCount == 1) {
            // About one normal turret in ten becomes the special "I'm Different" variant.
            // The dedicated Different Turret spawn egg still always creates one.
            if (!level().isClientSide && getType() == EntityInit.SENTRY_TURRET.get()
                    && random.nextInt(10) == 0) {
                setDifferent(true);
            }

            // Face away from the player who just placed/spawned the turret.
            Player placer = level().getNearestPlayer(this, 6.0D);
            if (placer != null) {
                double dx = getX() - placer.getX();
                double dz = getZ() - placer.getZ();
                float awayYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                setYRot(awayYaw);
                setYHeadRot(awayYaw);
                yRotO = awayYaw;
                yHeadRotO = awayYaw;
            }
            homeYaw = getYRot();
            if (!level().isClientSide && isDifferent()) {
                level().playSound(null, blockPosition(), SoundInit.TURRET_DIFFERENT_INTRO.get(), SoundSource.HOSTILE, 0.7F, 1.0F);
            }
        }
        if (fireTime > 0) fireTime--;

        if (!level().isClientSide) {
            updateSerenade();
            if (isDifferent() && !isSinging() && tickCount % 20 == 0 && random.nextInt(30) == 0
                    && level().getNearestPlayer(this, 5.0D) != null) {
                level().playSound(null, blockPosition(), SoundInit.TURRET_DIFFERENT_CHATTER.get(), SoundSource.HOSTILE, 0.7F, 1.0F);
            }
        }
        if (isSinging()) {
            setTarget(null);
            setOpen(true);
            setRetraction(Math.min(10, getRetraction() + 1));
            return;
        }

        if (isFallen()) {
            setTarget(null);
            setOpen(false);
            setRetraction(Math.max(0, getRetraction() - 2));
            setDeltaMovement(getDeltaMovement().multiply(0.82D, 1.0D, 0.82D));
            return;
        }

        LivingEntity target = getTarget();
        boolean validTarget = target != null && target.isAlive() && distanceToSqr(target) <= 576.0D && hasLineOfSight(target);

        if (validTarget && !isDefective()) {
            lostTargetTicks = 0;
            searchTimer = 0;
            setOpen(true);
            setRetraction(Math.min(10, getRetraction() + 1));
            faceTarget(target);
        } else {
            if (target != null && !validTarget) setTarget(null);
            if (++lostTargetTicks > 10) beginSearch();
            updateSearchSweep();
        }
    }

    /**
     * 1.7.10-style turret serenade grouping. The classic code scans a 5-block
     * expanded box and only starts when the group contains exactly four turrets.
     * This port lets the normal placed turrets form that quartet directly.
     */
    private void updateSerenade() {
        if (++singCheckTime >= 10) {
            singCheckTime = 0;
            java.util.List<SentryTurretEntity> group = level().getEntitiesOfClass(
                    SentryTurretEntity.class, getBoundingBox().inflate(5.0D),
                    t -> t.isAlive() && !t.isFallen() && t.isDifferent());

            boolean quartet = group.size() == 4;
            if (quartet) {
                // Match the old handleSingStatus rule: every singer must see the same quartet
                // and there must not be a fifth valid turret inside another singer's 5-block box.
                for (SentryTurretEntity singer : group) {
                    java.util.List<SentryTurretEntity> nearby = level().getEntitiesOfClass(
                            SentryTurretEntity.class, singer.getBoundingBox().inflate(5.0D),
                            t -> t.isAlive() && !t.isFallen() && t.isDifferent());
                    if (nearby.size() != 4 || !nearby.containsAll(group)) { quartet = false; break; }
                }
            }

            for (SentryTurretEntity singer : group) singer.setSinging(quartet);
            if (!quartet) setSinging(false);
        }

        if (!isSinging()) { singTime = 0; return; }
        singTime++;

        // Original 1.7.10 behavior: start the Turret Wife Serenade after 41 ticks.
        // Only the quartet leader plays the streamed record so four copies do not overlap.
        if (singTime == 41) {
            java.util.List<SentryTurretEntity> group = level().getEntitiesOfClass(
                    SentryTurretEntity.class, getBoundingBox().inflate(5.0D), SentryTurretEntity::isSinging);
            group.sort(java.util.Comparator.comparingInt(net.minecraft.world.entity.Entity::getId));
            if (group.size() == 4 && group.get(0) == this) {
                level().playSound(null, blockPosition(), SoundInit.TURRET_WIFE_SERENADE.get(),
                        SoundSource.RECORDS, 1.0F, 1.0F);
            }
        }
    }

    public boolean isDifferent() { return entityData.get(DIFFERENT); }
    public void setDifferent(boolean value) { entityData.set(DIFFERENT, value); }

    public boolean isSinging() { return entityData.get(SINGING); }
    public void setSinging(boolean value) { entityData.set(SINGING, value); }

    private void faceTarget(LivingEntity target) {
        getLookControl().setLookAt(target, 45.0F, 45.0F);
        float wantedYaw = (float)(Mth.atan2(target.getZ() - getZ(), target.getX() - getX()) * (180.0D / Math.PI)) - 90.0F;
        setYHeadRot(Mth.rotLerp(0.45F, getYHeadRot(), wantedYaw));
        setYRot(getYHeadRot());
    }

    private void beginSearch() {
        if (searchTimer == 0) searchTimer = 120; // same 120-tick search cycle used by the classic turret
        setOpen(true);
        setRetraction(Math.min(10, getRetraction() + 1));
    }

    private void updateSearchSweep() {
        if (searchTimer <= 0) return;
        searchTimer--;

        float phaseYaw;
        float pitch;
        if (searchTimer > 100) { phaseYaw = -15.0F; pitch = -10.0F; }
        else if (searchTimer > 80) { phaseYaw = 20.0F; pitch = 15.0F; }
        else if (searchTimer > 60) { phaseYaw = 15.0F; pitch = -10.0F; }
        else if (searchTimer > 40) { phaseYaw = -20.0F; pitch = 20.0F; }
        else { phaseYaw = 0.0F; pitch = 0.0F; }

        setYRot(Mth.rotLerp(0.18F, getYRot(), homeYaw + phaseYaw));
        setYHeadRot(getYRot());
        setXRot(Mth.rotLerp(0.18F, getXRot(), pitch));

        if (searchTimer == 0) {
            setOpen(false);
            setRetraction(0);
            lostTargetTicks = 0;
            setYRot(homeYaw);
            setYHeadRot(homeYaw);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        // Intentionally stationary. Rotation and model animations still update normally.
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (isDefective() || isFallen() || !isOpen() || fireTime > 0 || !hasLineOfSight(target)) return;
        fireTime = 2; // classic turret attacks every ~2 ticks
        TurretBulletEntity bullet = new TurretBulletEntity(level(), this, barrel);
        barrel = barrel >= 4 ? 1 : barrel + 1;
        level().addFreshEntity(bullet);
        level().playSound(null, blockPosition(), SoundEvents.DISPENSER_LAUNCH,
                SoundSource.HOSTILE, 0.4F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        // Turret remains anchored in place; impacts can still mark it as fallen.
        if (strength > 0.15D) setFallen(true);
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !level().isClientSide) {
            level().playSound(null, blockPosition(), SoundEvents.IRON_GOLEM_HURT, SoundSource.HOSTILE, 0.45F, 1.6F);
            if (isBouncy() && (amount >= 5.0F || random.nextFloat() < 0.25F)) knockOver();
        }
        return result;
    }

    private void knockOver() {
        setFallen(true);
        setOpen(false);
        setTarget(null);
        searchTimer = 0;
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && isDifferent()) {
            level().playSound(null, blockPosition(), SoundInit.TURRET_DIFFERENT_DEATH.get(), SoundSource.HOSTILE, 0.7F, 1.0F);
        }
        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            // If a member of a singing quartet dies, immediately end the quartet
            // and explicitly stop the streamed serenade on every client.
            boolean wasSinging = isSinging();

            java.util.List<SentryTurretEntity> nearby = serverLevel.getEntitiesOfClass(
                    SentryTurretEntity.class, getBoundingBox().inflate(5.0D),
                    turret -> turret != this && turret.isSinging());

            if (wasSinging || !nearby.isEmpty()) {
                setSinging(false);
                singTime = 0;

                for (SentryTurretEntity turret : nearby) {
                    turret.setSinging(false);
                    turret.singTime = 0;
                    turret.singCheckTime = 0;
                }

                ClientboundStopSoundPacket stopSerenade = new ClientboundStopSoundPacket(
                        SoundInit.TURRET_WIFE_SERENADE.get().getLocation(), SoundSource.RECORDS);

                for (ServerPlayer player : serverLevel.players()) {
                    player.connection.send(stopSerenade);
                }
            }
        }

        super.die(source);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.IRON_GOLEM_DEATH; }

    public boolean isOpen() { return entityData.get(OPEN); }
    public void setOpen(boolean value) { entityData.set(OPEN, value); }
    public boolean isFallen() { return entityData.get(FALLEN); }
    public void setFallen(boolean value) { entityData.set(FALLEN, value); }
    public boolean isBouncy() { return entityData.get(BOUNCY); }
    public void setBouncy(boolean value) { entityData.set(BOUNCY, value); }
    public boolean isDefective() { return entityData.get(DEFECTIVE); }
    public void setDefective(boolean value) { entityData.set(DEFECTIVE, value); }
    public int getRetraction() { return entityData.get(RETRACTION); }
    public void setRetraction(int value) { entityData.set(RETRACTION, Mth.clamp(value, 0, 10)); }
    public boolean isSearching() { return searchTimer > 0; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TurretOpen", isOpen());
        tag.putBoolean("TurretFallen", isFallen());
        tag.putBoolean("TurretBouncy", isBouncy());
        tag.putBoolean("TurretDefective", isDefective());
        tag.putBoolean("TurretDifferent", isDifferent());
        tag.putBoolean("TurretSinging", isSinging());
        tag.putInt("TurretRetraction", getRetraction());
        tag.putFloat("TurretHomeYaw", homeYaw);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOpen(tag.getBoolean("TurretOpen"));
        setFallen(tag.getBoolean("TurretFallen"));
        if (tag.contains("TurretBouncy")) setBouncy(tag.getBoolean("TurretBouncy"));
        setDefective(tag.getBoolean("TurretDefective"));
        setDifferent(tag.getBoolean("TurretDifferent"));
        setSinging(tag.getBoolean("TurretSinging"));
        setRetraction(tag.getInt("TurretRetraction"));
        homeYaw = tag.getFloat("TurretHomeYaw");
    }
}
