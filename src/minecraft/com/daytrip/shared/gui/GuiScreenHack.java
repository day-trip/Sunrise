package com.daytrip.shared.gui;

import com.daytrip.shared.gui.button.impl.GuiIconButtonClose;
import com.daytrip.sunrise.hack.HackManager;
import com.daytrip.sunrise.hack.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;

import java.io.IOException;

public class GuiScreenHack extends GuiScreen {
    private GuiScreenHack.List list;

    private final GuiScreen parentScreen;
    private final int index;

    public GuiScreenHack(GuiScreen parentScreen, int index) {
        this.parentScreen = parentScreen;
        this.index = index;
    }

    public void initGui()
    {
        buttonList.add(new GuiIconButtonClose(500, width - 23, height - 27));
        buttonList.add(new GuiButton(501, width / 2 - 30 + 40, height - 27, 60, 20, "Edit"));
        buttonList.add(new GuiButton(502, width / 2 - 30 - 40, height - 27, 60, 20, HackManager.getHack(index).isEnabled() ? "Enabled" : "Disabled"));

        list = new GuiScreenHack.List(mc);
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
                mc.displayGuiScreen(parentScreen);
            } else if (button.id == 501) {
                mc.displayGuiScreen(new GuiScreenHackSetting(this, list.selectedSlot, index));
            } else if (button.id == 502) {
                HackManager.getHack(index).toggle();
                button.displayString = HackManager.getHack(index).isEnabled() ? "Enabled" : "Disabled";
            }
            list.actionPerformed(button);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        list.drawScreen(mouseX, mouseY);
        drawCenteredString(fontRendererObj, HackManager.getHack(index).getName(), width / 2, 16, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public int getIndex() {
        return index;
    }

    class List extends GuiSlot {
        private int selectedSlot;
        private final int index;

        public List(Minecraft mcIn) {
            super(mcIn, GuiScreenHack.this.width, GuiScreenHack.this.height, 32, GuiScreenHack.this.height - 65 + 4, 18);
            index = GuiScreenHack.this.index;
        }

        protected int getSize() {
            return HackManager.getHack(index).getSettingManager().count();
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
            Setting setting = HackManager.getHack(index).getSettingManager().getSetting(entryID);
            drawCenteredString(fontRendererObj, setting.getName(), width / 2, p_180791_3_ + 1, 16777215);
        }
    }
}
