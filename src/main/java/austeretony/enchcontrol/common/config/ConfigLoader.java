package austeretony.enchcontrol.common.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import austeretony.enchcontrol.common.enchantments.EnchantmentWrapper;
import austeretony.enchcontrol.common.main.ECMain;
import austeretony.enchcontrol.common.main.UpdateChecker;
import austeretony.enchcontrol.common.reference.CommonReference;
import austeretony.enchcontrol.common.util.ECUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

public class ConfigLoader {

    private static final String 
    EXT_CONFIGURATION_FILE = CommonReference.getGameFolder() + "/config/enchcontrol/config.json",
    EXT_DATA_FILE = CommonReference.getGameFolder() + "/config/enchcontrol/enchantments.json",
    EXT_LOCALIZATION_FILE = CommonReference.getGameFolder() + "/config/enchcontrol/localization.json",
    LIST_ALL_FILE = CommonReference.getGameFolder() + "/config/enchcontrol/all_enchantments.txt",
    LIST_UNKNOWN_FILE = CommonReference.getGameFolder() + "/config/enchcontrol/unknown_enchantments.txt";

    public static final String 
    MIN_ENCH_DEFAULT_EVAL = "1+LVL*10",
    MAX_ENCH_DEFAULT_EVAL = "MIN+5";

    public static boolean runningAtDedicatedServer;

    private static final DateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yy_MM_dd-HH-mm-ss");

    public static void load() {
        ECMain.LOGGER.info("Loading configuration...");
        try {    
            JsonObject internalConfig = ECUtils.getInternalJsonData("assets/enchcontrol/config.json").getAsJsonObject();
            JsonArray internalSettings = ECUtils.getInternalJsonData("assets/enchcontrol/enchantments.json").getAsJsonArray();  
            EnumConfigSettings.EXTERNAL_CONFIG.initByType(internalConfig);
            if (EnumConfigSettings.EXTERNAL_CONFIG.isEnabled())             
                loadExternalConfig(internalConfig, internalSettings);
            else                  
                loadData(internalConfig, internalSettings);
        } catch (IOException exception) {       
            ECMain.LOGGER.error("Internal configuration files damaged!");
            exception.printStackTrace();
        }
    }

    private static void loadExternalConfig(JsonObject internalConfig, JsonArray internalSettings) {
        Path configPath = Paths.get(EXT_CONFIGURATION_FILE);      
        if (Files.exists(configPath)) {
            try {      
                loadData(updateConfig(internalConfig), ECUtils.getExternalJsonData(EXT_DATA_FILE).getAsJsonArray());
            } catch (IOException exception) {  
                ECMain.LOGGER.error("External configuration file damaged!");
                exception.printStackTrace();
            }       
        } else {                
            try {               
                Path dataPath = Paths.get(EXT_DATA_FILE);      
                Files.createDirectories(configPath.getParent());                                
                Files.createDirectories(dataPath.getParent());                            
                createExternalCopyAndLoad(internalConfig, internalSettings);  
            } catch (IOException exception) {               
                exception.printStackTrace();
            }                       
        }
    }

