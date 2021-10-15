package com.daytrip.sunrise.hack;

import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventListener;
import com.daytrip.sunrise.event.impl.EventRenderHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

import java.awt.Color;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class SunriseGUI implements EventListener {
    Comparator<Hack> hackNameLengthComparator = Comparator.comparingInt(o -> o.getName().length());

    private final Minecraft minecraft;

    public SunriseGUI() {
        minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventRenderHud) {
            ScaledResolution sr = new ScaledResolution(minecraft);

            minecraft.smoothFontRendererObj.drawString(I18n.format("client.name"), 7, 7, Color.ORANGE.getRGB(), false);

            AtomicInteger i = new AtomicInteger();

            HackManager.getHacks().values().stream().sorted(hackNameLengthComparator.reversed()).forEach(hack -> {
                if(hack.isEnabled()) {
                    int width = minecraft.fontRendererObj.getStringWidth(hack.getName()) + 3;
                    int height = 15;

                    Gui.drawRect(sr.getScaledWidth() - width, i.get() * height, sr.getScaledWidth(), i.get() * height + height, new Color(64, 64, 64, 150).getRGB());
                    minecraft.fontRendererObj.drawString(hack.getName(), sr.getScaledWidth() - width + 3, (i.get() * height) + height / 2 - (minecraft.fontRendererObj.FONT_HEIGHT / 2), Color.cyan.getRGB());

                    i.getAndIncrement();
                }
            });
        }
    }
}
