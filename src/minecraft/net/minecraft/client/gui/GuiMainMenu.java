package net.minecraft.client.gui;

import com.daytrip.shared.gui.button.impl.GuiIconButtonClose;
import com.daytrip.shared.gui.button.impl.GuiIconButtonSettings;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiMainMenu extends GuiScreen {

    private int startTicks = 30;

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        if(startTicks > 0) {
            startTicks--;
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        buttonList.add(new GuiButtonLanguage(5, 3, height - 20 - 7));
        buttonList.add(new GuiIconButtonSettings(0, 30, height - 20 - 7));
        buttonList.add(new GuiIconButtonClose(4, width - 20 - 3, height - 20 - 7));
        buttonList.add(new GuiButton(1, width / 2 - 50 - 40, height - 20 - 7, 80, 20, I18n.format("menu.singleplayer")));
        buttonList.add(new GuiButton(2, width / 2 + 50 - 40, height - 20 - 7, 80, 20, I18n.format("menu.multiplayer")));

        mc.func_181537_a(false);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }

        if (button.id == 5) {
            mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager()));
        }

        if (button.id == 1) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 4) {
            mc.shutdown();
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/title/background.png"));
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

        String copyright = "(C) Mojang AB & DayTrip. Do not distribute!";

        float colorAlpha = clamp(30 - startTicks, 0, 30) / 30f;

        drawString(fontRendererObj, I18n.format("client.version"), width - 3 - fontRendererObj.getStringWidth(I18n.format("client.version")), 3, new Color(0, 1f, 0, colorAlpha).getRGB());
        drawString(fontRendererObj, copyright, 3, 3, new Color(0, 1f, 0, colorAlpha).getRGB());

        for(GuiButton button : buttonList) {
            button.forceEnabled = startTicks < 1;
            button.alpha = colorAlpha;
            button.textAlpha = colorAlpha;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(clamp(startTicks / 2, 5, 15), clamp(startTicks / 2, 5, 10), 1);
        GlStateManager.translate(-(width / 2f), -(height / 2f), 0);
        drawCenteredStringWithoutShadow(mc.smoothFontRendererObj, I18n.format("client.name"), width / 2, height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0x0000FF);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
