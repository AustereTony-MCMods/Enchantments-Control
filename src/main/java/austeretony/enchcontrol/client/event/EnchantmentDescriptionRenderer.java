package austeretony.enchcontrol.client.event;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import austeretony.enchcontrol.client.reference.ClientReference;
import austeretony.enchcontrol.common.config.EnumConfigSettings;
import austeretony.enchcontrol.common.enchantment.EnchantmentWrapper;
import austeretony.enchcontrol.common.main.ECMain;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnchantmentDescriptionRenderer {

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        boolean enchantedBook = event.getItemStack().getItem() == Items.ENCHANTED_BOOK;
        if (!event.getItemStack().isEmpty() 
                && (enchantedBook || event.getItemStack().isItemEnchanted()) 
                && EnumConfigSettings.DESCRIPTIONS.isEnabled()) {
            if (EnumConfigSettings.DESCRIPTIONS_LOCATION.getIntValue() == 1) {
                List<String> tooltip = event.getToolTip();
                List<Enchantment> enchantments = getEnchantments(event.getItemStack());
                if (!enchantments.isEmpty()) {
                    if (ClientReference.getGameSettings().isKeyDown(ClientReference.getGameSettings().keyBindSneak)) {
                        EnchantmentWrapper wrapper;
                        int i = 0;
                        boolean curseMarked = false;
                        for (Enchantment enchantment : enchantments) {
                            wrapper = EnchantmentWrapper.get(enchantment);
                            if (!(!enchantedBook && wrapper.shouldHideOnItem()) 
                                    && !(enchantedBook && wrapper.shouldHideOnBook())) {
                                if (!(wrapper.isCurse() && EnumConfigSettings.HIDE_CURSES.isEnabled())) {
                                    if ((enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_BOOKS.isEnabled()) || (!enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_ITEMS.isEnabled())) {
                                        if (i > 0 && !EnumConfigSettings.DESCRIPTIONS_SEPARATOR.getStrValue().isEmpty())
                                            tooltip.add(EnumConfigSettings.DESCRIPTIONS_SEPARATOR.getStrValue());
                                        tooltip.add(I18n.format(wrapper.getName()));
                                        if (wrapper.hasDescription()) {
                                            for (String s : wrapper.getDescription()) 
                                                tooltip.add(" " + ChatFormatting.ITALIC + I18n.format(s));
                                        } else
                                            tooltip.add(" " + ChatFormatting.ITALIC + I18n.format("ec.tooltip.noDesc"));
                                        if (EnumConfigSettings.DESCRIPTIONS_SHOW_DOMAIN.isEnabled())
                                            tooltip.add(" " + I18n.format("ec.tooltip.addedBy") + ": " + ChatFormatting.AQUA + ECMain.MODS_NAMES.get(wrapper.modid));
                                    }
                                }
                                i++;
                            }
                        }
                    } else
                        if (EnumConfigSettings.DESCRIPTIONS_HINT.isEnabled()
                                && ((enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_BOOKS.isEnabled()) || (!enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_ITEMS.isEnabled())) 
                                && !ClientReference.getGameSettings().isKeyDown(ClientReference.getGameSettings().keyBindSneak))
                            tooltip.add(I18n.format("ec.tooltip.holdKey", ClientReference.getGameSettings().keyBindSneak.getDisplayName()));
                }
            } else 
                if (EnumConfigSettings.DESCRIPTIONS_HINT.isEnabled()
                        && ((enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_BOOKS.isEnabled()) || (!enchantedBook && EnumConfigSettings.DESCRIPTIONS_FOR_ITEMS.isEnabled())) 
                        && !ClientReference.getGameSettings().isKeyDown(ClientReference.getGameSettings().keyBindSneak))
                    event.getToolTip().add(I18n.format("ec.tooltip.holdKey", ClientReference.getGameSettings().keyBindSneak.getDisplayName()));
        }
    }

    public static List<Enchantment> getEnchantments(ItemStack itemStack) {
        List<Enchantment> enchantments = new ArrayList<Enchantment>();
        NBTTagList nbttaglist = itemStack.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(itemStack) : itemStack.getEnchantmentTagList();
        NBTTagCompound tagCompound;
        for (int i = 0; i < nbttaglist.tagCount(); ++i)
            enchantments.add(Enchantment.getEnchantmentByID(nbttaglist.getCompoundTagAt(i).getShort("id")));
        return enchantments;
    }
}
