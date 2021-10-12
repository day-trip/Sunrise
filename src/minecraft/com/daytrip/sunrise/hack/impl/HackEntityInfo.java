package com.daytrip.sunrise.hack.impl;

import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.impl.EventRenderHud;
import com.daytrip.sunrise.hack.Hack;
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class HackEntityInfo extends Hack {
    public HackEntityInfo() {
        super(Keyboard.KEY_Z, "Entity Info Display", "entity_info");
        settingManager.addSetting(new SettingBoolean("Show Name", "show_name", true));
        settingManager.addSetting(new SettingBoolean("Show Health", "show_health", true));
        settingManager.addSetting(new SettingBoolean("Show Hunger", "show_hunger", true));
        settingManager.addSetting(new SettingBoolean("Show Saturation", "show_saturation", true));
    }

    @Override
    public void onEvent(Event event) throws Exception {
        if(event instanceof EventRenderHud) {
            if(minecraft.objectMouseOver.entityHit != null) {
                String s = "";
                if(settingManager.<SettingBoolean>getSetting("show_name").getValue()) {
                    s += minecraft.objectMouseOver.entityHit.getDisplayName().getUnformattedText();
                    s += ": ";
                }
                if(minecraft.objectMouseOver.entityHit instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) minecraft.objectMouseOver.entityHit;
                    if(settingManager.<SettingBoolean>getSetting("show_health").getValue()) {
                        s += "Health - ";
                        s += entityLivingBase.getHealth();
                        s += "/";
                        s += entityLivingBase.getMaxHealth();
                    }
                    if(entityLivingBase instanceof EntityPlayer) {
                         EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
                         if(settingManager.<SettingBoolean>getSetting("show_hunger").getValue()) {
                             s += " | Hunger - ";
                             s += entityPlayer.getFoodStats().getFoodLevel();
                         }
                        if(settingManager.<SettingBoolean>getSetting("show_saturation").getValue()) {
                            s += " | Saturation - ";
                            s += entityPlayer.getFoodStats().getSaturationLevel();
                        }
                    }
                }

                ScaledResolution scaledResolution = new ScaledResolution(minecraft);
                minecraft.fontRendererObj.drawCenteredString(s, scaledResolution.getScaledWidth() / 2, 20, Color.GREEN.getRGB(), true);
            }
        }
    }
}
