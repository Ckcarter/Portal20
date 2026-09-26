package net.mistersecret312.aperture_innovations.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import net.mistersecret312.aperture_innovations.init.EntityInit;

/** Forge 1.20.1 port of PortalGun 1.7.10 EntityBullet. */
public class TurretBulletEntity extends Projectile {
    private int life;
    private Vec3 straightVelocity = Vec3.ZERO;
    public TurretBulletEntity(EntityType<? extends TurretBulletEntity> type, Level level){super(type,level);}
    public TurretBulletEntity(Level level,SentryTurretEntity owner,int barrel){
        this(EntityInit.TURRET_BULLET.get(),level); setOwner(owner);
        float yaw = owner.getTurretAimYaw();
        float pitch = owner.getTurretAimPitch();

        // The projectile uses the SAME aim values as the visible turret weapon.
        Vec3 forward = owner.getWeaponForwardVector();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();

        // Four front-mounted muzzle positions: upper/lower on the left/right gun pods.
        boolean rightBarrel = barrel == 2 || barrel == 4;
        boolean lowerBarrel = barrel == 3 || barrel == 4;
        double side = rightBarrel ? 0.27D : -0.27D;
        double height = lowerBarrel ? 0.91D : 1.09D;

        Vec3 muzzle = new Vec3(owner.getX(), owner.getY() + height, owner.getZ())
                .add(forward.scale(0.62D))
                .add(right.scale(side));
        setPos(muzzle.x, muzzle.y, muzzle.z);

        // Aim from THIS barrel's muzzle directly to the target at the instant of firing.
        // After launch the velocity never changes: no gravity, no arc, no random spread.
        Vec3 shotDirection = forward.normalize();
        LivingEntity target = owner.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 toTarget = target.getEyePosition().subtract(muzzle);
            if (toTarget.lengthSqr() > 1.0E-8D) shotDirection = toTarget.normalize();
        }
        // Publish the ACTUAL launch vector. The laser renderer reads this synced vector.
        owner.setActualShotDirection(shotDirection);
        straightVelocity = shotDirection.scale(3.999D);
        setDeltaMovement(straightVelocity);
    }
    @Override protected void defineSynchedData(){}

    @Override
    public boolean isNoGravity() {
        return true;
    }
    @Override public void tick(){
        super.tick();

        // Projectile has zero ballistic drop. Preserve the launch vector exactly
        // for its whole flight instead of allowing gravity/drag to bend it.
        if (!straightVelocity.equals(Vec3.ZERO)) {
            setDeltaMovement(straightVelocity);
        } else if (!getDeltaMovement().equals(Vec3.ZERO)) {
            straightVelocity = getDeltaMovement();
        }

        if(++life>12){discard();return;}
        Vec3 from=position(), to=from.add(getDeltaMovement());
        HitResult block=level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
        if(block.getType()!=HitResult.Type.MISS) to=block.getLocation();
        EntityHitResult eh=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(level(),this,from,to,
            getBoundingBox().expandTowards(getDeltaMovement()).inflate(0.3),e->e.isPickable()&&e!=getOwner());
        if(eh!=null){
            Entity hit=eh.getEntity();
            if(hit instanceof LivingEntity living && hit!=getOwner()){
                if(!level().isClientSide && random.nextInt(5)==0){
                    DamageSource src=damageSources().mobProjectile(this,(LivingEntity)getOwner());
                    living.hurt(src,4.0F);
                }
                discard(); return;
            }
            if(hit instanceof WeightedStorageCubeEntity || hit instanceof WeightedCompanionCubeEntity){discard();return;}
        }
        if(block.getType()!=HitResult.Type.MISS){
            BlockHitResult bh=(BlockHitResult)block;
            if(!level().isClientSide && level().getBlockState(bh.getBlockPos()).is(Blocks.GLASS) && random.nextInt(4)==0)
                level().destroyBlock(bh.getBlockPos(),false);
            discard(); return;
        }
        setPos(to.x,to.y,to.z);
    }
    @Override protected void readAdditionalSaveData(CompoundTag t){life=t.getInt("Life");}
    @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Life",life);}
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}