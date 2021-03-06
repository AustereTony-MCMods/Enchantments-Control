package austeretony.enchcontrol.common.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ECClassTransformer implements IClassTransformer {

    public static final Logger CORE_LOGGER = LogManager.getLogger("EC Core Plugin");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        switch (transformedName) {
        case "net.minecraft.client.resources.Locale":                    
            return patch(basicClass, EnumInputClass.MC_LOCALE);
        case "net.minecraft.enchantment.Enchantment":
            return patch(basicClass, EnumInputClass.MC_ENCHANTMENT);
        case "net.minecraft.enchantment.EnchantmentHelper":
            return patch(basicClass, EnumInputClass.MC_ENCHANTMENT_HELPER);
        case "net.minecraft.entity.passive.EntityVillager$ListEnchantedBookForEmeralds":
            return patch(basicClass, EnumInputClass.MC_LIST_ENCHANTED_BOOK_FOR_EMERALDS);
        case "net.minecraft.world.storage.loot.functions.EnchantRandomly":
            return patch(basicClass, EnumInputClass.MC_ENCHANT_RANDOMLY);
        case "net.minecraft.command.CommandEnchant":
            return patch(basicClass, EnumInputClass.MC_COMMAND_ENCHANT);
        case "net.minecraft.item.ItemEnchantedBook":
            return patch(basicClass, EnumInputClass.MC_ITEM_ENCHANTED_BOOK);
        case "net.minecraft.inventory.ContainerRepair":
            return patch(basicClass, EnumInputClass.MC_CONTAINER_REPAIR);
        case "net.minecraft.item.ItemStack":
            return patch(basicClass, EnumInputClass.MC_ITEM_STACK);

            //Support for Thermal Expansion Arcane Ensorcellator (tested for 5.5.3.41)
        case "cofh.thermalexpansion.util.managers.machine.EnchanterManager":
            return patch(basicClass, EnumInputClass.TE_ENCHANTER_MANAGER);
        }
        return basicClass;
    }

    private byte[] patch(byte[] basicClass, EnumInputClass enumInput) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(basicClass);
        classReader.accept(classNode, enumInput.readerFlags);
        if (enumInput.patch(classNode))
            CORE_LOGGER.info("{} <{}.class> patched!", enumInput.domain, enumInput.clazz);
        else
            CORE_LOGGER.error("{} <{}.class> patch FAILED!", enumInput.domain, enumInput.clazz);
        ClassWriter writer = new ClassWriter(enumInput.writerFlags);        
        classNode.accept(writer);
        return writer.toByteArray();    
    }
}
