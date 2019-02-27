package austeretony.enchcontrol.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public enum EnumConfigSettings {

    EXTERNAL_CONFIG(0, "main", "external_config", true),
    DEBUG_MODE(0, "main", "debug_mode"),
    CHECK_UPDATES(0, "main", "custom_update_checker"),
    CUSTOM_LOCALIZATION(0, "main", "enable_custom_localization"),
    ROMAN_NUMERALS(0, "tweaks", "roman_numerals"),
    ENCHANTMENT_TABLE_LEVEL_CAP(1, "tweaks", "enchantment_table_level_cap"),
    ANVIL_LEVEL_CAP(1, "tweaks", "anvil_level_cap"),
    MERCHANT_DEALS_LEVEL_CAP(1, "tweaks", "merchant_deals_level_cap"),
    DUNGEON_LOOT_LEVEL_CAP(1, "tweaks", "dungeon_loot_level_cap"),
    CREATIVE_TAB_LEVEL_CAP(1, "tweaks", "creative_tab_level_cap"),
    TOOLTIPS(0, "tooltips", "enabled"),
    TOOLTIPS_SHOW_SPLITTER(0, "tooltips", "show_splitter"),
    TOOLTIPS_FOR_BOOKS(0, "tooltips", "allow_for_books"),
    TOOLTIPS_FOR_ITEMS(0, "tooltips", "allow_for_items"),
    TOOLTIPS_SHOW_DOMAIN(0, "tooltips", "show_domain"),
    USE_ED_DESCRIPTION(0, "mod_support", "use_ed_description"),
    LOAD_DESCRIPTION_TO_ED(0, "mod_support", "load_description_to_ed"),
    SUPPORT_THERMAL_EXPANSION(0, "mod_support", "thermal_expansion");

    public final int type;//0 - boolean

    public final String configSection, configKey;

    public final boolean exclude;

    private boolean enabled;

    private int intValue;

    EnumConfigSettings(int type, String configSection, String configKey, boolean... exclude) {
        this.type = type;
        this.configSection = configSection;
        this.configKey = configKey;
        this.exclude = exclude.length > 0 ? exclude[0] : false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public int getIntValue() {
        return this.intValue;
    }

    private JsonElement getValue(JsonObject jsonObject) {
        return jsonObject.get(this.configSection).getAsJsonObject().get(this.configKey);
    }

    public void initByType(JsonObject jsonObject) {
        switch (this.type) {
        case 0:
            this.enabled = this.getValue(jsonObject).getAsBoolean();
            break;
        case 1:
            this.intValue = this.getValue(jsonObject).getAsInt();
            break;
        }
    }

    public static void initAll(JsonObject config) {
        for (EnumConfigSettings enumSetting : values())
            if (!enumSetting.exclude)
                enumSetting.initByType(config);
    }
}