    private static JsonObject updateConfig(JsonObject internalConfig) throws IOException {
        try {            
            JsonObject externalConfigOld, externalConfigNew, externalGroupNew;
            externalConfigOld = ECUtils.getExternalJsonData(EXT_CONFIGURATION_FILE).getAsJsonObject();   
            JsonElement versionElement = externalConfigOld.get("version");
            if (versionElement == null || UpdateChecker.isOutdated(versionElement.getAsString(), ECMain.VERSION_CUSTOM)) {
                ECMain.LOGGER.info("Updating external config file...");
                externalConfigNew = new JsonObject();
                externalConfigNew.add("version", new JsonPrimitive(ECMain.VERSION_CUSTOM));
                Map<String, JsonElement> 
                internalData = new LinkedHashMap<String, JsonElement>(),
                externlDataOld = new HashMap<String, JsonElement>(),
                internalGroup, externlGroupOld;
                for (Map.Entry<String, JsonElement> entry : internalConfig.entrySet())
                    internalData.put(entry.getKey(), entry.getValue());
                for (Map.Entry<String, JsonElement> entry : externalConfigOld.entrySet())
                    externlDataOld.put(entry.getKey(), entry.getValue());      
                for (String key : internalData.keySet()) {
                    internalGroup = new LinkedHashMap<String, JsonElement>();
                    externlGroupOld = new HashMap<String, JsonElement>();
                    externalGroupNew = new JsonObject();
                    for (Map.Entry<String, JsonElement> entry : internalData.get(key).getAsJsonObject().entrySet())
                        internalGroup.put(entry.getKey(), entry.getValue());
                    if (externlDataOld.containsKey(key)) {                    
                        for (Map.Entry<String, JsonElement> entry : externlDataOld.get(key).getAsJsonObject().entrySet())
                            externlGroupOld.put(entry.getKey(), entry.getValue());   
                        for (String k : internalGroup.keySet()) {
                            if (externlGroupOld.containsKey(k))
                                externalGroupNew.add(k, externlGroupOld.get(k));
                            else 
                                externalGroupNew.add(k, internalGroup.get(k));
                        }
                    } else {
                        for (String k : internalGroup.keySet())
                            externalGroupNew.add(k, internalGroup.get(k));
                    }
                    externalConfigNew.add(key, externalGroupNew);
                    ECUtils.createExternalJsonFile(EXT_CONFIGURATION_FILE, externalConfigNew);
                }
                return externalConfigNew;
            }
            ECMain.LOGGER.info("External config up-to-date!");
            return externalConfigOld;            
        } catch (IOException exception) {  
            ECMain.LOGGER.error("External configuration file damaged!");
            exception.printStackTrace();
        }
        return null;
    }

    private static void createExternalCopyAndLoad(JsonObject internalConfig, JsonArray internalSettings) {       
        try {           
            ECUtils.createExternalJsonFile(EXT_CONFIGURATION_FILE, internalConfig); 
            ECUtils.createExternalJsonFile(EXT_DATA_FILE, internalSettings);                                                                                                    
        } catch (IOException exception) {               
            exception.printStackTrace();
        }
        loadData(internalConfig, internalSettings);
    }

