package com.daytrip.sunrise.gui;

import com.daytrip.sunrise.gui.button.GuiIconButtonClose;
import com.daytrip.sunrise.hack.HackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiScreenHacks extends GuiScreen {
    private GuiScreenHacks.List list;

    public void initGui()
    {
        buttonList.add(new GuiIconButtonClose(500, width - 23, height - 27));
        buttonList.add(new GuiButton(501, width / 2 - 30, height - 27, 60, 20, "Edit"));

        list = new GuiScreenHacks.List(mc);
        list.registerScrollButtons(7, 8);
    }

    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 500) {
                mc.displayGuiScreen(null);

                if (mc.currentScreen == null) {
                    mc.setIngameFocus();
                }
            } else if (button.id == 501) {
                mc.displayGuiScreen(new GuiScreenHack(this, list.selectedSlot));
            }
            list.actionPerformed(button);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        list.drawScreen(mouseX, mouseY);
        drawCenteredString(fontRendererObj, I18n.format("client.screen.hacks.title"), width / 2, 16, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot {
        private int selectedSlot;

        public List(Minecraft mcIn) {
            super(mcIn, GuiScreenHacks.this.width, GuiScreenHacks.this.height, 32, GuiScreenHacks.this.height - 65 + 4, 18);
        }

        protected int getSize() {
            return HackManager.count();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedSlot = slotIndex;
        }

        protected boolean isSelected(int slotIndex)
        {
            return selectedSlot == slotIndex;
        }

        protected int getContentHeight()
        {
            return getSize() * 18;
        }

        protected void drawBackground()
        {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn)
        {
            drawCenteredString(fontRendererObj, HackManager.getHack(entryID).getName(), width / 2, p_180791_3_ + 1, 16777215);
        }
    }
}
