package net.minecraft.client.renderer.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.profiler.crash.CrashReport;
import net.minecraft.profiler.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3i;

public class RenderItem implements IResourceManagerReloadListener
{
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    /** Defines the zLevel of rendering of item on GUI. */
    public float zLevel;
    private final ItemModelMesher itemModelMesher;
    private final TextureManager textureManager;

    public RenderItem(TextureManager textureManager, ModelManager modelManager)
    {
        this.textureManager = textureManager;
        itemModelMesher = new ItemModelMesher(modelManager);
        registerItems();
    }

    public ItemModelMesher getItemModelMesher()
    {
        return itemModelMesher;
    }

    protected void registerItem(Item itm, int subType, String identifier)
    {
        itemModelMesher.register(itm, subType, new ModelResourceLocation(identifier, "inventory"));
    }

    protected void registerBlock(Block blk, int subType, String identifier)
    {
        registerItem(Item.getItemFromBlock(blk), subType, identifier);
    }

    private void registerBlock(Block blk, String identifier)
    {
        registerBlock(blk, 0, identifier);
    }

    private void registerItem(Item itm, String identifier)
    {
        registerItem(itm, 0, identifier);
    }

    private void renderModel(IBakedModel model, ItemStack stack)
    {
        renderModel(model, -1, stack);
    }

    private void renderModel(IBakedModel model)
    {
        renderModel(model, -8372020, null);
    }

