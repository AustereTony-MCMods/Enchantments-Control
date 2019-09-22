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

import austeretony.enchcontrol.common.enchantment.EnchantmentWrapper;
import austeretony.enchcontrol.common.main.ECMain;
import austeretony.enchcontrol.common.reference.CommonReference;
import austeretony.enchcontrol.common.util.ECUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

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

    private static Map<String, String> localization;

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
            if (versionElement == null || isOutdated(versionElement.getAsString(), ECMain.VERSION_CUSTOM)) {
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
        JsonArray slotsArray, descArray;
        EnchantmentWrapper wrapper;
        EntityEquipmentSlot[] equipmentSlots;
        int i = 0;
        for (JsonElement enchElement : settingsFile) {
            enchObject = enchElement.getAsJsonObject();
            wrapper = EnchantmentWrapper.create(
                    new ResourceLocation(enchObject.get(EnumEnchantmentsKey.ID.key).getAsString()), 
                    enchObject.get(EnumEnchantmentsKey.ENABLED.key).getAsBoolean(),
                    enchObject.get(EnumEnchantmentsKey.UNLOCALIZED_NAME.key).getAsString(),
                    enchObject.get(EnumEnchantmentsKey.MIN_LEVEL.key).getAsInt(),
                    enchObject.get(EnumEnchantmentsKey.MAX_LEVEL.key).getAsInt());
            wrapper.setHideOnItem(enchObject.get(EnumEnchantmentsKey.HIDE_ON_ITEM.key).getAsBoolean());
            wrapper.setHideOnBook(enchObject.get(EnumEnchantmentsKey.HIDE_ON_BOOK.key).getAsBoolean());
            slotsArray = enchObject.get(EnumEnchantmentsKey.EQUIPMENT_SLOTS.key).getAsJsonArray();
            wrapper.initEnumEquipmentSlotsStrings(slotsArray.size());
            i = 0;
            for (JsonElement e : slotsArray)
                wrapper.getEnumEquipmentSlotsStrings()[i++] = e.getAsString();
            wrapper.setEnumRarityString(enchObject.get(EnumEnchantmentsKey.RARITY.key).getAsString());
            wrapper.setEnumTypeString(enchObject.get(EnumEnchantmentsKey.TYPE.key).getAsString());
            wrapper.setTreasure(enchObject.get(EnumEnchantmentsKey.TREASURE.key).getAsBoolean());
            wrapper.setDoublePrice(enchObject.get(EnumEnchantmentsKey.DOUBLE_PRICE.key).getAsBoolean());
            wrapper.setCurse(enchObject.get(EnumEnchantmentsKey.CURSE.key).getAsBoolean());
            wrapper.setAllowedOnBooks(enchObject.get(EnumEnchantmentsKey.ALLOWED_ON_BOOKS.key).getAsBoolean());
            wrapper.setCustomEvals(enchObject.get(EnumEnchantmentsKey.CUSTOM_EVALUATIONS.key).getAsBoolean());
            wrapper.setMinEnchantabilityEvaluation(enchObject.get(EnumEnchantmentsKey.MIN_ENCH_EVAL.key).getAsString());
            wrapper.setMaxEnchantabilityEvaluation(enchObject.get(EnumEnchantmentsKey.MAX_ENCH_EVAL.key).getAsString());
            wrapper.setIncompatMode(enchObject.get(EnumEnchantmentsKey.INCOMPAT_MODE.key).getAsInt());
            for (JsonElement incompatElement : enchObject.get(EnumEnchantmentsKey.INCOMPATIBLE_ENCHANTMENTS.key).getAsJsonArray())
                wrapper.addIncompatibleEnchantment(new ResourceLocation(incompatElement.getAsString()));
            wrapper.setApplicabilityMode(enchObject.get(EnumEnchantmentsKey.APPLICABILITY_MODE.key).getAsInt());
            wrapper.setListMode(enchObject.get(EnumEnchantmentsKey.ITEMS_LIST_MODE.key).getAsInt());
            for (JsonElement incompatElement : enchObject.get(EnumEnchantmentsKey.ITEMS_LIST.key).getAsJsonArray())
                wrapper.addItem(new ResourceLocation(incompatElement.getAsString()));
            if ((descArray = enchObject.get(EnumEnchantmentsKey.DESCRIPTION.key).getAsJsonArray()).size() > 0) {
                wrapper.initDescription(descArray.size());
                i = 0;
                for (JsonElement lineElement : descArray)
                    wrapper.getDescription()[i++] = lineElement.getAsString();
            }
        }
    }

    public static void loadCustomLocalization(List<String> languageList, Map<String, String> properties) {
        localization = properties;
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

    public static void processEnchantmentDescriptionsSupport() {
        boolean 
        edLoaded = Loader.isModLoaded("enchdesc"),
        loadDesc = EnumConfigSettings.DESCRIPTIONS.isEnabled() && EnumConfigSettings.LOAD_ED_DESC.isEnabled(),
        uploadDesc = !EnumConfigSettings.DESCRIPTIONS.isEnabled() && EnumConfigSettings.UPLOAD_DESC_TO_ED.isEnabled();
        if (loadDesc || uploadDesc) {
            String formattedKey;
            for (EnchantmentWrapper wrapper : EnchantmentWrapper.getWrappers()) {
                formattedKey =  "enchantment." + wrapper.modid + "." + wrapper.resourceName + ".desc";
                if (loadDesc && !wrapper.hasDescription() && localization.containsKey(formattedKey)) {
                    wrapper.initDescription(1);
                    wrapper.getDescription()[0] = formattedKey;
                    wrapper.setTemporaryDescription();
                }
                if (uploadDesc && edLoaded && wrapper.hasDescription() && !localization.containsKey(formattedKey))
                    localization.put(formattedKey, I18n.format(wrapper.getDescription()[0]));
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
                    ECUtils.getExternalJsonData(EXT_DATA_FILE));         
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
            enchObject.add(EnumEnchantmentsKey.ID.key, new JsonPrimitive(wrapper.registryName.toString()));
            enchObject.add(EnumEnchantmentsKey.UNLOCALIZED_NAME.key, new JsonPrimitive(wrapper.getName()));
            enchObject.add(EnumEnchantmentsKey.ENABLED.key, new JsonPrimitive(wrapper.isEnabled()));
            enchObject.add(EnumEnchantmentsKey.HIDE_ON_ITEM.key, new JsonPrimitive(wrapper.shouldHideOnItem()));
            enchObject.add(EnumEnchantmentsKey.HIDE_ON_BOOK.key, new JsonPrimitive(wrapper.shouldHideOnBook()));
            enchObject.add(EnumEnchantmentsKey.RARITY.key, new JsonPrimitive(wrapper.getRarity().toString()));
            enchObject.add(EnumEnchantmentsKey.TREASURE.key, new JsonPrimitive(wrapper.isTreasure()));
            enchObject.add(EnumEnchantmentsKey.DOUBLE_PRICE.key, new JsonPrimitive(wrapper.shouldDoublePrice()));
            enchObject.add(EnumEnchantmentsKey.CURSE.key, new JsonPrimitive(wrapper.isCurse()));
            enchObject.add(EnumEnchantmentsKey.ALLOWED_ON_BOOKS.key, new JsonPrimitive(wrapper.isAllowedOnBooks()));
            enchObject.add(EnumEnchantmentsKey.MIN_LEVEL.key, new JsonPrimitive(wrapper.getMinLevel()));
            enchObject.add(EnumEnchantmentsKey.MAX_LEVEL.key, new JsonPrimitive(wrapper.getMaxLevel()));
            enchObject.add(EnumEnchantmentsKey.CUSTOM_EVALUATIONS.key, new JsonPrimitive(wrapper.useCustomEvals()));
            enchObject.add(EnumEnchantmentsKey.MIN_ENCH_EVAL.key, new JsonPrimitive(wrapper.getMinEnchantabilityEval()));
            enchObject.add(EnumEnchantmentsKey.MAX_ENCH_EVAL.key, new JsonPrimitive(wrapper.getMaxEnchantabilityEval()));
            enchObject.add(EnumEnchantmentsKey.TYPE.key, new JsonPrimitive(wrapper.getType() == null ? "NULL" : wrapper.getType().toString()));
            for (EntityEquipmentSlot equipment : wrapper.getEquipmentSlots())
                equipArray.add(new JsonPrimitive(equipment.toString()));
            enchObject.add(EnumEnchantmentsKey.EQUIPMENT_SLOTS.key, equipArray);
            enchObject.add(EnumEnchantmentsKey.INCOMPAT_MODE.key, new JsonPrimitive(wrapper.getIncompatMode()));
            if (wrapper.hasIncompatibleEnchantments()) 
                for (ResourceLocation regName : wrapper.getIncompatibleEnchantments())
                    incompatArray.add(new JsonPrimitive(regName.toString()));        
            enchObject.add(EnumEnchantmentsKey.INCOMPATIBLE_ENCHANTMENTS.key, incompatArray);
            enchObject.add(EnumEnchantmentsKey.APPLICABILITY_MODE.key, new JsonPrimitive(wrapper.getApplicabilityMode()));
            enchObject.add(EnumEnchantmentsKey.ITEMS_LIST_MODE.key, new JsonPrimitive(wrapper.getListMode()));
            if (wrapper.hasItemList()) 
                for (ResourceLocation regName : wrapper.getItemList())
                    itemsArray.add(new JsonPrimitive(regName.toString()));
            enchObject.add(EnumEnchantmentsKey.ITEMS_LIST.key, itemsArray);
            if (wrapper.hasDescription() && !wrapper.isTemporaryDescription())
                for (String line : wrapper.getDescription())
                    descArray.add(new JsonPrimitive(line));
            enchObject.add(EnumEnchantmentsKey.DESCRIPTION.key, descArray);
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
        String modName; 
        Set<String> sortedModNames = new TreeSet<String>();
        Multimap<String, EnchantmentWrapper> wrappersByModNames = HashMultimap.<String, EnchantmentWrapper>create();
        for (EnchantmentWrapper wrapper : enumType == EnumEnchantmentListType.ALL ? EnchantmentWrapper.getWrappers() : EnchantmentWrapper.UNKNOWN) {
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
                        .append(w.registryName.toString());
                data.add(stringBuilder.toString());
            }
        }
        try (PrintWriter writer = new PrintWriter(enumType == EnumEnchantmentListType.ALL ? LIST_ALL_FILE : LIST_UNKNOWN_FILE)) {      
            for (String line : data)
                writer.println(line);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static boolean isOutdated(String currentVersion, String availableVersion) {        
        try {
            String[] 
                    cSplitted = currentVersion.split("[:]"),
                    aSplitted = availableVersion.split("[:]");    
            String 
            cVer = cSplitted[0],
            cType = cSplitted[1],
            cRev = cSplitted[2],
            aVer = aSplitted[0],
            aType = aSplitted[1],
            aRev = aSplitted[2];
            String[]
                    cVerSplitted = cVer.split("[.]"),
                    aVerSplitted = aVer.split("[.]");
            int verDiff, revDiff;               
            for (int i = 0; i < 3; i++) {                                                             
                verDiff = Integer.parseInt(aVerSplitted[i]) - Integer.parseInt(cVerSplitted[i]);                                                                                           
                if (verDiff > 0)
                    return true;                                
                if (verDiff < 0)
                    return false;
            }  
            if (aType.equals("release") && (cType.equals("beta") || cType.equals("alpha")))
                return true;
            if (aType.equals("beta") && cType.equals("alpha"))
                return true;
            revDiff = Integer.parseInt(aRev) - Integer.parseInt(cRev);                                                                                           
            if (revDiff > 0)
                return true;                                
            if (revDiff < 0)
                return false;
            return false;
        } catch (Exception exception) { 
            ECMain.LOGGER.error("Versions comparison failed!");               
            exception.printStackTrace();
        }
        return true;
    }
}
