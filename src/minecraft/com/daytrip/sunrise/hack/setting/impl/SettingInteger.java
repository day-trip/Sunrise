package com.daytrip.sunrise.hack.setting.impl;

import com.daytrip.sunrise.hack.setting.Setting;

public class SettingInteger extends Setting {
    private int value;
    private int minValue;
    private int maxValue;

    public SettingInteger(String name, int defaultValue, int minValue, int maxValue) {
        super(name);
        value = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
