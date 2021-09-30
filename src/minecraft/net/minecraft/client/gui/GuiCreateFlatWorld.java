package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;

public class GuiCreateFlatWorld extends GuiScreen
{
    private final GuiCreateWorld createWorldGui;
    private FlatGeneratorInfo theFlatGeneratorInfo = FlatGeneratorInfo.getDefaultFlatGenerator();

    /** The title given to the flat world currently in creation */
    private String flatWorldTitle;
    private String field_146394_i;
    private String field_146391_r;
    private GuiCreateFlatWorld.Details createFlatWorldListSlotGui;
    private GuiButton field_146389_t;
    private GuiButton field_146388_u;
    private GuiButton field_146386_v;

    public GuiCreateFlatWorld(GuiCreateWorld createWorldGuiIn, String p_i1029_2_)
    {
        createWorldGui = createWorldGuiIn;
        func_146383_a(p_i1029_2_);
    }

    public String func_146384_e()
    {
        return theFlatGeneratorInfo.toString();
    }

    public void func_146383_a(String p_146383_1_)
    {
        theFlatGeneratorInfo = FlatGeneratorInfo.createFlatGeneratorFromString(p_146383_1_);
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();
        flatWorldTitle = I18n.format("createWorld.customize.flat.title");
        field_146394_i = I18n.format("createWorld.customize.flat.tile");
        field_146391_r = I18n.format("createWorld.customize.flat.height");
        createFlatWorldListSlotGui = new GuiCreateFlatWorld.Details();
        buttonList.add(field_146389_t = new GuiButton(2, width / 2 - 154, height - 52, 100, 20, I18n.format("createWorld.customize.flat.addLayer") + " (NYI)"));
        buttonList.add(field_146388_u = new GuiButton(3, width / 2 - 50, height - 52, 100, 20, I18n.format("createWorld.customize.flat.editLayer") + " (NYI)"));
        buttonList.add(field_146386_v = new GuiButton(4, width / 2 - 155, height - 52, 150, 20, I18n.format("createWorld.customize.flat.removeLayer")));
        buttonList.add(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(5, width / 2 + 5, height - 52, 150, 20, I18n.format("createWorld.customize.presets")));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        field_146389_t.visible = field_146388_u.visible = false;
        theFlatGeneratorInfo.func_82645_d();
        func_146375_g();
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        createFlatWorldListSlotGui.handleMouseInput();
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        int i = theFlatGeneratorInfo.getFlatLayers().size() - createFlatWorldListSlotGui.field_148228_k - 1;

        if (button.id == 1)
        {
            mc.displayGuiScreen(createWorldGui);
        }
        else if (button.id == 0)
        {
            createWorldGui.chunkProviderSettingsJson = func_146384_e();
            mc.displayGuiScreen(createWorldGui);
        }
        else if (button.id == 5)
        {
            mc.displayGuiScreen(new GuiFlatPresets(this));
        }
        else if (button.id == 4 && func_146382_i())
        {
            theFlatGeneratorInfo.getFlatLayers().remove(i);
            createFlatWorldListSlotGui.field_148228_k = Math.min(createFlatWorldListSlotGui.field_148228_k, theFlatGeneratorInfo.getFlatLayers().size() - 1);
        }

        theFlatGeneratorInfo.func_82645_d();
        func_146375_g();
    }

    public void func_146375_g()
    {
        boolean flag = func_146382_i();
        field_146386_v.enabled = flag;
        field_146388_u.enabled = flag;
        field_146388_u.enabled = false;
        field_146389_t.enabled = false;
    }

