package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityFootStepFX extends EntityFX
{
    private static final ResourceLocation FOOTPRINT_TEXTURE = new ResourceLocation("textures/particle/footprint.png");
    private int footstepAge;
    private final int footstepMaxAge;
    private final TextureManager currentFootSteps;

    protected EntityFootStepFX(TextureManager currentFootStepsIn, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        currentFootSteps = currentFootStepsIn;
        motionX = motionY = motionZ = 0.0D;
        footstepMaxAge = 200;
    }

    /**
     * Renders the particle
     */
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_)
    {
        float f = ((float) footstepAge + partialTicks) / (float) footstepMaxAge;
        f = f * f;
        float f1 = 2.0F - f * 2.0F;

        if (f1 > 1.0F)
        {
            f1 = 1.0F;
        }

        f1 = f1 * 0.2F;
        GlStateManager.disableLighting();
        float f3 = (float)(posX - interpPosX);
        float f4 = (float)(posY - interpPosY);
        float f5 = (float)(posZ - interpPosZ);
        float f6 = worldObj.getLightBrightness(new BlockPos(this));
        currentFootSteps.bindTexture(FOOTPRINT_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);

        /*
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldRendererIn.pos(f3 - 0.125F, f4, f5 + 0.125F).tex(0.0D, 1.0D).color(f6, f6, f6, f1).endVertex();
        worldRendererIn.pos(f3 + 0.125F, f4, f5 + 0.125F).tex(1.0D, 1.0D).color(f6, f6, f6, f1).endVertex();
        worldRendererIn.pos(f3 + 0.125F, f4, f5 - 0.125F).tex(1.0D, 0.0D).color(f6, f6, f6, f1).endVertex();
        worldRendererIn.pos(f3 - 0.125F, f4, f5 - 0.125F).tex(0.0D, 0.0D).color(f6, f6, f6, f1).endVertex();
        Tessellator.getInstance().draw();

         */

        worldRendererIn.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRendererIn.pos(f3 - 0.125F, f4, f5 + 0.125F).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        worldRendererIn.pos(f3 + 0.125F, f4, f5 + 0.125F).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        worldRendererIn.pos(f3 + 0.125F, f4, f5 - 0.125F).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        worldRendererIn.pos(f3 - 0.125F, f4, f5 - 0.125F).color(0.0f, 1.0f, 0.0f, 1.0f).endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        ++footstepAge;

        if (footstepAge == footstepMaxAge)
        {
            setDead();
        }
    }

    public int getFXLayer()
    {
        return 3;
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_)
        {
            return new EntityFootStepFX(Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
