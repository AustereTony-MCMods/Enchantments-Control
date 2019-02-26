package austeretony.enchcontrol.common.enchantment;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.udojava.evalex.Expression;

import austeretony.enchcontrol.common.config.ConfigLoader;
import austeretony.enchcontrol.common.main.ECMain;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class EnchantmentWrapper {

    private static final Map<ResourceLocation, EnchantmentWrapper> WRAPPERS = new HashMap<ResourceLocation, EnchantmentWrapper>();

    public static final Set<EnchantmentWrapper> UNKNOWN = new HashSet<EnchantmentWrapper>();

    public final ResourceLocation registryName;

    public final String modid;

    private boolean enabled, initialized, customEvals, isTreasure, doublePrice, isCurse, isAllowedOnBooks, hasIncompat, hasItemsList, hasDesc;

    private String name, minEnchEval, maxEnchEval, typeStr;

    private int[][] enchantability;//first column - MIN enchantability, second column - MAX enchantability

    private Enchantment.Rarity rarity;

    private int minLevel, maxLevel, 
    incompatMode,//"0" - use enchantment own settings AND config settings, "1" - use config settings ONLY
    applicabilityMode,//"0" - use enchantment own settings AND config settings, "1" - use config settings ONLY
    listMode;//"0" - items list will be considered as BLACKLIST, "1" - items list will be considered as WHITELIST

    private EnumEnchantmentType type;

    private EntityEquipmentSlot[] equipmentSlots;

    private Set<ResourceLocation> incompatibleEnchants, itemsList;

    private String[] description;

    private Enchantment wrapped;

    private EnchantmentWrapper(ResourceLocation registryName) {
        this.registryName = registryName;
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
        return wrapper;
    }

    public static Collection<EnchantmentWrapper> getWrappers() {
        return WRAPPERS.values();
    }

    public static EnchantmentWrapper get(Enchantment enchantment) {   
        ResourceLocation registryName = enchantment.getRegistryName();
        EnchantmentWrapper wrapper;
        if (!WRAPPERS.containsKey(registryName)) {
            wrapper = EnchantmentWrapper.create(
                    registryName, 
                    true, 
                    enchantment.getName(),
                    enchantment.rarity, 
                    enchantment.getMinLevel(),
                    enchantment.getMaxLevel(),
                    enchantment.type, 
                    enchantment.applicableEquipmentTypes);
            wrapper.initialized = true;
            wrapper.setMinEnchantabilityEvaluation(ConfigLoader.MIN_ENCH_DEFAULT_EVAL);
            wrapper.setMaxEnchantabilityEvaluation(ConfigLoader.MAX_ENCH_DEFAULT_EVAL);
            wrapper.setTreasure(enchantment.isTreasureEnchantment());
            wrapper.setDoublePrice(enchantment.isTreasureEnchantment());
            wrapper.setCurse(enchantment.isCurse());
            wrapper.setAllowedOnBooks(enchantment.isAllowedOnBooks());
            wrapper.setEnchantment(enchantment);
            wrapper.calculateEnchantability();
            UNKNOWN.add(wrapper);
            ECMain.LOGGER.info("Unknown enchantment <{}>. Data collected.", registryName);
        } else {
            wrapper = WRAPPERS.get(registryName);
            if (!wrapper.initialized) {
                wrapper.initialized = true;
                if (!wrapper.isEnabled())
                    ECMain.LOGGER.info("Enchantment <{}> disabled! It can't be obtained in survival mode.", registryName);
                enchantment.rarity = wrapper.getRarity();
                EnumEnchantmentType type;
                try {
                    type = EnumEnchantmentType.valueOf(wrapper.getTypeString());
                } catch(IllegalArgumentException exception) {
                    ECMain.LOGGER.error("Unknown enchantment type: <{}>! Enchantment will be initialized with base type: <{}>.", wrapper.getTypeString(), enchantment.type == null ? "NULL" : enchantment.type.toString());
                    type = enchantment.type;
                }
                wrapper.setType(type);
                enchantment.type = type;
                enchantment.applicableEquipmentTypes = wrapper.getEquipmentSlots();
                wrapper.setEnchantment(enchantment);
                wrapper.calculateEnchantability();
                ECMain.LOGGER.info("Initialized enchantment <{}> with config settings.", registryName);
            }
        }
        return wrapper;
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

    public void calculateEnchantability() {
        this.enchantability = new int[this.maxLevel - this.minLevel + 1][2];
        if (this.customEvals) {
            this.calcMinEnchantabilityCustom(this.minEnchEval);
            this.calcMaxEnchantabilityCustom(this.maxEnchEval);
        } else {
            for (int i = this.minLevel; i <= this.maxLevel; i++) {
                this.enchantability[i - this.minLevel][0] = this.wrapped.getMinEnchantability(i);
                this.enchantability[i - this.minLevel][1] = this.wrapped.getMaxEnchantability(i);
            }
        }
    }

    public boolean useCustomEvals() {
        return this.customEvals;
    }

    public void setCustomEvals(boolean flag) {
        this.customEvals = flag;
    }

    public int getMinEnchantability(int enchantmentLevel) {
        return this.enchantability[enchantmentLevel - this.minLevel][0];
    }

    public String getMinEnchantabilityEval() {
        return this.minEnchEval;
    }

    public void setMinEnchantabilityEvaluation(String eval) {
        this.minEnchEval = eval;
    }

    private void calcMinEnchantabilityCustom(String eval) {
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
        return this.enchantability[enchantmentLevel - this.minLevel][1];
    }

    public String getMaxEnchantabilityEval() {
        return this.maxEnchEval;
    }

    public void setMaxEnchantabilityEvaluation(String eval) {
        this.maxEnchEval = eval;
    }

    private void calcMaxEnchantabilityCustom(String eval) {
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

    public String getTypeString() {
        return this.typeStr;
    }

    public void setEnumTypeString(String typeStr) {
        this.typeStr = typeStr;
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
        this.incompatMode = MathHelper.clamp(value, 0, 1);
    }

    public boolean hasIncompatibleEnchantments() {
        return this.hasIncompat;
    }

    public Set<ResourceLocation> getIncompatibleEnchantments() {
        return this.incompatibleEnchants;
    }

    public void addIncompatibleEnchantment(ResourceLocation registryName) {
        if (!this.hasIncompat) {
            this.hasIncompat = true;
            this.incompatibleEnchants = new HashSet<ResourceLocation>();
        }
        this.incompatibleEnchants.add(registryName);
    }

    private boolean isEnchantmentExist(ResourceLocation registryName) {
        return this.hasIncompat && this.incompatibleEnchants.contains(registryName);
    }

    public boolean isCompatibleWith(Enchantment enchantment) {
        return this.incompatMode == 0 ? 
                this.wrapped.isCompatibleWith(enchantment) && !this.isEnchantmentExist(enchantment.getRegistryName()) 
                : !this.isEnchantmentExist(enchantment.getRegistryName());
    }

    public int getApplicabilityMode() {
        return this.applicabilityMode;
    }

    public void setApplicabilityMode(int value) {
        this.applicabilityMode = MathHelper.clamp(value, 0, 1);
    }

    public int getListMode() {
        return this.listMode;
    }

    public void setListMode(int value) {
        this.listMode = MathHelper.clamp(value, 0, 1);
    }

    public boolean hasItemList() {
        return this.hasItemsList;
    }

    public Set<ResourceLocation> getItemList() {
        return this.itemsList;
    }

    public void addItem(ResourceLocation registryName) {
        if (!this.hasItemsList) {
            this.hasItemsList = true;
            this.itemsList = new HashSet<ResourceLocation>();
        }
        this.itemsList.add(registryName);
    }

    private boolean isItemExist(ItemStack itemStack) {
        return this.hasItemsList && this.itemsList.contains(itemStack.getItem().getRegistryName());
    }

    public boolean canApply(ItemStack itemStack) {
        return this.applicabilityMode == 0 ? 
                this.wrapped.canApply(itemStack) && (this.listMode > 0 ? this.isItemExist(itemStack) : !this.isItemExist(itemStack))
                : (this.listMode > 0 ? this.isItemExist(itemStack) : !this.isItemExist(itemStack));
    }

    public boolean isTreasure() {
        return this.isTreasure;
    }

    public void setTreasure(boolean flag) {
        this.isTreasure = flag;
    }

    public boolean shouldDoublePrice() {
        return this.doublePrice;
    }

    public void setDoublePrice(boolean flag) {
        this.doublePrice = flag;
    }

    public boolean isCurse() {
        return this.isCurse;
    }

    public void setCurse(boolean flag) {
        this.isCurse = flag;
    }

    public boolean isAllowedOnBooks() {
        return this.isAllowedOnBooks;
    }

    public void setAllowedOnBooks(boolean flag) {
        this.isAllowedOnBooks = flag;
    }

    public void initDescription(int size) {
        this.description = new String[size];
        this.hasDesc = true;
    }

    public boolean hasDescription() {
        return this.hasDesc;
    }

    public String[] getDescription() {
        return  this.description;
    }
}
