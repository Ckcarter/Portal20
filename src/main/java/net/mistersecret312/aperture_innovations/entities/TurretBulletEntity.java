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
    public TurretBulletEntity(EntityType<? extends TurretBulletEntity> type, Level level){super(type,level);}
    public TurretBulletEntity(Level level,SentryTurretEntity owner,int barrel){
        this(EntityInit.TURRET_BULLET.get(),level); setOwner(owner);
        setPos(owner.getX(),owner.getEyeY()-0.15,owner.getZ());
        float yaw=owner.getYHeadRot(), pitch=owner.getXRot();
        Vec3 forward=Vec3.directionFromRotation(pitch,yaw).normalize();
        Vec3 right=new Vec3(-forward.z,0,forward.x);
        double side=(barrel<=2?0.23:-0.23), vertical=(barrel==1||barrel==3?0.06:-0.06);
        setPos(position().add(right.scale(side)).add(0,vertical,0).add(forward.scale(0.75)));
        setDeltaMovement(forward.scale(3.999).add(
            random.nextGaussian()*0.021,random.nextGaussian()*0.021,random.nextGaussian()*0.021));
    }
    @Override protected void defineSynchedData(){}
    @Override public void tick(){
        super.tick();
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