package com.daytrip.sunrise.gui;

import com.daytrip.sunrise.gui.button.GuiIconButtonClose;
import com.daytrip.sunrise.hack.HackManager;
import com.daytrip.sunrise.hack.setting.Setting;
import com.daytrip.sunrise.hack.setting.impl.SettingBoolean;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class GuiScreenHackSetting extends GuiScreen {
    private final GuiScreen parentScreen;
    private final int settingIndex;
    private final int hackIndex;

    private boolean valBool;

    public GuiScreenHackSetting(GuiScreen parentScreen, int settingIndex, int hackIndex) {
        this.parentScreen = parentScreen;
        this.settingIndex = settingIndex;
        this.hackIndex = hackIndex;
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiIconButtonClose(0, width - 23, height - 27));
        buttonList.add(new GuiButton(1, width / 2 - 30, height - 27, 60, 20, "Save"));

        Setting setting = HackManager.getHack(hackIndex).getSettingManager().getSetting(settingIndex);
        if(setting instanceof SettingBoolean) {
            valBool = ((SettingBoolean) setting).getValue();
            buttonList.add(new GuiButton(2, width / 2 - 30, height / 2 - 10, 60, 20, valBool ? "Enabled" : "Disabled"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        Setting setting = HackManager.getHack(hackIndex).getSettingManager().getSetting(settingIndex);
        if(button.id == 0) {
            mc.displayGuiScreen(parentScreen);
        } else if(button.id == 1) {
            if(setting instanceof SettingBoolean) {
                ((SettingBoolean) setting).setValue(valBool);
            }
            mc.displayGuiScreen(parentScreen);
        } else if(setting instanceof SettingBoolean && button.id == 2) {
            valBool = !valBool;
            button.displayString = valBool ? "Enabled" : "Disabled";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "Modifying setting: " + HackManager.getHack(hackIndex).getSettingManager().getSetting(settingIndex).getName(), width / 2, 16, 16777215);
    }

    public int getSettingIndex() {
        return settingIndex;
    }

    public int getHackIndex() {
        return hackIndex;
    }
}