    private static void loadData(JsonObject configFile, JsonArray settingsFile) {  
        ECMain.LOGGER.info("Loading data...");
        EnumConfigSettings.initAll(configFile);
        JsonObject enchObject;
        JsonArray descArray;
        EnchantmentWrapper wrapper;
        String rarityStr, typeStr;
        Enchantment.Rarity rarity;
        EnumEnchantmentType type = null;
        EntityEquipmentSlot[] equipmentSlots;
        int i = 0;
        for (JsonElement enchElement : settingsFile) {
            enchObject = enchElement.getAsJsonObject();
            rarityStr = enchObject.get(EnumEnchantmentsKeys.RARITY.key).getAsString();
            try {
                rarity = Enchantment.Rarity.valueOf(rarityStr);
            } catch(IllegalArgumentException exception) {
                ECMain.LOGGER.error("Unknown enchantment rarity: <{}>! Default type <COMMON> will be used.", rarityStr);
                rarity = Enchantment.Rarity.COMMON;
            }
            typeStr = enchObject.get(EnumEnchantmentsKeys.TYPE.key).getAsString();
            if (!typeStr.equals("NONE")) {
                try {
                    type = EnumEnchantmentType.valueOf(typeStr);
                } catch(IllegalArgumentException exception) {
                    ECMain.LOGGER.error("Unknown enchantment type: <{}>! Default type <ALL> will be used.", typeStr);
                    type = EnumEnchantmentType.ALL;
                }
            }
            wrapper = EnchantmentWrapper.create(
                    new ResourceLocation(enchObject.get(EnumEnchantmentsKeys.ID.key).getAsString()), 
                    enchObject.get(EnumEnchantmentsKeys.ENABLED.key).getAsBoolean(),
                    enchObject.get(EnumEnchantmentsKeys.UNLOCALIZED_NAME.key).getAsString(),
                    rarity == null ? Enchantment.Rarity.COMMON : rarity,
                            enchObject.get(EnumEnchantmentsKeys.MIN_LEVEL.key).getAsInt(),
                            enchObject.get(EnumEnchantmentsKeys.MAX_LEVEL.key).getAsInt(),
                            type,
                            getSlots(enchObject.get(EnumEnchantmentsKeys.EQUIPMENT_SLOTS.key).getAsJsonArray()));
            wrapper.setTreasure(enchObject.get(EnumEnchantmentsKeys.TREASURE.key).getAsBoolean());
            wrapper.setDoublePrice(enchObject.get(EnumEnchantmentsKeys.DOUBLE_PRICE.key).getAsBoolean());
            wrapper.setCurse(enchObject.get(EnumEnchantmentsKeys.CURSE.key).getAsBoolean());
            wrapper.setAllowedOnBooks(enchObject.get(EnumEnchantmentsKeys.ALLOWED_ON_BOOKS.key).getAsBoolean());
            wrapper.setCustomEvals(enchObject.get(EnumEnchantmentsKeys.CUSTOM_EVALUATIONS.key).getAsBoolean());
            wrapper.setMinEnchantabilityEvaluation(enchObject.get(EnumEnchantmentsKeys.MIN_ENCH_EVAL.key).getAsString());
            wrapper.setMaxEnchantabilityEvaluation(enchObject.get(EnumEnchantmentsKeys.MAX_ENCH_EVAL.key).getAsString());
            wrapper.setIncompatMode(enchObject.get(EnumEnchantmentsKeys.INCOMPAT_MODE.key).getAsInt());
            for (JsonElement incompatElement : enchObject.get(EnumEnchantmentsKeys.INCOMPATIBLE_ENCHANTMENTS.key).getAsJsonArray())
                wrapper.addIncompatibleEnchantment(new ResourceLocation(incompatElement.getAsString()));
            wrapper.setApplicabilityMode(enchObject.get(EnumEnchantmentsKeys.APPLICABILITY_MODE.key).getAsInt());
            wrapper.setListMode(enchObject.get(EnumEnchantmentsKeys.ITEMS_LIST_MODE.key).getAsInt());
            for (JsonElement incompatElement : enchObject.get(EnumEnchantmentsKeys.ITEMS_LIST.key).getAsJsonArray())
                wrapper.addItem(new ResourceLocation(incompatElement.getAsString()));
            if ((descArray = enchObject.get(EnumEnchantmentsKeys.DESCRIPTION.key).getAsJsonArray()).size() > 0) {
                wrapper.initDescription(descArray.size());
                i = 0;
                for (JsonElement lineElement : descArray)
                    wrapper.getDescription()[i++] = lineElement.getAsString();
            }
        }
    }

    private static EntityEquipmentSlot[] getSlots(JsonArray jsonArray) {
        if (jsonArray.size() == 0) 
            return EntityEquipmentSlot.values();   
        EntityEquipmentSlot[] slots = new EntityEquipmentSlot[jsonArray.size()];
        String slotName;
        int i = 0;
        EntityEquipmentSlot slot; 
        for (JsonElement slotElement : jsonArray) {
            slotName = slotElement.getAsString();
            if (slotName.equals("ALL")) 
                return EntityEquipmentSlot.values();  
            slot = EntityEquipmentSlot.valueOf(slotName);
            if (slot != null)
                slots[i] = slot;
            else   
                slots[i] = EntityEquipmentSlot.MAINHAND;
            i++;
        }
        return slots;
    }

    public static void loadCustomLocalization(List<String> languageList, Map<String, String> properties) {
        if (EnumConfigSettings.CUSTOM_LOCALIZATION.isEnabled()) {
            Path localizationPath = Paths.get(EXT_LOCALIZATION_FILE);      
            if (Files.exists(localizationPath)) {
                try {       
                    loadLocalization(ECUtils.getExternalJsonData(EXT_LOCALIZATION_FILE).getAsJsonObject(), languageList, properties);
                } catch (IOException exception) {       
                    exception.printStackTrace();
                    return;
                }
            } else {
                try {               
                    Files.createDirectories(localizationPath.getParent());
                    ECUtils.createAbsoluteJsonCopy(EXT_LOCALIZATION_FILE, ConfigLoader.class.getClassLoader().getResourceAsStream("assets/enchcontrol/localization.json"));    
                    loadLocalization(ECUtils.getInternalJsonData("assets/enchcontrol/localization.json").getAsJsonObject(), languageList, properties);
                } catch (IOException exception) {               
                    exception.printStackTrace();
                }   
            }
        }
    }

