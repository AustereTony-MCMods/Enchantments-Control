package austeretony.enchcontrol.common.core;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

import austeretony.enchcontrol.common.config.ConfigLoader;
import austeretony.enchcontrol.common.enchantments.EnchantmentWrapper;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandEnchant;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class ECHooks {

    //Hook to <Locale> class to <loadLocaleDataFiles()> method (calls custom localization initialization).
    public static void loadCustomLocalization(List<String> languageList, Map<String, String> properties) {
        ConfigLoader.loadCustomLocalization(languageList, properties);
    }

    public static int getMinLevel(Enchantment enchantment) {
        return EnchantmentWrapper.get(enchantment).getMinLevel();
    }

    public static int getMaxLevel(Enchantment enchantment) {
        return EnchantmentWrapper.get(enchantment).getMaxLevel();
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (overwrites <int j2> variable).
    public static int ceilMaxLevel(Enchantment enchantment, int level) {
        return level > getMaxLevel(enchantment) ? getMaxLevel(enchantment) : level;
    }

    public static int getMinEnchantability(Enchantment enchantment, int enchantmentLevel) {
        return EnchantmentWrapper.get(enchantment).getMinEnchantability(enchantmentLevel);
    }

    public static int getMaxEnchantability(Enchantment enchantment, int enchantmentLevel) {
        return EnchantmentWrapper.get(enchantment).getMaxEnchantability(enchantmentLevel);
    }

    //Hook to <Enchantment> class to <getTranslatedName()> method (overwrites <String s> variable).
    public static String getEnchantmentName(Enchantment enchantment) {
        return I18n.format(EnchantmentWrapper.get(enchantment).getName());
    }

    //Hook to <EnchantmentHelper> class to <getEnchantmentDatas()> method (replaces whole method).
    public static List<EnchantmentData> getEnchantmentDatas(int level, ItemStack itemStack, boolean allowTreasure) {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();
        Item item = itemStack.getItem();
        boolean flag = itemStack.getItem() == Items.BOOK;
        for (Enchantment enchantment : Enchantment.REGISTRY) {
            if (!EnchantmentWrapper.get(enchantment).isEnabled()) continue;
            if ((!enchantment.isTreasureEnchantment() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(itemStack) || (flag && enchantment.isAllowedOnBooks()))) {
                for (int i = getMaxLevel(enchantment); i > getMinLevel(enchantment) - 1; --i) {
                    if (level >= getMinEnchantability(enchantment, i) && level <= getMaxEnchantability(enchantment, i)) {
                        list.add(new EnchantmentData(enchantment, i));
                        break;
                    }
                }
            }
        }
        return list;
    }

    //Hook to <EntityVillager$ListEnchantedBookForEmeralds> class to <addMerchantRecipe()> method (overwrites <Enchantment enchantment> variable).
    public static Enchantment getRandomEnchantment(Random random) {
        Enchantment enchantment = null;
        for (int i = 0; i < Enchantment.REGISTRY.getKeys().size(); i++) {
            enchantment = (Enchantment) Enchantment.REGISTRY.getRandomObject(random);
            if (EnchantmentWrapper.get(enchantment).isEnabled())
                return enchantment;
        }
        return enchantment;//<null> should be impossible
    }

    //Hook to <EntityVillager$ListEnchantedBookForEmeralds> class to <addMerchantRecipe()> method (overwrites <int i> variable).
    public static int randomizeEnchantmentLevel(Enchantment enchantment, Random random) {
        return MathHelper.getInt(random, getMinLevel(enchantment), getMaxLevel(enchantment));
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (overwrites <boolean flag1> variable).
    public static boolean canApply(Enchantment enchantment, ItemStack itemStack) {
        return EnchantmentWrapper.get(enchantment).canApply(itemStack);
    }

    //Hook to <EnchantRandomly> class to <apply()> method (replaces whole method).
    public static ItemStack apply(ItemStack itemStack, Random rand, List<Enchantment> enchantments, Logger logger) {
        Enchantment enchantment;
        if (enchantments.isEmpty()) {
            List<Enchantment> list = Lists.<Enchantment>newArrayList();
            for (Enchantment enchantment1 : Enchantment.REGISTRY) {
                if (!EnchantmentWrapper.get(enchantment1).isEnabled()) continue;
                if (itemStack.getItem() == Items.BOOK || canApply(enchantment1, itemStack))
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
        int i = randomizeEnchantmentLevel(enchantment, rand);
        if (itemStack.getItem() == Items.BOOK) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantedBook.addEnchantment(itemStack, new EnchantmentData(enchantment, i));
        } else {
            itemStack.addEnchantment(enchantment, i);
        }
        return itemStack;
    }

    //Hook to <ContainerRepair> class to <updateRepairOutput()> method (replaces compatibility check at <L82>).
    public static boolean isCompatibleWith(Enchantment thisEnch, Enchantment thatEnch) {
        return EnchantmentWrapper.get(thisEnch).isCompatibleWith(thatEnch);
    }

    //Hook to <CommandEnchant> class to <execute()> method (replaces actual enchant logic).
    public static void execute(CommandEnchant command, EntityLivingBase livingBase, Enchantment enchantment, ICommandSender sender, String... args) throws CommandException {
        int i = 1;
        ItemStack itemStack = livingBase.getHeldItemMainhand();
        if (itemStack.isEmpty()) {
            throw new CommandException("commands.enchant.noItem");
        } else if (!canApply(enchantment, itemStack)) {
            throw new CommandException("commands.enchant.cantEnchant");
        } else {
            if (args.length >= 3)
                i = CommandBase.parseInt(args[2], getMinLevel(enchantment), getMaxLevel(enchantment));
            if (itemStack.hasTagCompound()) {
                NBTTagList nbttaglist = itemStack.getEnchantmentTagList();
                int k;
                for (int j = 0; j < nbttaglist.tagCount(); ++j) {
                    k = nbttaglist.getCompoundTagAt(j).getShort("id");
                    if (Enchantment.getEnchantmentByID(k) != null) {
                        Enchantment enchantment1 = Enchantment.getEnchantmentByID(k);
                        if (!isCompatibleWith(enchantment, enchantment1))
                            throw new CommandException("commands.enchant.cantCombine", enchantment.getTranslatedName(i), enchantment1.getTranslatedName(nbttaglist.getCompoundTagAt(j).getShort("lvl")));

                    }
                }
            }
            itemStack.addEnchantment(enchantment, i);
            CommandBase.notifyCommandListener(sender, command, "commands.enchant.success");
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
        }
    }

    //Hook to <ItemEnchantedBook> class to <getSubItems()> method (replaces whole method).
    public static void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH) {
            for (Enchantment enchantment : Enchantment.REGISTRY)
                if (enchantment.type != null)
                    for (int i = getMinLevel(enchantment); i <= getMaxLevel(enchantment); ++i)
                        items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, i)));
        } else if (tab.getRelevantEnchantmentTypes().length != 0) {
            for (Enchantment enchantment1 : Enchantment.REGISTRY)
                if (tab.hasRelevantEnchantmentType(enchantment1.type))
                    items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment1, getMaxLevel(enchantment1))));
        }
    }
}