    private void renderModel(IBakedModel model, int color, ItemStack stack)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color, stack);
        }

        renderQuads(worldrenderer, model.getGeneralQuads(), color, stack);
        tessellator.draw();
    }

    public void renderItem(ItemStack stack, IBakedModel model)
    {
        if (stack != null)
        {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);

            if (model.isBuiltInRenderer())
            {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            }
            else
            {
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                renderModel(model, stack);

                if (stack.hasEffect())
                {
                    renderEffect(model);
                }
            }

            GlStateManager.popMatrix();
        }
    }

    private void renderEffect(IBakedModel model)
    {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(768, 1);
        textureManager.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        renderModel(model);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        renderModel(model);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
    }

    private void putQuadNormal(WorldRenderer renderer, BakedQuad quad)
    {
        Vec3i vec3i = quad.getFace().getDirectionVec();
        renderer.putNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    }

    private void renderQuad(WorldRenderer renderer, BakedQuad quad, int color)
    {
        renderer.addVertexData(quad.getVertexData());
        renderer.putColor4(color);
        putQuadNormal(renderer, quad);
    }

    private void renderQuads(WorldRenderer renderer, List<BakedQuad> quads, int color, ItemStack stack)
    {
        boolean flag = color == -1 && stack != null;
        int i = 0;

        for (int j = quads.size(); i < j; ++i)
        {
            BakedQuad bakedquad = quads.get(i);
            int k = color;

            if (flag && bakedquad.hasTintIndex())
            {
                k = stack.getItem().getColorFromItemStack(stack, bakedquad.getTintIndex());

                if (EntityRenderer.anaglyphEnable)
                {
                    k = TextureUtil.anaglyphColor(k);
                }

                k = k | -16777216;
            }

            renderQuad(renderer, bakedquad, k);
        }
    }

    public boolean shouldRenderItemIn3D(ItemStack stack)
    {
        IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
        return ibakedmodel != null && ibakedmodel.isGui3d();
    }

    private void preTransform(ItemStack stack)
    {
        IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
        Item item = stack.getItem();

        if (item != null)
        {
            boolean flag = ibakedmodel.isGui3d();

            if (!flag)
            {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public void func_181564_a(ItemStack p_181564_1_, ItemCameraTransforms.TransformType p_181564_2_)
    {
        if (p_181564_1_ != null)
        {
            IBakedModel ibakedmodel = itemModelMesher.getItemModel(p_181564_1_);
            renderItemModelTransform(p_181564_1_, ibakedmodel, p_181564_2_);
        }
    }

    public void renderItemModelForEntity(ItemStack stack, EntityLivingBase entityToRenderFor, ItemCameraTransforms.TransformType cameraTransformType)
    {
        if (stack != null && entityToRenderFor != null)
        {
            IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);

            if (entityToRenderFor instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityToRenderFor;
                Item item = stack.getItem();
                ModelResourceLocation modelresourcelocation = null;

                if (item == Items.fishing_rod && entityplayer.fishEntity != null)
                {
                    modelresourcelocation = new ModelResourceLocation("fishing_rod_cast", "inventory");
                }
                else if (item == Items.bow && entityplayer.getItemInUse() != null)
                {
                    int i = stack.getMaxItemUseDuration() - entityplayer.getItemInUseCount();

                    if (i >= 18)
                    {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_2", "inventory");
                    }
                    else if (i > 13)
                    {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_1", "inventory");
                    }
                    else if (i > 0)
                    {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_0", "inventory");
                    }
                }

                if (modelresourcelocation != null)
                {
                    ibakedmodel = itemModelMesher.getModelManager().getModel(modelresourcelocation);
                }
            }

            renderItemModelTransform(stack, ibakedmodel, cameraTransformType);
        }
    }

    protected void renderItemModelTransform(ItemStack stack, IBakedModel model, ItemCameraTransforms.TransformType cameraTransformType)
    {
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        preTransform(stack);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        ItemCameraTransforms itemcameratransforms = model.getItemCameraTransforms();
        itemcameratransforms.applyTransform(cameraTransformType);

        if (func_183005_a(itemcameratransforms.getTransform(cameraTransformType)))
        {
            GlStateManager.cullFace(1028);
        }

        renderItem(stack, model);
        GlStateManager.cullFace(1029);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    private boolean func_183005_a(ItemTransformVec3f p_183005_1_)
    {
        return p_183005_1_.scale.x < 0.0F ^ p_183005_1_.scale.y < 0.0F ^ p_183005_1_.scale.z < 0.0F;
    }

    public void renderItemIntoGUI(ItemStack stack, int x, int y)
    {
        IBakedModel ibakedmodel = itemModelMesher.getItemModel(stack);
        GlStateManager.pushMatrix();
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        setupGuiTransform(x, y, ibakedmodel.isGui3d());
        ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        renderItem(stack, ibakedmodel);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    private void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d)
    {
        GlStateManager.translate((float)xPosition, (float)yPosition, 100.0F + zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        if (isGui3d)
        {
            GlStateManager.scale(40.0F, 40.0F, 40.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.scale(64.0F, 64.0F, 64.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableLighting();
        }
    }

    public void renderItemAndEffectIntoGUI(ItemStack stack, int xPosition, int yPosition)
    {
        if (stack != null && stack.getItem() != null)
        {
            zLevel += 50.0F;

            try
            {
                renderItemIntoGUI(stack, xPosition, yPosition);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                crashreportcategory.addCrashSectionCallable("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.addCrashSectionCallable("Item Aux", () -> String.valueOf(stack.getMetadata()));
                crashreportcategory.addCrashSectionCallable("Item NBT", () -> String.valueOf(stack.getTagCompound()));
                crashreportcategory.addCrashSectionCallable("Item Foil", () -> String.valueOf(stack.hasEffect()));
                throw new ReportedException(crashreport);
            }

            zLevel -= 50.0F;
        }
    }

    public void renderItemOverlays(FontRenderer fr, ItemStack stack, int xPosition, int yPosition)
    {
        renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, null);
    }

    /**
     * Renders the stack size and/or damage bar for the given ItemStack.
     */
    public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text)
    {
        if (stack != null)
        {
            if (stack.stackSize != 1 || text != null)
            {
                String s = text == null ? String.valueOf(stack.stackSize) : text;

                if (text == null && stack.stackSize < 1)
                {
                    s = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
                }

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }

            if (stack.isItemDamaged())
            {
                int j = (int)Math.round(13.0D - (double)stack.getItemDamage() * 13.0D / (double)stack.getMaxDamage());
                int i = (int)Math.round(255.0D - (double)stack.getItemDamage() * 255.0D / (double)stack.getMaxDamage());
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0);
                func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64);
                func_181565_a(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i);
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    private void func_181565_a(WorldRenderer p_181565_1_, int p_181565_2_, int p_181565_3_, int p_181565_4_, int p_181565_5_, int p_181565_6_, int p_181565_7_)
    {
        p_181565_1_.begin(7, DefaultVertexFormats.POSITION_COLOR);
        p_181565_1_.pos(p_181565_2_, p_181565_3_, 0.0D).color(p_181565_6_, p_181565_7_, 0, 255).endVertex();
        p_181565_1_.pos(p_181565_2_, p_181565_3_ + p_181565_5_, 0.0D).color(p_181565_6_, p_181565_7_, 0, 255).endVertex();
        p_181565_1_.pos(p_181565_2_ + p_181565_4_, p_181565_3_ + p_181565_5_, 0.0D).color(p_181565_6_, p_181565_7_, 0, 255).endVertex();
        p_181565_1_.pos(p_181565_2_ + p_181565_4_, p_181565_3_, 0.0D).color(p_181565_6_, p_181565_7_, 0, 255).endVertex();
        Tessellator.getInstance().draw();
    }

    private void registerItems()
    {
        registerBlock(Blocks.anvil, "anvil_intact");
        registerBlock(Blocks.anvil, 1, "anvil_slightly_damaged");
        registerBlock(Blocks.anvil, 2, "anvil_very_damaged");
        registerBlock(Blocks.carpet, EnumDyeColor.BLACK.getMetadata(), "black_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.BLUE.getMetadata(), "blue_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.BROWN.getMetadata(), "brown_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.CYAN.getMetadata(), "cyan_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.GRAY.getMetadata(), "gray_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.GREEN.getMetadata(), "green_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.LIME.getMetadata(), "lime_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.MAGENTA.getMetadata(), "magenta_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.ORANGE.getMetadata(), "orange_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.PINK.getMetadata(), "pink_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.PURPLE.getMetadata(), "purple_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.RED.getMetadata(), "red_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.SILVER.getMetadata(), "silver_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.WHITE.getMetadata(), "white_carpet");
        registerBlock(Blocks.carpet, EnumDyeColor.YELLOW.getMetadata(), "yellow_carpet");
        registerBlock(Blocks.cobblestone_wall, BlockWall.EnumType.MOSSY.getMetadata(), "mossy_cobblestone_wall");
        registerBlock(Blocks.cobblestone_wall, BlockWall.EnumType.NORMAL.getMetadata(), "cobblestone_wall");
        registerBlock(Blocks.dirt, BlockDirt.DirtType.COARSE_DIRT.getMetadata(), "coarse_dirt");
        registerBlock(Blocks.dirt, BlockDirt.DirtType.DIRT.getMetadata(), "dirt");
        registerBlock(Blocks.dirt, BlockDirt.DirtType.PODZOL.getMetadata(), "podzol");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.FERN.getMeta(), "double_fern");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.GRASS.getMeta(), "double_grass");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta(), "paeonia");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.ROSE.getMeta(), "double_rose");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta(), "sunflower");
        registerBlock(Blocks.double_plant, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta(), "syringa");
        registerBlock(Blocks.leaves, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_leaves");
        registerBlock(Blocks.leaves, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_leaves");
        registerBlock(Blocks.leaves, BlockPlanks.EnumType.OAK.getMetadata(), "oak_leaves");
        registerBlock(Blocks.leaves, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_leaves");
        registerBlock(Blocks.leaves2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_leaves");
        registerBlock(Blocks.leaves2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_leaves");
        registerBlock(Blocks.log, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_log");
        registerBlock(Blocks.log, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_log");
        registerBlock(Blocks.log, BlockPlanks.EnumType.OAK.getMetadata(), "oak_log");
        registerBlock(Blocks.log, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_log");
        registerBlock(Blocks.log2, BlockPlanks.EnumType.ACACIA.getMetadata() - 4, "acacia_log");
        registerBlock(Blocks.log2, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4, "dark_oak_log");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.CHISELED_STONEBRICK.getMetadata(), "chiseled_brick_monster_egg");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.COBBLESTONE.getMetadata(), "cobblestone_monster_egg");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.CRACKED_STONEBRICK.getMetadata(), "cracked_brick_monster_egg");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.MOSSY_STONEBRICK.getMetadata(), "mossy_brick_monster_egg");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.STONE.getMetadata(), "stone_monster_egg");
        registerBlock(Blocks.monster_egg, BlockSilverfish.EnumType.STONEBRICK.getMetadata(), "stone_brick_monster_egg");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_planks");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_planks");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_planks");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_planks");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.OAK.getMetadata(), "oak_planks");
        registerBlock(Blocks.planks, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_planks");
        registerBlock(Blocks.prismarine, BlockPrismarine.EnumType.BRICKS.getMetadata(), "prismarine_bricks");
        registerBlock(Blocks.prismarine, BlockPrismarine.EnumType.DARK.getMetadata(), "dark_prismarine");
        registerBlock(Blocks.prismarine, BlockPrismarine.EnumType.ROUGH.getMetadata(), "prismarine");
        registerBlock(Blocks.quartz_block, BlockQuartz.EnumType.CHISELED.getMetadata(), "chiseled_quartz_block");
        registerBlock(Blocks.quartz_block, BlockQuartz.EnumType.DEFAULT.getMetadata(), "quartz_block");
        registerBlock(Blocks.quartz_block, BlockQuartz.EnumType.LINES_Y.getMetadata(), "quartz_column");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.ALLIUM.getMeta(), "allium");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta(), "blue_orchid");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta(), "houstonia");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta(), "orange_tulip");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta(), "oxeye_daisy");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta(), "pink_tulip");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.POPPY.getMeta(), "poppy");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.RED_TULIP.getMeta(), "red_tulip");
        registerBlock(Blocks.red_flower, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta(), "white_tulip");
        registerBlock(Blocks.sand, BlockSand.EnumType.RED_SAND.getMetadata(), "red_sand");
        registerBlock(Blocks.sand, BlockSand.EnumType.SAND.getMetadata(), "sand");
        registerBlock(Blocks.sandstone, BlockSandStone.EnumType.CHISELED.getMetadata(), "chiseled_sandstone");
        registerBlock(Blocks.sandstone, BlockSandStone.EnumType.DEFAULT.getMetadata(), "sandstone");
        registerBlock(Blocks.sandstone, BlockSandStone.EnumType.SMOOTH.getMetadata(), "smooth_sandstone");
        registerBlock(Blocks.red_sandstone, BlockRedSandstone.EnumType.CHISELED.getMetadata(), "chiseled_red_sandstone");
        registerBlock(Blocks.red_sandstone, BlockRedSandstone.EnumType.DEFAULT.getMetadata(), "red_sandstone");
        registerBlock(Blocks.red_sandstone, BlockRedSandstone.EnumType.SMOOTH.getMetadata(), "smooth_red_sandstone");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_sapling");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_sapling");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_sapling");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_sapling");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.OAK.getMetadata(), "oak_sapling");
        registerBlock(Blocks.sapling, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_sapling");
        registerBlock(Blocks.sponge, 0, "sponge");
        registerBlock(Blocks.sponge, 1, "sponge_wet");
        registerBlock(Blocks.stained_glass, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.RED.getMetadata(), "red_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass");
        registerBlock(Blocks.stained_glass, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.BLACK.getMetadata(), "black_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.BLUE.getMetadata(), "blue_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.BROWN.getMetadata(), "brown_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.GRAY.getMetadata(), "gray_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.GREEN.getMetadata(), "green_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.LIME.getMetadata(), "lime_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.PINK.getMetadata(), "pink_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.RED.getMetadata(), "red_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.SILVER.getMetadata(), "silver_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.WHITE.getMetadata(), "white_stained_glass_pane");
        registerBlock(Blocks.stained_glass_pane, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_glass_pane");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.BLACK.getMetadata(), "black_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.BLUE.getMetadata(), "blue_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.BROWN.getMetadata(), "brown_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.CYAN.getMetadata(), "cyan_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.GRAY.getMetadata(), "gray_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.GREEN.getMetadata(), "green_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.LIME.getMetadata(), "lime_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.MAGENTA.getMetadata(), "magenta_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.ORANGE.getMetadata(), "orange_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.PINK.getMetadata(), "pink_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.PURPLE.getMetadata(), "purple_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.RED.getMetadata(), "red_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.SILVER.getMetadata(), "silver_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.WHITE.getMetadata(), "white_stained_hardened_clay");
        registerBlock(Blocks.stained_hardened_clay, EnumDyeColor.YELLOW.getMetadata(), "yellow_stained_hardened_clay");
        registerBlock(Blocks.stone, BlockStone.EnumType.ANDESITE.getMetadata(), "andesite");
        registerBlock(Blocks.stone, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata(), "andesite_smooth");
        registerBlock(Blocks.stone, BlockStone.EnumType.DIORITE.getMetadata(), "diorite");
        registerBlock(Blocks.stone, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata(), "diorite_smooth");
        registerBlock(Blocks.stone, BlockStone.EnumType.GRANITE.getMetadata(), "granite");
        registerBlock(Blocks.stone, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata(), "granite_smooth");
        registerBlock(Blocks.stone, BlockStone.EnumType.STONE.getMetadata(), "stone");
        registerBlock(Blocks.stonebrick, BlockStoneBrick.EnumType.CRACKED.getMetadata(), "cracked_stonebrick");
        registerBlock(Blocks.stonebrick, BlockStoneBrick.EnumType.DEFAULT.getMetadata(), "stonebrick");
        registerBlock(Blocks.stonebrick, BlockStoneBrick.EnumType.CHISELED.getMetadata(), "chiseled_stonebrick");
        registerBlock(Blocks.stonebrick, BlockStoneBrick.EnumType.MOSSY.getMetadata(), "mossy_stonebrick");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.BRICK.getMetadata(), "brick_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.COBBLESTONE.getMetadata(), "cobblestone_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.WOOD.getMetadata(), "old_wood_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.NETHERBRICK.getMetadata(), "nether_brick_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.QUARTZ.getMetadata(), "quartz_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.SAND.getMetadata(), "sandstone_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata(), "stone_brick_slab");
        registerBlock(Blocks.stone_slab, BlockStoneSlab.EnumType.STONE.getMetadata(), "stone_slab");
        registerBlock(Blocks.stone_slab2, BlockStoneSlabNew.EnumType.RED_SANDSTONE.getMetadata(), "red_sandstone_slab");
        registerBlock(Blocks.tallgrass, BlockTallGrass.EnumType.DEAD_BUSH.getMeta(), "dead_bush");
        registerBlock(Blocks.tallgrass, BlockTallGrass.EnumType.FERN.getMeta(), "fern");
        registerBlock(Blocks.tallgrass, BlockTallGrass.EnumType.GRASS.getMeta(), "tall_grass");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia_slab");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.BIRCH.getMetadata(), "birch_slab");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak_slab");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle_slab");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.OAK.getMetadata(), "oak_slab");
        registerBlock(Blocks.wooden_slab, BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce_slab");
        registerBlock(Blocks.wool, EnumDyeColor.BLACK.getMetadata(), "black_wool");
        registerBlock(Blocks.wool, EnumDyeColor.BLUE.getMetadata(), "blue_wool");
        registerBlock(Blocks.wool, EnumDyeColor.BROWN.getMetadata(), "brown_wool");
        registerBlock(Blocks.wool, EnumDyeColor.CYAN.getMetadata(), "cyan_wool");
        registerBlock(Blocks.wool, EnumDyeColor.GRAY.getMetadata(), "gray_wool");
        registerBlock(Blocks.wool, EnumDyeColor.GREEN.getMetadata(), "green_wool");
        registerBlock(Blocks.wool, EnumDyeColor.LIGHT_BLUE.getMetadata(), "light_blue_wool");
        registerBlock(Blocks.wool, EnumDyeColor.LIME.getMetadata(), "lime_wool");
        registerBlock(Blocks.wool, EnumDyeColor.MAGENTA.getMetadata(), "magenta_wool");
        registerBlock(Blocks.wool, EnumDyeColor.ORANGE.getMetadata(), "orange_wool");
        registerBlock(Blocks.wool, EnumDyeColor.PINK.getMetadata(), "pink_wool");
        registerBlock(Blocks.wool, EnumDyeColor.PURPLE.getMetadata(), "purple_wool");
        registerBlock(Blocks.wool, EnumDyeColor.RED.getMetadata(), "red_wool");
        registerBlock(Blocks.wool, EnumDyeColor.SILVER.getMetadata(), "silver_wool");
        registerBlock(Blocks.wool, EnumDyeColor.WHITE.getMetadata(), "white_wool");
        registerBlock(Blocks.wool, EnumDyeColor.YELLOW.getMetadata(), "yellow_wool");
        registerBlock(Blocks.acacia_stairs, "acacia_stairs");
        registerBlock(Blocks.activator_rail, "activator_rail");
        registerBlock(Blocks.beacon, "beacon");
        registerBlock(Blocks.bedrock, "bedrock");
        registerBlock(Blocks.birch_stairs, "birch_stairs");
        registerBlock(Blocks.bookshelf, "bookshelf");
        registerBlock(Blocks.brick_block, "brick_block");
        registerBlock(Blocks.brick_block, "brick_block");
        registerBlock(Blocks.brick_stairs, "brick_stairs");
        registerBlock(Blocks.brown_mushroom, "brown_mushroom");
        registerBlock(Blocks.cactus, "cactus");
        registerBlock(Blocks.clay, "clay");
        registerBlock(Blocks.coal_block, "coal_block");
        registerBlock(Blocks.coal_ore, "coal_ore");
        registerBlock(Blocks.cobblestone, "cobblestone");
        registerBlock(Blocks.crafting_table, "crafting_table");
        registerBlock(Blocks.dark_oak_stairs, "dark_oak_stairs");
        registerBlock(Blocks.daylight_detector, "daylight_detector");
        registerBlock(Blocks.deadbush, "dead_bush");
        registerBlock(Blocks.detector_rail, "detector_rail");
        registerBlock(Blocks.diamond_block, "diamond_block");
        registerBlock(Blocks.diamond_ore, "diamond_ore");
        registerBlock(Blocks.dispenser, "dispenser");
        registerBlock(Blocks.dropper, "dropper");
        registerBlock(Blocks.emerald_block, "emerald_block");
        registerBlock(Blocks.emerald_ore, "emerald_ore");
        registerBlock(Blocks.enchanting_table, "enchanting_table");
        registerBlock(Blocks.end_portal_frame, "end_portal_frame");
        registerBlock(Blocks.end_stone, "end_stone");
        registerBlock(Blocks.oak_fence, "oak_fence");
        registerBlock(Blocks.spruce_fence, "spruce_fence");
        registerBlock(Blocks.birch_fence, "birch_fence");
        registerBlock(Blocks.jungle_fence, "jungle_fence");
        registerBlock(Blocks.dark_oak_fence, "dark_oak_fence");
        registerBlock(Blocks.acacia_fence, "acacia_fence");
        registerBlock(Blocks.oak_fence_gate, "oak_fence_gate");
        registerBlock(Blocks.spruce_fence_gate, "spruce_fence_gate");
        registerBlock(Blocks.birch_fence_gate, "birch_fence_gate");
        registerBlock(Blocks.jungle_fence_gate, "jungle_fence_gate");
        registerBlock(Blocks.dark_oak_fence_gate, "dark_oak_fence_gate");
        registerBlock(Blocks.acacia_fence_gate, "acacia_fence_gate");
        registerBlock(Blocks.furnace, "furnace");
        registerBlock(Blocks.glass, "glass");
        registerBlock(Blocks.glass_pane, "glass_pane");
        registerBlock(Blocks.glowstone, "glowstone");
        registerBlock(Blocks.golden_rail, "golden_rail");
        registerBlock(Blocks.gold_block, "gold_block");
        registerBlock(Blocks.gold_ore, "gold_ore");
        registerBlock(Blocks.grass, "grass");
        registerBlock(Blocks.gravel, "gravel");
        registerBlock(Blocks.hardened_clay, "hardened_clay");
        registerBlock(Blocks.hay_block, "hay_block");
        registerBlock(Blocks.heavy_weighted_pressure_plate, "heavy_weighted_pressure_plate");
        registerBlock(Blocks.hopper, "hopper");
        registerBlock(Blocks.ice, "ice");
        registerBlock(Blocks.iron_bars, "iron_bars");
        registerBlock(Blocks.iron_block, "iron_block");
        registerBlock(Blocks.iron_ore, "iron_ore");
        registerBlock(Blocks.iron_trapdoor, "iron_trapdoor");
        registerBlock(Blocks.jukebox, "jukebox");
        registerBlock(Blocks.jungle_stairs, "jungle_stairs");
        registerBlock(Blocks.ladder, "ladder");
        registerBlock(Blocks.lapis_block, "lapis_block");
        registerBlock(Blocks.lapis_ore, "lapis_ore");
        registerBlock(Blocks.lever, "lever");
        registerBlock(Blocks.light_weighted_pressure_plate, "light_weighted_pressure_plate");
        registerBlock(Blocks.lit_pumpkin, "lit_pumpkin");
        registerBlock(Blocks.melon_block, "melon_block");
        registerBlock(Blocks.mossy_cobblestone, "mossy_cobblestone");
        registerBlock(Blocks.mycelium, "mycelium");
        registerBlock(Blocks.netherrack, "netherrack");
        registerBlock(Blocks.nether_brick, "nether_brick");
        registerBlock(Blocks.nether_brick_fence, "nether_brick_fence");
        registerBlock(Blocks.nether_brick_stairs, "nether_brick_stairs");
        registerBlock(Blocks.noteblock, "noteblock");
        registerBlock(Blocks.oak_stairs, "oak_stairs");
        registerBlock(Blocks.obsidian, "obsidian");
        registerBlock(Blocks.packed_ice, "packed_ice");
        registerBlock(Blocks.piston, "piston");
        registerBlock(Blocks.pumpkin, "pumpkin");
        registerBlock(Blocks.quartz_ore, "quartz_ore");
        registerBlock(Blocks.quartz_stairs, "quartz_stairs");
        registerBlock(Blocks.rail, "rail");
        registerBlock(Blocks.redstone_block, "redstone_block");
        registerBlock(Blocks.redstone_lamp, "redstone_lamp");
        registerBlock(Blocks.redstone_ore, "redstone_ore");
        registerBlock(Blocks.redstone_torch, "redstone_torch");
        registerBlock(Blocks.red_mushroom, "red_mushroom");
        registerBlock(Blocks.sandstone_stairs, "sandstone_stairs");
        registerBlock(Blocks.red_sandstone_stairs, "red_sandstone_stairs");
        registerBlock(Blocks.sea_lantern, "sea_lantern");
        registerBlock(Blocks.slime_block, "slime");
        registerBlock(Blocks.snow, "snow");
        registerBlock(Blocks.snow_layer, "snow_layer");
        registerBlock(Blocks.soul_sand, "soul_sand");
        registerBlock(Blocks.spruce_stairs, "spruce_stairs");
        registerBlock(Blocks.sticky_piston, "sticky_piston");
        registerBlock(Blocks.stone_brick_stairs, "stone_brick_stairs");
        registerBlock(Blocks.stone_button, "stone_button");
        registerBlock(Blocks.stone_pressure_plate, "stone_pressure_plate");
        registerBlock(Blocks.stone_stairs, "stone_stairs");
        registerBlock(Blocks.tnt, "tnt");
        registerBlock(Blocks.torch, "torch");
        registerBlock(Blocks.trapdoor, "trapdoor");
        registerBlock(Blocks.tripwire_hook, "tripwire_hook");
        registerBlock(Blocks.vine, "vine");
        registerBlock(Blocks.waterlily, "waterlily");
        registerBlock(Blocks.web, "web");
        registerBlock(Blocks.wooden_button, "wooden_button");
        registerBlock(Blocks.wooden_pressure_plate, "wooden_pressure_plate");
        registerBlock(Blocks.yellow_flower, BlockFlower.EnumFlowerType.DANDELION.getMeta(), "dandelion");
        registerBlock(Blocks.chest, "chest");
        registerBlock(Blocks.trapped_chest, "trapped_chest");
        registerBlock(Blocks.ender_chest, "ender_chest");
        registerItem(Items.iron_shovel, "iron_shovel");
        registerItem(Items.iron_pickaxe, "iron_pickaxe");
        registerItem(Items.iron_axe, "iron_axe");
        registerItem(Items.flint_and_steel, "flint_and_steel");
        registerItem(Items.apple, "apple");
        registerItem(Items.bow, 0, "bow");
        registerItem(Items.bow, 1, "bow_pulling_0");
        registerItem(Items.bow, 2, "bow_pulling_1");
        registerItem(Items.bow, 3, "bow_pulling_2");
        registerItem(Items.arrow, "arrow");
        registerItem(Items.coal, 0, "coal");
        registerItem(Items.coal, 1, "charcoal");
        registerItem(Items.diamond, "diamond");
        registerItem(Items.iron_ingot, "iron_ingot");
        registerItem(Items.gold_ingot, "gold_ingot");
        registerItem(Items.iron_sword, "iron_sword");
        registerItem(Items.wooden_sword, "wooden_sword");
        registerItem(Items.wooden_shovel, "wooden_shovel");
        registerItem(Items.wooden_pickaxe, "wooden_pickaxe");
        registerItem(Items.wooden_axe, "wooden_axe");
        registerItem(Items.stone_sword, "stone_sword");
        registerItem(Items.stone_shovel, "stone_shovel");
        registerItem(Items.stone_pickaxe, "stone_pickaxe");
        registerItem(Items.stone_axe, "stone_axe");
        registerItem(Items.diamond_sword, "diamond_sword");
        registerItem(Items.diamond_shovel, "diamond_shovel");
        registerItem(Items.diamond_pickaxe, "diamond_pickaxe");
        registerItem(Items.diamond_axe, "diamond_axe");
        registerItem(Items.stick, "stick");
        registerItem(Items.bowl, "bowl");
        registerItem(Items.mushroom_stew, "mushroom_stew");
        registerItem(Items.golden_sword, "golden_sword");
        registerItem(Items.golden_shovel, "golden_shovel");
        registerItem(Items.golden_pickaxe, "golden_pickaxe");
        registerItem(Items.golden_axe, "golden_axe");
        registerItem(Items.string, "string");
        registerItem(Items.feather, "feather");
        registerItem(Items.gunpowder, "gunpowder");
        registerItem(Items.wooden_hoe, "wooden_hoe");
        registerItem(Items.stone_hoe, "stone_hoe");
        registerItem(Items.iron_hoe, "iron_hoe");
        registerItem(Items.diamond_hoe, "diamond_hoe");
        registerItem(Items.golden_hoe, "golden_hoe");
        registerItem(Items.wheat_seeds, "wheat_seeds");
        registerItem(Items.wheat, "wheat");
        registerItem(Items.bread, "bread");
        registerItem(Items.leather_helmet, "leather_helmet");
        registerItem(Items.leather_chestplate, "leather_chestplate");
        registerItem(Items.leather_leggings, "leather_leggings");
        registerItem(Items.leather_boots, "leather_boots");
        registerItem(Items.chainmail_helmet, "chainmail_helmet");
        registerItem(Items.chainmail_chestplate, "chainmail_chestplate");
        registerItem(Items.chainmail_leggings, "chainmail_leggings");
        registerItem(Items.chainmail_boots, "chainmail_boots");
        registerItem(Items.iron_helmet, "iron_helmet");
        registerItem(Items.iron_chestplate, "iron_chestplate");
        registerItem(Items.iron_leggings, "iron_leggings");
        registerItem(Items.iron_boots, "iron_boots");
        registerItem(Items.diamond_helmet, "diamond_helmet");
        registerItem(Items.diamond_chestplate, "diamond_chestplate");
        registerItem(Items.diamond_leggings, "diamond_leggings");
        registerItem(Items.diamond_boots, "diamond_boots");
        registerItem(Items.golden_helmet, "golden_helmet");
        registerItem(Items.golden_chestplate, "golden_chestplate");
        registerItem(Items.golden_leggings, "golden_leggings");
        registerItem(Items.golden_boots, "golden_boots");
        registerItem(Items.flint, "flint");
        registerItem(Items.porkchop, "porkchop");
        registerItem(Items.cooked_porkchop, "cooked_porkchop");
        registerItem(Items.painting, "painting");
        registerItem(Items.golden_apple, "golden_apple");
        registerItem(Items.golden_apple, 1, "golden_apple");
        registerItem(Items.sign, "sign");
        registerItem(Items.oak_door, "oak_door");
        registerItem(Items.spruce_door, "spruce_door");
        registerItem(Items.birch_door, "birch_door");
        registerItem(Items.jungle_door, "jungle_door");
        registerItem(Items.acacia_door, "acacia_door");
        registerItem(Items.dark_oak_door, "dark_oak_door");
        registerItem(Items.bucket, "bucket");
        registerItem(Items.water_bucket, "water_bucket");
        registerItem(Items.lava_bucket, "lava_bucket");
        registerItem(Items.minecart, "minecart");
        registerItem(Items.saddle, "saddle");
        registerItem(Items.iron_door, "iron_door");
        registerItem(Items.redstone, "redstone");
        registerItem(Items.snowball, "snowball");
        registerItem(Items.boat, "boat");
        registerItem(Items.leather, "leather");
        registerItem(Items.milk_bucket, "milk_bucket");
        registerItem(Items.brick, "brick");
        registerItem(Items.clay_ball, "clay_ball");
        registerItem(Items.reeds, "reeds");
        registerItem(Items.paper, "paper");
        registerItem(Items.book, "book");
        registerItem(Items.slime_ball, "slime_ball");
        registerItem(Items.chest_minecart, "chest_minecart");
        registerItem(Items.furnace_minecart, "furnace_minecart");
        registerItem(Items.egg, "egg");
        registerItem(Items.compass, "compass");
        registerItem(Items.fishing_rod, "fishing_rod");
        registerItem(Items.fishing_rod, 1, "fishing_rod_cast");
        registerItem(Items.clock, "clock");
        registerItem(Items.glowstone_dust, "glowstone_dust");
        registerItem(Items.fish, ItemFishFood.FishType.COD.getMetadata(), "cod");
        registerItem(Items.fish, ItemFishFood.FishType.SALMON.getMetadata(), "salmon");
        registerItem(Items.fish, ItemFishFood.FishType.CLOWNFISH.getMetadata(), "clownfish");
        registerItem(Items.fish, ItemFishFood.FishType.PUFFERFISH.getMetadata(), "pufferfish");
        registerItem(Items.cooked_fish, ItemFishFood.FishType.COD.getMetadata(), "cooked_cod");
        registerItem(Items.cooked_fish, ItemFishFood.FishType.SALMON.getMetadata(), "cooked_salmon");
        registerItem(Items.dye, EnumDyeColor.BLACK.getDyeDamage(), "dye_black");
        registerItem(Items.dye, EnumDyeColor.RED.getDyeDamage(), "dye_red");
        registerItem(Items.dye, EnumDyeColor.GREEN.getDyeDamage(), "dye_green");
        registerItem(Items.dye, EnumDyeColor.BROWN.getDyeDamage(), "dye_brown");
        registerItem(Items.dye, EnumDyeColor.BLUE.getDyeDamage(), "dye_blue");
        registerItem(Items.dye, EnumDyeColor.PURPLE.getDyeDamage(), "dye_purple");
        registerItem(Items.dye, EnumDyeColor.CYAN.getDyeDamage(), "dye_cyan");
        registerItem(Items.dye, EnumDyeColor.SILVER.getDyeDamage(), "dye_silver");
        registerItem(Items.dye, EnumDyeColor.GRAY.getDyeDamage(), "dye_gray");
        registerItem(Items.dye, EnumDyeColor.PINK.getDyeDamage(), "dye_pink");
        registerItem(Items.dye, EnumDyeColor.LIME.getDyeDamage(), "dye_lime");
        registerItem(Items.dye, EnumDyeColor.YELLOW.getDyeDamage(), "dye_yellow");
        registerItem(Items.dye, EnumDyeColor.LIGHT_BLUE.getDyeDamage(), "dye_light_blue");
        registerItem(Items.dye, EnumDyeColor.MAGENTA.getDyeDamage(), "dye_magenta");
        registerItem(Items.dye, EnumDyeColor.ORANGE.getDyeDamage(), "dye_orange");
        registerItem(Items.dye, EnumDyeColor.WHITE.getDyeDamage(), "dye_white");
        registerItem(Items.bone, "bone");
        registerItem(Items.sugar, "sugar");
        registerItem(Items.cake, "cake");
        registerItem(Items.bed, "bed");
        registerItem(Items.repeater, "repeater");
        registerItem(Items.cookie, "cookie");
        registerItem(Items.shears, "shears");
        registerItem(Items.melon, "melon");
        registerItem(Items.pumpkin_seeds, "pumpkin_seeds");
        registerItem(Items.melon_seeds, "melon_seeds");
        registerItem(Items.beef, "beef");
        registerItem(Items.cooked_beef, "cooked_beef");
        registerItem(Items.chicken, "chicken");
        registerItem(Items.cooked_chicken, "cooked_chicken");
        registerItem(Items.rabbit, "rabbit");
        registerItem(Items.cooked_rabbit, "cooked_rabbit");
        registerItem(Items.mutton, "mutton");
        registerItem(Items.cooked_mutton, "cooked_mutton");
        registerItem(Items.rabbit_foot, "rabbit_foot");
        registerItem(Items.rabbit_hide, "rabbit_hide");
        registerItem(Items.rabbit_stew, "rabbit_stew");
        registerItem(Items.rotten_flesh, "rotten_flesh");
        registerItem(Items.ender_pearl, "ender_pearl");
        registerItem(Items.blaze_rod, "blaze_rod");
        registerItem(Items.ghast_tear, "ghast_tear");
        registerItem(Items.gold_nugget, "gold_nugget");
        registerItem(Items.nether_wart, "nether_wart");
        itemModelMesher.register(Items.potionitem, stack -> ItemPotion.isSplash(stack.getMetadata()) ? new ModelResourceLocation("bottle_splash", "inventory") : new ModelResourceLocation("bottle_drinkable", "inventory"));
        registerItem(Items.glass_bottle, "glass_bottle");
        registerItem(Items.spider_eye, "spider_eye");
        registerItem(Items.fermented_spider_eye, "fermented_spider_eye");
        registerItem(Items.blaze_powder, "blaze_powder");
        registerItem(Items.magma_cream, "magma_cream");
        registerItem(Items.brewing_stand, "brewing_stand");
        registerItem(Items.cauldron, "cauldron");
        registerItem(Items.ender_eye, "ender_eye");
        registerItem(Items.speckled_melon, "speckled_melon");
        itemModelMesher.register(Items.spawn_egg, stack -> new ModelResourceLocation("spawn_egg", "inventory"));
        registerItem(Items.experience_bottle, "experience_bottle");
        registerItem(Items.fire_charge, "fire_charge");
        registerItem(Items.writable_book, "writable_book");
        registerItem(Items.emerald, "emerald");
        registerItem(Items.item_frame, "item_frame");
        registerItem(Items.flower_pot, "flower_pot");
        registerItem(Items.carrot, "carrot");
        registerItem(Items.potato, "potato");
        registerItem(Items.baked_potato, "baked_potato");
        registerItem(Items.poisonous_potato, "poisonous_potato");
        registerItem(Items.map, "map");
        registerItem(Items.golden_carrot, "golden_carrot");
        registerItem(Items.skull, 0, "skull_skeleton");
        registerItem(Items.skull, 1, "skull_wither");
        registerItem(Items.skull, 2, "skull_zombie");
        registerItem(Items.skull, 3, "skull_char");
        registerItem(Items.skull, 4, "skull_creeper");
        registerItem(Items.carrot_on_a_stick, "carrot_on_a_stick");
        registerItem(Items.nether_star, "nether_star");
        registerItem(Items.pumpkin_pie, "pumpkin_pie");
        registerItem(Items.firework_charge, "firework_charge");
        registerItem(Items.comparator, "comparator");
        registerItem(Items.netherbrick, "netherbrick");
        registerItem(Items.quartz, "quartz");
        registerItem(Items.tnt_minecart, "tnt_minecart");
        registerItem(Items.hopper_minecart, "hopper_minecart");
        registerItem(Items.armor_stand, "armor_stand");
        registerItem(Items.iron_horse_armor, "iron_horse_armor");
        registerItem(Items.golden_horse_armor, "golden_horse_armor");
        registerItem(Items.diamond_horse_armor, "diamond_horse_armor");
        registerItem(Items.lead, "lead");
        registerItem(Items.name_tag, "name_tag");
        itemModelMesher.register(Items.banner, stack -> new ModelResourceLocation("banner", "inventory"));
        registerItem(Items.record_13, "record_13");
        registerItem(Items.record_cat, "record_cat");
        registerItem(Items.record_blocks, "record_blocks");
        registerItem(Items.record_chirp, "record_chirp");
        registerItem(Items.record_far, "record_far");
        registerItem(Items.record_mall, "record_mall");
        registerItem(Items.record_mellohi, "record_mellohi");
        registerItem(Items.record_stal, "record_stal");
        registerItem(Items.record_strad, "record_strad");
        registerItem(Items.record_ward, "record_ward");
        registerItem(Items.record_11, "record_11");
        registerItem(Items.record_wait, "record_wait");
        registerItem(Items.prismarine_shard, "prismarine_shard");
        registerItem(Items.prismarine_crystals, "prismarine_crystals");
        itemModelMesher.register(Items.enchanted_book, stack -> new ModelResourceLocation("enchanted_book", "inventory"));
        itemModelMesher.register(Items.filled_map, stack -> new ModelResourceLocation("filled_map", "inventory"));
        registerBlock(Blocks.command_block, "command_block");
        registerItem(Items.fireworks, "fireworks");
        registerItem(Items.command_block_minecart, "command_block_minecart");
        registerBlock(Blocks.barrier, "barrier");
        registerBlock(Blocks.mob_spawner, "mob_spawner");
        registerItem(Items.written_book, "written_book");
        registerBlock(Blocks.brown_mushroom_block, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "brown_mushroom_block");
        registerBlock(Blocks.red_mushroom_block, BlockHugeMushroom.EnumType.ALL_INSIDE.getMetadata(), "red_mushroom_block");
        registerBlock(Blocks.dragon_egg, "dragon_egg");
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        itemModelMesher.rebuildCache();
    }
}
