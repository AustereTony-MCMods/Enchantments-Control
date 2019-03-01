package austeretony.enchcontrol.common.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;

import austeretony.enchcontrol.client.reference.ClientReference;
import austeretony.enchcontrol.common.config.ConfigLoader;
import austeretony.enchcontrol.common.config.EnumConfigSettings;
import austeretony.enchcontrol.common.enchantment.EnchantmentWrapper;
import austeretony.enchcontrol.common.main.ECMain;
import austeretony.enchcontrol.common.util.ECUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandEnchant;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class ECHooks {

    //Hook to <Locale> class to <loadLocaleDataFiles()> method (calls custom localization initialization).
    public static void loadCustomLocalization(List<String> languageList, Map<String, String> properties) {
        ConfigLoader.loadCustomLocalization(languageList, properties);
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (overwrites <int j2> variable).
    public static int ceilMaxLevel(Enchantment enchantment, int level) {
        EnchantmentWrapper wrapper = EnchantmentWrapper.get(enchantment);
        int levelCap = MathHelper.clamp(wrapper.getMaxLevel(), wrapper.getMinLevel(), EnumConfigSettings.ANVIL_LEVEL_CAP.getIntValue());
        return level > levelCap ? levelCap : level;
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (overwrites <boolean flag1> variable).
    public static boolean canApply(Enchantment enchantment, ItemStack itemStack) {
        return EnchantmentWrapper.get(enchantment).canApply(itemStack);
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (replaces compatibility check at <L82>).
    public static boolean isCompatibleWith(Enchantment thisEnch, Enchantment thatEnch) {
        return EnchantmentWrapper.get(thisEnch).isCompatibleWith(thatEnch);
    }

    //Hook to <Enchantment> class to <getTranslatedName()> method (replaces whole method).
    public static String getTranslatedName(Enchantment enchantment, int level) {
        EnchantmentWrapper wrapper = EnchantmentWrapper.get(enchantment);
        String s = I18n.format(wrapper.getName());
        if (wrapper.isCurse())
            s = TextFormatting.RED + s;
        return level == 1 && wrapper.getMaxLevel() == 1 ? s : s + " " + (EnumConfigSettings.ROMAN_NUMERALS.isEnabled() ? ECUtils.toRomanNumeral(level) : level);
    }

    //Hook to <EnchantmentHelper> class to <removeIncompatible()> method (replaces whole method).
    public static void removeIncompatible(List<EnchantmentData> list, EnchantmentData enchantmentData) {
        Iterator<EnchantmentData> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (!EnchantmentWrapper.get(enchantmentData.enchantmentobj).isCompatibleWith((iterator.next()).enchantmentobj))
                iterator.remove();
        }
    }

    //Hook to <EnchantmentHelper> class to <getEnchantmentDatas()> method (replaces whole method).
    public static List<EnchantmentData> getEnchantmentDatas(int level, ItemStack itemStack, boolean allowTreasure) {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();
        Item item = itemStack.getItem();
        boolean flag = itemStack.getItem() == Items.BOOK;
        EnchantmentWrapper wrapper;
        int levelCap;
        for (Enchantment enchantment : Enchantment.REGISTRY) {
            wrapper = EnchantmentWrapper.get(enchantment);
            if (wrapper.isEnabled()) {
                if ((!wrapper.isTreasure() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(itemStack) || (flag && wrapper.isAllowedOnBooks()))) {
                    levelCap = MathHelper.clamp(wrapper.getMaxLevel(), wrapper.getMinLevel(), EnumConfigSettings.ENCHANTMENT_TABLE_LEVEL_CAP.getIntValue());
                    for (int i = levelCap; i > wrapper.getMinLevel() - 1; --i) {
                        if (level >= wrapper.getMinEnchantability(i) && level <= wrapper.getMaxEnchantability(i)) {
                            list.add(new EnchantmentData(enchantment, i));
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    public static Enchantment getRandomEnchantment(Random random) {
        Enchantment enchantment = null;
        for (int i = 0; i < Enchantment.REGISTRY.getKeys().size(); i++) {
            enchantment = (Enchantment) Enchantment.REGISTRY.getRandomObject(random);
            if (EnchantmentWrapper.get(enchantment).isEnabled())
                return enchantment;
        }
        return enchantment;//<null> should be impossible
    }

    //Hook to <EntityVillager$ListEnchantedBookForEmeralds> class to <addMerchantRecipe()> method (replaces whole method).
    public static void addMerchantRecipe(MerchantRecipeList recipeList, Random random) {
        Enchantment enchantment = getRandomEnchantment(random);
        EnchantmentWrapper wrapper = EnchantmentWrapper.get(enchantment);
        int 
        levelCap = MathHelper.clamp(wrapper.getMaxLevel(), wrapper.getMinLevel(), EnumConfigSettings.MERCHANT_DEALS_LEVEL_CAP.getIntValue()),
        i = MathHelper.getInt(random, wrapper.getMinLevel(), levelCap);
        ItemStack itemStack = Items.ENCHANTED_BOOK.getEnchantedItemStack(new EnchantmentData(enchantment, i));
        int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
        if (wrapper.shouldDoublePrice())
            j *= 2;
        if (j > 64)
            j = 64;
        recipeList.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, j), itemStack));
    }

    //Hook to <EnchantRandomly> class to <apply()> method (replaces whole method).
    public static ItemStack apply(ItemStack itemStack, Random rand, List<Enchantment> enchantments, Logger logger) {
        Enchantment enchantment;
        EnchantmentWrapper wrapper;
        if (enchantments.isEmpty()) {
            List<Enchantment> list = Lists.<Enchantment>newArrayList();
            for (Enchantment enchantment1 : Enchantment.REGISTRY) {
                wrapper = EnchantmentWrapper.get(enchantment1);
                if (wrapper.isEnabled())
                    if (itemStack.getItem() == Items.BOOK || wrapper.canApply(itemStack))
                        list.add(enchantment1);
            }
            if (list.isEmpty()) {
                logger.warn("Couldn't find a compatible enchantment for {}", itemStack);
                return itemStack;
            }
            enchantment = list.get(rand.nextInt(list.size()));
        } else {
            enchantment = enchantments.get(rand.nextInt(enchantments.size()));
        }
        wrapper = EnchantmentWrapper.get(enchantment);
        int 
        levelCap = MathHelper.clamp(wrapper.getMaxLevel(), wrapper.getMinLevel(), EnumConfigSettings.DUNGEON_LOOT_LEVEL_CAP.getIntValue()),
        i = MathHelper.getInt(rand, wrapper.getMinLevel(), levelCap);
        if (itemStack.getItem() == Items.BOOK) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            Items.ENCHANTED_BOOK.addEnchantment(itemStack, new EnchantmentData(enchantment, i));
        } else {
            itemStack.addEnchantment(enchantment, i);
        }
        return itemStack;
    }

    //Hook to <CommandEnchant> class to <execute()> method (replaces actual enchant logic).
    public static void execute(CommandEnchant command, EntityLivingBase livingBase, Enchantment enchantment, ICommandSender sender, String... args) throws CommandException {
        EnchantmentWrapper wrapper = EnchantmentWrapper.get(enchantment);
        int i = 1;
        ItemStack itemStack = livingBase.getHeldItemMainhand();
        if (itemStack.isEmpty()) {
            throw new CommandException("commands.enchant.noItem");
        } else if (!wrapper.canApply(itemStack)) {
            throw new CommandException("commands.enchant.cantEnchant");
        } else {
            if (args.length >= 3)
                i = CommandBase.parseInt(args[2], wrapper.getMinLevel(), wrapper.getMaxLevel());
            if (itemStack.hasTagCompound()) {
                NBTTagList nbttaglist = itemStack.getEnchantmentTagList();
                int k;
                for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                    k = nbttaglist.getCompoundTagAt(j).getShort("id");
                    if (Enchantment.getEnchantmentByID(k) != null) {
                        Enchantment enchantment1 = Enchantment.getEnchantmentByID(k);
                        if (!wrapper.isCompatibleWith(enchantment1))
                            throw new CommandException("commands.enchant.cantCombine", enchantment.getTranslatedName(i), enchantment1.getTranslatedName(nbttaglist.getCompoundTagAt(j).getShort("lvl")));

                    }
                }
            }
            itemStack.addEnchantment(enchantment, i);
            CommandBase.notifyCommandListener(sender, command, "commands.enchant.success");
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
        }
    }

    //TODO
    //Hook to <ItemEnchantedBook> class to <getAll()> method (replaces whole method).
    public static void getAll(Enchantment enchantment, List<ItemStack> items) {
        EnchantmentWrapper wrapper = EnchantmentWrapper.get(enchantment);
        int levelCap = MathHelper.clamp(wrapper.getMaxLevel(), wrapper.getMinLevel(), EnumConfigSettings.CREATIVE_TAB_LEVEL_CAP.getIntValue());
        for (int i = wrapper.getMinLevel(); i <= levelCap; ++i)
            items.add(Items.ENCHANTED_BOOK.getEnchantedItemStack(new EnchantmentData(enchantment, i)));
    }

    //Hook to <CreativeTabs> class to <addEnchantmentBooksToList()> method (replaces max level getter).
    public static int getMaxLevel(Enchantment enchantment) {
        return EnchantmentWrapper.get(enchantment).getMaxLevel();
    }

    //Hook to <ItemStack> class to <getTooltip()> method (replaces enchantment tooltip).
    public static void modifyItemStackTooltip(int flag, ItemStack itemStack, List<String> tooltip, boolean enchantedBook) {
        if ((flag & 1) == 0 || enchantedBook) {
            NBTTagList tagList = enchantedBook ? Items.ENCHANTED_BOOK.getEnchantments(itemStack) : itemStack.getEnchantmentTagList();
            int k, l;
            Enchantment enchantment;
            EnchantmentWrapper wrapper;
            for (int j = 0; j < tagList.tagCount(); ++j) {
                NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(j);
                k = nbttagcompound.getShort("id");
                l = nbttagcompound.getShort("lvl");
                enchantment = Enchantment.getEnchantmentByID(k);
                wrapper = EnchantmentWrapper.get(enchantment);
                if (enchantment != null) {
                    wrapper = EnchantmentWrapper.get(enchantment);
                    boolean curseMarked = false;
                    if (!(!enchantedBook && wrapper.shouldHideOnItem()) 
                            && !(enchantedBook && wrapper.shouldHideOnBook())) { 
                        if (!(wrapper.isCurse() && EnumConfigSettings.HIDE_CURSES.isEnabled())) {
                            if (EnumConfigSettings.DESCRIPTIONS.isEnabled() 
                                    && EnumConfigSettings.DESCRIPTIONS_LOCATION.getIntValue() == 0 
                                    && ((!enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_ITEMS.isEnabled()) || (enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_BOOKS.isEnabled()))) {
                                if (ClientReference.getGameSettings().isKeyDown(ClientReference.getGameSettings().keyBindSneak)) {
                                    if (j > 0 && !EnumConfigSettings.DESCRIPTIONS_SEPARATOR.getStrValue().isEmpty())
                                        tooltip.add(EnumConfigSettings.DESCRIPTIONS_SEPARATOR.getStrValue());
                                    tooltip.add(enchantment.getTranslatedName(l));
                                    if (wrapper.hasDescription()) {
                                        for (String s : wrapper.getDescription()) 
                                            tooltip.add(" " + ChatFormatting.ITALIC + I18n.format(s));
                                    } else
                                        tooltip.add(" " + ChatFormatting.ITALIC + I18n.format("ec.tooltip.noDesc"));
                                    if (EnumConfigSettings.DESCRIPTIONS_SHOW_DOMAIN.isEnabled())
                                        tooltip.add(" " + I18n.format("ec.tooltip.addedBy") + ": " + ChatFormatting.AQUA + ECMain.MODS_NAMES.get(wrapper.modid));
                                } else
                                    tooltip.add(enchantment.getTranslatedName(l));
                            } else
                                tooltip.add(enchantment.getTranslatedName(l));
                        } else
                            if (!curseMarked) {
                                curseMarked = true;
                                if (EnumConfigSettings.NOTIFY_CURSED.isEnabled())
                                    tooltip.add(ChatFormatting.RED + I18n.format("ec.tooltip.cursed"));
                            }
                    }
                }
            }
        }
    }
}
