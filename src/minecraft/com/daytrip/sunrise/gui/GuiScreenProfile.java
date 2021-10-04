package com.daytrip.sunrise.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class GuiScreenProfile extends GuiScreen {
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        Gui.drawRect(width - 30 - mc.fontRendererObj.getStringWidth(mc.thePlayer.getDisplayName().getFormattedText()), 2, width - 1, 29, Color.DARK_GRAY.getRGB());

        Minecraft.getMinecraft().getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawScaledCustomSizeModalRect(width - 28, 3, 8.0F, 8.0F, 8, 8, 25, 25, 64.0F, 64.0F);
        Gui.drawScaledCustomSizeModalRect(width - 28, 3, 40.0F, 8.0F, 8, 8, 25, 25, 64.0F, 64.0F);

        mc.fontRendererObj.drawString(mc.thePlayer.getDisplayName().getFormattedText(), width - 28 - mc.fontRendererObj.getStringWidth(mc.thePlayer.getDisplayName().getFormattedText()), 3 + (25 / 2) - mc.fontRendererObj.FONT_HEIGHT / 2, Color.cyan.getRGB());
    }
}
