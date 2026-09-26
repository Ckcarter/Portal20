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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SHOT_X = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SHOT_Y = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SHOT_Z = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private boolean fireFromLeft = true;
    private int barrel = 1;
    private int searchTimer = 0;
    private int lostTargetTicks = 0;
    private int targetScanTimer = 0;
    private boolean combatActivated = false;
    private float turretAimYaw = 0.0F;
    private float turretAimPitch = 0.0F;
    private double anchorX;
    private double anchorY;
    private double anchorZ;
    private boolean anchorSet = false;
    private int fireTime = 0;
    private int firingOpenHoldTicks = 0;
    private int targetLostGraceTicks = 0;
    // Direct ports of the important 1.7.10 EntityTurret state values.
    private int prevRetraction = 0;
    private boolean hasTarget = false;
    private boolean hasAttacked = false;
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
                .add(Attributes.MAX_HEALTH, 2.0D)
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
        entityData.define(AIM_YAW, 0.0F);
        entityData.define(AIM_PITCH, 0.0F);
        entityData.define(SHOT_X, 0.0F);
        entityData.define(SHOT_Y, 0.0F);
        entityData.define(SHOT_Z, -1.0F);
    }

    @Override
    protected void registerGoals() {
        // Classic PortalGun turret behavior is driven by the turret's own state
        // machine in tick(), not by Minecraft's RangedAttackGoal/targetSelector.
        // Keeping vanilla combat goals out prevents them from fighting turret aim,
        // deployment and search timing.
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && getHealth() > 2.0F) setHealth(2.0F);

        // Portal turret is stationary. The BODY always keeps its placement yaw;
        // only AIM_YAW/AIM_PITCH move the gun assembly.
        getNavigation().stop();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        hasImpulse = false;

        if (tickCount == 1) {
            if (!level().isClientSide && getType() == EntityInit.SENTRY_TURRET.get()
                    && random.nextInt(10) == 0) setDifferent(true);

            Player placer = level().getNearestPlayer(this, 6.0D);
            if (placer != null) {
                double dx = getX() - placer.getX();
                double dz = getZ() - placer.getZ();
                float awayYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                setYRot(awayYaw);
            }
            homeYaw = getYRot();
            turretAimYaw = homeYaw;
            turretAimPitch = 0.0F;
            entityData.set(AIM_YAW, turretAimYaw);
            entityData.set(AIM_PITCH, turretAimPitch);
            anchorX = getX(); anchorY = getY(); anchorZ = getZ(); anchorSet = true;

            if (!level().isClientSide && isDifferent()) {
                level().playSound(null, blockPosition(), SoundInit.TURRET_DIFFERENT_INTRO.get(),
                        SoundSource.HOSTILE, 0.7F, 1.0F);
            }
        }

        // Never rotate the physical/model body toward a target.
        setYRot(homeYaw);
        yRotO = homeYaw;
        setYHeadRot(homeYaw);
        yHeadRot = homeYaw;
        yHeadRotO = homeYaw;
        yBodyRot = homeYaw;
        yBodyRotO = homeYaw;
        setXRot(0.0F);
        xRotO = 0.0F;

        if (fireTime > 0) fireTime--;

        if (!level().isClientSide) {
            updateSerenade();
            if (isDifferent() && !isSinging() && tickCount % 20 == 0 && random.nextInt(30) == 0
                    && level().getNearestPlayer(this, 5.0D) != null) {
                level().playSound(null, blockPosition(), SoundInit.TURRET_DIFFERENT_CHATTER.get(),
                        SoundSource.HOSTILE, 0.7F, 1.0F);
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
            return;
        }

        // SERVER owns target selection. Scan every tick: no 10-tick hole where doors
        // can close or tracking can lose a still-valid target.
        if (!level().isClientSide) {
            LivingEntity current = getTarget();
            if (!isCombatTargetValid(current)) {
                current = findClassicTarget();
                setTarget(current);
            }

            if (current != null && isCombatTargetValid(current)) {
                targetLostGraceTicks = 6;
                combatActivated = true;
                hasTarget = true;

                // Aim during OPENING as well as FIRING so the gun is already on target.
                faceTarget(current);

                // The renderer receives the same direct firing line used by projectiles.
                Vec3 laserToTarget = current.getEyePosition().subtract(getLaserMuzzlePosition());
                if (laserToTarget.lengthSqr() > 1.0E-8D) setActualShotDirection(laserToTarget);

                // CLOSED/OPENING -> fully deployed.
                setOpen(true);
                if (getRetraction() < 10) setRetraction(getRetraction() + 1);

                // FULLY OPEN/TRACKING/FIRING. Retraction is pinned for the entire engagement.
                if (getRetraction() >= 10) {
                    setRetraction(10);
                    setOpen(true);
                    if (fireTime <= 0 && isTargetInFrontForFiring(current)) {
                        fireClassicVolley(current);
                        hasAttacked = true;
                    }
                }
            } else {
                if (targetLostGraceTicks > 0) targetLostGraceTicks--;

                if (targetLostGraceTicks > 0 && combatActivated) {
                    // Brief LOS/scan jitter cannot flap the doors.
                    setOpen(true);
                    setRetraction(10);
                } else {
                    // TARGET LOST -> CLOSING.
                    setTarget(null);
                    hasTarget = false;
                    hasAttacked = false;
                    combatActivated = false;
                    setOpen(false);
                    setActualShotDirection(getWeaponForwardVector());
                    if (getRetraction() > 0) setRetraction(getRetraction() - 1);

                    turretAimYaw = Mth.rotateIfNecessary(turretAimYaw, homeYaw, 10.0F);
                    turretAimPitch = Mth.rotateIfNecessary(turretAimPitch, 0.0F, 10.0F);
                    entityData.set(AIM_YAW, turretAimYaw);
                    entityData.set(AIM_PITCH, turretAimPitch);
                }
            }
        }

        prevRetraction = getRetraction();
    }

    /**
     * 1.7.10-style turret serenade grouping. The classic code scans a 5-block
     * expanded box and only starts when the group contains exactly four turrets.
     * This port lets the normal placed turrets form that quartet directly.
     */
    /**
     * The serenade quartet must be physically lined up in one row.
     * Allows a small placement tolerance, but rejects clusters/squares.
     */
    private boolean isDifferentTurretRow(java.util.List<SentryTurretEntity> group) {
        if (group.size() != 4) return false;

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (SentryTurretEntity turret : group) {
            minX = Math.min(minX, turret.getX());
            maxX = Math.max(maxX, turret.getX());
            minZ = Math.min(minZ, turret.getZ());
            maxZ = Math.max(maxZ, turret.getZ());
        }

        // One coordinate must stay essentially constant. The other forms the row.
        boolean rowAlongX = (maxZ - minZ) <= 0.75D && (maxX - minX) >= 2.0D;
        boolean rowAlongZ = (maxX - minX) <= 0.75D && (maxZ - minZ) >= 2.0D;
        return rowAlongX || rowAlongZ;
    }

    private void updateSerenade() {
        if (++singCheckTime >= 10) {
            singCheckTime = 0;
            java.util.List<SentryTurretEntity> group = level().getEntitiesOfClass(
                    SentryTurretEntity.class, getBoundingBox().inflate(5.0D),
                    t -> t.isAlive() && !t.isFallen() && t.isDifferent());

            boolean quartet = group.size() == 4 && isDifferentTurretRow(group);
            if (quartet) {
                // Every singer must see the same exact quartet; a fifth Different turret cancels it.
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
public float getTurretAimYaw() { return entityData.get(AIM_YAW); }
    public float getTurretAimPitch() { return entityData.get(AIM_PITCH); }

    public boolean isDifferent() { return entityData.get(DIFFERENT); }
    public void setDifferent(boolean value) { entityData.set(DIFFERENT, value); }

    public boolean isSinging() { return entityData.get(SINGING); }
    public void setSinging(boolean value) { entityData.set(SINGING, value); }

    private boolean isCombatTargetValid(LivingEntity entity) {
        return isClassicValidTarget(entity)
                && distanceToSqr(entity) <= 400.0D
                && isInDetectionCone(entity)
                && hasLineOfSight(entity);
    }

    private boolean isClassicValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this) return false;
        if (entity instanceof SentryTurretEntity) return false;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        return entity instanceof Player || entity instanceof Mob;
    }

    private LivingEntity findClassicTarget() {
        final double range = 20.0D;
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : level().getEntitiesOfClass(
                LivingEntity.class, getBoundingBox().inflate(range, 6.0D, range),
                this::isCombatTargetValid)) {
            double distance = distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    private boolean isInDetectionCone(LivingEntity target) {
        if (target == null) return false;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        if (dx * dx + dz * dz < 0.0001D) return true;
        float targetYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) + 90.0F;
        return Math.abs(Mth.wrapDegrees(targetYaw - homeYaw)) <= 54.0F;
    }

    public boolean canAimLaserAt(LivingEntity target) {
        return target != null && target.isAlive()
                && isClassicValidTarget(target)
                && isInDetectionCone(target);
    }

    public Vec3 getWeaponForwardVector() {
        // ModelTurret's visible gun/front is 180 degrees opposite the vanilla
        // entity-forward convention used by Vec3.directionFromRotation.
        return Vec3.directionFromRotation(getTurretAimPitch(), getTurretAimYaw() + 180.0F).normalize();
    }

    /** Centerline used by the laser. Individual bullets still start at their own barrels. */
    public Vec3 getLaserMuzzlePosition() {
        return position().add(0.0D, 0.8325D, 0.0D).add(getWeaponForwardVector().scale(0.245D));
    }

    /**
     * Single authoritative combat line. With a live target this points directly
     * at its eye position; idle laser falls back to the visible gun direction.
     */
    public Vec3 getCombatLineDirection() {
        Vec3 synced = new Vec3(entityData.get(SHOT_X), entityData.get(SHOT_Y), entityData.get(SHOT_Z));
        if (synced.lengthSqr() > 1.0E-8D) return synced.normalize();
        return getWeaponForwardVector().normalize();
    }

    public void setActualShotDirection(Vec3 direction) {
        if (direction == null || direction.lengthSqr() < 1.0E-8D) return;
        Vec3 d = direction.normalize();
        entityData.set(SHOT_X, (float)d.x);
        entityData.set(SHOT_Y, (float)d.y);
        entityData.set(SHOT_Z, (float)d.z);
    }

    private Vec3 getFrontMuzzlePosition(int barrelIndex) {
        Vec3 forward = getWeaponForwardVector();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();

        // Four classic barrels: left/right, upper/lower. All start at the FRONT.
        boolean rightBarrel = barrelIndex == 2 || barrelIndex == 4;
        boolean lowerBarrel = barrelIndex == 3 || barrelIndex == 4;
        double side = rightBarrel ? 0.28D : -0.28D;
        double height = lowerBarrel ? 0.88D : 1.12D;

        return new Vec3(getX(), getY() + height, getZ())
                .add(forward.scale(0.48D))
                .add(right.scale(side));
    }

    private boolean isTargetInFrontForFiring(LivingEntity target) {
        if (!isCombatTargetValid(target)) return false;

        Vec3 muzzle = getFrontMuzzlePosition(0);
        Vec3 toTarget = target.getEyePosition().subtract(muzzle).normalize();
        Vec3 weaponForward = getWeaponForwardVector().normalize();

        // Gun must be aimed within ~20 degrees before a volley is released.
        return weaponForward.dot(toTarget) >= 0.94D;
    }

    private void faceTarget(LivingEntity target) {
        if (target == null) return;
        double dx=target.getX()-getX(), dz=target.getZ()-getZ();
        double dy=target.getEyeY()-(getY()+1.05D);
        double horizontal=Math.sqrt(dx*dx+dz*dz);
        float wantedYaw=(float)(Mth.atan2(dz,dx)*(180.0D/Math.PI))+90.0F;
        float wantedPitch=(float)(-(Mth.atan2(dy,horizontal)*(180.0D/Math.PI)));
        float limitedYaw=homeYaw+Mth.clamp(Mth.wrapDegrees(wantedYaw-homeYaw),-40.0F,40.0F);
        float limitedPitch=Mth.clamp(wantedPitch,-50.0F,50.0F);
        turretAimYaw=Mth.rotateIfNecessary(turretAimYaw,limitedYaw,10.0F);
        turretAimPitch=Mth.rotateIfNecessary(turretAimPitch,limitedPitch,10.0F);
        entityData.set(AIM_YAW,turretAimYaw);
        entityData.set(AIM_PITCH,turretAimPitch);
    }

    private void beginSearch() {
        if (searchTimer == 0) searchTimer = 120; // same 120-tick search cycle used by the classic turret
        setOpen(true);
        setRetraction(Math.min(10, getRetraction() + 1));
    }

    private void updateSearchSweep() {
        if (searchTimer <= 0) return;

        // Exact 1.7.10 search phases from EntityTurret.updateSearch().
        float phaseYaw;
        float pitch;
        if (searchTimer > 100) { phaseYaw = -15.0F; pitch = -10.0F; }
        else if (searchTimer > 80) { phaseYaw = 20.0F; pitch = 15.0F; }
        else if (searchTimer > 60) { phaseYaw = 15.0F; pitch = -10.0F; }
        else if (searchTimer > 40) { phaseYaw = -20.0F; pitch = 20.0F; }
        else { phaseYaw = 0.0F; pitch = 0.0F; }

        turretAimYaw = Mth.rotLerp(0.50F, turretAimYaw, homeYaw + phaseYaw);
        turretAimPitch = Mth.rotLerp(0.50F, turretAimPitch, pitch);
        entityData.set(AIM_YAW, turretAimYaw);
        entityData.set(AIM_PITCH, turretAimPitch);

        setYRot(turretAimYaw);
        setYHeadRot(turretAimYaw);
        yBodyRot = turretAimYaw;
        yBodyRotO = turretAimYaw;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
        // Placed Portal turrets are anchored and do not get shoved by entities.
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // Intentionally stationary. Rotation and model animations still update normally.
        setDeltaMovement(0.0D, 0.0D, 0.0D);
    }

    private void fireClassicVolley(LivingEntity target) {
        // Portal "I'm Different" turret: talks/sings/tracks, but NEVER fires bullets.
        if (isDifferent()) {
            return;
        }

        // Hard safety gate: visible doors must be fully open before ANY bullet spawns.
        if (target == null || !target.isAlive() || !isOpen() || getRetraction() != 10) {
            return;
        }

        if (target == null || !target.isAlive() || isDefective() || isFallen()
                || !isOpen() || getRetraction() < 10 || !hasLineOfSight(target)) return;

        fireTime = 2;
        // Original EntityTurret alternated paired barrels:
        // first 1 + 3, then 2 + 4.
        int first = fireFromLeft ? 1 : 2;
        int second = fireFromLeft ? 3 : 4;
        fireFromLeft = !fireFromLeft;

        level().addFreshEntity(new TurretBulletEntity(level(), this, first));
        level().addFreshEntity(new TurretBulletEntity(level(), this, second));

        level().playSound(null, blockPosition(), SoundEvents.DISPENSER_LAUNCH,
                SoundSource.HOSTILE, 0.4F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // Required by RangedAttackMob, but intentionally not used. Classic turret
        // firing is controlled by fireClassicVolley() from the state machine.
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
        tag.putDouble("TurretAnchorX", anchorSet ? anchorX : getX());
        tag.putDouble("TurretAnchorY", anchorSet ? anchorY : getY());
        tag.putDouble("TurretAnchorZ", anchorSet ? anchorZ : getZ());
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
        if (tag.contains("TurretAnchorX")) {
            anchorX = tag.getDouble("TurretAnchorX");
            anchorY = tag.getDouble("TurretAnchorY");
            anchorZ = tag.getDouble("TurretAnchorZ");
            anchorSet = true;
        }
        setSinging(tag.getBoolean("TurretSinging"));
        setRetraction(tag.getInt("TurretRetraction"));
        homeYaw = tag.getFloat("TurretHomeYaw");
    }
}
