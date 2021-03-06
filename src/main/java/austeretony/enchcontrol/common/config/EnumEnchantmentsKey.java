package austeretony.enchcontrol.common.config;

public enum EnumEnchantmentsKey {

    ID("id"),
    ENABLED("enabled"),
    HIDE_ON_ITEM("hide_on_item"),
    HIDE_ON_BOOK("hide_on_book"),
    UNLOCALIZED_NAME("name"),
    RARITY("rarity"),
    TREASURE("treasure"),
    DOUBLE_PRICE("double_price"),
    CURSE("curse"),
    ALLOWED_ON_BOOKS("allowed_on_books"),
    MIN_LEVEL("min_lvl"),
    MAX_LEVEL("max_lvl"),
    CUSTOM_EVALUATIONS("custom_evaluations"),
    MIN_ENCH_EVAL("min_ench_eval"),
    MAX_ENCH_EVAL("max_ench_eval"),
    TYPE("type"),
    EQUIPMENT_SLOTS("equipment_slots"),
    INCOMPAT_MODE("incompat_mode"),
    INCOMPATIBLE_ENCHANTMENTS("incompatible"),
    APPLICABILITY_MODE("applicability_mode"),
    ITEMS_LIST_MODE("items_list_mode"),
    ITEMS_LIST("items_list"),
    DESCRIPTION("description");

    public final String key;

    EnumEnchantmentsKey(String key) {
        this.key = key;
    }
}
