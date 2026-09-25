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
    private static final EntityDataAccessor<Integer> PREV_RETRACTION = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AIM_YAW = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH = SynchedEntityData.defineId(SentryTurretEntity.class, EntityDataSerializers.FLOAT);
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
        entityData.define(PREV_RETRACTION, 0);
        entityData.define(AIM_YAW, 0.0F);
        entityData.define(AIM_PITCH, 0.0F);
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

        // Turrets have one heart (2 health points), including turrets loaded
        // from older saves that previously had a larger health pool.
        if (!level().isClientSide && getHealth() > 2.0F) {
            setHealth(2.0F);
        }

        // Portal-style sentry turrets are stationary once placed. Keep all target,
        // search, deploy/retract and serenade logic, but never let AI/pathing move the entity.
        getNavigation().stop();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        hasImpulse = false;

        // Keep the physical entity at its placement rotation. Target tracking is
        // handled separately by turretAimYaw/turretAimPitch so vanilla mob rotation
        // controllers cannot fight our renderer and cause placement jitter.
        if (tickCount > 1) {
            // Keep the turret anchored, but allow its visible body to face the
            // target while it is actively tracking/firing. When idle it returns
            // to the original placement direction.
            // Original 1.7.10 behavior: render/body yaw is the fixed placement yaw.
            // The moving gun assembly aims independently. Do NOT rotate the whole turret.
            float bodyYaw = homeYaw;
            setYRot(bodyYaw);
            yRotO = bodyYaw;
            setYHeadRot(bodyYaw);
            yHeadRot = bodyYaw;
            yHeadRotO = bodyYaw;
            yBodyRot = bodyYaw;
            yBodyRotO = bodyYaw;
            setXRot(0.0F);
            xRotO = 0.0F;
        }
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
            turretAimYaw = homeYaw;
            turretAimPitch = 0.0F;
            entityData.set(AIM_YAW, turretAimYaw);
            entityData.set(AIM_PITCH, turretAimPitch);
            anchorX = getX();
            anchorY = getY();
            anchorZ = getZ();
            anchorSet = true;
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

        // ---- PortalGun 1.7.10 EntityTurret state-machine port ----
        // The old turret scans every 10 ticks. It does not use vanilla combat goals.
        if (!level().isClientSide && --targetScanTimer <= 0) {
            targetScanTimer = 10;
            LivingEntity found = findClassicTarget();

            if (found != null) {
                setTarget(found);
                hasTarget = true;
                searchTimer = 0;
            } else if (getTarget() != null) {
                // Lost the target: stop shooting. The branch below now closes immediately.
                setTarget(null);
                hasTarget = false;
                searchTimer = 0;
            }
        }

        LivingEntity target = getTarget();
        // A turret may ONLY engage something in front of its weapon face.
        // Do not let combatActivated bypass the front-cone test; that was allowing
        // a previously acquired mob to walk behind the turret and remain a target.
        boolean validTarget = isClassicValidTarget(target)
                && isInDetectionCone(target);

        prevRetraction = getRetraction();
        entityData.set(PREV_RETRACTION, prevRetraction);

        if (validTarget && !isDefective()) {
            combatActivated = true;
            hasTarget = true;
            lostTargetTicks = 0;
            searchTimer = 0;

            // Original EntityTurret deploys first. It only aims/fires at retraction 10.
            setOpen(true);
            if (getRetraction() < 10) {
                setRetraction(Math.min(10, getRetraction() + 1));
            }

            if (getRetraction() == 10) {
                // RETRACTION drives the actual visible gun doors in SentryTurretModel.
                // Never fire until the visible doors are completely deployed.
                setOpen(true);
                faceTarget(target);

                if (!level().isClientSide && fireTime <= 0
                        && getRetraction() == 10
                        && isOpen()
                        && isTargetInFrontForFiring(target)) {
                    fireClassicVolley(target);
                    hasAttacked = true;
                }
            }
        } else {
            // Shooting is finished (target dead, gone, out of range, or LOS lost).
            // Close immediately instead of staying open for the old search sweep.
            if (target != null) {
                setTarget(null);
            }

            hasTarget = false;
            hasAttacked = false;
            searchTimer = 0;
            fireTime = 0;
            setOpen(false);

            // Visibly retract the side gun assemblies back to the closed state.
            if (getRetraction() > 0) {
                setRetraction(Math.max(0, getRetraction() - 1));
            }

            // Bring the turret's aim back home while it closes.
            turretAimYaw = Mth.rotLerp(0.35F, turretAimYaw, homeYaw);
            turretAimPitch = Mth.rotLerp(0.35F, turretAimPitch, 0.0F);
            entityData.set(AIM_YAW, turretAimYaw);
            entityData.set(AIM_PITCH, turretAimPitch);

            if (getRetraction() == 0) {
                combatActivated = false;
                lostTargetTicks = 0;
                turretAimYaw = homeYaw;
                turretAimPitch = 0.0F;
                entityData.set(AIM_YAW, turretAimYaw);
                entityData.set(AIM_PITCH, turretAimPitch);
            }
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
public float getTurretAimYaw() { return entityData.get(AIM_YAW); }
    public float getTurretAimPitch() { return entityData.get(AIM_PITCH); }

    public boolean isDifferent() { return entityData.get(DIFFERENT); }
    public void setDifferent(boolean value) { entityData.set(DIFFERENT, value); }

    public boolean isSinging() { return entityData.get(SINGING); }
    public void setSinging(boolean value) { entityData.set(SINGING, value); }

    private boolean isClassicValidTarget(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this) return false;
        if (entity instanceof SentryTurretEntity) return false;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (!(entity instanceof Player) && !(entity instanceof Mob)) return false;

        // User-requested modern override: activation/combat area is 2 blocks.
        if (distanceToSqr(entity) > 4.0D) return false;

        return hasLineOfSight(entity);
    }

    private LivingEntity findClassicTarget() {
        // Original turret logic periodically scans rather than allowing vanilla
        // target goals to constantly rewrite the target.
        java.util.List<LivingEntity> candidates = level().getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(2.0D),
                this::isClassicValidTarget);

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            // A dormant turret only notices entities crossing its forward path.
            if (!isInDetectionCone(candidate)) continue;
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

        Vec3 eye = new Vec3(getX(), getEyeY(), getZ());
        Vec3 toTarget = target.getEyePosition().subtract(eye);
        if (toTarget.lengthSqr() < 1.0E-6D) return true;

        Vec3 homeForward = Vec3.directionFromRotation(0.0F, homeYaw + 180.0F).normalize();
        double dot = Mth.clamp(homeForward.dot(toTarget.normalize()), -1.0D, 1.0D);
        double angle = Math.acos(dot);

        // Exact normal PortalGun 1.7.10 view threshold: 0.942477796... radians (54 degrees).
        return angle < 0.9424777960769379D && hasLineOfSight(target);
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
        if (target == null || !isClassicValidTarget(target) || !isInDetectionCone(target)) return false;

        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float targetYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) + 90.0F;
        if (Math.abs(Mth.wrapDegrees(targetYaw - homeYaw)) > 40.0F) return false;

        Vec3 muzzle = new Vec3(getX(), getY() + 1.02D, getZ());
        Vec3 toTarget = target.getEyePosition().subtract(muzzle).normalize();
        return getWeaponForwardVector().normalize().dot(toTarget) > 0.94D
                && hasLineOfSight(target);
    }

    private void faceTarget(LivingEntity target) {
        if (target == null) return;

        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getEyeY() - (getY() + getEyeHeight());
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float wantedYaw = (float)(Mth.atan2(dz, dx) * (180.0D / Math.PI)) + 90.0F;
        float wantedPitch = (float)(-(Mth.atan2(dy, horizontal) * (180.0D / Math.PI)));

        // PortalGun 1.7.10 EntityTurret clamps rotation to 40 degrees either
        // side of renderYawOffset and pitch to +/-50 degrees.
        float yawFromHome = Mth.wrapDegrees(wantedYaw - homeYaw);
        float limitedYaw = homeYaw + Mth.clamp(yawFromHome, -40.0F, 40.0F);
        float limitedPitch = Mth.clamp(wantedPitch, -50.0F, 50.0F);

        // Original faceEntity used 10 degrees/tick maximum rotation.
        turretAimYaw = Mth.rotateIfNecessary(turretAimYaw, limitedYaw, 10.0F);
        turretAimPitch = Mth.rotateIfNecessary(turretAimPitch, limitedPitch, 10.0F);

        entityData.set(AIM_YAW, turretAimYaw);
        entityData.set(AIM_PITCH, turretAimPitch);
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
    public int getPrevRetraction() { return entityData.get(PREV_RETRACTION); }
    public float getInterpolatedRetraction(float partialTick) {
        float prev = getPrevRetraction();
        float current = getRetraction();
        return prev + (current - prev) * Mth.clamp(partialTick, 0.0F, 1.0F);
    }
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
        entityData.set(PREV_RETRACTION, getRetraction());
        prevRetraction = getRetraction();
        if (tag.contains("TurretHomeYaw")) homeYaw = tag.getFloat("TurretHomeYaw");
        homeYaw = tag.getFloat("TurretHomeYaw");
    }
}
