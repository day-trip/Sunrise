package com.daytrip.sunrise.hack;

import com.daytrip.sunrise.event.Event;
import com.daytrip.sunrise.event.EventListener;
import com.daytrip.sunrise.hack.task.TaskManager;
import com.daytrip.sunrise.util.math.AsyncHackMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

public class Hack implements EventListener {
    protected final Minecraft minecraft;

    protected final String id;
    protected String name;

    protected int key;

    protected boolean isEnabled;

    protected final TaskManager taskManager;

    protected final AsyncHackMath math;

    protected HackSettingManager settingManager = new HackSettingManager();

    public Hack(int key, String name, String id) {
        minecraft = Minecraft.getMinecraft();
        this.key = key;
        this.name = name;
        this.id = id;
        math = new AsyncHackMath();
        taskManager = new TaskManager();
        registerAll();
    }

    protected void init() {

    }

    private void registerAll() {
        init();
        registerSettings();
        registerTasks();
    }

    protected void registerTasks() {

    }

    protected void registerSettings() {

    }

    public void onEvent(Event event) throws Exception {
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        if(enabled) {
            enable();
        } else {
            disable();
        }
        Minecraft.logger.info(isEnabled);
    }

    public void toggle() {
        if(isEnabled) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }

    protected void enable() {
        if(minecraft.inWorld()) {
            ChatComponentText clientName = new ChatComponentText("[" + I18n.format("client.name") + "]:");
            clientName.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE));

            ChatComponentText action = new ChatComponentText("Enabled");
            action.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));

            ChatComponentText hack = new ChatComponentText(name);
            hack.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));

            ChatComponentText keybind = new ChatComponentText(Keyboard.getKeyName(key));
            keybind.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE));

            ChatComponentText m = new ChatComponentText("");
            m.appendSibling(clientName);
            m.appendText(" ");
            m.appendSibling(action);
            m.appendText(" hack ");
            m.appendSibling(hack);
            m.appendText("! (Keybind: ");
            m.appendSibling(keybind);
            m.appendText(")");

            minecraft.thePlayer.addChatMessage(m);
        }
    }

    protected void disable() {
        if(minecraft.inWorld()) {
            ChatComponentText clientName = new ChatComponentText("[" + I18n.format("client.name") + "]:");
            clientName.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE));

            ChatComponentText action = new ChatComponentText("Disabled");
            action.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));

            ChatComponentText hack = new ChatComponentText(name);
            hack.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));

            ChatComponentText keybind = new ChatComponentText(Keyboard.getKeyName(key));
            keybind.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_PURPLE));

            ChatComponentText m = new ChatComponentText("");
            m.appendSibling(clientName);
            m.appendText(" ");
            m.appendSibling(action);
            m.appendText(" hack ");
            m.appendSibling(hack);
            m.appendText("! (Keybind: ");
            m.appendSibling(keybind);
            m.appendText(")");

            minecraft.thePlayer.addChatMessage(m);
        }
    }

    @Override
    public boolean ignore(Event event) {
        return !isEnabled;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public HackSettingManager getSettingManager() {
        return settingManager;
    }
}
