package com.daytrip.sunrise.hack;

import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.EventListener;
import com.daytrip.shared.event.impl.EventRenderHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.awt.*;

public class SunriseGUI implements EventListener {
    private final Minecraft minecraft;

    public SunriseGUI() {
        minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventRenderHud) {
            //ScaledResolution scaledResolution = new ScaledResolution(minecraft);
            minecraft.smoothFontRendererObj.drawString(I18n.format("client.name"), 7, 7, Color.ORANGE.getRGB(), false);
        }
    }

    @Override
    public String getListenerName() {
        return "sunrisegui";
    }
}
