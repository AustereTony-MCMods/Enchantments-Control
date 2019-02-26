package austeretony.enchcontrol.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public enum EnumConfigSettings {

    EXTERNAL_CONFIG(0, "main", "external_config", true),
    DEBUG_MODE(0, "main", "debug_mode"),
    CHECK_UPDATES(0, "main", "custom_update_checker"),
    CUSTOM_LOCALIZATION(0, "main", "enable_custom_localization"),
    TOOLTIPS(0, "tooltips", "enabled"),
    TOOLTIPS_SHOW_SPLITTER(0, "tooltips", "show_splitter"),
    TOOLTIPS_FOR_BOOKS(0, "tooltips", "allow_for_books"),
    TOOLTIPS_FOR_ITEMS(0, "tooltips", "allow_for_items"),
    TOOLTIPS_SHOW_DOMAIN(0, "tooltips", "show_domain"),
    SUPPORT_THERMAL_EXPANSION(0, "mod_support", "thermal_expansion");

    public final int type;//0 - boolean

    public final String configSection, configKey;

    public final boolean exclude;

    private boolean enabled;

    EnumConfigSettings(int type, String configSection, String configKey, boolean... exclude) {
        this.type = type;
        this.configSection = configSection;
        this.configKey = configKey;
        this.exclude = exclude.length > 0 ? exclude[0] : false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    private JsonElement getValue(JsonObject jsonObject) {
        return jsonObject.get(this.configSection).getAsJsonObject().get(this.configKey);
    }

    public void initByType(JsonObject jsonObject) {
        switch (this.type) {
        case 0:
            this.enabled = this.getValue(jsonObject).getAsBoolean();
            break;
        }
    }

    public static void initAll(JsonObject config) {
        for (EnumConfigSettings enumSetting : values())
            if (!enumSetting.exclude)
                enumSetting.initByType(config);
    }
}
