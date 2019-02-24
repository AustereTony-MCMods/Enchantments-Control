package austeretony.enchcontrol.common.enchantments;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.udojava.evalex.Expression;

import austeretony.enchcontrol.common.config.ConfigLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class EnchantmentWrapper {

    public static final Map<ResourceLocation, EnchantmentWrapper> WRAPPERS = new HashMap<ResourceLocation, EnchantmentWrapper>();

    public static final Set<EnchantmentWrapper> UNKNOWN = new HashSet<EnchantmentWrapper>();

    public final ResourceLocation id;

    public final String modid;

    private boolean enabled, customEvals;

    private String name, minEnchEval, maxEnchEval;

    private int[][] enchantability;//first column - MIN enchantability, second column - MAX enchantability

    private Enchantment.Rarity rarity;

    private int minLevel, maxLevel, 
    incompatMode,//"0" - use enchantment own settings AND config settings, "1" - use config settings ONLY
    applicabilityMode,//"0" - use enchantment own settings AND config settings, "1" - use config settings ONLY
    listMode;//"0" - items list will be considered as BLACKLIST, "1" - items list will be considered as WHITELIST

    private EnumEnchantmentType type;

    private EntityEquipmentSlot[] equipmentSlots;

    private Set<ResourceLocation> 
    incompatibleEnchants = new HashSet<ResourceLocation>(),
    itemsList = new HashSet<ResourceLocation>();

    private Enchantment wrapped;

    private EnchantmentWrapper(ResourceLocation registryName) {
        this.id = registryName;
        this.modid = registryName.getResourceDomain();
        WRAPPERS.put(registryName, this);
    }

    public static EnchantmentWrapper create(ResourceLocation registryName, boolean isEnabled, String name, Enchantment.Rarity rarity, int minLevel, int maxLevel, EnumEnchantmentType type, EntityEquipmentSlot[] slots) {
        EnchantmentWrapper wrapper = new EnchantmentWrapper(registryName);
        wrapper.setEnabled(isEnabled);
        wrapper.setName(name);
        wrapper.setRarity(rarity);
        wrapper.setMinLevel(minLevel);
        wrapper.setMaxLevel(maxLevel);
        wrapper.setType(type);
        wrapper.setEquipmentSlots(slots);
        wrapper.enchantability = new int[maxLevel - minLevel + 1][2];
        return wrapper;
    }

    public static EnchantmentWrapper get(ResourceLocation registryName) {
        return WRAPPERS.get(registryName);
    }

    public static EnchantmentWrapper get(Enchantment enchantment) {
        return get(enchantment.getRegistryName());
    }

    public static void clearData() {
        WRAPPERS.clear();
        UNKNOWN.clear();
    }

    public Enchantment getEnchantment() {
        return this.wrapped;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.wrapped = enchantment;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean flag) {
        this.enabled = flag;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean useCustomEvals() {
        return this.customEvals;
    }

    public void setCustomEvals(boolean flag) {
        this.customEvals = flag;
    }

    public int getMinEnchantability(int enchantmentLevel) {
        return this.customEvals ? this.enchantability[enchantmentLevel - this.minLevel][0] : this.wrapped.getMinEnchantability(enchantmentLevel);
    }

    public String getMinEnchantabilityEval() {
        return this.minEnchEval;
    }

    public void setMinEnchantabilityEvaluation(String eval) {
        this.minEnchEval = eval;
        if (this.customEvals)
            this.calcMinEnchantability(eval);
    }

    private void calcMinEnchantability(String eval) {
        if (eval.isEmpty())             
            eval = ConfigLoader.MIN_ENCH_DEFAULT_EVAL;
        BigDecimal currLvl, result;
        for (int i = this.minLevel; i <= this.maxLevel; i++) {
            currLvl = BigDecimal.valueOf(i);
            result = new Expression(eval).with("LVL", currLvl).eval();
            this.enchantability[i - this.minLevel][0] = result.intValue();
        }
    }

    public int getMaxEnchantability(int enchantmentLevel) {
        return this.customEvals ? this.enchantability[enchantmentLevel - this.minLevel][1] : this.wrapped.getMaxEnchantability(enchantmentLevel);
    }

    public String getMaxEnchantabilityEval() {
        return this.maxEnchEval;
    }

    public void setMaxEnchantabilityEvaluation(String eval) {
        this.maxEnchEval = eval;
        if (this.customEvals)
            this.calcMaxEnchantability(eval);
    }

    private void calcMaxEnchantability(String eval) {
        if (eval.isEmpty()) 
            eval = ConfigLoader.MAX_ENCH_DEFAULT_EVAL;
        BigDecimal minEnch, currLvl, result;
        for (int i = this.minLevel; i <= this.maxLevel; i++) {
            minEnch = BigDecimal.valueOf(this.getMinEnchantability(i));
            currLvl = BigDecimal.valueOf(i);
            result = new Expression(eval).with("MIN", minEnch).and("LVL", currLvl).eval();
            this.enchantability[i - this.minLevel][1] = result.intValue();
        }
    }

    public Enchantment.Rarity getRarity() {
        return this.rarity;
    }

    public void setRarity(Enchantment.Rarity rarity) {
        this.rarity = rarity;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public void setMinLevel(int value) {
        this.minLevel = value;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public void setMaxLevel(int value) {
        this.maxLevel = value;
    }

    public EnumEnchantmentType getType() {
        return this.type;
    }

    public void setType(EnumEnchantmentType type) {
        this.type = type;
    }

    public EntityEquipmentSlot[] getEquipmentSlots() {
        return this.equipmentSlots;
    }

    public void setEquipmentSlots(EntityEquipmentSlot[] slots) {
        this.equipmentSlots = slots;
    }

    public int getIncompatMode() {
        return this.incompatMode;
    }

    public void setIncompatMode(int value) {
        this.incompatMode = value;
    }

    public Set<ResourceLocation> getIncompatibleEnchantments() {
        return this.incompatibleEnchants;
    }

    public void addIncompatibleEnchantment(ResourceLocation registryName) {
        this.incompatibleEnchants.add(registryName);
    }

    public boolean isCompatibleWith(Enchantment enchantment) {
        return this.incompatMode == 0 ? 
                this.wrapped.isCompatibleWith(enchantment) && !this.incompatibleEnchants.contains(enchantment.getRegistryName()) 
                : !this.incompatibleEnchants.contains(enchantment.getRegistryName());
    }

    public int getApplicabilityMode() {
        return this.applicabilityMode;
    }

    public void setApplicabilityMode(int value) {
        this.applicabilityMode = value;
    }

    public int getListMode() {
        return this.listMode;
    }

    public void setListMode(int value) {
        this.listMode = value;
    }

    public Set<ResourceLocation> getItems() {
        return this.itemsList;
    }

    public boolean isItemExist(ItemStack itemStack) {
        return this.itemsList.contains(itemStack.getItem().getRegistryName());
    }

    public void addItem(ResourceLocation registryName) {
        this.itemsList.add(registryName);
    }

    public boolean canApply(ItemStack itemStack) {
        return this.applicabilityMode == 0 ? 
                this.wrapped.canApply(itemStack) && (this.listMode > 0 ? this.isItemExist(itemStack) : !this.isItemExist(itemStack))
                : (this.listMode > 0 ? this.isItemExist(itemStack) : !this.isItemExist(itemStack));
    }
}
