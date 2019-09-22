package austeretony.enchcontrol.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public enum EnumConfigSettings {

    EXTERNAL_CONFIG(0, "main", "external_config", true),
    DEBUG_MODE(0, "main", "debug_mode"),
    CUSTOM_LOCALIZATION(0, "main", "custom_localization"),
    HIDE_CURSES(0, "tweaks", "hide_curses"),
    NOTIFY_CURSED(0, "tweaks", "notify_cursed"),
    ROMAN_NUMERALS(0, "tweaks", "roman_numerals"),
    ENCHANTMENT_TABLE_LEVEL_CAP(1, "tweaks", "enchantment_table_level_cap"),
    ANVIL_LEVEL_CAP(1, "tweaks", "anvil_level_cap"),
    MERCHANT_DEALS_LEVEL_CAP(1, "tweaks", "merchant_deals_level_cap"),
    DUNGEON_LOOT_LEVEL_CAP(1, "tweaks", "dungeon_loot_level_cap"),
    CREATIVE_TAB_LEVEL_CAP(1, "tweaks", "creative_tab_level_cap"),
    DESCRIPTIONS(0, "description", "enabled"),
    DESCRIPTIONS_HINT(0, "description", "show_hint"),
    DESCRIPTIONS_LOCATION(1, "description", "location"),
    DESCRIPTIONS_SEPARATOR(2, "description", "separator"),
    DESCRIPTIONS_FOR_BOOKS(0, "description", "for_books"),
    DESCRIPTIONS_FOR_ITEMS(0, "description", "for_items"),
    DESCRIPTIONS_SHOW_DOMAIN(0, "description", "show_domain"),
    LOAD_ED_DESC(0, "mod_support", "load_enchantment_descriptions_desc"),
    UPLOAD_DESC_TO_ED(0, "mod_support", "upload_desc_to_enchantment_descriptions"),
    SUPPORT_THERMAL_EXPANSION(0, "mod_support", "thermal_expansion");

    public final int type;//0 - boolean, 1 - int, 2 - String

    public final String configSection, configKey;

    public final boolean exclude;

    private boolean enabled;

    private int intValue;
    
    private String strValue;

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
    
    public String getStrValue() {
        return this.strValue;
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
        case 2:
            this.strValue = this.getValue(jsonObject).getAsString();
            break;
        }
    }

    public static void initAll(JsonObject config) {
        for (EnumConfigSettings enumSetting : values())
            if (!enumSetting.exclude)
                enumSetting.initByType(config);
    }
}
