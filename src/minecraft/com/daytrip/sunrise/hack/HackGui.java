package com.daytrip.sunrise.hack;

import com.daytrip.shared.event.Event;
import com.daytrip.shared.event.EventListener;
import com.daytrip.shared.event.impl.EventRenderHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

public class HackGui implements EventListener {
    private final Minecraft minecraft;

    public HackGui() {
        minecraft = Minecraft.getMinecraft();
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventRenderHud) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(minecraft.displayWidth / 2f, minecraft.displayHeight / 2f, 0);
            GlStateManager.scale(2, 2, 1);
            GlStateManager.translate(-(minecraft.displayWidth / 2f), -(minecraft.displayHeight / 2f), 0);
            minecraft.smoothFontRendererObj.drawString(I18n.format("client.name"), 3, 3, 0xFFFFA500, false);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public String getListenerName() {
        return "hackhud";
    }

    @Override
    public boolean ignore(Event event) {
        return false;
    }
}
