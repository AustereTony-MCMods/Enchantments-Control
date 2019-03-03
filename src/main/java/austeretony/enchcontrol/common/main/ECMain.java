package austeretony.enchcontrol.common.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.enchcontrol.client.event.EnchantmentDescriptionRenderer;
import austeretony.enchcontrol.common.command.CommandEC;
import austeretony.enchcontrol.common.config.ConfigLoader;
import austeretony.enchcontrol.common.config.EnumConfigSettings;
import austeretony.enchcontrol.common.enchantment.EnchantmentWrapper;
import austeretony.enchcontrol.common.reference.CommonReference;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = ECMain.MODID, 
        name = ECMain.NAME, 
        version = ECMain.VERSION,
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = ECMain.VERSIONS_FORGE_URL)
public class ECMain {

    public static final String 
    MODID = "enchcontrol", 
    NAME = "Enchantments Control", 
    VERSION = "1.1.4", 
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.9.4",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Enchantments-Control/info/mod_versions_forge.json",
    VERSIONS_CUSTOM_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Enchantments-Control/info/mod_versions_custom.json",
    PROJECT_LOCATION = "minecraft.curseforge.com",
    PROJECT_URL = "https://minecraft.curseforge.com/projects/enchantments-control";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final Map<String, String> MODS_NAMES = new HashMap<String, String>();

    static {
        ConfigLoader.load();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (EnumConfigSettings.CHECK_UPDATES.isEnabled())
            CommonReference.registerEvent(new UpdateChecker());
        if (event.getSide() == Side.CLIENT) 
            CommonReference.registerEvent(new EnchantmentDescriptionRenderer());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        this.collectModNames();
        this.removeUnloadedModdedEnchantments();
        if (event.getSide() == Side.CLIENT) 
            ConfigLoader.processEnchantmentDescriptionsSupport();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {  
        if (event.getSide() == Side.SERVER)
            ConfigLoader.runningAtDedicatedServer = true;
        CommonReference.registerCommand(event, new CommandEC());
        if (EnumConfigSettings.CHECK_UPDATES.isEnabled())
            new Thread(new UpdateChecker(), "Enchantments Control Update Check").start();  
    }

    private void collectModNames() {
        MODS_NAMES.put("minecraft", "Minecraft");
        for (ModContainer mod : Loader.instance().getModList())
            MODS_NAMES.put(mod.getModId(), mod.getName());
    }

    private void removeUnloadedModdedEnchantments() {
        Iterator<EnchantmentWrapper> iterator = EnchantmentWrapper.getWrappers().iterator();
        EnchantmentWrapper wrapper;
        while (iterator.hasNext()) {
            wrapper = iterator.next();
            if (!MODS_NAMES.containsKey(wrapper.modid))
                iterator.remove();
        }
    }
}
