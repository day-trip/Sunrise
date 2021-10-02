package com.daytrip.sunrise.hack;

import com.daytrip.sunrise.hack.setting.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HackSettingManager {
    private final Map<String, Setting> settingMap = new HashMap<>();

    public void addSetting(Setting setting) {
        settingMap.put(setting.getId(), setting);
    }

    public <T extends Setting> T getSetting(String id) {
        return (T) settingMap.get(id);
    }

    public <T extends Setting> T getSetting(int id) {
        Set<Map.Entry<String, Setting>> entry = settingMap.entrySet();
        return (T) new ArrayList<>(entry).get(id).getValue();
    }

    public int count() {
        return settingMap.size();
    }
}
