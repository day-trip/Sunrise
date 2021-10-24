package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.ListChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.block.tileentity.TileEntity;
import net.minecraft.block.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");

    /** A reference to the Minecraft object. */
    private final Minecraft mc;

    /** The RenderEngine instance used by RenderGlobal */
    private final TextureManager renderEngine;
    private final RenderManager renderManager;
    private WorldClient theWorld;
    private Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();
    private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = Lists.newArrayListWithCapacity(69696);
    private final Set<TileEntity> field_181024_n = Sets.newHashSet();
    private ViewFrustum viewFrustum;

    /** The star GL Call list */
    private int starGLCallList = -1;

    /** OpenGL sky list */
    private int glSkyList = -1;

    /** OpenGL sky list 2 */
    private int glSkyList2 = -1;
    private final VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;

    /**
     * counts the cloud render updates. Used with mod to stagger some updates
     */
    private int cloudTickCounter;
    private final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.newHashMap();
    private final Map<BlockPos, ISound> mapSoundPositions = Maps.newHashMap();
    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
    private Framebuffer entityOutlineFramebuffer;

    /** Stores the shader group for the entity_outline shader */
    private ShaderGroup entityOutlineShader;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;

    /** Render entities startup counter (init value=2) */
    private int renderEntitiesStartupCounter = 2;

    /** Count entities total */
    private int countEntitiesTotal;

    /** Count entities rendered */
    private int countEntitiesRendered;

    /** Count entities hidden */
    private int countEntitiesHidden;
    private boolean debugFixTerrainFrustum;
    private ClippingHelper debugFixedClippingHelper;
    private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
    private final Vector3d debugTerrainFrustumPosition = new Vector3d();
    private boolean vboEnabled;
    IRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    private boolean displayListEntitiesDirty = true;

    public RenderGlobal(Minecraft mcIn)
    {
        mc = mcIn;
        renderManager = mcIn.getRenderManager();
        renderEngine = mcIn.getTextureManager();
        renderEngine.bindTexture(locationForcefieldPng);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        updateDestroyBlockIcons();
        vboEnabled = OpenGlHelper.useVbo();

        if (vboEnabled)
        {
            renderContainer = new VboRenderList();
            renderChunkFactory = new VboChunkFactory();
        }
        else
        {
            renderContainer = new RenderList();
            renderChunkFactory = new ListChunkFactory();
        }

        vertexBufferFormat = new VertexFormat();
        vertexBufferFormat.func_181721_a(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        generateStars();
        generateSky();
        generateSky2();
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons()
    {
        TextureMap texturemap = mc.getTextureMapBlocks();

        for (int i = 0; i < destroyBlockIcons.length; ++i)
        {
            destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }

    /**
     * Creates the entity outline shader to be stored in RenderGlobal.entityOutlineShader
     */
    public void makeEntityOutlineShader()
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (ShaderLinkHelper.getStaticShaderLinkHelper() == null)
            {
                ShaderLinkHelper.setNewStaticShaderLinkHelper();
            }

            ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

            try
            {
                entityOutlineShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), resourcelocation);
                entityOutlineShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
                entityOutlineFramebuffer = entityOutlineShader.getFramebufferRaw("final");
            }
            catch (IOException | JsonSyntaxException ioexception)
            {
                logger.warn("Failed to load shader: " + resourcelocation, ioexception);
                entityOutlineShader = null;
                entityOutlineFramebuffer = null;
            }
        }
        else
        {
            entityOutlineShader = null;
            entityOutlineFramebuffer = null;
        }
    }

    public void renderEntityOutlineFramebuffer()
    {
        if (isRenderEntityOutlines())
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            entityOutlineFramebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
            GlStateManager.disableBlend();
        }
    }

    protected boolean isRenderEntityOutlines()
    {
        return entityOutlineFramebuffer != null && entityOutlineShader != null && mc.thePlayer != null && mc.thePlayer.isSpectator() && mc.gameSettings.keyBindSpectatorOutlines.isKeyDown();
    }

    private void generateSky2()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (sky2VBO != null)
        {
            sky2VBO.deleteGlBuffers();
        }

        if (glSkyList2 >= 0)
        {
            GLAllocation.deleteDisplayLists(glSkyList2);
            glSkyList2 = -1;
        }

        if (vboEnabled)
        {
            sky2VBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, -16.0F, true);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            sky2VBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            glSkyList2 = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList2, GL11.GL_COMPILE);
            renderSky(worldrenderer, -16.0F, true);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (skyVBO != null)
        {
            skyVBO.deleteGlBuffers();
        }

        if (glSkyList >= 0)
        {
            GLAllocation.deleteDisplayLists(glSkyList);
            glSkyList = -1;
        }

        if (vboEnabled)
        {
            skyVBO = new VertexBuffer(vertexBufferFormat);
            renderSky(worldrenderer, 16.0F, false);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            skyVBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            glSkyList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(glSkyList, GL11.GL_COMPILE);
            renderSky(worldrenderer, 16.0F, false);
            tessellator.draw();
            GL11.glEndList();
        }
    }

    private void renderSky(WorldRenderer worldRendererIn, float p_174968_2_, boolean p_174968_3_)
    {
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int k = -384; k <= 384; k += 64)
        {
            for (int l = -384; l <= 384; l += 64)
            {
                float f = (float)k;
                float f1 = (float)(k + 64);

                if (p_174968_3_)
                {
                    f1 = (float)k;
                    f = (float)(k + 64);
                }

                worldRendererIn.pos(f, p_174968_2_, l).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, l).endVertex();
                worldRendererIn.pos(f1, p_174968_2_, l + 64).endVertex();
                worldRendererIn.pos(f, p_174968_2_, l + 64).endVertex();
            }
        }
    }

    private void generateStars()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        if (starVBO != null)
        {
            starVBO.deleteGlBuffers();
        }

        if (starGLCallList >= 0)
        {
            GLAllocation.deleteDisplayLists(starGLCallList);
            starGLCallList = -1;
        }

        if (vboEnabled)
        {
            starVBO = new VertexBuffer(vertexBufferFormat);
            renderStars(worldrenderer);
            worldrenderer.finishDrawing();
            worldrenderer.reset();
            starVBO.func_181722_a(worldrenderer.getByteBuffer());
        }
        else
        {
            starGLCallList = GLAllocation.generateDisplayLists(1);
            GlStateManager.pushMatrix();
            GL11.glNewList(starGLCallList, GL11.GL_COMPILE);
            renderStars(worldrenderer);
            tessellator.draw();
            GL11.glEndList();
            GlStateManager.popMatrix();
        }
    }

    private void renderStars(WorldRenderer worldRendererIn)
    {
        Random random = new Random(10842L);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    worldRendererIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }

    /**
     * set null to clear
     */
    public void setWorldAndLoadRenderers(WorldClient worldClientIn)
    {
        if (theWorld != null)
        {
            theWorld.removeWorldAccess(this);
        }

        frustumUpdatePosX = Double.MIN_VALUE;
        frustumUpdatePosY = Double.MIN_VALUE;
        frustumUpdatePosZ = Double.MIN_VALUE;
        frustumUpdatePosChunkX = Integer.MIN_VALUE;
        frustumUpdatePosChunkY = Integer.MIN_VALUE;
        frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        renderManager.set(worldClientIn);
        theWorld = worldClientIn;

        if (worldClientIn != null)
        {
            worldClientIn.addWorldAccess(this);
            loadRenderers();
        }
    }

    /**
     * Loads all the renderers and sets up the basic settings usage
     */
    public void loadRenderers()
    {
        if (theWorld != null)
        {
            displayListEntitiesDirty = true;
            Blocks.leaves.setGraphicsLevel(mc.gameSettings.fancyGraphics);
            Blocks.leaves2.setGraphicsLevel(mc.gameSettings.fancyGraphics);
            renderDistanceChunks = mc.gameSettings.renderDistanceChunks;
            boolean flag = vboEnabled;
            vboEnabled = OpenGlHelper.useVbo();

            if (flag && !vboEnabled)
            {
                renderContainer = new RenderList();
                renderChunkFactory = new ListChunkFactory();
            }
            else if (!flag && vboEnabled)
            {
                renderContainer = new VboRenderList();
                renderChunkFactory = new VboChunkFactory();
            }

            if (flag != vboEnabled)
            {
                generateStars();
                generateSky();
                generateSky2();
            }

            if (viewFrustum != null)
            {
                viewFrustum.deleteGlResources();
            }

            stopChunkUpdates();

            synchronized (field_181024_n)
            {
                field_181024_n.clear();
            }

            viewFrustum = new ViewFrustum(theWorld, mc.gameSettings.renderDistanceChunks, this, renderChunkFactory);

            if (theWorld != null)
            {
                Entity entity = mc.getRenderViewEntity();

                if (entity != null)
                {
                    viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }

            renderEntitiesStartupCounter = 2;
        }
    }

    protected void stopChunkUpdates()
    {
        chunksToUpdate.clear();
        renderDispatcher.stopChunkUpdates();
    }

    public void createBindEntityOutlineFbs(int p_72720_1_, int p_72720_2_)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (entityOutlineShader != null)
            {
                entityOutlineShader.createBindFramebuffers(p_72720_1_, p_72720_2_);
            }
        }
    }

    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks)
    {
        if (renderEntitiesStartupCounter > 0)
        {
            --renderEntitiesStartupCounter;
        }
        else
        {
            double d0 = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
            double d1 = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
            double d2 = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;
            theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(theWorld, mc.getTextureManager(), mc.fontRendererObj, mc.getRenderViewEntity(), partialTicks);
            renderManager.cacheActiveRenderInfo(theWorld, mc.fontRendererObj, mc.getRenderViewEntity(), mc.pointedEntity, mc.gameSettings, partialTicks);
            countEntitiesTotal = 0;
            countEntitiesRendered = 0;
            countEntitiesHidden = 0;
            Entity entity = mc.getRenderViewEntity();
            double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = d3;
            TileEntityRendererDispatcher.staticPlayerY = d4;
            TileEntityRendererDispatcher.staticPlayerZ = d5;
            renderManager.setRenderPosition(d3, d4, d5);
            mc.entityRenderer.enableLightmap();
            theWorld.theProfiler.endStartSection("global");
            List<Entity> list = theWorld.getLoadedEntityList();
            countEntitiesTotal = list.size();

            for (int i = 0; i < theWorld.weatherEffects.size(); ++i)
            {
                Entity entity1 = theWorld.weatherEffects.get(i);
                ++countEntitiesRendered;

                if (entity1.isInRangeToRender3d(d0, d1, d2))
                {
                    renderManager.renderEntitySimple(entity1, partialTicks);
                }
            }

            if (isRenderEntityOutlines())
            {
                GlStateManager.depthFunc(519);
                GlStateManager.disableFog();
                entityOutlineFramebuffer.framebufferClear();
                entityOutlineFramebuffer.bindFramebuffer(false);
                theWorld.theProfiler.endStartSection("entityOutlines");
                RenderHelper.disableStandardItemLighting();
                renderManager.setRenderOutlines(true);

                for (Entity entity3 : list) {
                    boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
                    boolean flag1 = entity3.isInRangeToRender3d(d0, d1, d2) && (entity3.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity3.getEntityBoundingBox()) || entity3.riddenByEntity == mc.thePlayer) && entity3 instanceof EntityPlayer;

                    if ((entity3 != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag) && flag1) {
                        renderManager.renderEntitySimple(entity3, partialTicks);
                    }
                }

                renderManager.setRenderOutlines(false);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.depthMask(false);
                entityOutlineShader.loadShaderGroup(partialTicks);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.enableFog();
                GlStateManager.enableBlend();
                GlStateManager.enableColorMaterial();
                GlStateManager.depthFunc(515);
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
            }

            theWorld.theProfiler.endStartSection("entities");
            label738:

            for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : renderInfos)
            {
                Chunk chunk = theWorld.getChunkFromBlockCoords(renderglobal$containerlocalrenderinformation.renderChunk.getPosition());
                ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty())
                {
                    Iterator<Entity> iterator = classinheritancemultimap.iterator();

                    while (true)
                    {
                        Entity entity2;
                        boolean flag2;

                        while (true)
                        {
                            if (!iterator.hasNext())
                            {
                                continue label738;
                            }

                            entity2 = (Entity)iterator.next();
                            flag2 = renderManager.shouldRender(entity2, camera, d0, d1, d2) || entity2.riddenByEntity == mc.thePlayer;

                            if (!flag2)
                            {
                                break;
                            }

                            boolean flag3 = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

                            if ((entity2 != mc.getRenderViewEntity() || mc.gameSettings.thirdPersonView != 0 || flag3) && (entity2.posY < 0.0D || entity2.posY >= 256.0D || theWorld.isBlockLoaded(new BlockPos(entity2))))
                            {
                                ++countEntitiesRendered;
                                renderManager.renderEntitySimple(entity2, partialTicks);
                                break;
                            }
                        }

                        if (!flag2 && entity2 instanceof EntityWitherSkull)
                        {
                            mc.getRenderManager().renderWitherSkull(entity2, partialTicks);
                        }
                    }
                }
            }

            theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();

            for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 : renderInfos)
            {
                List<TileEntity> list1 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();

                if (!list1.isEmpty())
                {
                    for (TileEntity tileentity2 : list1)
                    {
                        TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, -1);
                    }
                }
            }

            synchronized (field_181024_n)
            {
                for (TileEntity tileentity : field_181024_n)
                {
                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity, partialTicks, -1);
                }
            }

            preRenderDamagedBlocks();

            for (DestroyBlockProgress destroyblockprogress : damagedBlocks.values())
            {
                BlockPos blockpos = destroyblockprogress.getPosition();
                TileEntity tileentity1 = theWorld.getTileEntity(blockpos);

                if (tileentity1 instanceof TileEntityChest)
                {
                    TileEntityChest tileentitychest = (TileEntityChest)tileentity1;

                    if (tileentitychest.adjacentChestXNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.WEST);
                        tileentity1 = theWorld.getTileEntity(blockpos);
                    }
                    else if (tileentitychest.adjacentChestZNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.NORTH);
                        tileentity1 = theWorld.getTileEntity(blockpos);
                    }
                }

                Block block = theWorld.getBlockState(blockpos).getBlock();

                if (tileentity1 != null && (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull))
                {
                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity1, partialTicks, destroyblockprogress.getPartialBlockDamage());
                }
            }

            postRenderDamagedBlocks();
            mc.entityRenderer.disableLightmap();
            mc.mcProfiler.endSection();
        }
    }

    /**
     * Gets the render info for use on the Debug screen
     */
    public String getDebugInfoRenders()
    {
        int i = viewFrustum.renderChunks.length;
        int j = 0;

        for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : renderInfos)
        {
            CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;

            if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty())
            {
                ++j;
            }
        }

        return String.format("C: %d/%d %sD: %d, %s", j, i, mc.renderChunksMany ? "(s) " : "", renderDistanceChunks, renderDispatcher.getDebugInfo());
    }

    /**
     * Gets the entities info for use on the Debug screen
     */
    public String getDebugInfoEntities()
    {
        return "E: " + countEntitiesRendered + "/" + countEntitiesTotal + ", B: " + countEntitiesHidden + ", I: " + (countEntitiesTotal - countEntitiesHidden - countEntitiesRendered);
    }

    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator)
    {
        if (mc.gameSettings.renderDistanceChunks != renderDistanceChunks)
        {
            loadRenderers();
        }

        theWorld.theProfiler.startSection("camera");
        double d0 = viewEntity.posX - frustumUpdatePosX;
        double d1 = viewEntity.posY - frustumUpdatePosY;
        double d2 = viewEntity.posZ - frustumUpdatePosZ;

        if (frustumUpdatePosChunkX != viewEntity.chunkCoordX || frustumUpdatePosChunkY != viewEntity.chunkCoordY || frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || d0 * d0 + d1 * d1 + d2 * d2 > 16.0D)
        {
            frustumUpdatePosX = viewEntity.posX;
            frustumUpdatePosY = viewEntity.posY;
            frustumUpdatePosZ = viewEntity.posZ;
            frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        theWorld.theProfiler.endStartSection("renderlistcamera");
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        renderContainer.initialize(d3, d4, d5);
        theWorld.theProfiler.endStartSection("cull");

        if (debugFixedClippingHelper != null)
        {
            Frustum frustum = new Frustum(debugFixedClippingHelper);
            frustum.setPosition(debugTerrainFrustumPosition.field_181059_a, debugTerrainFrustumPosition.field_181060_b, debugTerrainFrustumPosition.field_181061_c);
            camera = frustum;
        }

        mc.mcProfiler.endStartSection("culling");
        BlockPos blockpos1 = new BlockPos(d3, d4 + (double)viewEntity.getEyeHeight(), d5);
        RenderChunk renderchunk = viewFrustum.getRenderChunk(blockpos1);
        BlockPos blockpos = new BlockPos(MathHelper.floor_double(d3 / 16.0D) * 16, MathHelper.floor_double(d4 / 16.0D) * 16, MathHelper.floor_double(d5 / 16.0D) * 16);
        displayListEntitiesDirty = displayListEntitiesDirty || !chunksToUpdate.isEmpty() || viewEntity.posX != lastViewEntityX || viewEntity.posY != lastViewEntityY || viewEntity.posZ != lastViewEntityZ || (double) viewEntity.getRotationPitch() != lastViewEntityPitch || (double) viewEntity.getRotationYaw() != lastViewEntityYaw;
        lastViewEntityX = viewEntity.posX;
        lastViewEntityY = viewEntity.posY;
        lastViewEntityZ = viewEntity.posZ;
        lastViewEntityPitch = viewEntity.getRotationPitch();
        lastViewEntityYaw = viewEntity.getRotationYaw();
        boolean flag = debugFixedClippingHelper != null;

        if (!flag && displayListEntitiesDirty)
        {
            displayListEntitiesDirty = false;
            renderInfos = Lists.newArrayList();
            Queue<RenderGlobal.ContainerLocalRenderInformation> queue = Lists.newLinkedList();
            boolean flag1 = mc.renderChunksMany;

            if (renderchunk != null)
            {
                boolean flag2 = false;
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 = new ContainerLocalRenderInformation(renderchunk, null, 0);
                Set<EnumFacing> set1 = getVisibleFacings(blockpos1);

                if (set1.size() == 1)
                {
                    Vector3f vector3f = getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    set1.remove(enumfacing);
                }

                if (set1.isEmpty())
                {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator)
                {
                    renderInfos.add(renderglobal$containerlocalrenderinformation3);
                }
                else
                {
                    if (playerSpectator && theWorld.getBlockState(blockpos1).getBlock().isOpaqueCube())
                    {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    queue.add(renderglobal$containerlocalrenderinformation3);
                }
            }
            else
            {
                int i = blockpos1.getY() > 0 ? 248 : 8;

                for (int j = -renderDistanceChunks; j <= renderDistanceChunks; ++j)
                {
                    for (int k = -renderDistanceChunks; k <= renderDistanceChunks; ++k)
                    {
                        RenderChunk renderchunk1 = viewFrustum.getRenderChunk(new BlockPos((j << 4) + 8, i, (k << 4) + 8));

                        if (renderchunk1 != null && camera.isBoundingBoxInFrustum(renderchunk1.boundingBox))
                        {
                            renderchunk1.setFrameIndex(frameCount);
                            queue.add(new ContainerLocalRenderInformation(renderchunk1, null, 0));
                        }
                    }
                }
            }

            while (!queue.isEmpty())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = queue.poll();
                RenderChunk renderchunk3 = renderglobal$containerlocalrenderinformation1.renderChunk;
                EnumFacing enumfacing2 = renderglobal$containerlocalrenderinformation1.facing;
                BlockPos blockpos2 = renderchunk3.getPosition();
                renderInfos.add(renderglobal$containerlocalrenderinformation1);

                for (EnumFacing enumfacing1 : EnumFacing.values())
                {
                    RenderChunk renderchunk2 = func_181562_a(blockpos, renderchunk3, enumfacing1);

                    if ((!flag1 || !renderglobal$containerlocalrenderinformation1.setFacing.contains(enumfacing1.getOpposite())) && (!flag1 || enumfacing2 == null || renderchunk3.getCompiledChunk().isVisible(enumfacing2.getOpposite(), enumfacing1)) && renderchunk2 != null && renderchunk2.setFrameIndex(frameCount) && camera.isBoundingBoxInFrustum(renderchunk2.boundingBox))
                    {
                        RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = new ContainerLocalRenderInformation(renderchunk2, enumfacing1, renderglobal$containerlocalrenderinformation1.counter + 1);
                        renderglobal$containerlocalrenderinformation.setFacing.addAll(renderglobal$containerlocalrenderinformation1.setFacing);
                        renderglobal$containerlocalrenderinformation.setFacing.add(enumfacing1);
                        queue.add(renderglobal$containerlocalrenderinformation);
                    }
                }
            }
        }

        if (debugFixTerrainFrustum)
        {
            fixTerrainFrustum(d3, d4, d5);
            debugFixTerrainFrustum = false;
        }

        renderDispatcher.clearChunkUpdates();
        Set<RenderChunk> set = chunksToUpdate;
        chunksToUpdate = Sets.newLinkedHashSet();

        for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 : renderInfos)
        {
            RenderChunk renderchunk4 = renderglobal$containerlocalrenderinformation2.renderChunk;

            if (renderchunk4.isNeedsUpdate() || set.contains(renderchunk4))
            {
                displayListEntitiesDirty = true;

                if (isPositionInRenderChunk(blockpos, renderglobal$containerlocalrenderinformation2.renderChunk))
                {
                    mc.mcProfiler.startSection("build near");
                    renderDispatcher.updateChunkNow(renderchunk4);
                    renderchunk4.setNeedsUpdate(false);
                    mc.mcProfiler.endSection();
                }
                else
                {
                    chunksToUpdate.add(renderchunk4);
                }
            }
        }

        chunksToUpdate.addAll(set);
        mc.mcProfiler.endSection();
    }

    private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn)
    {
        BlockPos blockpos = renderChunkIn.getPosition();
        return Math.abs(pos.getX() - blockpos.getX()) <= 16 && (Math.abs(pos.getY() - blockpos.getY()) <= 16 && Math.abs(pos.getZ() - blockpos.getZ()) <= 16);
    }

    private Set<EnumFacing> getVisibleFacings(BlockPos pos)
    {
        VisGraph visgraph = new VisGraph();
        BlockPos blockpos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
        Chunk chunk = theWorld.getChunkFromBlockCoords(blockpos);

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos.add(15, 15, 15)))
        {
            if (chunk.getBlock(blockpos$mutableblockpos).isOpaqueCube())
            {
                visgraph.func_178606_a(blockpos$mutableblockpos);
            }
        }
        return visgraph.func_178609_b(pos);
    }

    private RenderChunk func_181562_a(BlockPos p_181562_1_, RenderChunk p_181562_2_, EnumFacing p_181562_3_)
    {
        BlockPos blockpos = p_181562_2_.func_181701_a(p_181562_3_);
        return Math.abs(p_181562_1_.getX() - blockpos.getX()) > renderDistanceChunks * 16 ? null : (blockpos.getY() >= 0 && blockpos.getY() < 256 ? (Math.abs(p_181562_1_.getZ() - blockpos.getZ()) > renderDistanceChunks * 16 ? null : viewFrustum.getRenderChunk(blockpos)) : null);
    }

    private void fixTerrainFrustum(double x, double y, double z)
    {
        debugFixedClippingHelper = new ClippingHelperImpl();
        ((ClippingHelperImpl) debugFixedClippingHelper).init();
        Matrix4f matrix4f = new Matrix4f(debugFixedClippingHelper.modelviewMatrix);
        matrix4f.transpose();
        Matrix4f matrix4f1 = new Matrix4f(debugFixedClippingHelper.projectionMatrix);
        matrix4f1.transpose();
        Matrix4f matrix4f2 = new Matrix4f();
        org.lwjgl.util.vector.Matrix4f.mul(matrix4f1, matrix4f, matrix4f2);
        matrix4f2.invert();
        debugTerrainFrustumPosition.field_181059_a = x;
        debugTerrainFrustumPosition.field_181060_b = y;
        debugTerrainFrustumPosition.field_181061_c = z;
        debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i)
        {
            org.lwjgl.util.vector.Matrix4f.transform(matrix4f2, debugTerrainMatrix[i], debugTerrainMatrix[i]);
            debugTerrainMatrix[i].x /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].y /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].z /= debugTerrainMatrix[i].w;
            debugTerrainMatrix[i].w = 1.0F;
        }
    }

    protected Vector3f getViewVector(Entity entityIn, double partialTicks)
    {
        float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.getRotationPitch() - entityIn.prevRotationPitch) * partialTicks);
        float f1 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.getRotationYaw() - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
        {
            f += 180.0F;
        }

        float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        return new Vector3f(f3 * f4, f5, f2 * f4);
    }

    public int renderBlockLayer(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn)
    {
        RenderHelper.disableStandardItemLighting();

        if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT)
        {
            mc.mcProfiler.startSection("translucent_sort");
            double d0 = entityIn.posX - prevRenderSortX;
            double d1 = entityIn.posY - prevRenderSortY;
            double d2 = entityIn.posZ - prevRenderSortZ;

            if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
            {
                prevRenderSortX = entityIn.posX;
                prevRenderSortY = entityIn.posY;
                prevRenderSortZ = entityIn.posZ;
                int k = 0;

                for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : renderInfos)
                {
                    if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(EnumWorldBlockLayer.TRANSLUCENT) && k++ < 15)
                    {
                        renderDispatcher.updateTransparencyLater(renderglobal$containerlocalrenderinformation.renderChunk);
                    }
                }
            }

            mc.mcProfiler.endSection();
        }

        mc.mcProfiler.startSection("filterempty");
        int l = 0;
        boolean flag = blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT;
        int i1 = flag ? renderInfos.size() - 1 : 0;
        int i = flag ? -1 : renderInfos.size();
        int j1 = flag ? -1 : 1;

        for (int j = i1; j != i; j += j1)
        {
            RenderChunk renderchunk = renderInfos.get(j).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn))
            {
                ++l;
                renderContainer.addRenderChunk(renderchunk, blockLayerIn);
            }
        }

        mc.mcProfiler.endStartSection("render_" + blockLayerIn);
        renderBlockLayer(blockLayerIn);
        mc.mcProfiler.endSection();
        return l;
    }

    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn)
    {
        mc.entityRenderer.enableLightmap();

        if (OpenGlHelper.useVbo())
        {
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }

        renderContainer.renderChunkLayer(blockLayerIn);

        if (OpenGlHelper.useVbo())
        {
            for (VertexFormatElement vertexformatelement : DefaultVertexFormats.BLOCK.getElements())
            {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (vertexformatelement$enumusage)
                {
                    case POSITION:
                        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        break;

                    case UV:
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                        break;

                    case COLOR:
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                }
            }
        }

        mc.entityRenderer.disableLightmap();
    }

    private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn)
    {
        while (iteratorIn.hasNext())
        {
            DestroyBlockProgress destroyblockprogress = iteratorIn.next();
            int i = destroyblockprogress.getCreationCloudUpdateTick();

            if (cloudTickCounter - i > 400)
            {
                iteratorIn.remove();
            }
        }
    }

    public void updateClouds()
    {
        ++cloudTickCounter;

        if (cloudTickCounter % 20 == 0)
        {
            cleanupDamagedBlocks(damagedBlocks.values().iterator());
        }
    }

    private void renderSkyEnd()
    {
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.depthMask(false);
        renderEngine.bindTexture(locationEndSkyPng);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        for (int i = 0; i < 6; ++i)
        {
            GlStateManager.pushMatrix();

            if (i == 1)
            {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 2)
            {
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 3)
            {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            }

            if (i == 4)
            {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            }

            if (i == 5)
            {
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(40, 40, 40, 255).endVertex();
            worldrenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(40, 40, 40, 255).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
    }

    public void renderSky(float partialTicks, int pass)
    {
        if (mc.theWorld.provider.getDimensionId() == 1)
        {
            renderSkyEnd();
        }
        else if (mc.theWorld.provider.isSurfaceWorld())
        {
            GlStateManager.disableTexture2D();
            Vec3 vec3 = theWorld.getSkyColor(mc.getRenderViewEntity(), partialTicks);
            float f = (float)vec3.xCoord;
            float f1 = (float)vec3.yCoord;
            float f2 = (float)vec3.zCoord;

            if (pass != 2)
            {
                float f3 = (f * 30.0F + f1 * 59.0F + f2 * 11.0F) / 100.0F;
                float f4 = (f * 30.0F + f1 * 70.0F) / 100.0F;
                float f5 = (f * 30.0F + f2 * 70.0F) / 100.0F;
                f = f3;
                f1 = f4;
                f2 = f5;
            }

            GlStateManager.color(f, f1, f2);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();
            GlStateManager.color(f, f1, f2);

            if (vboEnabled)
            {
                skyVBO.bindBuffer();
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                skyVBO.drawArrays(7);
                skyVBO.unbindBuffer();
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            }
            else
            {
                GlStateManager.callList(glSkyList);
            }

            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = theWorld.provider.calcSunriseSunsetColors(theWorld.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null)
            {
                GlStateManager.disableTexture2D();
                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float f6 = afloat[0];
                float f7 = afloat[1];
                float f8 = afloat[2];

                if (pass != 2)
                {
                    float f9 = (f6 * 30.0F + f7 * 59.0F + f8 * 11.0F) / 100.0F;
                    float f10 = (f6 * 30.0F + f7 * 70.0F) / 100.0F;
                    float f11 = (f6 * 30.0F + f8 * 70.0F) / 100.0F;
                    f6 = f9;
                    f7 = f10;
                    f8 = f11;
                }

                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 100.0D, 0.0D).color(f6, f7, f8, afloat[3]).endVertex();
                int j = 16;

                for (int l = 0; l <= 16; ++l)
                {
                    float f21 = (float)l * (float)Math.PI * 2.0F / 16.0F;
                    float f12 = MathHelper.sin(f21);
                    float f13 = MathHelper.cos(f21);
                    worldrenderer.pos(f12 * 120.0F, f13 * 120.0F, -f13 * 40.0F * afloat[3]).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float f16 = 1.0F - theWorld.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, f16);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
            float f17 = 30.0F;
            renderEngine.bindTexture(locationSunPng);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-f17, 100.0D, -f17).tex(0.0D, 0.0D).endVertex();
            worldrenderer.pos(f17, 100.0D, -f17).tex(1.0D, 0.0D).endVertex();
            worldrenderer.pos(f17, 100.0D, f17).tex(1.0D, 1.0D).endVertex();
            worldrenderer.pos(-f17, 100.0D, f17).tex(0.0D, 1.0D).endVertex();
            tessellator.draw();
            f17 = 20.0F;
            renderEngine.bindTexture(locationMoonPhasesPng);
            int i = theWorld.getMoonPhase();
            int k = i % 4;
            int i1 = i / 4 % 2;
            float f22 = (float)(k) / 4.0F;
            float f23 = (float)(i1) / 2.0F;
            float f24 = (float)(k + 1) / 4.0F;
            float f14 = (float)(i1 + 1) / 2.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-f17, -100.0D, f17).tex(f24, f14).endVertex();
            worldrenderer.pos(f17, -100.0D, f17).tex(f22, f14).endVertex();
            worldrenderer.pos(f17, -100.0D, -f17).tex(f22, f23).endVertex();
            worldrenderer.pos(-f17, -100.0D, -f17).tex(f24, f23).endVertex();
            tessellator.draw();
            GlStateManager.disableTexture2D();
            float f15 = theWorld.getStarBrightness(partialTicks) * f16;

            if (f15 > 0.0F)
            {
                GlStateManager.color(f15, f15, f15, f15);

                if (vboEnabled)
                {
                    starVBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    starVBO.drawArrays(7);
                    starVBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(starGLCallList);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();
            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double d0 = mc.thePlayer.getPositionEyes(partialTicks).yCoord - theWorld.getHorizon();

            if (d0 < 0.0D)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                if (vboEnabled)
                {
                    sky2VBO.bindBuffer();
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                    sky2VBO.drawArrays(7);
                    sky2VBO.unbindBuffer();
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                else
                {
                    GlStateManager.callList(glSkyList2);
                }

                GlStateManager.popMatrix();
                float f19 = -((float)(d0 + 65.0D));
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f19, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, f19, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (theWorld.provider.isSkyColored())
            {
                GlStateManager.color(f * 0.2F + 0.04F, f1 * 0.2F + 0.04F, f2 * 0.6F + 0.1F);
            }
            else
            {
                GlStateManager.color(f, f1, f2);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(d0 - 16.0D)), 0.0F);
            GlStateManager.callList(glSkyList2);
            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();
            GlStateManager.depthMask(true);
        }
    }

    public void renderClouds(float partialTicks, int pass)
    {
        if (mc.theWorld.provider.isSurfaceWorld())
        {
            if (mc.gameSettings.func_181147_e() == 2)
            {
                renderCloudsFancy(partialTicks, pass);
            }
            else
            {
                GlStateManager.disableCull();
                float f = (float)(mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                renderEngine.bindTexture(locationCloudsPng);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                Vec3 vec3 = theWorld.getCloudColour(partialTicks);
                float f1 = (float)vec3.xCoord;
                float f2 = (float)vec3.yCoord;
                float f3 = (float)vec3.zCoord;

                if (pass != 2)
                {
                    float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
                    float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
                    float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
                    f1 = f4;
                    f2 = f5;
                    f3 = f6;
                }

                double d2 = (float) cloudTickCounter + partialTicks;
                double d0 = mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d2 * 0.029999999329447746D;
                double d1 = mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double)partialTicks;
                int k = MathHelper.floor_double(d0 / 2048.0D);
                int l = MathHelper.floor_double(d1 / 2048.0D);
                d0 = d0 - (double)(k * 2048);
                d1 = d1 - (double)(l * 2048);
                float f7 = theWorld.provider.getCloudHeight() - f + 0.33F;
                float f8 = (float)(d0 * 4.8828125E-4D);
                float f9 = (float)(d1 * 4.8828125E-4D);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                for (int i1 = -256; i1 < 256; i1 += 32)
                {
                    for (int j1 = -256; j1 < 256; j1 += 32)
                    {
                        worldrenderer.pos(i1, f7, j1 + 32).tex((float)(i1) * 4.8828125E-4F + f8, (float)(j1 + 32) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
                        worldrenderer.pos(i1 + 32, f7, j1 + 32).tex((float)(i1 + 32) * 4.8828125E-4F + f8, (float)(j1 + 32) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
                        worldrenderer.pos(i1 + 32, f7, j1).tex((float)(i1 + 32) * 4.8828125E-4F + f8, (float)(j1) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
                        worldrenderer.pos(i1, f7, j1).tex((float)(i1) * 4.8828125E-4F + f8, (float)(j1) * 4.8828125E-4F + f9).color(f1, f2, f3, 0.8F).endVertex();
                    }
                }

                tessellator.draw();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.enableCull();
            }
        }
    }

    /**
     * Checks if the given position is to be rendered with cloud fog
     */
    public boolean hasCloudFog()
    {
        return false;
    }

    private void renderCloudsFancy(float partialTicks, int pass)
    {
        GlStateManager.disableCull();
        float f = (float)(mc.getRenderViewEntity().lastTickPosY + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        double d0 = (float) cloudTickCounter + partialTicks;
        double d1 = (mc.getRenderViewEntity().prevPosX + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().prevPosX) * (double)partialTicks + d0 * 0.029999999329447746D) / 12.0D;
        double d2 = (mc.getRenderViewEntity().prevPosZ + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().prevPosZ) * (double)partialTicks) / 12.0D + 0.33000001311302185D;
        float f3 = theWorld.provider.getCloudHeight() - f + 0.33F;
        int i = MathHelper.floor_double(d1 / 2048.0D);
        int j = MathHelper.floor_double(d2 / 2048.0D);
        d1 = d1 - (double)(i * 2048);
        d2 = d2 - (double)(j * 2048);
        renderEngine.bindTexture(locationCloudsPng);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Vec3 vec3 = theWorld.getCloudColour(partialTicks);
        float f4 = (float)vec3.xCoord;
        float f5 = (float)vec3.yCoord;
        float f6 = (float)vec3.zCoord;

        if (pass != 2)
        {
            float f7 = (f4 * 30.0F + f5 * 59.0F + f6 * 11.0F) / 100.0F;
            float f8 = (f4 * 30.0F + f5 * 70.0F) / 100.0F;
            float f9 = (f4 * 30.0F + f6 * 70.0F) / 100.0F;
            f4 = f7;
            f5 = f8;
            f6 = f9;
        }

        float f26 = f4 * 0.9F;
        float f27 = f5 * 0.9F;
        float f28 = f6 * 0.9F;
        float f10 = f4 * 0.7F;
        float f11 = f5 * 0.7F;
        float f12 = f6 * 0.7F;
        float f13 = f4 * 0.8F;
        float f14 = f5 * 0.8F;
        float f15 = f6 * 0.8F;
        float f17 = (float)MathHelper.floor_double(d1) * 0.00390625F;
        float f18 = (float)MathHelper.floor_double(d2) * 0.00390625F;
        float f19 = (float)(d1 - (double)MathHelper.floor_double(d1));
        float f20 = (float)(d2 - (double)MathHelper.floor_double(d2));
        GlStateManager.scale(12.0F, 1.0F, 12.0F);

        for (int i1 = 0; i1 < 2; ++i1)
        {
            if (i1 == 0)
            {
                GlStateManager.colorMask(false, false, false, false);
            }
            else
            {
                switch (pass)
                {
                    case 0:
                        GlStateManager.colorMask(false, true, true, true);
                        break;

                    case 1:
                        GlStateManager.colorMask(true, false, false, true);
                        break;

                    case 2:
                        GlStateManager.colorMask(true, true, true, true);
                }
            }

            for (int j1 = -3; j1 <= 4; ++j1)
            {
                for (int k1 = -3; k1 <= 4; ++k1)
                {
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                    float f22 = (float)(j1 * 8);
                    float f23 = (float)(k1 * 8);
                    float f24 = f22 - f19;
                    float f25 = f23 - f20;

                    if (f3 > -5.0F)
                    {
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f10, f11, f12, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (f3 <= 5.0F)
                    {
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 8.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 8.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldrenderer.pos(f24 + 0.0F, f3 + 4.0F - 9.765625E-4F, f25 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f4, f5, f6, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (j1 > -1)
                    {
                        for (int l1 = 0; l1 < 8; ++l1)
                        {
                            worldrenderer.pos(f24 + (float)l1 + 0.0F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float)l1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)l1 + 0.0F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float)l1 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)l1 + 0.0F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float)l1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)l1 + 0.0F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float)l1 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (j1 <= 1)
                    {
                        for (int i2 = 0; i2 < 8; ++i2)
                        {
                            worldrenderer.pos(f24 + (float)i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 8.0F).tex((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 8.0F).tex((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 8.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i2 + 1.0F - 9.765625E-4F, f3 + 4.0F, f25 + 0.0F).tex((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldrenderer.pos(f24 + (float)i2 + 1.0F - 9.765625E-4F, f3 + 0.0F, f25 + 0.0F).tex((f22 + (float)i2 + 0.5F) * 0.00390625F + f17, (f23 + 0.0F) * 0.00390625F + f18).color(f26, f27, f28, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (k1 > -1)
                    {
                        for (int j2 = 0; j2 < 8; ++j2)
                        {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float)j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float)j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float)j2 + 0.0F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float)j2 + 0.0F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)j2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (k1 <= 1)
                    {
                        for (int k2 = 0; k2 < 8; ++k2)
                        {
                            worldrenderer.pos(f24 + 0.0F, f3 + 4.0F, f25 + (float)k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 4.0F, f25 + (float)k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 8.0F, f3 + 0.0F, f25 + (float)k2 + 1.0F - 9.765625E-4F).tex((f22 + 8.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldrenderer.pos(f24 + 0.0F, f3 + 0.0F, f25 + (float)k2 + 1.0F - 9.765625E-4F).tex((f22 + 0.0F) * 0.00390625F + f17, (f23 + (float)k2 + 0.5F) * 0.00390625F + f18).color(f13, f14, f15, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    public void updateChunks(long finishTimeNano)
    {
        displayListEntitiesDirty |= renderDispatcher.runChunkUploads(finishTimeNano);

        if (!chunksToUpdate.isEmpty())
        {
            Iterator<RenderChunk> iterator = chunksToUpdate.iterator();

            while (iterator.hasNext())
            {
                RenderChunk renderchunk = iterator.next();

                if (!renderDispatcher.updateChunkLater(renderchunk))
                {
                    break;
                }

                renderchunk.setNeedsUpdate(false);
                iterator.remove();
                long i = finishTimeNano - System.nanoTime();

                if (i < 0L)
                {
                    break;
                }
            }
        }
    }

    public void renderWorldBorder(Entity p_180449_1_, float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        WorldBorder worldborder = theWorld.getWorldBorder();
        double d0 = mc.gameSettings.renderDistanceChunks * 16;

        if (p_180449_1_.posX >= worldborder.maxX() - d0 || p_180449_1_.posX <= worldborder.minX() + d0 || p_180449_1_.posZ >= worldborder.maxZ() - d0 || p_180449_1_.posZ <= worldborder.minZ() + d0)
        {
            double d1 = 1.0D - worldborder.getClosestDistance(p_180449_1_) / d0;
            d1 = Math.pow(d1, 4.0D);
            double d2 = p_180449_1_.lastTickPosX + (p_180449_1_.posX - p_180449_1_.lastTickPosX) * (double)partialTicks;
            double d3 = p_180449_1_.lastTickPosY + (p_180449_1_.posY - p_180449_1_.lastTickPosY) * (double)partialTicks;
            double d4 = p_180449_1_.lastTickPosZ + (p_180449_1_.posZ - p_180449_1_.lastTickPosZ) * (double)partialTicks;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            renderEngine.bindTexture(locationForcefieldPng);
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            int i = worldborder.getStatus().getID();
            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, (float)d1);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            float f3 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setTranslation(-d2, -d3, -d4);
            double d5 = Math.max(MathHelper.floor_double(d4 - d0), worldborder.minZ());
            double d6 = Math.min(MathHelper.ceiling_double_int(d4 + d0), worldborder.maxZ());

            if (d2 > worldborder.maxX() - d0)
            {
                float f7 = 0.0F;

                for (double d7 = d5; d7 < d6; f7 += 0.5F)
                {
                    double d8 = Math.min(1.0D, d6 - d7);
                    float f8 = (float)d8 * 0.5F;
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7).tex(f3 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 256.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7 + d8).tex(f3 + f8 + f7, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, d7).tex(f3 + f7, f3 + 128.0F).endVertex();
                    ++d7;
                }
            }

            if (d2 < worldborder.minX() + d0)
            {
                float f9 = 0.0F;

                for (double d9 = d5; d9 < d6; f9 += 0.5F)
                {
                    double d12 = Math.min(1.0D, d6 - d9);
                    float f12 = (float)d12 * 0.5F;
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9).tex(f3 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 256.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 0.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9 + d12).tex(f3 + f12 + f9, f3 + 128.0F).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, d9).tex(f3 + f9, f3 + 128.0F).endVertex();
                    ++d9;
                }
            }

            d5 = Math.max(MathHelper.floor_double(d2 - d0), worldborder.minX());
            d6 = Math.min(MathHelper.ceiling_double_int(d2 + d0), worldborder.maxX());

            if (d4 > worldborder.maxZ() - d0)
            {
                float f10 = 0.0F;

                for (double d10 = d5; d10 < d6; f10 += 0.5F)
                {
                    double d13 = Math.min(1.0D, d6 - d10);
                    float f13 = (float)d13 * 0.5F;
                    worldrenderer.pos(d10, 256.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 256.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d10 + d13, 0.0D, worldborder.maxZ()).tex(f3 + f13 + f10, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d10, 0.0D, worldborder.maxZ()).tex(f3 + f10, f3 + 128.0F).endVertex();
                    ++d10;
                }
            }

            if (d4 < worldborder.minZ() + d0)
            {
                float f11 = 0.0F;

                for (double d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    double d14 = Math.min(1.0D, d6 - d11);
                    float f14 = (float)d14 * 0.5F;
                    worldrenderer.pos(d11, 256.0D, worldborder.minZ()).tex(f3 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 256.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 0.0F).endVertex();
                    worldrenderer.pos(d11 + d14, 0.0D, worldborder.minZ()).tex(f3 + f14 + f11, f3 + 128.0F).endVertex();
                    worldrenderer.pos(d11, 0.0D, worldborder.minZ()).tex(f3 + f11, f3 + 128.0F).endVertex();
                    ++d11;
                }
            }

            tessellator.draw();
            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableAlpha();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
        }
    }

    private void preRenderDamagedBlocks()
    {
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    private void postRenderDamagedBlocks()
    {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks)
    {
        double d0 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double d1 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double d2 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

        if (!damagedBlocks.isEmpty())
        {
            renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-d0, -d1, -d2);
            worldRendererIn.markDirty();
            Iterator<DestroyBlockProgress> iterator = damagedBlocks.values().iterator();

            while (iterator.hasNext())
            {
                DestroyBlockProgress destroyblockprogress = iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double d3 = (double)blockpos.getX() - d0;
                double d4 = (double)blockpos.getY() - d1;
                double d5 = (double)blockpos.getZ() - d2;
                Block block = theWorld.getBlockState(blockpos).getBlock();

                if (!(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull))
                {
                    if (d3 * d3 + d4 * d4 + d5 * d5 > 1024.0D)
                    {
                        iterator.remove();
                    }
                    else
                    {
                        IBlockState iblockstate = theWorld.getBlockState(blockpos);

                        if (iblockstate.getBlock().getMaterial() != Material.air)
                        {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, theWorld);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            postRenderDamagedBlocks();
        }
    }

    /**
     * Draws the selection box for the player. Args: entityPlayer, rayTraceHit, i, itemStack, partialTickTime
     */
    public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int p_72731_3_, float partialTicks)
    {
        if (p_72731_3_ == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            BlockPos blockpos = movingObjectPositionIn.getBlockPos();
            Block block = theWorld.getBlockState(blockpos).getBlock();

            if (block.getMaterial() != Material.air && theWorld.getWorldBorder().contains(blockpos))
            {
                block.setBlockBoundsBasedOnState(theWorld, blockpos);
                double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                func_181561_a(block.getSelectedBoundingBox(theWorld, blockpos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2));
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    public static void func_181561_a(AxisAlignedBB boundingBox)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void func_181563_a(AxisAlignedBB p_181563_0_, int p_181563_1_, int p_181563_2_, int p_181563_3_, int p_181563_4_)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.minZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.maxX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.minY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        worldrenderer.pos(p_181563_0_.minX, p_181563_0_.maxY, p_181563_0_.maxZ).color(p_181563_1_, p_181563_2_, p_181563_3_, p_181563_4_).endVertex();
        tessellator.draw();
    }

    /**
     * Marks the blocks in the given range for update
     */
    private void markBlocksForUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        viewFrustum.markBlocksForUpdate(x1, y1, z1, x2, y2, z2);
    }

    public void markBlockForUpdate(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void notifyLightSet(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    /**
     * On the client, re-renders all blocks in this range, inclusive. On the server, does nothing. Args: min x, min y,
     * min z, max x, max y, max z
     */
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        markBlocksForUpdate(x1 - 1, y1 - 1, z1 - 1, x2 + 1, y2 + 1, z2 + 1);
    }

    public void playRecord(String recordName, BlockPos blockPosIn)
    {
        ISound isound = mapSoundPositions.get(blockPosIn);

        if (isound != null)
        {
            mc.getSoundHandler().stopSound(isound);
            mapSoundPositions.remove(blockPosIn);
        }

        if (recordName != null)
        {
            ItemRecord itemrecord = ItemRecord.getRecord(recordName);

            if (itemrecord != null)
            {
                mc.ingameGUI.setRecordPlayingMessage(itemrecord.getRecordNameLocal());
            }

            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(new ResourceLocation(recordName), (float)blockPosIn.getX(), (float)blockPosIn.getY(), (float)blockPosIn.getZ());
            mapSoundPositions.put(blockPosIn, positionedsoundrecord);
            mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    /**
     * Plays the specified sound. Arg: soundName, x, y, z, volume, pitch
     */
    public void playSound(String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    /**
     * Plays sound to all near players except the player reference given
     */
    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... p_180442_15_)
    {
        try
        {
            spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_180442_15_);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being added");
            crashreportcategory.addCrashSection("ID", particleID);

            if (p_180442_15_ != null)
            {
                crashreportcategory.addCrashSection("Parameters", p_180442_15_);
            }

            crashreportcategory.addCrashSectionCallable("Position", () -> CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord));
            throw new ReportedException(crashreport);
        }
    }

    private void spawnParticle(EnumParticleTypes particleIn, double p_174972_2_, double p_174972_4_, double p_174972_6_, double p_174972_8_, double p_174972_10_, double p_174972_12_, int... p_174972_14_)
    {
        spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), p_174972_2_, p_174972_4_, p_174972_6_, p_174972_8_, p_174972_10_, p_174972_12_, p_174972_14_);
    }

    private EntityFX spawnEntityFX(int p_174974_1_, boolean ignoreRange, double p_174974_3_, double p_174974_5_, double p_174974_7_, double p_174974_9_, double p_174974_11_, double p_174974_13_, int... p_174974_15_)
    {
        if (mc != null && mc.getRenderViewEntity() != null && mc.effectRenderer != null)
        {
            int i = mc.gameSettings.particleSetting;

            if (i == 1 && theWorld.rand.nextInt(3) == 0)
            {
                i = 2;
            }

            double d0 = mc.getRenderViewEntity().posX - p_174974_3_;
            double d1 = mc.getRenderViewEntity().posY - p_174974_5_;
            double d2 = mc.getRenderViewEntity().posZ - p_174974_7_;

            if (ignoreRange)
            {
                return mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_);
            }
            else
            {
                return d0 * d0 + d1 * d1 + d2 * d2 > 256.0D ? null : (i > 1 ? null : mc.effectRenderer.spawnEffectParticle(p_174974_1_, p_174974_3_, p_174974_5_, p_174974_7_, p_174974_9_, p_174974_11_, p_174974_13_, p_174974_15_));
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Called on all IWorldAccesses when an entity is created or loaded. On client worlds, starts downloading any
     * necessary textures. On server worlds, adds the entity to the entity tracker.
     */
    public void onEntityAdded(Entity entityIn)
    {
    }

    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed. On client worlds, releases any downloaded
     * textures. On server worlds, removes the entity from the entity tracker.
     */
    public void onEntityRemoved(Entity entityIn)
    {
    }

    public void broadcastSound(int p_180440_1_, BlockPos p_180440_2_, int p_180440_3_)
    {
        switch (p_180440_1_)
        {
            case 1013:
            case 1018:
                if (mc.getRenderViewEntity() != null)
                {
                    double d0 = (double)p_180440_2_.getX() - mc.getRenderViewEntity().posX;
                    double d1 = (double)p_180440_2_.getY() - mc.getRenderViewEntity().posY;
                    double d2 = (double)p_180440_2_.getZ() - mc.getRenderViewEntity().posZ;
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = mc.getRenderViewEntity().posX;
                    double d5 = mc.getRenderViewEntity().posY;
                    double d6 = mc.getRenderViewEntity().posZ;

                    if (d3 > 0.0D)
                    {
                        d4 += d0 / d3 * 2.0D;
                        d5 += d1 / d3 * 2.0D;
                        d6 += d2 / d3 * 2.0D;
                    }

                    if (p_180440_1_ == 1013)
                    {
                        theWorld.playSound(d4, d5, d6, "mob.wither.spawn", 1.0F, 1.0F, false);
                    }
                    else
                    {
                        theWorld.playSound(d4, d5, d6, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int p_180439_4_)
    {
        Random random = theWorld.rand;

        switch (sfxType)
        {
            case 1000:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
                break;

            case 1001:
                theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
                break;

            case 1002:
                theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
                break;

            case 1003:
                theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1004:
                theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;

            case 1005:
                if (Item.getItemById(p_180439_4_) instanceof ItemRecord)
                {
                    theWorld.playRecord(blockPosIn, "records." + ((ItemRecord)Item.getItemById(p_180439_4_)).recordName);
                }
                else
                {
                    theWorld.playRecord(blockPosIn, null);
                }

                break;

            case 1006:
                theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1007:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1008:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1009:
                theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1010:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1011:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1012:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1014:
                theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1015:
                theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1016:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1017:
                theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1020:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1021:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1022:
                theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2000:
                int l = p_180439_4_ % 3 - 1;
                int i = p_180439_4_ / 3 % 3 - 1;
                double d15 = (double)blockPosIn.getX() + (double)l * 0.6D + 0.5D;
                double d17 = (double)blockPosIn.getY() + 0.5D;
                double d19 = (double)blockPosIn.getZ() + (double)i * 0.6D + 0.5D;

                for (int k1 = 0; k1 < 10; ++k1)
                {
                    double d20 = random.nextDouble() * 0.2D + 0.01D;
                    double d21 = d15 + (double)l * 0.01D + (random.nextDouble() - 0.5D) * (double)i * 0.5D;
                    double d4 = d17 + (random.nextDouble() - 0.5D) * 0.5D;
                    double d6 = d19 + (double)i * 0.01D + (random.nextDouble() - 0.5D) * (double)l * 0.5D;
                    double d8 = (double)l * d20 + random.nextGaussian() * 0.01D;
                    double d10 = -0.03D + random.nextGaussian() * 0.01D;
                    double d12 = (double)i * d20 + random.nextGaussian() * 0.01D;
                    spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d21, d4, d6, d8, d10, d12);
                }

                return;

            case 2001:
                Block block = Block.getBlockById(p_180439_4_ & 4095);

                if (block.getMaterial() != Material.air)
                {
                    mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float)blockPosIn.getX() + 0.5F, (float)blockPosIn.getY() + 0.5F, (float)blockPosIn.getZ() + 0.5F));
                }

                mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(p_180439_4_ >> 12 & 255));
                break;

            case 2002:
                double d13 = blockPosIn.getX();
                double d14 = blockPosIn.getY();
                double d16 = blockPosIn.getZ();

                for (int i1 = 0; i1 < 8; ++i1)
                {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, d13, d14, d16, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.potionitem), p_180439_4_);
                }

                int j1 = Items.potionitem.getColorFromDamage(p_180439_4_);
                float f = (float)(j1 >> 16 & 255) / 255.0F;
                float f1 = (float)(j1 >> 8 & 255) / 255.0F;
                float f2 = (float)(j1 & 255) / 255.0F;
                EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;

                if (Items.potionitem.isEffectInstant(p_180439_4_))
                {
                    enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
                }

                for (int l1 = 0; l1 < 100; ++l1)
                {
                    double d22 = random.nextDouble() * 4.0D;
                    double d23 = random.nextDouble() * Math.PI * 2.0D;
                    double d24 = Math.cos(d23) * d22;
                    double d9 = 0.01D + random.nextDouble() * 0.5D;
                    double d11 = Math.sin(d23) * d22;
                    EntityFX entityfx = spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), d13 + d24 * 0.1D, d14 + 0.3D, d16 + d11 * 0.1D, d24, d9, d11);

                    if (entityfx != null)
                    {
                        float f3 = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * f3, f1 * f3, f2 * f3);
                        entityfx.multiplyVelocity((float)d22);
                    }
                }

                theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2003:
                double d0 = (double)blockPosIn.getX() + 0.5D;
                double d1 = blockPosIn.getY();
                double d2 = (double)blockPosIn.getZ() + 0.5D;

                for (int j = 0; j < 8; ++j)
                {
                    spawnParticle(EnumParticleTypes.ITEM_CRACK, d0, d1, d2, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, Item.getIdFromItem(Items.ender_eye));
                }

                for (double d18 = 0.0D; d18 < (Math.PI * 2D); d18 += 0.15707963267948966D)
                {
                    spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d18) * 5.0D, d1 - 0.4D, d2 + Math.sin(d18) * 5.0D, Math.cos(d18) * -5.0D, 0.0D, Math.sin(d18) * -5.0D);
                    spawnParticle(EnumParticleTypes.PORTAL, d0 + Math.cos(d18) * 5.0D, d1 - 0.4D, d2 + Math.sin(d18) * 5.0D, Math.cos(d18) * -7.0D, 0.0D, Math.sin(d18) * -7.0D);
                }

                return;

            case 2004:
                for (int k = 0; k < 20; ++k)
                {
                    double d3 = (double)blockPosIn.getX() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d5 = (double)blockPosIn.getY() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double d7 = (double)blockPosIn.getZ() + 0.5D + ((double) theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                    theWorld.spawnParticle(EnumParticleTypes.FLAME, d3, d5, d7, 0.0D, 0.0D, 0.0D);
                }

                return;

            case 2005:
                ItemDye.spawnBonemealParticles(theWorld, blockPosIn, p_180439_4_);
        }
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        if (progress >= 0 && progress < 10)
        {
            DestroyBlockProgress destroyblockprogress = damagedBlocks.get(breakerId);

            if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ())
            {
                destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
                damagedBlocks.put(breakerId, destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick(cloudTickCounter);
        }
        else
        {
            damagedBlocks.remove(breakerId);
        }
    }

    public void setDisplayListEntitiesDirty()
    {
        displayListEntitiesDirty = true;
    }

    public void func_181023_a(Collection<TileEntity> p_181023_1_, Collection<TileEntity> p_181023_2_)
    {
        synchronized (field_181024_n)
        {
            field_181024_n.removeAll(p_181023_1_);
            field_181024_n.addAll(p_181023_2_);
        }
    }

    static class ContainerLocalRenderInformation
    {
        final RenderChunk renderChunk;
        final EnumFacing facing;
        final Set<EnumFacing> setFacing;
        final int counter;

        private ContainerLocalRenderInformation(RenderChunk renderChunkIn, EnumFacing facingIn, int counterIn)
        {
            setFacing = EnumSet.noneOf(EnumFacing.class);
            renderChunk = renderChunkIn;
            facing = facingIn;
            counter = counterIn;
        }
    }
}
