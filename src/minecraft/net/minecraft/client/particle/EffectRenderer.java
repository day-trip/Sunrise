package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class EffectRenderer
{
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");

    /** Reference to the World object. */
    protected World worldObj;
    private final List<EntityFX>[][] fxLayers = new List[4][];
    private final List<EntityParticleEmitter> particleEmitters = Lists.newArrayList();
    private final TextureManager renderer;

    /** RNG. */
    private final Random rand = new Random();
    private final Map<Integer, IParticleFactory> particleTypes = Maps.newHashMap();

    public EffectRenderer(World worldIn, TextureManager rendererIn)
    {
        worldObj = worldIn;
        renderer = rendererIn;

        for (int i = 0; i < 4; ++i)
        {
            fxLayers[i] = new List[2];

            for (int j = 0; j < 2; ++j)
            {
                fxLayers[i][j] = Lists.newCopyOnWriteArrayList();
            }
        }

        registerVanillaParticles();
    }

    private void registerVanillaParticles()
    {
        registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new EntityExplodeFX.Factory());
        registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new EntityBubbleFX.Factory());
        registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new EntitySplashFX.Factory());
        registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new EntityFishWakeFX.Factory());
        registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new EntityRainFX.Factory());
        registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new EntitySuspendFX.Factory());
        registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(EnumParticleTypes.CRIT.getParticleID(), new EntityCrit2FX.Factory());
        registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new EntityCrit2FX.MagicFactory());
        registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new EntitySmokeFX.Factory());
        registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new EntityCritFX.Factory());
        registerParticle(EnumParticleTypes.SPELL.getParticleID(), new EntitySpellParticleFX.Factory());
        registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new EntitySpellParticleFX.InstantFactory());
        registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new EntitySpellParticleFX.MobFactory());
        registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new EntitySpellParticleFX.AmbientMobFactory());
        registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new EntitySpellParticleFX.WitchFactory());
        registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new EntityDropParticleFX.WaterFactory());
        registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new EntityDropParticleFX.LavaFactory());
        registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new EntityHeartFX.AngryVillagerFactory());
        registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new EntityAuraFX.HappyVillagerFactory());
        registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new EntityAuraFX.Factory());
        registerParticle(EnumParticleTypes.NOTE.getParticleID(), new EntityNoteFX.Factory());
        registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new EntityPortalFX.Factory());
        registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new EntityEnchantmentTableParticleFX.EnchantmentTable());
        registerParticle(EnumParticleTypes.FLAME.getParticleID(), new EntityFlameFX.Factory());
        registerParticle(EnumParticleTypes.LAVA.getParticleID(), new EntityLavaFX.Factory());
        registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new EntityFootStepFX.Factory());
        registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new EntityCloudFX.Factory());
        registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new EntityReddustFX.Factory());
        registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new EntityBreakingFX.SnowballFactory());
        registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new EntitySnowShovelFX.Factory());
        registerParticle(EnumParticleTypes.SLIME.getParticleID(), new EntityBreakingFX.SlimeFactory());
        registerParticle(EnumParticleTypes.HEART.getParticleID(), new EntityHeartFX.Factory());
        registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new EntityBreakingFX.Factory());
        registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new EntityDiggingFX.Factory());
        registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new EntityBlockDustFX.Factory());
        registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new EntityHugeExplodeFX.Factory());
        registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new EntityLargeExplodeFX.Factory());
        registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new EntityFirework.Factory());
        registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new MobAppearance.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory)
    {
        particleTypes.put(id, particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes)
    {
        particleEmitters.add(new EntityParticleEmitter(worldObj, entityIn, particleTypes));
    }

    /**
     * Spawns the relevant particle according to the particle id.
     */
    public EntityFX spawnEffectParticle(int particleId, double p_178927_2_, double p_178927_4_, double p_178927_6_, double p_178927_8_, double p_178927_10_, double p_178927_12_, int... p_178927_14_)
    {
        IParticleFactory iparticlefactory = particleTypes.get(particleId);

        if (iparticlefactory != null)
        {
            EntityFX entityfx = iparticlefactory.getEntityFX(particleId, worldObj, p_178927_2_, p_178927_4_, p_178927_6_, p_178927_8_, p_178927_10_, p_178927_12_, p_178927_14_);

            if (entityfx != null)
            {
                addEffect(entityfx);
                return entityfx;
            }
        }

        return null;
    }

    public void addEffect(EntityFX effect)
    {
        int i = effect.getFXLayer();
        int j = effect.getAlpha() != 1.0F ? 0 : 1;

        if (fxLayers[i][j].size() >= 4000)
        {
            fxLayers[i][j].remove(0);
        }

        fxLayers[i][j].add(effect);
    }

    public void updateEffects()
    {
        for (int i = 0; i < 4; ++i)
        {
            updateEffectLayer(i);
        }

        List<EntityParticleEmitter> list = Lists.newArrayList();

        for (EntityParticleEmitter entityparticleemitter : particleEmitters)
        {
            entityparticleemitter.onUpdate();

            if (entityparticleemitter.isDead)
            {
                list.add(entityparticleemitter);
            }
        }

        particleEmitters.removeAll(list);
    }

    private void updateEffectLayer(int p_178922_1_)
    {
        for (int i = 0; i < 2; ++i)
        {
            updateEffectAlphaLayer(fxLayers[p_178922_1_][i]);
        }
    }

    private void updateEffectAlphaLayer(List<EntityFX> p_178925_1_)
    {
        List<EntityFX> list = Lists.newArrayList();

        for (EntityFX entityfx : p_178925_1_) {
            tickParticle(entityfx);

            if (entityfx.isDead) {
                list.add(entityfx);
            }
        }

        p_178925_1_.removeAll(list);
    }

    private void tickParticle(EntityFX p_178923_1_)
    {
        try
        {
            p_178923_1_.onUpdate();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            int i = p_178923_1_.getFXLayer();
            crashreportcategory.addCrashSectionCallable("Particle", p_178923_1_::toString);
            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i)));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Renders all current particles. Args player, partialTickTime
     */
    public void renderParticles(Entity entityIn, float partialTicks)
    {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                int i_f = i;

                if (!fxLayers[i][j].isEmpty())
                {
                    switch (j)
                    {
                        case 0:
                            GlStateManager.depthMask(false);
                            break;

                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    switch (i)
                    {
                        case 0:
                        default:
                            renderer.bindTexture(particleTextures);
                            break;

                        case 1:
                            renderer.bindTexture(TextureMap.locationBlocksTexture);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (int k = 0; k < fxLayers[i][j].size(); ++k)
                    {
                        EntityFX entityfx = fxLayers[i][j].get(k);

                        try
                        {
                            entityfx.renderParticle(worldrenderer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        }
                        catch (Throwable throwable)
                        {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.addCrashSectionCallable("Particle", entityfx::toString);
                            crashreportcategory.addCrashSectionCallable("Particle Type", () -> i_f == 0 ? "MISC_TEXTURE" : i_f == 1 ? "TERRAIN_TEXTURE" : "Unknown - " + i_f);
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Entity entityIn, float p_78872_2_)
    {
        float f1 = MathHelper.cos(entityIn.getRotationYaw() * 0.017453292F);
        float f2 = MathHelper.sin(entityIn.getRotationYaw() * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(entityIn.getRotationPitch() * 0.017453292F);
        float f4 = f1 * MathHelper.sin(entityIn.getRotationPitch() * 0.017453292F);
        float f5 = MathHelper.cos(entityIn.getRotationPitch() * 0.017453292F);

        for (int i = 0; i < 2; ++i)
        {
            List<EntityFX> list = fxLayers[3][i];

            if (!list.isEmpty())
            {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                for (EntityFX entityfx : list) {
                    entityfx.renderParticle(worldrenderer, entityIn, p_78872_2_, f1, f5, f2, f3, f4);
                }
            }
        }
    }

    public void clearEffects(World worldIn)
    {
        worldObj = worldIn;

        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                fxLayers[i][j].clear();
            }
        }

        particleEmitters.clear();
    }

    public void addBlockDestroyEffects(BlockPos pos, IBlockState state)
    {
        if (state.getBlock().getMaterial() != Material.air)
        {
            state = state.getBlock().getActualState(state, worldObj, pos);
            int i = 4;

            for (int j = 0; j < i; ++j)
            {
                for (int k = 0; k < i; ++k)
                {
                    for (int l = 0; l < i; ++l)
                    {
                        double d0 = (double)pos.getX() + ((double)j + 0.5D) / (double)i;
                        double d1 = (double)pos.getY() + ((double)k + 0.5D) / (double)i;
                        double d2 = (double)pos.getZ() + ((double)l + 0.5D) / (double)i;
                        addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, d0 - (double)pos.getX() - 0.5D, d1 - (double)pos.getY() - 0.5D, d2 - (double)pos.getZ() - 0.5D, state)).func_174846_a(pos));
                    }
                }
            }
        }
    }

    /**
     * Adds block hit particles for the specified block
     */
    public void addBlockHitEffects(BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = worldObj.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block.getRenderType() != -1)
        {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            float f = 0.1F;
            double d0 = (double)i + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinX();
            double d1 = (double)j + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinY();
            double d2 = (double)k + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double)(f * 2.0F)) + (double)f + block.getBlockBoundsMinZ();

            if (side == EnumFacing.DOWN)
            {
                d1 = (double)j + block.getBlockBoundsMinY() - (double)f;
            }

            if (side == EnumFacing.UP)
            {
                d1 = (double)j + block.getBlockBoundsMaxY() + (double)f;
            }

            if (side == EnumFacing.NORTH)
            {
                d2 = (double)k + block.getBlockBoundsMinZ() - (double)f;
            }

            if (side == EnumFacing.SOUTH)
            {
                d2 = (double)k + block.getBlockBoundsMaxZ() + (double)f;
            }

            if (side == EnumFacing.WEST)
            {
                d0 = (double)i + block.getBlockBoundsMinX() - (double)f;
            }

            if (side == EnumFacing.EAST)
            {
                d0 = (double)i + block.getBlockBoundsMaxX() + (double)f;
            }

            addEffect((new EntityDiggingFX(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate)).func_174846_a(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    public void moveToAlphaLayer(EntityFX effect)
    {
        moveToLayer(effect, 1, 0);
    }

    public void moveToNoAlphaLayer(EntityFX effect)
    {
        moveToLayer(effect, 0, 1);
    }

    private void moveToLayer(EntityFX effect, int p_178924_2_, int p_178924_3_)
    {
        for (int i = 0; i < 4; ++i)
        {
            if (fxLayers[i][p_178924_2_].contains(effect))
            {
                fxLayers[i][p_178924_2_].remove(effect);
                fxLayers[i][p_178924_3_].add(effect);
            }
        }
    }

    public String getStatistics()
    {
        int i = 0;

        for (int j = 0; j < 4; ++j)
        {
            for (int k = 0; k < 2; ++k)
            {
                i += fxLayers[j][k].size();
            }
        }

        return "" + i;
    }
}