    private boolean func_146382_i()
    {
        return createFlatWorldListSlotGui.field_148228_k > -1 && createFlatWorldListSlotGui.field_148228_k < theFlatGeneratorInfo.getFlatLayers().size();
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        createFlatWorldListSlotGui.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, flatWorldTitle, width / 2, 8, 16777215);
        int i = width / 2 - 92 - 16;
        drawString(fontRendererObj, field_146394_i, i, 32, 16777215);
        drawString(fontRendererObj, field_146391_r, i + 2 + 213 - fontRendererObj.getStringWidth(field_146391_r), 32, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class Details extends GuiSlot
    {
        public int field_148228_k = -1;

        public Details()
        {
            super(GuiCreateFlatWorld.this.mc, GuiCreateFlatWorld.this.width, GuiCreateFlatWorld.this.height, 43, GuiCreateFlatWorld.this.height - 60, 24);
        }

        private void func_148225_a(int p_148225_1_, int p_148225_2_, ItemStack p_148225_3_)
        {
            func_148226_e(p_148225_1_ + 1, p_148225_2_ + 1);
            GlStateManager.enableRescaleNormal();

            if (p_148225_3_ != null && p_148225_3_.getItem() != null)
            {
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemIntoGUI(p_148225_3_, p_148225_1_ + 2, p_148225_2_ + 2);
                RenderHelper.disableStandardItemLighting();
            }

            GlStateManager.disableRescaleNormal();
        }

        private void func_148226_e(int p_148226_1_, int p_148226_2_)
        {
            func_148224_c(p_148226_1_, p_148226_2_, 0, 0);
        }

        private void func_148224_c(int p_148224_1_, int p_148224_2_, int p_148224_3_, int p_148224_4_)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(Gui.statIcons);
            float f = 0.0078125F;
            float f1 = 0.0078125F;
            int i = 18;
            int j = 18;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(p_148224_1_ + 0, p_148224_2_ + 18, zLevel).tex((float)(p_148224_3_ + 0) * 0.0078125F, (float)(p_148224_4_ + 18) * 0.0078125F).endVertex();
            worldrenderer.pos(p_148224_1_ + 18, p_148224_2_ + 18, zLevel).tex((float)(p_148224_3_ + 18) * 0.0078125F, (float)(p_148224_4_ + 18) * 0.0078125F).endVertex();
            worldrenderer.pos(p_148224_1_ + 18, p_148224_2_ + 0, zLevel).tex((float)(p_148224_3_ + 18) * 0.0078125F, (float)(p_148224_4_ + 0) * 0.0078125F).endVertex();
            worldrenderer.pos(p_148224_1_ + 0, p_148224_2_ + 0, zLevel).tex((float)(p_148224_3_ + 0) * 0.0078125F, (float)(p_148224_4_ + 0) * 0.0078125F).endVertex();
            tessellator.draw();
        }

        protected int getSize()
        {
            return theFlatGeneratorInfo.getFlatLayers().size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY)
        {
            field_148228_k = slotIndex;
            func_146375_g();
        }

        protected boolean isSelected(int slotIndex)
        {
            return slotIndex == field_148228_k;
        }

        protected void drawBackground()
        {
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            FlatLayerInfo flatlayerinfo = theFlatGeneratorInfo.getFlatLayers().get(theFlatGeneratorInfo.getFlatLayers().size() - entryID - 1);
            IBlockState iblockstate = flatlayerinfo.func_175900_c();
            Block block = iblockstate.getBlock();
            Item item = Item.getItemFromBlock(block);
            ItemStack itemstack = block != Blocks.air && item != null ? new ItemStack(item, 1, block.getMetaFromState(iblockstate)) : null;
            String s = itemstack == null ? "Air" : item.getItemStackDisplayName(itemstack);

            if (item == null)
            {
                if (block != Blocks.water && block != Blocks.flowing_water)
                {
                    if (block == Blocks.lava || block == Blocks.flowing_lava)
                    {
                        item = Items.lava_bucket;
                    }
                }
                else
                {
                    item = Items.water_bucket;
                }

                if (item != null)
                {
                    itemstack = new ItemStack(item, 1, block.getMetaFromState(iblockstate));
                    s = block.getLocalizedName();
                }
            }

            func_148225_a(p_180791_2_, p_180791_3_, itemstack);
            fontRendererObj.drawString(s, p_180791_2_ + 18 + 5, p_180791_3_ + 3, 16777215);
            String s1;

            if (entryID == 0)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.top", Integer.valueOf(flatlayerinfo.getLayerCount()));
            }
            else if (entryID == theFlatGeneratorInfo.getFlatLayers().size() - 1)
            {
                s1 = I18n.format("createWorld.customize.flat.layer.bottom", Integer.valueOf(flatlayerinfo.getLayerCount()));
            }
            else
            {
                s1 = I18n.format("createWorld.customize.flat.layer", Integer.valueOf(flatlayerinfo.getLayerCount()));
            }

            fontRendererObj.drawString(s1, p_180791_2_ + 2 + 213 - fontRendererObj.getStringWidth(s1), p_180791_3_ + 3, 16777215);
        }

        protected int getScrollBarX()
        {
            return width - 70;
        }
    }
}
