package com.daytrip.sunrise.hack.setting.impl;

import com.daytrip.sunrise.hack.setting.Setting;

public class SettingBoolean extends Setting {
    private boolean value;

    public SettingBoolean(String name, boolean defaultValue) {
        super(name);
        value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
