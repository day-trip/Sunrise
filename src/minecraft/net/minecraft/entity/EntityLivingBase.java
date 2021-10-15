package net.minecraft.entity;

import com.daytrip.sunrise.event.impl.EventEntityDeath;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.*;

public abstract class EntityLivingBase extends Entity
{
    private static final UUID sprintingSpeedBoostModifierUUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier sprintingSpeedBoostModifier = (new AttributeModifier(sprintingSpeedBoostModifierUUID, "Sprinting speed boost", 0.30000001192092896D, 2)).setSaved(false);
    private BaseAttributeMap attributeMap;
    private final CombatTracker _combatTracker = new CombatTracker(this);
    private final Map<Integer, PotionEffect> activePotionsMap = Maps.newHashMap();

    /** The equipment this mob was previously wearing, used for syncing. */
    private final ItemStack[] previousEquipment = new ItemStack[5];

    /** Whether an arm swing is currently in progress. */
    public boolean isSwingInProgress;
    public int swingProgressInt;
    public int arrowHitTimer;

    /**
     * The amount of time remaining this entity should act 'hurt'. (Visual appearance of red tint)
     */
    public int hurtTime;

    /** What the hurt time was max set to last. */
    public int maxHurtTime;

    /** The yaw at which this entity was last attacked from. */
    public float attackedAtYaw;

    /**
     * The amount of time remaining this entity should act 'dead', i.e. have a corpse in the world.
     */
    public int deathTime;
    public float prevSwingProgress;
    public float swingProgress;
    public float prevLimbSwingAmount;
    public float limbSwingAmount;

    /**
     * Only relevant when limbYaw is not 0(the entity is moving). Influences where in its swing legs and arms currently
     * are.
     */
    public float limbSwing;
    public int maxHurtResistantTime = 20;
    public float prevCameraPitch;
    public float cameraPitch;
    public float field_70769_ao;
    public float field_70770_ap;
    public float renderYawOffset;
    public float prevRenderYawOffset;

    /** Entity head rotation yaw */
    public float rotationYawHead;

    /** Entity head rotation yaw at previous tick */
    public float prevRotationYawHead;

    /**
     * A factor used to determine how far this entity will move each tick if it is jumping or falling.
     */
    public float jumpMovementFactor = 0.02F;

    /** The most recent player that has attacked this entity */
    protected EntityPlayer attackingPlayer;

    /**
     * Set to 60 when hit by the player or the player's wolf, then decrements. Used to determine whether the entity
     * should drop items on death.
     */
    protected int recentlyHit;

    /**
     * This gets set on entity death, but never used. Looks like a duplicate of isDead
     */
    protected boolean dead;

    /** The age of this EntityLiving (used to determine when it dies) */
    protected int entityAge;
    protected float prevOnGroundSpeedFactor;
    protected float onGroundSpeedFactor;
    protected float movedDistance;
    protected float prevMovedDistance;
    protected float field_70741_aB;

    /** The score value of the Mob, the amount of points the mob is worth. */
    protected int scoreValue;

    /**
     * Damage taken in the last hit. Mobs are resistant to damage less than this for a short time after taking damage.
     */
    protected float lastDamage;

    /** used to check whether entity is jumping. */
    protected boolean isJumping;
    public float moveStrafing;
    public float moveForward;
    protected float randomYawVelocity;

    /**
     * The number of updates over which the new position and rotation are to be applied to the entity.
     */
    protected int newPosRotationIncrements;

    /** The new X position to be applied to the entity. */
    protected double newPosX;

    /** The new Y position to be applied to the entity. */
    protected double newPosY;
    protected double newPosZ;

    /** The new yaw rotation to be applied to the entity. */
    protected double newRotationYaw;

    /** The new yaw rotation to be applied to the entity. */
    protected double newRotationPitch;

    /** Whether the DataWatcher needs to be updated with the active potions */
    private boolean potionsNeedUpdate = true;

    /** is only being set, has no uses as of MC 1.1 */
    private EntityLivingBase entityLivingToAttack;
    private int revengeTimer;
    private EntityLivingBase lastAttacker;

    /** Holds the value of ticksExisted when setLastAttacker was last called. */
    private int lastAttackerTime;

    /**
     * A factor used to determine how far this entity will move each tick if it is walking on land. Adjusted by speed,
     * and slipperiness of the current block.
     */
    private float landMovementFactor;

    /** Number of ticks since last jump */
    private int jumpTicks;
    private float absorptionAmount;

    /**
     * Called by the /kill command.
     */
    public void onKillCommand()
    {
        attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
    }

    public EntityLivingBase(World worldIn)
    {
        super(worldIn);
        applyEntityAttributes();
        setHealth(getMaxHealth());
        preventEntitySpawning = true;
        field_70770_ap = (float)((Math.random() + 1.0D) * 0.009999999776482582D);
        setPosition(posX, posY, posZ);
        field_70769_ao = (float)Math.random() * 12398.0F;
        rotationYaw = (float)(Math.random() * Math.PI * 2.0D);
        rotationYawHead = rotationYaw;
        stepHeight = 0.6F;
    }

    protected void entityInit()
    {
        dataWatcher.addObject(7, 0);
        dataWatcher.addObject(8, (byte) 0);
        dataWatcher.addObject(9, (byte) 0);
        dataWatcher.addObject(6, 1.0F);
    }

