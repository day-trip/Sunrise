package com.daytrip.sunrise.gui;

import com.daytrip.shared.gui.util.Rectangle;
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
        int size = width / 6;

        int x = 0;
        int y = 0;
        for(Hack hack : HackManager.getHacks().values()) {
            if(x == 0) {
                new Rectangle(10, hackDisplayStart + size * y, size, size, Color.BLUE.getRGB()).draw();
                drawCenteredString(mc.fontRendererObj, hack.getName(), 10 + size / 2, hackDisplayStart + size * y + 20, Color.yellow.getRGB());
            }
            if(x == 1) {
                new Rectangle(width / 2, hackDisplayStart + size * y, size, size, Color.BLUE.getRGB()).centerWidth().draw();
                drawCenteredString(mc.fontRendererObj, hack.getName(), width / 2, hackDisplayStart + size * y + 20, Color.yellow.getRGB());
            }
            if(x == 2) {
                x = 0;
                y++;
                new Rectangle(width - size - 10, hackDisplayStart + size * y, size, size, Color.BLUE.getRGB()).draw();
                drawCenteredString(mc.fontRendererObj, hack.getName(), width - size / 2 - 10, hackDisplayStart + size * y + 20, Color.yellow.getRGB());
            } else {
                x++;
            }
        }
    }
}
