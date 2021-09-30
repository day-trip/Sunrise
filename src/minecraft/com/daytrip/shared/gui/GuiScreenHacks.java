package com.daytrip.shared.gui;

import com.daytrip.sunrise.SunriseClient;
import com.daytrip.sunrise.hack.Hack;
import com.google.common.collect.HashBasedTable;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.util.Map;

public class GuiScreenHacks extends GuiScreen {
    private final HashBasedTable<Integer, Integer, Hack> hacksOrderTable = HashBasedTable.create();

    @Override
    public void initGui() {
        generateHacksOrderTable();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Color grayStart = new Color(128, 128, 128, 200);
        Color grayEnd = new Color(64, 64, 64, 100);
        drawGradientRect(0, 0, width, height, grayStart.getRGB(), grayEnd.getRGB());

        int r = 0;
        int c = 0;

        int w = width / 6;

        for(Map<Integer, Hack> row : hacksOrderTable.rowMap().values()) {
            for(Hack hack : row.values()) {
                Color blue = new Color(r, c, w, 50);
                drawRectCentered(r * 2 * w + 10, 10, r * 2 * w * w, w, blue.getRGB());
                c++;
            }
            c = 0;
            r++;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void generateHacksOrderTable() {
        int row = 0;
        int column = 0;

        for(Hack hack : SunriseClient.hacks) {
            if(hacksOrderTable.row(row).size() == 3) {
                row++;
                column = 0;
            } else {
                column++;
            }
            hacksOrderTable.put(row, column, hack);
        }
    }
}
