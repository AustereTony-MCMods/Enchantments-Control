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
        return level > wrapper.getMaxLevel() ? wrapper.getMaxLevel() : level;
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
        return level == 1 && wrapper.getMaxLevel() == 1 ? s : s + " " + I18n.format("enchantment.level." + level);
    }

    //Hook to <EnchantmentHelper> class to <getEnchantmentDatas()> method (replaces whole method).
    public static List<EnchantmentData> getEnchantmentDatas(int level, ItemStack itemStack, boolean allowTreasure) {
        List<EnchantmentData> list = Lists.<EnchantmentData>newArrayList();
        Item item = itemStack.getItem();
        boolean flag = itemStack.getItem() == Items.BOOK;
        EnchantmentWrapper wrapper;
        for (Enchantment enchantment : Enchantment.REGISTRY) {
            wrapper = EnchantmentWrapper.get(enchantment);
            if (wrapper.isEnabled()) {
                if ((!wrapper.isTreasure() || allowTreasure) && (enchantment.canApplyAtEnchantingTable(itemStack) || (flag && wrapper.isAllowedOnBooks()))) {
                    for (int i = wrapper.getMaxLevel(); i > wrapper.getMinLevel() - 1; --i) {
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
        int i = MathHelper.getInt(random, wrapper.getMinLevel(), wrapper.getMaxLevel());
        ItemStack itemstack = ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, i));
        int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
        if (wrapper.shouldDoublePrice())
            j *= 2;
        if (j > 64)
            j = 64;
        recipeList.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, j), itemstack));
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
        int i = MathHelper.getInt(rand, wrapper.getMinLevel(), wrapper.getMaxLevel());
        if (itemStack.getItem() == Items.BOOK) {
            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantedBook.addEnchantment(itemStack, new EnchantmentData(enchantment, i));
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

    //Hook to <ItemEnchantedBook> class to <getSubItems()> method (replaces whole method).
    public static void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == CreativeTabs.SEARCH) {
            EnchantmentWrapper wrapper;
            for (Enchantment enchantment : Enchantment.REGISTRY) {
                wrapper = EnchantmentWrapper.get(enchantment);
                if (enchantment.type != null)
                    for (int i = wrapper.getMinLevel(); i <= wrapper.getMaxLevel(); ++i)
                        items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment, i)));
            }
        } else if (tab.getRelevantEnchantmentTypes().length != 0) {
            for (Enchantment enchantment1 : Enchantment.REGISTRY)
                if (tab.hasRelevantEnchantmentType(enchantment1.type))
                    items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(enchantment1, EnchantmentWrapper.get(enchantment1).getMaxLevel())));
        }
    }
}