    private static void loadLocalization(JsonObject localizationFile, List<String> languageList, Map<String, String> properties) {
        ECMain.LOGGER.info("Searching for custom localization...");
        for (String lang : languageList) {
            JsonElement entriesElement = localizationFile.get(lang.toLowerCase());
            if (entriesElement != null) {
                ECMain.LOGGER.info("Loading custom <{}> localization...", lang);
                JsonArray entries = entriesElement.getAsJsonArray();
                JsonObject entryObject;
                for (JsonElement entryElement : entries) {
                    entryObject = entryElement.getAsJsonObject();
                    if (entryObject.has("key") && entryObject.has("value")) 
                        properties.put(
                                entryObject.get("key").getAsString(), 
                                entryObject.get("value").getAsString());
                }
            } else {
                ECMain.LOGGER.error("Custom localization for <{}> undefined!", lang);
            }
        }
    }

    public static void clear() {
        try (PrintStream printStream = new PrintStream(new File(EXT_DATA_FILE))) {
            printStream.println("[]");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void backup() {
        try {
            ECUtils.createExternalJsonFile(
                    CommonReference.getGameFolder() + "/config/enchcontrol/enchantments-" + BACKUP_DATE_FORMAT.format(new Date()) + ".json", 
                    EnumConfigSettings.EXTERNAL_CONFIG.isEnabled() ? ECUtils.getExternalJsonData(EXT_DATA_FILE) : ECUtils.getInternalJsonData("assets/enchcontrol/enchantments.json"));         
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void save() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();  
        JsonArray 
        enchsArray = new JsonArray(),
        equipArray, incompatArray, itemsArray, descArray;
        JsonObject enchObject;
        for (EnchantmentWrapper wrapper : EnchantmentWrapper.getWrappers()) {
            enchObject = new JsonObject();
            equipArray = new JsonArray();
            incompatArray = new JsonArray();
            itemsArray = new JsonArray();
            descArray = new JsonArray();
            enchObject.add(EnumEnchantmentsKeys.ID.key, new JsonPrimitive(wrapper.id.toString()));
            enchObject.add(EnumEnchantmentsKeys.UNLOCALIZED_NAME.key, new JsonPrimitive(wrapper.getName()));
            enchObject.add(EnumEnchantmentsKeys.ENABLED.key, new JsonPrimitive(wrapper.isEnabled()));
            enchObject.add(EnumEnchantmentsKeys.RARITY.key, new JsonPrimitive(wrapper.getRarity().toString()));
            enchObject.add(EnumEnchantmentsKeys.TREASURE.key, new JsonPrimitive(wrapper.isTreasure()));
            enchObject.add(EnumEnchantmentsKeys.DOUBLE_PRICE.key, new JsonPrimitive(wrapper.shouldDoublePrice()));
            enchObject.add(EnumEnchantmentsKeys.CURSE.key, new JsonPrimitive(wrapper.isCurse()));
            enchObject.add(EnumEnchantmentsKeys.ALLOWED_ON_BOOKS.key, new JsonPrimitive(wrapper.isAllowedOnBooks()));
            enchObject.add(EnumEnchantmentsKeys.MIN_LEVEL.key, new JsonPrimitive(wrapper.getMinLevel()));
            enchObject.add(EnumEnchantmentsKeys.MAX_LEVEL.key, new JsonPrimitive(wrapper.getMaxLevel()));
            enchObject.add(EnumEnchantmentsKeys.CUSTOM_EVALUATIONS.key, new JsonPrimitive(wrapper.useCustomEvals()));
            enchObject.add(EnumEnchantmentsKeys.MIN_ENCH_EVAL.key, new JsonPrimitive(wrapper.getMinEnchantabilityEval()));
            enchObject.add(EnumEnchantmentsKeys.MAX_ENCH_EVAL.key, new JsonPrimitive(wrapper.getMaxEnchantabilityEval()));
            enchObject.add(EnumEnchantmentsKeys.TYPE.key, new JsonPrimitive(wrapper.getType() == null ? "NONE" : wrapper.getType().toString()));
            for (EntityEquipmentSlot equipment : wrapper.getEquipmentSlots())
                equipArray.add(new JsonPrimitive(equipment.toString()));
            enchObject.add(EnumEnchantmentsKeys.EQUIPMENT_SLOTS.key, equipArray);
            enchObject.add(EnumEnchantmentsKeys.INCOMPAT_MODE.key, new JsonPrimitive(wrapper.getIncompatMode()));
            if (wrapper.hasIncompatibleEnchantments()) 
                for (ResourceLocation regName : wrapper.getIncompatibleEnchantments())
                    incompatArray.add(new JsonPrimitive(regName.toString()));        
            enchObject.add(EnumEnchantmentsKeys.INCOMPATIBLE_ENCHANTMENTS.key, incompatArray);
            enchObject.add(EnumEnchantmentsKeys.APPLICABILITY_MODE.key, new JsonPrimitive(wrapper.getApplicabilityMode()));
            enchObject.add(EnumEnchantmentsKeys.ITEMS_LIST_MODE.key, new JsonPrimitive(wrapper.getListMode()));
            if (wrapper.hasItemList()) 
                for (ResourceLocation regName : wrapper.getItemList())
                    itemsArray.add(new JsonPrimitive(regName.toString()));
            enchObject.add(EnumEnchantmentsKeys.ITEMS_LIST.key, itemsArray);
            if (wrapper.hasDescription())
                for (String line : wrapper.getDescription())
                    descArray.add(new JsonPrimitive(line));
            enchObject.add(EnumEnchantmentsKeys.DESCRIPTION.key, descArray);
            enchsArray.add(enchObject);
        }              
        try (Writer writer = new FileWriter(EXT_DATA_FILE)) {             
            gson.toJson(enchsArray, writer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void createEnchantmentsListFile(EnumEnchantmentListType enumType) {
        List<String> data = new ArrayList<String>();
        data.add("mod name - enchantment name - enchantment registry name");
        String file, modName;
        if (enumType == EnumEnchantmentListType.ALL) {
            file = LIST_ALL_FILE;
            Set<String> sortedModNames = new TreeSet<String>();
            Multimap<String, EnchantmentWrapper> wrappersByModNames = HashMultimap.<String, EnchantmentWrapper>create();
            for (EnchantmentWrapper wrapper: EnchantmentWrapper.getWrappers()) {
                modName = ECMain.MODS_NAMES.get(wrapper.modid);
                modName = modName == null ? "Undefined" : modName;
                sortedModNames.add(modName);
                wrappersByModNames.put(modName, wrapper);
            }
            StringBuilder stringBuilder;
            for (String s : sortedModNames) {
                for (EnchantmentWrapper w : wrappersByModNames.get(s)) {
                    stringBuilder = new StringBuilder()
                            .append(s)
                            .append(" - ")
                            .append(I18n.format(w.getName()))
                            .append(" - ")
                            .append(w.id.toString());
                    data.add(stringBuilder.toString());
                }
            }
        } else {
            file = LIST_UNKNOWN_FILE;
            Set<String> sortedModNames = new TreeSet<String>();
            Multimap<String, String> enchNamesByModNames = HashMultimap.<String, String>create();
            for (EnchantmentWrapper wrapper: EnchantmentWrapper.UNKNOWN) {
                modName = ECMain.MODS_NAMES.get(wrapper.modid);
                modName = modName == null ? "Undefined" : modName;
                sortedModNames.add(modName);
                enchNamesByModNames.put(modName, wrapper.getName());
            }
            StringBuilder stringBuilder;
            for (String s : sortedModNames) {
                for (String n : enchNamesByModNames.get(s)) {
                    stringBuilder = new StringBuilder()
                            .append(s)
                            .append(" - ")
                            .append(I18n.format(n));
                    data.add(stringBuilder.toString());
                }
            }
        }
        try (PrintWriter writer = new PrintWriter(file)) {      
            for (String line : data)
                writer.println(line);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
