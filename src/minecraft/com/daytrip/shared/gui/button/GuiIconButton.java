package com.daytrip.shared.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class GuiIconButton extends GuiButton {
    protected final int texX;
    protected final int texY;

    public GuiIconButton(int buttonID, int x, int y, int width, int height, int texX, int texY)
    {
        super(buttonID, x, y, width, height, "");
        this.texX = texX;
        this.texY = texY;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible)
        {
            mc.getTextureManager().bindTexture(GuiButton.buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            boolean flag = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

            drawTexturedModalRect(xPosition, yPosition, texX, texY + (flag ? height : 0), width, height);
        }
    }

    public int getTexX() {
        return texX;
    }

    public int getTexY() {
        return texY;
    }
}
