package net.minecraft.client.gui;

import java.io.IOException;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;

public class GuiGameOver extends GuiScreen {
    /**
     * The integer value containing the number of ticks that have passed since the player's death
     */
    private int enableButtonsTimer;

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui()
    {
        buttonList.clear();

        if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
        {
            if (mc.isIntegratedServerRunning())
            {
                buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.deleteWorld")));
            }
            else
            {
                buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.leaveServer")));
            }
        }
        else
        {
            buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.respawn")));
            buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (mc.getSession() == null)
            {
                buttonList.get(1).enabled = false;
            }
        }

        for (GuiButton guibutton : buttonList)
        {
            guibutton.enabled = false;
        }
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException
    {
        switch (button.id)
        {
            case 0:
                mc.thePlayer.respawnPlayer();
                mc.displayGuiScreen(null);
                break;

            case 1:
                if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
                {
                    mc.displayGuiScreen(new GuiMainMenu());
                }
                else
                {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id)
    {
        if (result)
        {
            mc.theWorld.sendQuittingDisconnectingPacket();
            mc.loadWorld(null);
            mc.displayGuiScreen(new GuiMainMenu());
        }
        else
        {
            mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawGradientRect(0, 0, width, height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
        String s = flag ? I18n.format("deathScreen.title.hardcore") : I18n.format("deathScreen.title");
        drawCenteredString(fontRendererObj, s, width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (flag)
        {
            drawCenteredString(fontRendererObj, I18n.format("deathScreen.hardcoreInfo"), width / 2, 144, 16777215);
        }

        drawCenteredString(fontRendererObj, I18n.format("deathScreen.score") + ": " + EnumChatFormatting.YELLOW + mc.thePlayer.getScore(), width / 2, 100, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
        ++enableButtonsTimer;

        if (enableButtonsTimer == 20)
        {
            for (GuiButton guibutton : buttonList)
            {
                guibutton.enabled = true;
            }
        }
    }
}
