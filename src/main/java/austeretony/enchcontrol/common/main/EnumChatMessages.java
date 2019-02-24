package austeretony.enchcontrol.common.main;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import austeretony.enchcontrol.common.commands.EnumCommandECArgs;
import austeretony.enchcontrol.common.enchantments.EnchantmentWrapper;
import austeretony.enchcontrol.common.reference.CommonReference;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public enum EnumChatMessages {

    UPDATE_MESSAGE,
    COMMAND_EC_HELP,
    COMMAND_EC_LIST_ALL,
    COMMAND_EC_FILE_ALL,
    COMMAND_EC_LIST_UNKNOWN,
    COMMAND_EC_FILE_UNKNOWN,
    COMMAND_EC_CLEAR,
    COMMAND_EC_RELOAD,
    COMMAND_EC_BACKUP,
    COMMAND_EC_UPDATE,
    COMMAND_EC_INFO,
    COMMAND_EC_EVAL,
    COMMAND_EC_ERR_NO_UNKNOWN_ENCHANTMENTS,
    COMMAND_EC_ERR_EXTERNAL_CONFIG_DISABLED, 
    COMMAND_EC_ERR_DEBUG_MODE_DISABLED,
    COMMAND_EC_ERR_WRONG_EXPRESSION,
    COMMAND_EC_ERR_RUNNING_AT_DEDICATED_SERVER;

    public static final ITextComponent PREFIX;

    static {
        PREFIX = new TextComponentString("[EC] ");
        PREFIX.getStyle().setColor(TextFormatting.AQUA);                   
    }

    private static ITextComponent prefix() {
        return PREFIX.createCopy();
    }

    private String formatVersion(String input) {
        try {  
            String[] splitted = input.split("[:]");
            return splitted[0] + " " + splitted[1] + " r-" + splitted[2];
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return input;
    }

    public void sendMessage(EntityPlayer player, String... args) {
        ITextComponent msg1, msg2, msg3, msg4, modLog, mod, nameLog, name, regNameLog, regName;
        String modName;
        switch (this) {
        case UPDATE_MESSAGE:
            msg1 = new TextComponentTranslation("ec.update.newVersion");
            msg2 = new TextComponentString(" " + this.formatVersion(ECMain.VERSION_CUSTOM) + " / " + this.formatVersion(args[0]));        
            CommonReference.sendMessage(player, prefix().appendSibling(msg1).appendSibling(msg2));
            msg1 = new TextComponentTranslation("ec.update.projectPage");
            msg2 = new TextComponentString(": ");
            msg3 = new TextComponentString(ECMain.PROJECT_LOCATION);   
            msg1.getStyle().setColor(TextFormatting.AQUA);      
            msg3.getStyle().setColor(TextFormatting.WHITE);                             
            msg3.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ECMain.PROJECT_URL));             
            CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
            break;
        case COMMAND_EC_HELP:
            CommonReference.sendMessage(player, prefix().appendSibling(new TextComponentTranslation("ec.command.help.title")));
            for (EnumCommandECArgs arg : EnumCommandECArgs.values()) {
                if (arg != EnumCommandECArgs.HELP) {
                    msg1 = new TextComponentString("/ec " + arg);
                    msg2 = new TextComponentString(" - ");
                    msg1.getStyle().setColor(TextFormatting.GREEN);  
                    msg2.getStyle().setColor(TextFormatting.WHITE); 
                    CommonReference.sendMessage(player, msg1.appendSibling(msg2.appendSibling(new TextComponentTranslation("ec.command.help." + arg))));
                }
            }
            break;
        case COMMAND_EC_LIST_ALL:
            CommonReference.sendMessage(player, prefix().appendSibling(new TextComponentTranslation("ec.command.list-all")));
            Set<String> sortedModNames = new TreeSet<String>();
            Multimap<String, EnchantmentWrapper> wrappersByModNames = HashMultimap.<String, EnchantmentWrapper>create();
            for (EnchantmentWrapper wrapper: EnchantmentWrapper.WRAPPERS.values()) {
                modName = ECMain.MODS_NAMES.get(wrapper.modid);
                modName = modName == null ? "Undefined" : modName;
                sortedModNames.add(modName);
                wrappersByModNames.put(modName, wrapper);
            }
            for (String s : sortedModNames) {
                for (EnchantmentWrapper w : wrappersByModNames.get(s)) {
                    modLog = new TextComponentString("M: ");
                    modLog.getStyle().setColor(TextFormatting.AQUA);
                    mod = new TextComponentTranslation(s);
                    mod.getStyle().setColor(TextFormatting.WHITE);
                    nameLog = new TextComponentString(", N: ");
                    nameLog.getStyle().setColor(TextFormatting.AQUA);
                    name = new TextComponentTranslation(w.getName());
                    name.getStyle().setColor(TextFormatting.WHITE);
                    regNameLog = new TextComponentString(", RN: ");
                    regName = new TextComponentString(w.id.toString());
                    regName.getStyle().setColor(TextFormatting.WHITE);
                    CommonReference.sendMessage(player, modLog.appendSibling(mod).appendSibling(nameLog).appendSibling(name).appendSibling(regNameLog).appendSibling(regName));
                }
            }
            break;
        case COMMAND_EC_FILE_ALL:
            msg1 = new TextComponentTranslation("ec.command.file-all");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_LIST_UNKNOWN:
            CommonReference.sendMessage(player, prefix().appendSibling(new TextComponentTranslation("ec.command.list-unknown")));
            Set<String> sortedModNames2 = new TreeSet<String>();
            Multimap<String, String> enchNamesByModNames = HashMultimap.<String, String>create();
            for (EnchantmentWrapper wrapper: EnchantmentWrapper.UNKNOWN) {
                modName = ECMain.MODS_NAMES.get(wrapper.modid);
                modName = modName == null ? "Undefined" : modName;
                sortedModNames2.add(modName);
                enchNamesByModNames.put(modName, wrapper.getName());
            }
            for (String s : sortedModNames2) {
                for (String n : enchNamesByModNames.get(s)) {
                    modLog = new TextComponentString("M: ");
                    modLog.getStyle().setColor(TextFormatting.AQUA);
                    mod = new TextComponentTranslation(s);
                    mod.getStyle().setColor(TextFormatting.WHITE);
                    nameLog = new TextComponentString(", N: ");
                    nameLog.getStyle().setColor(TextFormatting.AQUA);
                    name = new TextComponentTranslation(n);
                    name.getStyle().setColor(TextFormatting.WHITE);
                    CommonReference.sendMessage(player, modLog.appendSibling(mod).appendSibling(nameLog).appendSibling(name));
                }
            }
            break;
        case COMMAND_EC_FILE_UNKNOWN:
            msg1 = new TextComponentTranslation("ec.command.file-unknown");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_CLEAR:
            msg1 = new TextComponentTranslation("ec.command.clear");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_RELOAD:
            msg1 = new TextComponentTranslation("ec.command.reload");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_BACKUP:
            msg1 = new TextComponentTranslation("ec.command.backup");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_UPDATE:
            msg1 = new TextComponentTranslation("ec.command.update");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_INFO:
            ResourceLocation registryName = new ResourceLocation(args[0]);
            if (EnchantmentWrapper.WRAPPERS.containsKey(registryName)) {
                EnchantmentWrapper wrapper = EnchantmentWrapper.WRAPPERS.get(registryName);
                Enchantment ench = wrapper.getEnchantment();
                //Name, registry name
                msg1 = new TextComponentTranslation(wrapper.getName());
                msg1.getStyle().setColor(TextFormatting.WHITE);
                msg2 = new TextComponentString(" / ");
                msg2.getStyle().setColor(TextFormatting.GREEN);
                msg3 = new TextComponentString(args[0] + " ");
                msg3.getStyle().setColor(TextFormatting.WHITE);
                msg4 = new TextComponentTranslation("ec.command.info");
                msg4.getStyle().setColor(TextFormatting.GREEN);
                CommonReference.sendMessage(player, prefix().appendSibling(msg1).appendSibling(msg2).appendSibling(msg3).appendSibling(msg4));
                //Status
                msg1 = new TextComponentTranslation("ec.command.info.status");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" ");
                msg3 = new TextComponentTranslation(wrapper.isEnabled() ? "ec.command.info.status.enabled" : "ec.command.info.status.disabled");
                msg3.getStyle().setColor(wrapper.isEnabled() ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
                //Rarity
                msg1 = new TextComponentTranslation("ec.command.info.rarity");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" " + ench.getRarity().toString());
                msg2.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2));
                //Min - max levels
                msg1 = new TextComponentTranslation("ec.command.info.levels");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" " + wrapper.getMinLevel() + " - " + wrapper.getMaxLevel());
                msg2.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2));
                //Custom evaluations status
                msg1 = new TextComponentTranslation("ec.command.info.evalsStatus");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" ");
                msg3 = new TextComponentTranslation(wrapper.useCustomEvals() ? "ec.command.info.status.enabled" : "ec.command.info.status.disabled");
                msg3.getStyle().setColor(wrapper.useCustomEvals() ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
                //Evaluations (if enabled)
                if (wrapper.useCustomEvals()) {
                    msg1 = new TextComponentTranslation("ec.command.info.evals");
                    msg1.getStyle().setColor(TextFormatting.AQUA);
                    msg2 = new TextComponentString(" " + wrapper.getMinEnchantabilityEval() + " / " + wrapper.getMaxEnchantabilityEval());
                    msg2.getStyle().setColor(TextFormatting.WHITE);
                    CommonReference.sendMessage(player, msg1.appendSibling(msg2));
                }
                //Enchantability levels
                msg1 = new TextComponentTranslation("ec.command.info.enchantability");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                CommonReference.sendMessage(player, msg1);
                for (int i = wrapper.getMinLevel(); i <= wrapper.getMaxLevel(); i++) {
                    msg1 = new TextComponentString("Level " + i + ": ");
                    msg1.getStyle().setColor(TextFormatting.AQUA);
                    msg2 = new TextComponentString(wrapper.getMinEnchantability(i) + " - " + wrapper.getMaxEnchantability(i));
                    msg2.getStyle().setColor(TextFormatting.WHITE);
                    CommonReference.sendMessage(player, msg1.appendSibling(msg2));
                }
                //Type
                msg1 = new TextComponentTranslation("ec.command.info.type");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" " + ench.type.toString());
                msg2.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2));
                //Slots
                msg1 = new TextComponentTranslation("ec.command.info.slots");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                CommonReference.sendMessage(player, msg1);
                for (EntityEquipmentSlot slot : ench.applicableEquipmentTypes)
                    CommonReference.sendMessage(player, new TextComponentString(slot.toString()));
                //Incompat status
                msg1 = new TextComponentTranslation("ec.command.info.incompatStatus");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" ");
                msg3 = new TextComponentTranslation(wrapper.getIncompatMode() == 0 ? "ec.command.info.incompatStatus.both" : "ec.command.info.incompatStatus.custom");
                msg3.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
                //Incompat
                msg1 = new TextComponentTranslation("ec.command.info.incompat");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                CommonReference.sendMessage(player, msg1);
                for (ResourceLocation l : wrapper.getIncompatibleEnchantments())
                    CommonReference.sendMessage(player, new TextComponentString(l.toString()));
                if (wrapper.getIncompatibleEnchantments().isEmpty())
                    CommonReference.sendMessage(player, new TextComponentTranslation("ec.command.info.empty"));
                //Items list status
                msg1 = new TextComponentTranslation("ec.command.info.listStatus");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" ");
                msg3 = new TextComponentTranslation(wrapper.getApplicabilityMode() == 0 ? "ec.command.info.incompatStatus.both" : "ec.command.info.incompatStatus.custom");
                msg3.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
                //Items list mode
                msg1 = new TextComponentTranslation("ec.command.info.listMode");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                msg2 = new TextComponentString(" ");
                msg3 = new TextComponentTranslation(wrapper.getListMode() > 0 ? "ec.command.info.whitelist" : "ec.command.info.blacklist");
                msg3.getStyle().setColor(wrapper.getListMode() > 0 ? TextFormatting.DARK_GREEN : TextFormatting.DARK_RED);
                CommonReference.sendMessage(player, msg1.appendSibling(msg2).appendSibling(msg3));
                //Items list
                msg1 = new TextComponentTranslation("ec.command.info.itemsList");
                msg1.getStyle().setColor(TextFormatting.AQUA);
                CommonReference.sendMessage(player, msg1);
                for (ResourceLocation l : wrapper.getItems())
                    CommonReference.sendMessage(player, new TextComponentString(l.toString()));
                if (wrapper.getItems().isEmpty())
                    CommonReference.sendMessage(player, new TextComponentTranslation("ec.command.info.empty"));

            } else {
                msg1 = new TextComponentTranslation("ec.command.err.unknownEnch");
                msg1.getStyle().setColor(TextFormatting.RED);
                msg2 = new TextComponentString(" " + args[0]);
                msg2.getStyle().setColor(TextFormatting.WHITE);
                CommonReference.sendMessage(player, prefix().appendSibling(msg1).appendSibling(msg2));
            }
            break;
        case COMMAND_EC_EVAL:
            msg1 = new TextComponentTranslation("ec.command.eval");
            msg1.getStyle().setColor(TextFormatting.GREEN);
            msg2 = new TextComponentString(" " + args[0]);
            msg2.getStyle().setColor(TextFormatting.WHITE);
            msg3 = new TextComponentString(" = ");
            msg3.getStyle().setColor(TextFormatting.GREEN);
            msg4 = new TextComponentString(args[1]);
            msg4.getStyle().setColor(TextFormatting.WHITE);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1).appendSibling(msg2).appendSibling(msg3).appendSibling(msg4));
            break;
        case COMMAND_EC_ERR_NO_UNKNOWN_ENCHANTMENTS:
            msg1 = new TextComponentTranslation("ec.command.err.noUnknown");
            msg1.getStyle().setColor(TextFormatting.RED);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_ERR_EXTERNAL_CONFIG_DISABLED:
            msg1 = new TextComponentTranslation("ec.command.err.externalConfigDisabled");
            msg1.getStyle().setColor(TextFormatting.RED);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_ERR_DEBUG_MODE_DISABLED:
            msg1 = new TextComponentTranslation("ec.command.err.debugModeDisabled");
            msg1.getStyle().setColor(TextFormatting.RED);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        case COMMAND_EC_ERR_WRONG_EXPRESSION:
            msg1 = new TextComponentTranslation("ec.command.err.wrongExpression");
            msg1.getStyle().setColor(TextFormatting.RED);
            msg2 = new TextComponentString(" " + args[0]);
            msg2.getStyle().setColor(TextFormatting.WHITE);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1).appendSibling(msg2));
            break;
        case COMMAND_EC_ERR_RUNNING_AT_DEDICATED_SERVER:
            msg1 = new TextComponentTranslation("ec.command.err.runningDedicatedServer");
            msg1.getStyle().setColor(TextFormatting.RED);
            CommonReference.sendMessage(player, prefix().appendSibling(msg1));
            break;
        }
    }
}
