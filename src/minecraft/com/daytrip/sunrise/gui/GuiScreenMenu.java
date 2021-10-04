package com.daytrip.sunrise.gui;

import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.HackManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class GuiScreenMenu extends GuiScreen {
    @Override
    public void initGui() {
        buttonList.add(new GuiButton(0, width / 2 - 75, 30, 60, 20, "Graphics").setCentered());
        buttonList.add(new GuiButton(1, width / 2 + 75, 30, 60, 20, "Hacks").setCentered());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int bottom = mc.fontRendererObj.FONT_HEIGHT + 50;
        drawRect(0, 0, width, bottom, new Color(64, 64, 64, 200).getRGB());
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredStringWithoutShadow(mc.smoothFontRendererObj, "Sunrise Client Menu", width / 2, 15, Color.cyan.getRGB());

        int hackDisplayStart = bottom + 10;
        for(Hack hack : HackManager.getHacks().values()) {

        }
    }
}