    protected void applyEntityAttributes()
    {
        getAttributeMap().registerAttribute(SharedMonsterAttributes.maxHealth);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.knockbackResistance);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.movementSpeed);
    }

    protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos)
    {
        if (!isInWater())
        {
            handleWaterMovement();
        }

        if (!worldObj.isRemote && fallDistance > 3.0F && onGroundIn)
        {
            IBlockState iblockstate = worldObj.getBlockState(pos);
            Block block = iblockstate.getBlock();
            float f = (float)MathHelper.ceiling_float_int(fallDistance - 3.0F);

            if (block.getMaterial() != Material.air)
            {
                double d0 = Math.min(0.2F + f / 15.0F, 10.0F);

                if (d0 > 2.5D)
                {
                    d0 = 2.5D;
                }

                int i = (int)(150.0D * d0);
                ((WorldServer) worldObj).spawnParticle(EnumParticleTypes.BLOCK_DUST, posX, posY, posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(iblockstate));
            }
        }

        super.updateFallState(y, onGroundIn, blockIn, pos);
    }

    public boolean canBreatheUnderwater()
    {
        return false;
    }

    /**
     * Gets called every tick from main Entity class
     */
    public void onEntityUpdate()
    {
        prevSwingProgress = swingProgress;
        super.onEntityUpdate();
        worldObj.theProfiler.startSection("livingEntityBaseTick");
        boolean flag = this instanceof EntityPlayer;

        if (isEntityAlive())
        {
            if (isEntityInsideOpaqueBlock())
            {
                attackEntityFrom(DamageSource.inWall, 1.0F);
            }
            else if (flag && !worldObj.getWorldBorder().contains(getEntityBoundingBox()))
            {
                double d0 = worldObj.getWorldBorder().getClosestDistance(this) + worldObj.getWorldBorder().getDamageBuffer();

                if (d0 < 0.0D)
                {
                    attackEntityFrom(DamageSource.inWall, (float)Math.max(1, MathHelper.floor_double(-d0 * worldObj.getWorldBorder().getDamageAmount())));
                }
            }
        }

        if (isImmuneToFire() || worldObj.isRemote)
        {
            extinguish();
        }

        boolean flag1 = flag && ((EntityPlayer)this).capabilities.disableDamage;

        if (isEntityAlive())
        {
            if (isInsideOfMaterial(Material.water))
            {
                if (!canBreatheUnderwater() && !isPotionActive(Potion.waterBreathing.id) && !flag1)
                {
                    setAir(decreaseAirSupply(getAir()));

                    if (getAir() == -20)
                    {
                        setAir(0);

                        for (int i = 0; i < 8; ++i)
                        {
                            float f = rand.nextFloat() - rand.nextFloat();
                            float f1 = rand.nextFloat() - rand.nextFloat();
                            float f2 = rand.nextFloat() - rand.nextFloat();
                            worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX + (double)f, posY + (double)f1, posZ + (double)f2, motionX, motionY, motionZ);
                        }

                        attackEntityFrom(DamageSource.drown, 2.0F);
                    }
                }

                if (!worldObj.isRemote && isRiding() && ridingEntity instanceof EntityLivingBase)
                {
                    mountEntity(null);
                }
            }
            else
            {
                setAir(300);
            }
        }

        if (isEntityAlive() && isWet())
        {
            extinguish();
        }

        prevCameraPitch = cameraPitch;

        if (hurtTime > 0)
        {
            --hurtTime;
        }

        if (hurtResistantTime > 0 && !(this instanceof EntityPlayerMP))
        {
            --hurtResistantTime;
        }

        if (getHealth() <= 0.0F)
        {
            onDeathUpdate();
        }

        if (recentlyHit > 0)
        {
            --recentlyHit;
        }
        else
        {
            attackingPlayer = null;
        }

        if (lastAttacker != null && !lastAttacker.isEntityAlive())
        {
            lastAttacker = null;
        }

        if (entityLivingToAttack != null)
        {
            if (!entityLivingToAttack.isEntityAlive())
            {
                setRevengeTarget(null);
            }
            else if (ticksExisted - revengeTimer > 100)
            {
                setRevengeTarget(null);
            }
        }

        updatePotionEffects();
        prevMovedDistance = movedDistance;
        prevRenderYawOffset = renderYawOffset;
        prevRotationYawHead = rotationYawHead;
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        worldObj.theProfiler.endSection();
    }

    /**
     * If Animal, checks if the age timer is negative
     */
    public boolean isChild()
    {
        return false;
    }

    /**
     * handles entity death timer, experience orb and particle creation
     */
    protected void onDeathUpdate()
    {
        ++deathTime;

        if (deathTime == 20)
        {
            if (!worldObj.isRemote && (recentlyHit > 0 || isPlayer()) && canDropLoot() && worldObj.getGameRules().getBoolean("doMobLoot"))
            {
                int i = getExperiencePoints(attackingPlayer);

                while (i > 0)
                {
                    int j = EntityXPOrb.getXPSplit(i);
                    i -= j;
                    worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, j));
                }
            }

            setDead();

            for (int k = 0; k < 20; ++k)
            {
                double d2 = rand.nextGaussian() * 0.02D;
                double d0 = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width, posY + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d2, d0, d1);
            }
        }
    }

    /**
     * Entity won't drop items or experience points if this returns false
     */
    protected boolean canDropLoot()
    {
        return !isChild();
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    protected int decreaseAirSupply(int p_70682_1_)
    {
        int i = EnchantmentHelper.getRespiration(this);
        return i > 0 && rand.nextInt(i + 1) > 0 ? p_70682_1_ : p_70682_1_ - 1;
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer player)
    {
        return 0;
    }

    /**
     * Only use is to identify if class is an instance of player for experience dropping
     */
    protected boolean isPlayer()
    {
        return false;
    }

    public Random getRNG()
    {
        return rand;
    }

    public EntityLivingBase getAITarget()
    {
        return entityLivingToAttack;
    }

    public int getRevengeTimer()
    {
        return revengeTimer;
    }

    public void setRevengeTarget(EntityLivingBase livingBase)
    {
        entityLivingToAttack = livingBase;
        revengeTimer = ticksExisted;
    }

    public EntityLivingBase getLastAttacker()
    {
        return lastAttacker;
    }

    public int getLastAttackerTime()
    {
        return lastAttackerTime;
    }

    public void setLastAttacker(Entity entityIn)
    {
        if (entityIn instanceof EntityLivingBase)
        {
            lastAttacker = (EntityLivingBase)entityIn;
        }
        else
        {
            lastAttacker = null;
        }

        lastAttackerTime = ticksExisted;
    }

    public int getAge()
    {
        return entityAge;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagCompound)
    {
        tagCompound.setFloat("HealF", getHealth());
        tagCompound.setShort("Health", (short)((int)Math.ceil(getHealth())));
        tagCompound.setShort("HurtTime", (short) hurtTime);
        tagCompound.setInteger("HurtByTimestamp", revengeTimer);
        tagCompound.setShort("DeathTime", (short) deathTime);
        tagCompound.setFloat("AbsorptionAmount", getAbsorptionAmount());

        for (ItemStack itemstack : getInventory())
        {
            if (itemstack != null)
            {
                attributeMap.removeAttributeModifiers(itemstack.getAttributeModifiers());
            }
        }

        tagCompound.setTag("Attributes", SharedMonsterAttributes.writeBaseAttributeMapToNBT(getAttributeMap()));

        for (ItemStack itemstack1 : getInventory())
        {
            if (itemstack1 != null)
            {
                attributeMap.applyAttributeModifiers(itemstack1.getAttributeModifiers());
            }
        }

        if (!activePotionsMap.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (PotionEffect potioneffect : activePotionsMap.values())
            {
                nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
            }

            tagCompound.setTag("ActiveEffects", nbttaglist);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagCompund)
    {
        setAbsorptionAmount(tagCompund.getFloat("AbsorptionAmount"));

        if (tagCompund.hasKey("Attributes", 9) && worldObj != null && !worldObj.isRemote)
        {
            SharedMonsterAttributes.func_151475_a(getAttributeMap(), tagCompund.getTagList("Attributes", 10));
        }

        if (tagCompund.hasKey("ActiveEffects", 9))
        {
            NBTTagList nbttaglist = tagCompund.getTagList("ActiveEffects", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                PotionEffect potioneffect = PotionEffect.readCustomPotionEffectFromNBT(nbttagcompound);

                if (potioneffect != null)
                {
                    activePotionsMap.put(potioneffect.getPotionID(), potioneffect);
                }
            }
        }

        if (tagCompund.hasKey("HealF", 99))
        {
            setHealth(tagCompund.getFloat("HealF"));
        }
        else
        {
            NBTBase nbtbase = tagCompund.getTag("Health");

            if (nbtbase == null)
            {
                setHealth(getMaxHealth());
            }
            else if (nbtbase.getId() == 5)
            {
                setHealth(((NBTTagFloat)nbtbase).getFloat());
            }
            else if (nbtbase.getId() == 2)
            {
                setHealth(((NBTTagShort)nbtbase).getShort());
            }
        }

        hurtTime = tagCompund.getShort("HurtTime");
        deathTime = tagCompund.getShort("DeathTime");
        revengeTimer = tagCompund.getInteger("HurtByTimestamp");
    }

    protected void updatePotionEffects()
    {
        Iterator<Integer> iterator = activePotionsMap.keySet().iterator();

        while (iterator.hasNext())
        {
            Integer integer = iterator.next();
            PotionEffect potioneffect = activePotionsMap.get(integer);

            if (!potioneffect.onUpdate(this))
            {
                if (!worldObj.isRemote)
                {
                    iterator.remove();
                    onFinishedPotionEffect(potioneffect);
                }
            }
            else if (potioneffect.getDuration() % 600 == 0)
            {
                onChangedPotionEffect(potioneffect, false);
            }
        }

        if (potionsNeedUpdate)
        {
            if (!worldObj.isRemote)
            {
                updatePotionMetadata();
            }

            potionsNeedUpdate = false;
        }

        int i = dataWatcher.getWatchableObjectInt(7);
        boolean flag1 = dataWatcher.getWatchableObjectByte(8) > 0;

        if (i > 0)
        {
            boolean flag;

            if (!isInvisible())
            {
                flag = rand.nextBoolean();
            }
            else
            {
                flag = rand.nextInt(15) == 0;
            }

            if (flag1)
            {
                flag &= rand.nextInt(5) == 0;
            }

            if (flag)
            {
                double d0 = (double)(i >> 16 & 255) / 255.0D;
                double d1 = (double)(i >> 8 & 255) / 255.0D;
                double d2 = (double)(i & 255) / 255.0D;
                worldObj.spawnParticle(flag1 ? EnumParticleTypes.SPELL_MOB_AMBIENT : EnumParticleTypes.SPELL_MOB, posX + (rand.nextDouble() - 0.5D) * (double) width, posY + rand.nextDouble() * (double) height, posZ + (rand.nextDouble() - 0.5D) * (double) width, d0, d1, d2);
            }
        }
    }

    /**
     * Clears potion metadata values if the entity has no potion effects. Otherwise, updates potion effect color,
     * ambience, and invisibility metadata values
     */
    protected void updatePotionMetadata()
    {
        if (activePotionsMap.isEmpty())
        {
            resetPotionEffectMetadata();
            setInvisible(false);
        }
        else
        {
            int i = PotionHelper.calcPotionLiquidColor(activePotionsMap.values());
            dataWatcher.updateObject(8, (byte) (PotionHelper.getAreAmbient(activePotionsMap.values()) ? 1 : 0));
            dataWatcher.updateObject(7, i);
            setInvisible(isPotionActive(Potion.invisibility.id));
        }
    }

    /**
     * Resets the potion effect color and ambience metadata values
     */
    protected void resetPotionEffectMetadata()
    {
        dataWatcher.updateObject(8, (byte) 0);
        dataWatcher.updateObject(7, 0);
    }

    public void clearActivePotions()
    {
        Iterator<Integer> iterator = activePotionsMap.keySet().iterator();

        while (iterator.hasNext())
        {
            Integer integer = iterator.next();
            PotionEffect potioneffect = activePotionsMap.get(integer);

            if (!worldObj.isRemote)
            {
                iterator.remove();
                onFinishedPotionEffect(potioneffect);
            }
        }
    }

    public Collection<PotionEffect> getActivePotionEffects()
    {
        return activePotionsMap.values();
    }

    public boolean isPotionActive(int potionId)
    {
        return activePotionsMap.containsKey(potionId);
    }

    public boolean isPotionActive(Potion potionIn)
    {
        return activePotionsMap.containsKey(potionIn.id);
    }

    /**
     * returns the PotionEffect for the supplied Potion if it is active, null otherwise.
     */
    public PotionEffect getActivePotionEffect(Potion potionIn)
    {
        return activePotionsMap.get(potionIn.id);
    }

    /**
     * adds a PotionEffect to the entity
     */
    public void addPotionEffect(PotionEffect potioneffectIn)
    {
        if (isPotionApplicable(potioneffectIn))
        {
            if (activePotionsMap.containsKey(potioneffectIn.getPotionID()))
            {
                activePotionsMap.get(potioneffectIn.getPotionID()).combine(potioneffectIn);
                onChangedPotionEffect(activePotionsMap.get(potioneffectIn.getPotionID()), true);
            }
            else
            {
                activePotionsMap.put(potioneffectIn.getPotionID(), potioneffectIn);
                onNewPotionEffect(potioneffectIn);
            }
        }
    }

    public boolean isPotionApplicable(PotionEffect potioneffectIn)
    {
        if (getCreatureAttribute() == EnumCreatureAttribute.UNDEAD)
        {
            int i = potioneffectIn.getPotionID();

            return i != Potion.regeneration.id && i != Potion.poison.id;
        }

        return true;
    }

    /**
     * Returns true if this entity is undead.
     */
    public boolean isEntityUndead()
    {
        return getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
    }

    /**
     * Remove the speified potion effect from this entity.
     */
    public void removePotionEffectClient(int potionId)
    {
        activePotionsMap.remove(potionId);
    }

    /**
     * Remove the specified potion effect from this entity.
     */
    public void removePotionEffect(int potionId)
    {
        PotionEffect potioneffect = activePotionsMap.remove(potionId);

        if (potioneffect != null)
        {
            onFinishedPotionEffect(potioneffect);
        }
    }

    protected void onNewPotionEffect(PotionEffect id)
    {
        potionsNeedUpdate = true;

        if (!worldObj.isRemote)
        {
            Potion.potionTypes[id.getPotionID()].applyAttributesModifiersToEntity(this, getAttributeMap(), id.getAmplifier());
        }
    }

    protected void onChangedPotionEffect(PotionEffect id, boolean p_70695_2_)
    {
        potionsNeedUpdate = true;

        if (p_70695_2_ && !worldObj.isRemote)
        {
            Potion.potionTypes[id.getPotionID()].removeAttributesModifiersFromEntity(this, getAttributeMap(), id.getAmplifier());
            Potion.potionTypes[id.getPotionID()].applyAttributesModifiersToEntity(this, getAttributeMap(), id.getAmplifier());
        }
    }

    protected void onFinishedPotionEffect(PotionEffect p_70688_1_)
    {
        potionsNeedUpdate = true;

        if (!worldObj.isRemote)
        {
            Potion.potionTypes[p_70688_1_.getPotionID()].removeAttributesModifiersFromEntity(this, getAttributeMap(), p_70688_1_.getAmplifier());
        }
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */
    public void heal(float healAmount)
    {
        float f = getHealth();

        if (f > 0.0F)
        {
            setHealth(f + healAmount);
        }
    }

    public final float getHealth()
    {
        return dataWatcher.getWatchableObjectFloat(6);
    }

    public void setHealth(float health)
    {
        dataWatcher.updateObject(6, MathHelper.clamp_float(health, 0.0F, getMaxHealth()));
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (isEntityInvulnerable(source))
        {
            return false;
        }
        else if (worldObj.isRemote)
        {
            return false;
        }
        else
        {
            entityAge = 0;

            if (getHealth() <= 0.0F)
            {
                return false;
            }
            else if (source.isFireDamage() && isPotionActive(Potion.fireResistance))
            {
                return false;
            }
            else
            {
                if ((source == DamageSource.anvil || source == DamageSource.fallingBlock) && getEquipmentInSlot(4) != null)
                {
                    getEquipmentInSlot(4).damageItem((int)(amount * 4.0F + rand.nextFloat() * amount * 2.0F), this);
                    amount *= 0.75F;
                }

                limbSwingAmount = 1.5F;
                boolean flag = true;

                if ((float) hurtResistantTime > (float) maxHurtResistantTime / 2.0F)
                {
                    if (amount <= lastDamage)
                    {
                        return false;
                    }

                    damageEntity(source, amount - lastDamage);
                    lastDamage = amount;
                    flag = false;
                }
                else
                {
                    lastDamage = amount;
                    hurtResistantTime = maxHurtResistantTime;
                    damageEntity(source, amount);
                    hurtTime = maxHurtTime = 10;
                }

                attackedAtYaw = 0.0F;
                Entity entity = source.getEntity();

                if (entity != null)
                {
                    if (entity instanceof EntityLivingBase)
                    {
                        setRevengeTarget((EntityLivingBase)entity);
                    }

                    if (entity instanceof EntityPlayer)
                    {
                        recentlyHit = 100;
                        attackingPlayer = (EntityPlayer)entity;
                    }
                    else if (entity instanceof EntityWolf)
                    {
                        EntityWolf entitywolf = (EntityWolf)entity;

                        if (entitywolf.isTamed())
                        {
                            recentlyHit = 100;
                            attackingPlayer = null;
                        }
                    }
                }

                if (flag)
                {
                    worldObj.setEntityState(this, (byte)2);

                    if (source != DamageSource.drown)
                    {
                        setBeenAttacked();
                    }

                    if (entity != null)
                    {
                        double d1 = entity.posX - posX;
                        double d0;

                        for (d0 = entity.posZ - posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D)
                        {
                            d1 = (Math.random() - Math.random()) * 0.01D;
                        }

                        attackedAtYaw = (float)(MathHelper.func_181159_b(d0, d1) * 180.0D / Math.PI - (double) rotationYaw);
                        knockBack(d1, d0);
                    }
                    else
                    {
                        attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
                    }
                }

                if (getHealth() <= 0.0F)
                {
                    String s = getDeathSound();

                    if (flag && s != null)
                    {
                        playSound(s, getSoundVolume(), getSoundPitch());
                    }

                    onDeath(source);
                }
                else
                {
                    String s1 = getHurtSound();

                    if (flag && s1 != null)
                    {
                        playSound(s1, getSoundVolume(), getSoundPitch());
                    }
                }

                return true;
            }
        }
    }

    /**
     * Renders broken item particles using the given ItemStack
     */
    public void renderBrokenItemStack(ItemStack stack)
    {
        playSound("random.break", 0.8F, 0.8F + worldObj.rand.nextFloat() * 0.4F);

        for (int i = 0; i < 5; ++i)
        {
            Vec3 vec3 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.rotatePitch(-rotationPitch * (float)Math.PI / 180.0F);
            vec3 = vec3.rotateYaw(-rotationYaw * (float)Math.PI / 180.0F);
            double d0 = (double)(-rand.nextFloat()) * 0.6D - 0.3D;
            Vec3 vec31 = new Vec3(((double) rand.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
            vec31 = vec31.rotatePitch(-rotationPitch * (float)Math.PI / 180.0F);
            vec31 = vec31.rotateYaw(-rotationYaw * (float)Math.PI / 180.0F);
            vec31 = vec31.add(posX, posY + (double) getEyeHeight(), posZ);
            worldObj.spawnParticle(EnumParticleTypes.ITEM_CRACK, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05D, vec3.zCoord, Item.getIdFromItem(stack.getItem()));
        }
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource cause) {
        EventEntityDeath death = new EventEntityDeath();
        death.setEntity(this);
        death.setSource(cause);
        try {
            death.post();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(death.isCancelled()) return;

        Entity entity = cause.getEntity();
        EntityLivingBase entitylivingbase = func_94060_bK();

        if (scoreValue >= 0 && entitylivingbase != null)
        {
            entitylivingbase.addToPlayerScore(this, scoreValue);
        }

        if (entity != null)
        {
            entity.onKillEntity(this);
        }

        dead = true;
        getCombatTracker().reset();

        if (!worldObj.isRemote)
        {
            int i = 0;

            if (entity instanceof EntityPlayer)
            {
                i = EnchantmentHelper.getLootingModifier((EntityLivingBase)entity);
            }

            if (canDropLoot() && worldObj.getGameRules().getBoolean("doMobLoot"))
            {
                dropFewItems(recentlyHit > 0, i);
                dropEquipment(recentlyHit > 0, i);

                if (recentlyHit > 0 && rand.nextFloat() < 0.025F + (float)i * 0.01F)
                {
                    addRandomDrop();
                }
            }
        }

        worldObj.setEntityState(this, (byte)3);
    }

    /**
     * Drop the equipment for this entity.
     */
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_)
    {
    }

    /**
     * knocks back this entity
     */
    public void knockBack(double p_70653_3_, double p_70653_5_)
    {
        if (rand.nextDouble() >= getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue())
        {
            isAirBorne = true;
            float f = MathHelper.sqrt_double(p_70653_3_ * p_70653_3_ + p_70653_5_ * p_70653_5_);
            float f1 = 0.4F;
            motionX /= 2.0D;
            motionY /= 2.0D;
            motionZ /= 2.0D;
            motionX -= p_70653_3_ / (double)f * (double)f1;
            motionY += f1;
            motionZ -= p_70653_5_ / (double)f * (double)f1;

            if (motionY > 0.4000000059604645D)
            {
                motionY = 0.4000000059604645D;
            }
        }
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "game.neutral.hurt";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "game.neutral.die";
    }

    /**
     * Causes this Entity to drop a random item.
     */
    protected void addRandomDrop()
    {
    }

    /**
     * Drop 0-2 items of this living's type
     */
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_)
    {
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadder()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(getEntityBoundingBox().minY);
        int k = MathHelper.floor_double(posZ);
        Block block = worldObj.getBlockState(new BlockPos(i, j, k)).getBlock();
        return (block == Blocks.ladder || block == Blocks.vine) && (!(this instanceof EntityPlayer) || !((EntityPlayer)this).isSpectator());
    }

    /**
     * Checks whether target entity is alive.
     */
    public boolean isEntityAlive()
    {
        return !isDead && getHealth() > 0.0F;
    }

    public void fall(float distance, float damageMultiplier)
    {
        super.fall(distance, damageMultiplier);
        PotionEffect potioneffect = getActivePotionEffect(Potion.jump);
        float f = potioneffect != null ? (float)(potioneffect.getAmplifier() + 1) : 0.0F;
        int i = MathHelper.ceiling_float_int((distance - 3.0F - f) * damageMultiplier);

        if (i > 0)
        {
            playSound(getFallSoundString(i), 1.0F, 1.0F);
            attackEntityFrom(DamageSource.fall, (float)i);
            int j = MathHelper.floor_double(posX);
            int k = MathHelper.floor_double(posY - 0.20000000298023224D);
            int l = MathHelper.floor_double(posZ);
            Block block = worldObj.getBlockState(new BlockPos(j, k, l)).getBlock();

            if (block.getMaterial() != Material.air)
            {
                Block.SoundType block$soundtype = block.stepSound;
                playSound(block$soundtype.getStepSound(), block$soundtype.getVolume() * 0.5F, block$soundtype.getFrequency() * 0.75F);
            }
        }
    }

    protected String getFallSoundString(int damageValue)
    {
        return damageValue > 4 ? "game.neutral.hurt.fall.big" : "game.neutral.hurt.fall.small";
    }

    /**
     * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
     */
    public void performHurtAnimation()
    {
        hurtTime = maxHurtTime = 10;
        attackedAtYaw = 0.0F;
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue()
    {
        int i = 0;

        for (ItemStack itemstack : getInventory())
        {
            if (itemstack != null && itemstack.getItem() instanceof ItemArmor)
            {
                int j = ((ItemArmor)itemstack.getItem()).damageReduceAmount;
                i += j;
            }
        }

        return i;
    }

    protected void damageArmor(float p_70675_1_)
    {
    }

    /**
     * Reduces damage, depending on armor
     */
    protected float applyArmorCalculations(DamageSource source, float damage)
    {
        if (!source.isUnblockable())
        {
            int i = 25 - getTotalArmorValue();
            float f = damage * (float)i;
            damageArmor(damage);
            damage = f / 25.0F;
        }

        return damage;
    }

    /**
     * Reduces damage, depending on potions
     */
    protected float applyPotionDamageCalculations(DamageSource source, float damage)
    {
        if (source.isDamageAbsolute())
        {
            return damage;
        }
        else
        {
            if (isPotionActive(Potion.resistance) && source != DamageSource.outOfWorld)
            {
                int i = (getActivePotionEffect(Potion.resistance).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float)j;
                damage = f / 25.0F;
            }

            if (damage <= 0.0F)
            {
                return 0.0F;
            }
            else
            {
                int k = EnchantmentHelper.getEnchantmentModifierDamage(getInventory(), source);

                if (k > 20)
                {
                    k = 20;
                }

                if (k > 0)
                {
                    int l = 25 - k;
                    float f1 = damage * (float)l;
                    damage = f1 / 25.0F;
                }

                return damage;
            }
        }
    }

    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    protected void damageEntity(DamageSource damageSrc, float damageAmount)
    {
        if (!isEntityInvulnerable(damageSrc))
        {
            damageAmount = applyArmorCalculations(damageSrc, damageAmount);
            damageAmount = applyPotionDamageCalculations(damageSrc, damageAmount);
            float f = damageAmount;
            damageAmount = Math.max(damageAmount - getAbsorptionAmount(), 0.0F);
            setAbsorptionAmount(getAbsorptionAmount() - (f - damageAmount));

            if (damageAmount != 0.0F)
            {
                float f1 = getHealth();
                setHealth(f1 - damageAmount);
                getCombatTracker().trackDamage(damageSrc, f1, damageAmount);
                setAbsorptionAmount(getAbsorptionAmount() - damageAmount);
            }
        }
    }

    public CombatTracker getCombatTracker()
    {
        return _combatTracker;
    }

    public EntityLivingBase func_94060_bK()
    {
        return _combatTracker.func_94550_c() != null ? _combatTracker.func_94550_c() : (attackingPlayer != null ? attackingPlayer : (entityLivingToAttack != null ? entityLivingToAttack : null));
    }

    public final float getMaxHealth()
    {
        return (float) getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue();
    }

    /**
     * counts the amount of arrows stuck in the entity. getting hit by arrows increases this, used in rendering
     */
    public final int getArrowCountInEntity()
    {
        return dataWatcher.getWatchableObjectByte(9);
    }

    /**
     * sets the amount of arrows stuck in the entity. used for rendering those
     */
    public final void setArrowCountInEntity(int count)
    {
        dataWatcher.updateObject(9, (byte) count);
    }

    /**
     * Returns an integer indicating the end point of the swing animation, used by {@link #swingProgress} to provide a
     * progress indicator. Takes dig speed enchantments into account.
     */
    private int getArmSwingAnimationEnd()
    {
        return isPotionActive(Potion.digSpeed) ? 6 - (1 + getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (isPotionActive(Potion.digSlowdown) ? 6 + (1 + getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
    }

    /**
     * Swings the item the player is holding.
     */
    public void swingItem()
    {
        if (!isSwingInProgress || swingProgressInt >= getArmSwingAnimationEnd() / 2 || swingProgressInt < 0)
        {
            swingProgressInt = -1;
            isSwingInProgress = true;

            if (worldObj instanceof WorldServer)
            {
                ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S0BPacketAnimation(this, 0));
            }
        }
    }

    public void handleStatusUpdate(byte id)
    {
        if (id == 2)
        {
            limbSwingAmount = 1.5F;
            hurtResistantTime = maxHurtResistantTime;
            hurtTime = maxHurtTime = 10;
            attackedAtYaw = 0.0F;
            String s = getHurtSound();

            if (s != null)
            {
                playSound(getHurtSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }

            attackEntityFrom(DamageSource.generic, 0.0F);
        }
        else if (id == 3)
        {
            String s1 = getDeathSound();

            if (s1 != null)
            {
                playSound(getDeathSound(), getSoundVolume(), (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F);
            }

            setHealth(0.0F);
            onDeath(DamageSource.generic);
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }

    /**
     * sets the dead flag. Used when you fall off the bottom of the world.
     */
    protected void kill()
    {
        attackEntityFrom(DamageSource.outOfWorld, 4.0F);
    }

    /**
     * Updates the arm swing progress counters and animation progress
     */
    protected void updateArmSwingProgress()
    {
        int i = getArmSwingAnimationEnd();

        if (isSwingInProgress)
        {
            ++swingProgressInt;

            if (swingProgressInt >= i)
            {
                swingProgressInt = 0;
                isSwingInProgress = false;
            }
        }
        else
        {
            swingProgressInt = 0;
        }

        swingProgress = (float) swingProgressInt / (float)i;
    }

    public IAttributeInstance getEntityAttribute(IAttribute attribute)
    {
        return getAttributeMap().getAttributeInstance(attribute);
    }

    public BaseAttributeMap getAttributeMap()
    {
        if (attributeMap == null)
        {
            attributeMap = new ServersideAttributeMap();
        }

        return attributeMap;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEFINED;
    }

    /**
     * Returns the item that this EntityLiving is holding, if any.
     */
    public abstract ItemStack getHeldItem();

    /**
     * 0: Tool in Hand; 1-4: Armor
     */
    public abstract ItemStack getEquipmentInSlot(int slotIn);

    public abstract ItemStack getCurrentArmor(int slotIn);

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public abstract void setCurrentItemOrArmor(int slotIn, ItemStack stack);

    /**
     * Set sprinting switch for Entity.
     */
    public void setSprinting(boolean sprinting)
    {
        super.setSprinting(sprinting);
        IAttributeInstance iattributeinstance = getEntityAttribute(SharedMonsterAttributes.movementSpeed);

        if (iattributeinstance.getModifier(sprintingSpeedBoostModifierUUID) != null)
        {
            iattributeinstance.removeModifier(sprintingSpeedBoostModifier);
        }

        if (sprinting)
        {
            iattributeinstance.applyModifier(sprintingSpeedBoostModifier);
        }
    }

    /**
     * returns the inventory of this entity (only used in EntityPlayerMP it seems)
     */
    public abstract ItemStack[] getInventory();

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 1.0F;
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    protected float getSoundPitch()
    {
        return isChild() ? (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.5F : (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
    }

    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isMovementBlocked()
    {
        return getHealth() <= 0.0F;
    }

    /**
     * Moves the entity to a position out of the way of its mount.
     */
    public void dismountEntity(Entity p_110145_1_)
    {
        double d0 = p_110145_1_.posX;
        double d1 = p_110145_1_.getEntityBoundingBox().minY + (double)p_110145_1_.height;
        double d2 = p_110145_1_.posZ;
        int i = 1;

        for (int j = -i; j <= i; ++j)
        {
            for (int k = -i; k < i; ++k)
            {
                if (j != 0 || k != 0)
                {
                    int l = (int)(posX + (double)j);
                    int i1 = (int)(posZ + (double)k);
                    AxisAlignedBB axisalignedbb = getEntityBoundingBox().offset(j, 1.0D, k);

                    if (worldObj.func_147461_a(axisalignedbb).isEmpty())
                    {
                        if (World.doesBlockHaveSolidTopSurface(worldObj, new BlockPos(l, (int) posY, i1)))
                        {
                            setPositionAndUpdate(posX + (double)j, posY + 1.0D, posZ + (double)k);
                            return;
                        }

                        if (World.doesBlockHaveSolidTopSurface(worldObj, new BlockPos(l, (int) posY - 1, i1)) || worldObj.getBlockState(new BlockPos(l, (int) posY - 1, i1)).getBlock().getMaterial() == Material.water)
                        {
                            d0 = posX + (double)j;
                            d1 = posY + 1.0D;
                            d2 = posZ + (double)k;
                        }
                    }
                }
            }
        }

        setPositionAndUpdate(d0, d1, d2);
    }

    public boolean getAlwaysRenderNameTagForRender()
    {
        return false;
    }

    protected float getJumpUpwardsMotion()
    {
        return 0.42F;
    }

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    protected void jump()
    {
        motionY = getJumpUpwardsMotion();

        if (isPotionActive(Potion.jump))
        {
            motionY += (float)(getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        if (isSprinting())
        {
            float f = rotationYaw * 0.017453292F;
            motionX -= MathHelper.sin(f) * 0.2F;
            motionZ += MathHelper.cos(f) * 0.2F;
        }

        isAirBorne = true;
    }

    /**
     * main AI tick function, replaces updateEntityActionState
     */
    protected void updateAITick()
    {
        motionY += 0.03999999910593033D;
    }

    protected void handleJumpLava()
    {
        motionY += 0.03999999910593033D;
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float strafe, float forward)
    {
        if (isServerWorld())
        {
            if (!isInWater() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying)
            {
                if (!isInLava() || this instanceof EntityPlayer && ((EntityPlayer)this).capabilities.isFlying)
                {
                    float f4 = 0.91F;

                    if (onGround)
                    {
                        f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91F;
                    }

                    float f = 0.16277136F / (f4 * f4 * f4);
                    float f5;

                    if (onGround)
                    {
                        f5 = getAIMoveSpeed() * f;
                    }
                    else
                    {
                        f5 = jumpMovementFactor;
                    }

                    moveFlying(strafe, forward, f5);
                    f4 = 0.91F;

                    if (onGround)
                    {
                        f4 = worldObj.getBlockState(new BlockPos(MathHelper.floor_double(posX), MathHelper.floor_double(getEntityBoundingBox().minY) - 1, MathHelper.floor_double(posZ))).getBlock().slipperiness * 0.91F;
                    }

                    if (isOnLadder())
                    {
                        float f6 = 0.15F;
                        motionX = MathHelper.clamp_double(motionX, -f6, f6);
                        motionZ = MathHelper.clamp_double(motionZ, -f6, f6);
                        fallDistance = 0.0F;

                        if (motionY < -0.15D)
                        {
                            motionY = -0.15D;
                        }

                        boolean flag = isSneaking() && this instanceof EntityPlayer;

                        if (flag && motionY < 0.0D)
                        {
                            motionY = 0.0D;
                        }
                    }

                    moveEntity(motionX, motionY, motionZ);

                    if (isCollidedHorizontally && isOnLadder())
                    {
                        motionY = 0.2D;
                    }

                    if (worldObj.isRemote && (!worldObj.isBlockLoaded(new BlockPos((int) posX, 0, (int) posZ)) || !worldObj.getChunkFromBlockCoords(new BlockPos((int) posX, 0, (int) posZ)).isLoaded()))
                    {
                        if (posY > 0.0D)
                        {
                            motionY = -0.1D;
                        }
                        else
                        {
                            motionY = 0.0D;
                        }
                    }
                    else
                    {
                        motionY -= 0.08D;
                    }

                    motionY *= 0.9800000190734863D;
                    motionX *= f4;
                    motionZ *= f4;
                }
                else
                {
                    double d1 = posY;
                    moveFlying(strafe, forward, 0.02F);
                    moveEntity(motionX, motionY, motionZ);
                    motionX *= 0.5D;
                    motionY *= 0.5D;
                    motionZ *= 0.5D;
                    motionY -= 0.02D;

                    if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, motionY + 0.6000000238418579D - posY + d1, motionZ))
                    {
                        motionY = 0.30000001192092896D;
                    }
                }
            }
            else
            {
                double d0 = posY;
                float f1 = 0.8F;
                float f2 = 0.02F;
                float f3 = (float)EnchantmentHelper.getDepthStriderModifier(this);

                if (f3 > 3.0F)
                {
                    f3 = 3.0F;
                }

                if (!onGround)
                {
                    f3 *= 0.5F;
                }

                if (f3 > 0.0F)
                {
                    f1 += (0.54600006F - f1) * f3 / 3.0F;
                    f2 += (getAIMoveSpeed() - f2) * f3 / 3.0F;
                }

                moveFlying(strafe, forward, f2);
                moveEntity(motionX, motionY, motionZ);
                motionX *= f1;
                motionY *= 0.800000011920929D;
                motionZ *= f1;
                motionY -= 0.02D;

                if (isCollidedHorizontally && isOffsetPositionInLiquid(motionX, motionY + 0.6000000238418579D - posY + d0, motionZ))
                {
                    motionY = 0.30000001192092896D;
                }
            }
        }

        prevLimbSwingAmount = limbSwingAmount;
        double d2 = posX - prevPosX;
        double d3 = posZ - prevPosZ;
        float f7 = MathHelper.sqrt_double(d2 * d2 + d3 * d3) * 4.0F;

        if (f7 > 1.0F)
        {
            f7 = 1.0F;
        }

        limbSwingAmount += (f7 - limbSwingAmount) * 0.4F;
        limbSwing += limbSwingAmount;
    }

    /**
     * the movespeed used for the new AI system
     */
    public float getAIMoveSpeed()
    {
        return landMovementFactor;
    }

    /**
     * set the movespeed used for the new AI system
     */
    public void setAIMoveSpeed(float speedIn)
    {
        landMovementFactor = speedIn;
    }

    public boolean attackEntityAsMob(Entity entityIn)
    {
        setLastAttacker(entityIn);
        return false;
    }

    /**
     * Returns whether player is sleeping or not
     */
    public boolean isPlayerSleeping()
    {
        return false;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (!worldObj.isRemote)
        {
            int i = getArrowCountInEntity();

            if (i > 0)
            {
                if (arrowHitTimer <= 0)
                {
                    arrowHitTimer = 20 * (30 - i);
                }

                --arrowHitTimer;

                if (arrowHitTimer <= 0)
                {
                    setArrowCountInEntity(i - 1);
                }
            }

            for (int j = 0; j < 5; ++j)
            {
                ItemStack itemstack = previousEquipment[j];
                ItemStack itemstack1 = getEquipmentInSlot(j);

                if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
                {
                    ((WorldServer) worldObj).getEntityTracker().sendToAllTrackingEntity(this, new S04PacketEntityEquipment(getEntityId(), j, itemstack1));

                    if (itemstack != null)
                    {
                        attributeMap.removeAttributeModifiers(itemstack.getAttributeModifiers());
                    }

                    if (itemstack1 != null)
                    {
                        attributeMap.applyAttributeModifiers(itemstack1.getAttributeModifiers());
                    }

                    previousEquipment[j] = itemstack1 == null ? null : itemstack1.copy();
                }
            }

            if (ticksExisted % 20 == 0)
            {
                getCombatTracker().reset();
            }
        }

        onLivingUpdate();
        double d0 = posX - prevPosX;
        double d1 = posZ - prevPosZ;
        float f = (float)(d0 * d0 + d1 * d1);
        float f1 = renderYawOffset;
        float f2 = 0.0F;
        prevOnGroundSpeedFactor = onGroundSpeedFactor;
        float f3 = 0.0F;

        if (f > 0.0025000002F)
        {
            f3 = 1.0F;
            f2 = (float)Math.sqrt(f) * 3.0F;
            f1 = (float)MathHelper.func_181159_b(d1, d0) * 180.0F / (float)Math.PI - 90.0F;
        }

        if (swingProgress > 0.0F)
        {
            f1 = rotationYaw;
        }

        if (!onGround)
        {
            f3 = 0.0F;
        }

        onGroundSpeedFactor += (f3 - onGroundSpeedFactor) * 0.3F;
        worldObj.theProfiler.startSection("headTurn");
        f2 = func_110146_f(f1, f2);
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("rangeChecks");

        while (rotationYaw - prevRotationYaw < -180.0F)
        {
            prevRotationYaw -= 360.0F;
        }

        while (rotationYaw - prevRotationYaw >= 180.0F)
        {
            prevRotationYaw += 360.0F;
        }

        while (renderYawOffset - prevRenderYawOffset < -180.0F)
        {
            prevRenderYawOffset -= 360.0F;
        }

        while (renderYawOffset - prevRenderYawOffset >= 180.0F)
        {
            prevRenderYawOffset += 360.0F;
        }

        while (rotationPitch - prevRotationPitch < -180.0F)
        {
            prevRotationPitch -= 360.0F;
        }

        while (rotationPitch - prevRotationPitch >= 180.0F)
        {
            prevRotationPitch += 360.0F;
        }

        while (rotationYawHead - prevRotationYawHead < -180.0F)
        {
            prevRotationYawHead -= 360.0F;
        }

        while (rotationYawHead - prevRotationYawHead >= 180.0F)
        {
            prevRotationYawHead += 360.0F;
        }

        worldObj.theProfiler.endSection();
        movedDistance += f2;
    }

    protected float func_110146_f(float p_110146_1_, float p_110146_2_)
    {
        float f = MathHelper.wrapAngleTo180_float(p_110146_1_ - renderYawOffset);
        renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - renderYawOffset);
        boolean flag = f1 < -90.0F || f1 >= 90.0F;

        if (f1 < -75.0F)
        {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F)
        {
            f1 = 75.0F;
        }

        renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F)
        {
            renderYawOffset += f1 * 0.2F;
        }

        if (flag)
        {
            p_110146_2_ *= -1.0F;
        }

        return p_110146_2_;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (jumpTicks > 0)
        {
            --jumpTicks;
        }

        if (newPosRotationIncrements > 0)
        {
            double d0 = posX + (newPosX - posX) / (double) newPosRotationIncrements;
            double d1 = posY + (newPosY - posY) / (double) newPosRotationIncrements;
            double d2 = posZ + (newPosZ - posZ) / (double) newPosRotationIncrements;
            double d3 = MathHelper.wrapAngleTo180_double(newRotationYaw - (double) rotationYaw);
            rotationYaw = (float)((double) rotationYaw + d3 / (double) newPosRotationIncrements);
            rotationPitch = (float)((double) rotationPitch + (newRotationPitch - (double) rotationPitch) / (double) newPosRotationIncrements);
            --newPosRotationIncrements;
            setPosition(d0, d1, d2);
            setRotation(rotationYaw, rotationPitch);
        }
        else if (!isServerWorld())
        {
            motionX *= 0.98D;
            motionY *= 0.98D;
            motionZ *= 0.98D;
        }

        if (Math.abs(motionX) < 0.005D)
        {
            motionX = 0.0D;
        }

        if (Math.abs(motionY) < 0.005D)
        {
            motionY = 0.0D;
        }

        if (Math.abs(motionZ) < 0.005D)
        {
            motionZ = 0.0D;
        }

        worldObj.theProfiler.startSection("ai");

        if (isMovementBlocked())
        {
            isJumping = false;
            moveStrafing = 0.0F;
            moveForward = 0.0F;
            randomYawVelocity = 0.0F;
        }
        else if (isServerWorld())
        {
            worldObj.theProfiler.startSection("newAi");
            updateEntityActionState();
            worldObj.theProfiler.endSection();
        }

        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("jump");

        if (isJumping)
        {
            if (isInWater())
            {
                updateAITick();
            }
            else if (isInLava())
            {
                handleJumpLava();
            }
            else if (onGround && jumpTicks == 0)
            {
                jump();
                jumpTicks = 10;
            }
        }
        else
        {
            jumpTicks = 0;
        }

        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("travel");
        moveStrafing *= 0.98F;
        moveForward *= 0.98F;
        randomYawVelocity *= 0.9F;
        moveEntityWithHeading(moveStrafing, moveForward);
        worldObj.theProfiler.endSection();
        worldObj.theProfiler.startSection("push");

        if (!worldObj.isRemote)
        {
            collideWithNearbyEntities();
        }

        worldObj.theProfiler.endSection();
    }

    protected void updateEntityActionState()
    {
    }

    protected void collideWithNearbyEntities()
    {
        List<Entity> list = worldObj.getEntitiesInAABBexcluding(this, getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D), Predicates.and (EntitySelectors.NOT_SPECTATING, Entity::canBePushed));

        if (!list.isEmpty())
        {
            for (Entity entity : list) {
                collideWithEntity(entity);
            }
        }
    }

    protected void collideWithEntity(Entity p_82167_1_)
    {
        p_82167_1_.applyEntityCollision(this);
    }

    /**
     * Called when a player mounts an entity. e.g. mounts a pig, mounts a boat.
     */
    public void mountEntity(Entity entityIn)
    {
        if (ridingEntity != null && entityIn == null)
        {
            if (!worldObj.isRemote)
            {
                dismountEntity(ridingEntity);
            }

            if (ridingEntity != null)
            {
                ridingEntity.riddenByEntity = null;
            }

            ridingEntity = null;
        }
        else
        {
            super.mountEntity(entityIn);
        }
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        super.updateRidden();
        prevOnGroundSpeedFactor = onGroundSpeedFactor;
        onGroundSpeedFactor = 0.0F;
        fallDistance = 0.0F;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_)
    {
        newPosX = x;
        newPosY = y;
        newPosZ = z;
        newRotationYaw = yaw;
        newRotationPitch = pitch;
        newPosRotationIncrements = posRotationIncrements;
    }

    public void setJumping(boolean p_70637_1_)
    {
        isJumping = p_70637_1_;
    }

    /**
     * Called whenever an item is picked up from walking over it. Args: pickedUpEntity, stackSize
     */
    public void onItemPickup(Entity p_71001_1_, int p_71001_2_)
    {
        if (!p_71001_1_.isDead && !worldObj.isRemote)
        {
            EntityTracker entitytracker = ((WorldServer) worldObj).getEntityTracker();

            if (p_71001_1_ instanceof EntityItem)
            {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }

            if (p_71001_1_ instanceof EntityArrow)
            {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }

            if (p_71001_1_ instanceof EntityXPOrb)
            {
                entitytracker.sendToAllTrackingEntity(p_71001_1_, new S0DPacketCollectItem(p_71001_1_.getEntityId(), getEntityId()));
            }
        }
    }

    /**
     * returns true if the entity provided in the argument can be seen. (Raytrace)
     */
    public boolean canEntityBeSeen(Entity entityIn)
    {
        return worldObj.rayTraceBlocks(new Vec3(posX, posY + (double) getEyeHeight(), posZ), new Vec3(entityIn.posX, entityIn.posY + (double)entityIn.getEyeHeight(), entityIn.posZ)) == null;
    }

    /**
     * returns a (normalized) vector of where this entity is looking
     */
    public Vec3 getLookVec()
    {
        return getLook(1.0F);
    }

    /**
     * interpolated look vector
     */
    public Vec3 getLook(float partialTicks)
    {
        if (partialTicks == 1.0F)
        {
            return getVectorForRotation(rotationPitch, rotationYawHead);
        }
        else
        {
            float f = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks;
            float f1 = prevRotationYawHead + (rotationYawHead - prevRotationYawHead) * partialTicks;
            return getVectorForRotation(f, f1);
        }
    }

    /**
     * Returns where in the swing animation the living entity is (from 0 to 1).  Args: partialTickTime
     */
    public float getSwingProgress(float partialTickTime)
    {
        float f = swingProgress - prevSwingProgress;

        if (f < 0.0F)
        {
            ++f;
        }

        return prevSwingProgress + f * partialTickTime;
    }

    /**
     * Returns whether the entity is in a server world
     */
    public boolean isServerWorld()
    {
        return !worldObj.isRemote;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !isDead;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return !isDead;
    }

    /**
     * Sets that this entity has been attacked.
     */
    protected void setBeenAttacked()
    {
        velocityChanged = rand.nextDouble() >= getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue();
    }

    public float getRotationYawHead()
    {
        return rotationYawHead;
    }

    /**
     * Sets the head's yaw rotation of the entity.
     */
    public void setRotationYawHead(float rotation)
    {
        rotationYawHead = rotation;
    }

    public void func_181013_g(float p_181013_1_)
    {
        renderYawOffset = p_181013_1_;
    }

    public float getAbsorptionAmount()
    {
        return absorptionAmount;
    }

    public void setAbsorptionAmount(float amount)
    {
        if (amount < 0.0F)
        {
            amount = 0.0F;
        }

        absorptionAmount = amount;
    }

    public Team getTeam()
    {
        return worldObj.getScoreboard().getPlayersTeam(getUniqueID().toString());
    }

    public boolean isOnSameTeam(EntityLivingBase otherEntity)
    {
        return isOnTeam(otherEntity.getTeam());
    }

    /**
     * Returns true if the entity is on a specific team.
     */
    public boolean isOnTeam(Team p_142012_1_)
    {
        return getTeam() != null && getTeam().isSameTeam(p_142012_1_);
    }

    /**
     * Sends an ENTER_COMBAT packet to the client
     */
    public void sendEnterCombat()
    {
    }

    /**
     * Sends an END_COMBAT packet to the client
     */
    public void sendEndCombat()
    {
    }

    protected void markPotionsDirty()
    {
        potionsNeedUpdate = true;
    }
}
