package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Locale;

public class GuiButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

    /** Button width in pixels */
    protected int width;

    /** Button height in pixels */
    protected int height;

    /** The x position of this control. */
    public int xPosition;

    /** The y position of this control. */
    public int yPosition;

    /** The string displayed on this control. */
    public String displayString;
    public int id;

    /** True if this control is enabled, false to disable. */
    public boolean enabled;

    public boolean forceEnabled;

    /** Hides the button completely if false. */
    public boolean visible;
    protected boolean hovered;

    public float alpha = 1.0F;
    public float textAlpha = 1.0F;

    public boolean drawText = true;
    public boolean fontSmooth = false;

    public GuiButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 200, 20, buttonText);
    }

    public GuiButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        width = 200;
        height = 20;
        enabled = true;
        forceEnabled = true;
        visible = true;
        id = buttonId;
        xPosition = x;
        yPosition = y;
        width = widthIn;
        height = heightIn;
        displayString = buttonText;
    }

    public GuiButton setSmoothFont() {
        fontSmooth = true;
        return this;
    }

    public GuiButton setCentered() {
        xPosition -= width / 2;
        return this;
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        i = forceEnabled ? i : 1;

        return i;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible)
        {
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            int i = getHoverState(hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            drawTexturedModalRect(xPosition, yPosition, 0, 46 + i * 20, width / 2, height);
            drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + i * 20, width / 2, height);
            mouseDragged(mc, mouseX, mouseY);

            if(drawText) {
                int j = new Color(32 / 255f, 32 / 255f, 128 / 255f, textAlpha).getRGB();

                if (!enabled)
                {
                    j = new Color(160 / 255f, 160 / 255f, 160 / 255f, textAlpha).getRGB();
                }
                else if (hovered)
                {
                    j = new Color(255 / 255f, 140 / 255f, 0 / 255f, textAlpha).getRGB();
                }

                if(!forceEnabled) {
                    j = new Color(160 / 255f, 160 / 255f, 160 / 255f, textAlpha).getRGB();
                }

                drawCenteredStringWithoutShadow(fontSmooth ? mc.smoothFontRendererObj : mc.fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, j);
            }
        }
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return enabled && forceEnabled && visible && mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
    }

    /**
     * Whether the mouse cursor is currently over the button.
     */
    public boolean isMouseOver()
    {
        return hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
