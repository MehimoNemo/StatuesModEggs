package com.svennieke.statues.entity.fakeentity;

import com.google.common.base.Predicate;
import com.svennieke.statues.init.StatuesEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Particles;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class FakeGuardian extends EntityGuardian implements IFakeEntity{

	private static final DataParameter<Boolean> MOVING = EntityDataManager.<Boolean>createKey(FakeGuardian.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TARGET_ENTITY = EntityDataManager.<Integer>createKey(FakeGuardian.class, DataSerializers.VARINT);
    protected float clientSideTailAnimation;
    protected float clientSideTailAnimationO;
    protected float clientSideTailAnimationSpeed;
    protected float clientSideSpikesAnimation;
    protected float clientSideSpikesAnimationO;
    private EntityLivingBase targetedEntity;
    private int clientSideAttackTime;
    private boolean clientSideTouchedGround;
    protected EntityAIWander wander;
	private int lifetime;

    public FakeGuardian(World worldIn)
    {
        super(StatuesEntity.FAKE_GUARDIAN, worldIn);
        this.experienceValue = 10;
        this.setSize(0.85F, 0.85F);
        this.moveHelper = new FakeGuardian.GuardianMoveHelper(this);
        this.clientSideTailAnimation = this.rand.nextFloat();
        this.clientSideTailAnimationO = this.clientSideTailAnimation;
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0.0D);
        damageArmor(0F);
        damageShield(0F);
    }
    
    protected void initEntityAI()
    {
        EntityAIMoveTowardsRestriction entityaimovetowardsrestriction = new EntityAIMoveTowardsRestriction(this, 1.0D);
        this.wander = new EntityAIWander(this, 1.0D, 80);
        this.tasks.addTask(4, new FakeGuardian.AIGuardianAttack(this));
        this.tasks.addTask(5, entityaimovetowardsrestriction);
        this.tasks.addTask(7, this.wander);
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, FakeGuardian.class, 12.0F, 0.01F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.wander.setMutexBits(3);
        entityaimovetowardsrestriction.setMutexBits(3);
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 10, true, false, new FakeGuardian.GuardianTargetSelector(this)));
    }

    protected void registerData()
    {
        super.registerData();
        this.dataManager.register(MOVING, Boolean.valueOf(false));
        this.dataManager.register(TARGET_ENTITY, Integer.valueOf(0));
    }

    public boolean isMoving()
    {
        return ((Boolean)this.dataManager.get(MOVING)).booleanValue();
    }

    private void setMoving(boolean moving)
    {
        this.dataManager.set(MOVING, Boolean.valueOf(moving));
    }

    public int getAttackDuration()
    {
        return 80;
    }

    private void setTargetedEntity(int entityId)
    {
        this.dataManager.set(TARGET_ENTITY, Integer.valueOf(entityId));
    }

    public boolean hasTargetedEntity()
    {
        return ((Integer)this.dataManager.get(TARGET_ENTITY)).intValue() != 0;
    }

    @Nullable
    public EntityLivingBase getTargetedEntity()
    {
        if (!this.hasTargetedEntity())
        {
            return null;
        }
        else if (this.world.isRemote)
        {
            if (this.targetedEntity != null)
            {
                return this.targetedEntity;
            }
            else
            {
                Entity entity = this.world.getEntityByID(((Integer)this.dataManager.get(TARGET_ENTITY)).intValue());

                if (entity instanceof EntityLivingBase)
                {
                    this.targetedEntity = (EntityLivingBase)entity;
                    return this.targetedEntity;
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return this.getAttackTarget();
        }
    }

    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);

        if (TARGET_ENTITY.equals(key))
        {
            this.clientSideAttackTime = 0;
            this.targetedEntity = null;
        }
    }

    /**
     * Get number of ticks, at least during which the living entity will be silent.
     */
    public int getTalkInterval()
    {
        return 160;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public float getEyeHeight()
    {
        return this.height * 0.5F;
    }

    public float getBlockPathWeight(BlockPos p_205022_1_, IWorldReaderBase worldIn) {
        return worldIn.getFluidState(p_205022_1_).isTagged(FluidTags.WATER) ? 10.0F + worldIn.getBrightness(p_205022_1_) - 0.5F : super.getBlockPathWeight(p_205022_1_, worldIn);
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
    public void livingTick()
    {
    	if (!this.world.isRemote)
    	{
    		if (!this.isNoDespawnRequired())
            {
                ++this.lifetime;
            }

            if (this.lifetime >= 2400)
            {
                this.remove();
            }
    	}
    	
    	if (this.world.isRemote) {
            this.clientSideTailAnimationO = this.clientSideTailAnimation;
            if (!this.isInWater()) {
               this.clientSideTailAnimationSpeed = 2.0F;
               if (this.motionY > 0.0D && this.clientSideTouchedGround && !this.isSilent()) {
                  this.world.playSound(this.posX, this.posY, this.posZ, this.getFlopSound(), this.getSoundCategory(), 1.0F, 1.0F, false);
               }

               this.clientSideTouchedGround = this.motionY < 0.0D && this.world.isTopSolid((new BlockPos(this)).down());
            } else if (this.isMoving()) {
               if (this.clientSideTailAnimationSpeed < 0.5F) {
                  this.clientSideTailAnimationSpeed = 4.0F;
               } else {
                  this.clientSideTailAnimationSpeed += (0.5F - this.clientSideTailAnimationSpeed) * 0.1F;
               }
            } else {
               this.clientSideTailAnimationSpeed += (0.125F - this.clientSideTailAnimationSpeed) * 0.2F;
            }

            this.clientSideTailAnimation += this.clientSideTailAnimationSpeed;
            this.clientSideSpikesAnimationO = this.clientSideSpikesAnimation;
            if (!this.isInWaterOrBubbleColumn()) {
               this.clientSideSpikesAnimation = this.rand.nextFloat();
            } else if (this.isMoving()) {
               this.clientSideSpikesAnimation += (0.0F - this.clientSideSpikesAnimation) * 0.25F;
            } else {
               this.clientSideSpikesAnimation += (1.0F - this.clientSideSpikesAnimation) * 0.06F;
            }

            if (this.isMoving() && this.isInWater()) {
               Vec3d vec3d = this.getLook(0.0F);

               for(int i = 0; i < 2; ++i) {
                  this.world.spawnParticle(Particles.BUBBLE, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width - vec3d.x * 1.5D, this.posY + this.rand.nextDouble() * (double)this.height - vec3d.y * 1.5D, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width - vec3d.z * 1.5D, 0.0D, 0.0D, 0.0D);
               }
            }

            if (this.hasTargetedEntity()) {
               if (this.clientSideAttackTime < this.getAttackDuration()) {
                  ++this.clientSideAttackTime;
               }

               EntityLivingBase entitylivingbase = this.getTargetedEntity();
               if (entitylivingbase != null) {
                  this.getLookHelper().setLookPositionWithEntity(entitylivingbase, 90.0F, 90.0F);
                  this.getLookHelper().tick();
                  double d5 = (double)this.getAttackAnimationscalef(0.0F);
                  double d0 = entitylivingbase.posX - this.posX;
                  double d1 = entitylivingbase.posY + (double)(entitylivingbase.height * 0.5F) - (this.posY + (double)this.getEyeHeight());
                  double d2 = entitylivingbase.posZ - this.posZ;
                  double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                  d0 = d0 / d3;
                  d1 = d1 / d3;
                  d2 = d2 / d3;
                  double d4 = this.rand.nextDouble();

                  while(d4 < d3) {
                     d4 += 1.8D - d5 + this.rand.nextDouble() * (1.7D - d5);
                     this.world.spawnParticle(Particles.BUBBLE, this.posX + d0 * d4, this.posY + d1 * d4 + (double)this.getEyeHeight(), this.posZ + d2 * d4, 0.0D, 0.0D, 0.0D);
                  }
               }
            }
         }

         if (this.isInWaterOrBubbleColumn()) {
            this.setAir(300);
         } else if (this.onGround) {
            this.motionY += 0.5D;
            this.motionX += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
            this.motionZ += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.4F);
            this.rotationYaw = this.rand.nextFloat() * 360.0F;
            this.onGround = false;
            this.isAirBorne = true;
         }

         if (this.hasTargetedEntity()) {
            this.rotationYaw = this.rotationYawHead;
         }

         super.livingTick();
    }

    protected SoundEvent getFlopSound()
    {
        return SoundEvents.ENTITY_GUARDIAN_FLOP;
    }

    @OnlyIn(Dist.CLIENT)
    public float getTailAnimation(float p_175471_1_) {
       return this.clientSideTailAnimationO + (this.clientSideTailAnimation - this.clientSideTailAnimationO) * p_175471_1_;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSpikesAnimation(float p_175469_1_) {
       return this.clientSideSpikesAnimationO + (this.clientSideSpikesAnimation - this.clientSideSpikesAnimationO) * p_175469_1_;
    }

    public float getAttackAnimationscalef(float p_175477_1_)
    {
        return ((float)this.clientSideAttackTime + p_175477_1_) / (float)this.getAttackDuration();
    }

    @Override
	@Nullable
    protected ResourceLocation getLootTable()
    {
        return null;
    }
	
	@Override
	protected boolean canDropLoot() {
		return false;
	}

    /**
     * Checks to make sure the light is not too bright where the mob is spawning
     */
    protected boolean isValidLightLevel()
    {
        return true;
    }

    /**
     * Checks that the entity is not colliding with any blocks / liquids
     */
    public boolean isNotColliding(IWorldReaderBase worldIn) {
        return worldIn.checkNoEntityCollision(this, this.getBoundingBox()) && worldIn.isCollisionBoxesEmpty(this, this.getBoundingBox());
     }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean canSpawn(IWorld worldIn, boolean value) {
        return (this.rand.nextInt(20) == 0 || !worldIn.canBlockSeeSky(new BlockPos(this))) && super.canSpawn(worldIn, value);
    }
    
    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.wander != null)
        {
            this.wander.makeUpdate();
        }

        return super.attackEntityFrom(source, amount);
    }

    /**
     * The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently
     * use in wolves.
     */
    public int getVerticalFaceSpeed()
    {
        return 180;
    }

    public void travel(float p_191986_1_, float p_191986_2_, float p_191986_3_)
    {
        if (this.isServerWorld() && this.isInWater())
        {
            this.moveRelative(p_191986_1_, p_191986_2_, p_191986_3_, 0.1F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8999999761581421D;
            this.motionY *= 0.8999999761581421D;
            this.motionZ *= 0.8999999761581421D;

            if (!this.isMoving() && this.getAttackTarget() == null)
            {
                this.motionY -= 0.005D;
            }
        }
        else
        {
            super.travel(p_191986_1_, p_191986_2_, p_191986_3_);
        }
    }

    static class AIGuardianAttack extends EntityAIBase
        {
            private final FakeGuardian guardian;
            private int tickCounter;
            private final boolean isElder;

            public AIGuardianAttack(FakeGuardian guardian)
            {
                this.guardian = guardian;
                this.isElder = false;
                this.setMutexBits(3);
            }

            /**
             * Returns whether the EntityAIBase should begin execution.
             */
            public boolean shouldExecute()
            {
                EntityLivingBase entitylivingbase = this.guardian.getAttackTarget();
                return entitylivingbase != null && entitylivingbase.isAlive();
            }

            /**
             * Returns whether an in-progress EntityAIBase should continue executing
             */
            public boolean shouldContinueExecuting()
            {
                return super.shouldContinueExecuting() && (this.isElder || this.guardian.getDistanceSq(this.guardian.getAttackTarget()) > 9.0D);
            }

            /**
             * Execute a one shot task or start executing a continuous task
             */
            public void startExecuting()
            {
                this.tickCounter = -10;
                this.guardian.getNavigator().clearPath();
                this.guardian.getLookHelper().setLookPositionWithEntity(this.guardian.getAttackTarget(), 90.0F, 90.0F);
                this.guardian.isAirBorne = true;
            }

            /**
             * Reset the task's internal state. Called when this task is interrupted by another one
             */
            public void resetTask()
            {
                this.guardian.setTargetedEntity(0);
                this.guardian.setAttackTarget((EntityLivingBase)null);
                this.guardian.wander.makeUpdate();
            }

            /**
             * Keep ticking a continuous task that has already been started
             */
            public void tick()
            {
                EntityLivingBase entitylivingbase = this.guardian.getAttackTarget();
                this.guardian.getNavigator().clearPath();
                this.guardian.getLookHelper().setLookPositionWithEntity(entitylivingbase, 90.0F, 90.0F);

                if (!this.guardian.canEntityBeSeen(entitylivingbase))
                {
                    this.guardian.setAttackTarget((EntityLivingBase)null);
                }
                else
                {
                    ++this.tickCounter;

                    if (this.tickCounter == 0)
                    {
                        this.guardian.setTargetedEntity(this.guardian.getAttackTarget().getEntityId());
                        this.guardian.world.setEntityState(this.guardian, (byte)21);
                    }
                    else if (this.tickCounter >= this.guardian.getAttackDuration())
                    {
                        @SuppressWarnings("unused")
						float f = 1.0F;

                        if (this.guardian.world.getDifficulty() == EnumDifficulty.HARD)
                        {
                            f += 2.0F;
                        }

                        if (this.isElder)
                        {
                            f += 2.0F;
                        }

                        this.guardian.setAttackTarget((EntityLivingBase)null);
                    }

                    super.tick();
                }
            }
        }

    static class GuardianMoveHelper extends EntityMoveHelper
        {
            private final FakeGuardian FakeGuardian;

            public GuardianMoveHelper(FakeGuardian guardian)
            {
                super(guardian);
                this.FakeGuardian = guardian;
            }

            public void onUpdateMoveHelper()
            {
                if (this.action == EntityMoveHelper.Action.MOVE_TO && !this.FakeGuardian.getNavigator().noPath())
                {
                    double d0 = this.posX - this.FakeGuardian.posX;
                    double d1 = this.posY - this.FakeGuardian.posY;
                    double d2 = this.posZ - this.FakeGuardian.posZ;
                    double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    d1 = d1 / d3;
                    float f = (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                    this.FakeGuardian.rotationYaw = this.limitAngle(this.FakeGuardian.rotationYaw, f, 90.0F);
                    this.FakeGuardian.renderYawOffset = this.FakeGuardian.rotationYaw;
                    float f1 = (float)(this.speed * this.FakeGuardian.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
                    this.FakeGuardian.setAIMoveSpeed(this.FakeGuardian.getAIMoveSpeed() + (f1 - this.FakeGuardian.getAIMoveSpeed()) * 0.125F);
                    double d4 = Math.sin((double)(this.FakeGuardian.ticksExisted + this.FakeGuardian.getEntityId()) * 0.5D) * 0.05D;
                    double d5 = Math.cos((double)(this.FakeGuardian.rotationYaw * 0.017453292F));
                    double d6 = Math.sin((double)(this.FakeGuardian.rotationYaw * 0.017453292F));
                    this.FakeGuardian.motionX += d4 * d5;
                    this.FakeGuardian.motionZ += d4 * d6;
                    d4 = Math.sin((double)(this.FakeGuardian.ticksExisted + this.FakeGuardian.getEntityId()) * 0.75D) * 0.05D;
                    this.FakeGuardian.motionY += d4 * (d6 + d5) * 0.25D;
                    this.FakeGuardian.motionY += (double)this.FakeGuardian.getAIMoveSpeed() * d1 * 0.1D;
                    EntityLookHelper entitylookhelper = this.FakeGuardian.getLookHelper();
                    double d7 = this.FakeGuardian.posX + d0 / d3 * 2.0D;
                    double d8 = (double)this.FakeGuardian.getEyeHeight() + this.FakeGuardian.posY + d1 / d3;
                    double d9 = this.FakeGuardian.posZ + d2 / d3 * 2.0D;
                    double d10 = entitylookhelper.getLookPosX();
                    double d11 = entitylookhelper.getLookPosY();
                    double d12 = entitylookhelper.getLookPosZ();

                    if (!entitylookhelper.getIsLooking())
                    {
                        d10 = d7;
                        d11 = d8;
                        d12 = d9;
                    }

                    this.FakeGuardian.getLookHelper().setLookPosition(d10 + (d7 - d10) * 0.125D, d11 + (d8 - d11) * 0.125D, d12 + (d9 - d12) * 0.125D, 10.0F, 40.0F);
                    this.FakeGuardian.setMoving(true);
                }
                else
                {
                    this.FakeGuardian.setAIMoveSpeed(0.0F);
                    this.FakeGuardian.setMoving(false);
                }
            }
        }

    static class GuardianTargetSelector implements Predicate<EntityLivingBase>
        {
            private final FakeGuardian parentEntity;

            public GuardianTargetSelector(FakeGuardian guardian)
            {
                this.parentEntity = guardian;
            }

            public boolean apply(@Nullable EntityLivingBase p_apply_1_)
            {
                return (p_apply_1_ instanceof EntityPlayer || p_apply_1_ instanceof EntitySquid) && p_apply_1_.getDistanceSq(this.parentEntity) > 9.0D;
            }
        }

    @Override
	public void writeAdditional(NBTTagCompound compound)
    {
        super.writeAdditional(compound);
        compound.setInt("Lifetime", this.lifetime);
    }

	@Override
	public void readAdditional(NBTTagCompound compound)
    {
        super.readAdditional(compound);
        this.lifetime = compound.getInt("Lifetime");
    }
}
