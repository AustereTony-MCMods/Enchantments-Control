package austeretony.enchcontrol.common.core;

import java.util.Iterator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public enum EnumInputClasses {

    MC_LOCALE("Minecraft", "Locale", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_ENCHANTMENT("Minecraft", "Enchantment", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_ENCHANTMENT_HELPER("Minecraft", "EnchantmentHelper", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_LIST_ENCHANTED_BOOK_FOR_EMERALDS("Minecraft", "ListEnchantedBookForEmeralds", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_ENCHANT_RANDOMLY("Minecraft", "EnchantRandomly", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_COMMAND_ENCHANT("Minecraft", "CommandEnchant", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_ITEM_ENCHANTED_BOOK("Minecraft", "ItemEnchantedBook", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_CREATIVE_TABS("Minecraft", "CreativeTabs", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_CONTAINER_REPAIR("Minecraft", "ContainerRepair", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES),
    MC_ITEM_STACK("Minecraft", "ItemStack", 0, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

    private static final String HOOKS_CLASS = "austeretony/enchcontrol/common/core/ECHooks";

    public final String domain, clazz;

    public final int readerFlags, writerFlags;

    EnumInputClasses(String domain, String clazz, int readerFlags, int writerFlags) {
        this.domain = domain;
        this.clazz = clazz;
        this.readerFlags = readerFlags;
        this.writerFlags = writerFlags;
    }

    public boolean patch(ClassNode classNode) {
        switch (this) {
        case MC_LOCALE:
            return this.pathcMCLocale(classNode);
        case MC_ENCHANTMENT:
            return this.pathcMCEnchantment(classNode);
        case MC_ENCHANTMENT_HELPER:
            return this.pathcMCEnchantmentHelper(classNode);
        case MC_LIST_ENCHANTED_BOOK_FOR_EMERALDS:
            return this.pathcMCListEnchantedBookForEmeralds(classNode);
        case MC_ENCHANT_RANDOMLY:
            return this.pathcMCEnchantRandomly(classNode);
        case MC_COMMAND_ENCHANT:
            return this.pathcMCCommandEnchant(classNode);
        case MC_ITEM_ENCHANTED_BOOK:
            return this.pathcMCItemEnchantedBook(classNode);
        case MC_CREATIVE_TABS:
            return this.pathcMCCreativeTabs(classNode);
        case MC_CONTAINER_REPAIR:
            return this.pathcMCContainerRepair(classNode);
        case MC_ITEM_STACK:
            return this.pathcMCItemStack(classNode);
        }
        return false;
    }

    private boolean pathcMCLocale(ClassNode classNode) {
        String
        propertiesFieldName = ECCorePlugin.isObfuscated() ? "a" : "properties",
                loadLocaleDataFilesMethodName = ECCorePlugin.isObfuscated() ? "a" : "loadLocaleDataFiles",
                        localeClassName = ECCorePlugin.isObfuscated() ? "bxo" : "net/minecraft/client/resources/Locale",
                                iResourceManagerClassName = ECCorePlugin.isObfuscated() ? "bxd" : "net/minecraft/client/resources/IResourceManager",
                                        listClassName = "java/util/List",
                                        mapClassName = "java/util/Map";
        boolean isSuccessful = false;   
        int invokespecialCount = 0;
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(loadLocaleDataFilesMethodName) && methodNode.desc.equals("(L" + iResourceManagerClassName + ";L" + listClassName + ";)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.INVOKESPECIAL) {    
                        invokespecialCount++;
                        if (invokespecialCount == 3) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            nodesList.add(new FieldInsnNode(Opcodes.GETFIELD, localeClassName, propertiesFieldName, "L" + mapClassName + ";"));
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "loadCustomLocalization", "(L" + listClassName + ";L" + mapClassName + ";)V", false));
                            methodNode.instructions.insertBefore(currentInsn.getPrevious(), nodesList); 
                            isSuccessful = true;                        
                            break;
                        }
                    }
                }    
                break;
            }
        }
        return isSuccessful;
    }

    private boolean pathcMCEnchantment(ClassNode classNode) {
        String
        getTranslatedNameMethodName = ECCorePlugin.isObfuscated() ? "d" : "getTranslatedName",
                enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                        stringClassName = "java/lang/String";
        boolean isSuccessful = false;   
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {       
            if (methodNode.name.equals(getTranslatedNameMethodName) && methodNode.desc.equals("(I)L" + stringClassName + ";")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ALOAD) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        nodesList.add(new VarInsnNode(Opcodes.ILOAD, 1));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "getTranslatedName", "(L" + enchantmentClassName + ";I)L" + stringClassName + ";", false));
                        nodesList.add(new InsnNode(Opcodes.ARETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful;    
    }

    private boolean pathcMCEnchantmentHelper(ClassNode classNode) {
        String
        removeIncompatibleMethodName = ECCorePlugin.isObfuscated() ? "a" : "removeIncompatible",
                getEnchantmentDatasMethodName = ECCorePlugin.isObfuscated() ? "a" : "getEnchantmentDatas",
                        enchantmentDataClassName = ECCorePlugin.isObfuscated() ? "agy" : "net/minecraft/enchantment/EnchantmentData",
                                itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack",
                                        listClassName = "java/util/List";
        boolean 
        isSuccessful0 = false,
        isSuccessful1 = false;  
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {      
            if (methodNode.name.equals(removeIncompatibleMethodName) && methodNode.desc.equals("(L" + listClassName + ";L" + enchantmentDataClassName + ";)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ALOAD) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "removeIncompatible", "(L" + listClassName + ";L" + enchantmentDataClassName + ";)V", false));
                        nodesList.add(new InsnNode(Opcodes.RETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful0 = true;                        
                    }
                }    
            }
            if (methodNode.name.equals(getEnchantmentDatasMethodName) && methodNode.desc.equals("(IL" + itemStackClassName + ";Z)L" + listClassName + ";")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.INVOKESTATIC) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ILOAD, 0));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new VarInsnNode(Opcodes.ILOAD, 2));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "getEnchantmentDatas", "(IL" + itemStackClassName + ";Z)L" + listClassName + ";", false));
                        nodesList.add(new InsnNode(Opcodes.ARETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful1 = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful0 && isSuccessful1;    
    }

    private boolean pathcMCListEnchantedBookForEmeralds(ClassNode classNode) {
        String
        modifyMerchantRecipeListMethodName = ECCorePlugin.isObfuscated() ? "a" : "modifyMerchantRecipeList",
                enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                        merchantRecipeListClassName = ECCorePlugin.isObfuscated() ? "ahq" : "net/minecraft/village/MerchantRecipeList",
                                randomClassName = "java/util/Random";
        boolean isSuccessful = false;   
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(modifyMerchantRecipeListMethodName)) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();       
                    if (currentInsn.getOpcode() == Opcodes.GETSTATIC) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "addMerchantRecipe", "(L" + merchantRecipeListClassName + ";L" + randomClassName + ";)V", false));
                        nodesList.add(new InsnNode(Opcodes.RETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful;    
    }

    private boolean pathcMCEnchantRandomly(ClassNode classNode) {
        String
        enchantmentsFieldName = ECCorePlugin.isObfuscated() ? "b" : "enchantments",
                loggerFieldName = ECCorePlugin.isObfuscated() ? "a" : "LOGGER",
                        applyMethodName = ECCorePlugin.isObfuscated() ? "a" : "apply",
                                itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack",
                                        enchantRandomlyClassName = ECCorePlugin.isObfuscated() ? "bau" : "net/minecraft/world/storage/loot/functions/EnchantRandomly",
                                                randomClassName = "java/util/Random",
                                                listClassName = "java/util/List",
                                                loggerClassName = "org/apache/logging/log4j/Logger";
        boolean isSuccessful = false;   
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(applyMethodName)) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ALOAD) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        nodesList.add(new FieldInsnNode(Opcodes.GETFIELD, enchantRandomlyClassName, enchantmentsFieldName, "L" + listClassName + ";"));
                        nodesList.add(new FieldInsnNode(Opcodes.GETSTATIC, enchantRandomlyClassName, loggerFieldName, "L" + loggerClassName + ";"));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "apply", "(L" + itemStackClassName + ";L" + randomClassName + ";L" + listClassName + ";L" + loggerClassName + ";)L" + itemStackClassName + ";", false));
                        nodesList.add(new InsnNode(Opcodes.ARETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful;    
    }

    private boolean pathcMCCommandEnchant(ClassNode classNode) {
        String
        executeMethodName = ECCorePlugin.isObfuscated() ? "a" : "execute",
                minecraftServerClassName = "net/minecraft/server/MinecraftServer",
                iCommandSenderClassName = ECCorePlugin.isObfuscated() ? "n" : "net/minecraft/command/ICommandSender",
                        itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack",
                                enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                                        entityLivingBaseClassName = ECCorePlugin.isObfuscated() ? "sf" : "net/minecraft/entity/EntityLivingBase",
                                                commandEnchantClassName = ECCorePlugin.isObfuscated() ? "ac" : "net/minecraft/command/CommandEnchant",
                                                        stringClassName = "java/lang/String";
        boolean isSuccessful = false;   
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(executeMethodName) && methodNode.desc.equals("(L" + minecraftServerClassName + ";L" + iCommandSenderClassName + ";[L" + stringClassName + ";)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ISTORE) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 5));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 3));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "execute", "(L" + commandEnchantClassName + ";L" + entityLivingBaseClassName + ";L" + enchantmentClassName + ";L" + iCommandSenderClassName + ";[L" + stringClassName + ";)V", false));
                        nodesList.add(new InsnNode(Opcodes.RETURN));
                        methodNode.instructions.insertBefore(currentInsn.getPrevious(), nodesList); 
                        isSuccessful = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful;    
    }

    private boolean pathcMCItemEnchantedBook(ClassNode classNode) {
        String
        addInformationMethodName = ECCorePlugin.isObfuscated() ? "a" : "addInformation",
                getAllMethodName = ECCorePlugin.isObfuscated() ? "a" : "getAll",
                        itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack",
                                entityPlayerClassName = ECCorePlugin.isObfuscated() ? "zs" : "net/minecraft/entity/player/EntityPlayer",
                                        enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                                                listClassName = "java/util/List";
        boolean 
        isSuccessful0 = false,
        isSuccessful1 = false;   
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {    
            if (methodNode.name.equals(addInformationMethodName) && methodNode.desc.equals("(L" + itemStackClassName + ";L" + entityPlayerClassName + ";L" + listClassName + ";Z)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.INVOKESPECIAL) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new InsnNode(Opcodes.ICONST_0));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 3));
                        nodesList.add(new InsnNode(Opcodes.ICONST_1));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "modifyItemStackTooltip", "(IL" + itemStackClassName + ";L" + listClassName + ";Z)V", false));
                        nodesList.add(new InsnNode(Opcodes.RETURN));
                        methodNode.instructions.insert(currentInsn, nodesList); 
                        isSuccessful0 = true;
                    }
                }    
            }
            if (methodNode.name.equals(getAllMethodName) && methodNode.desc.equals("(L" + enchantmentClassName + ";L" + listClassName + ";)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ALOAD) {    
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        nodesList.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "getAll", "(L" + enchantmentClassName + ";L" + listClassName + ";)V", false));
                        nodesList.add(new InsnNode(Opcodes.RETURN));
                        methodNode.instructions.insertBefore(currentInsn, nodesList); 
                        isSuccessful1 = true;                        
                        break;
                    }
                }    
                break;
            }
        }
        return isSuccessful0 && isSuccessful1;    
    }

    private boolean pathcMCCreativeTabs(ClassNode classNode) {
        String
        addEnchantmentBooksToListMethodName = ECCorePlugin.isObfuscated() ? "a" : "addEnchantmentBooksToList",
                enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                        enumEnchantmentTypeClassName = ECCorePlugin.isObfuscated() ? "agw" : "net/minecraft/enchantment/EnumEnchantmentType",
                                listClassName = "java/util/List";
        boolean isSuccessful = false;   
        int invokevirtualCount = 0;
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {    
            if (methodNode.name.equals(addEnchantmentBooksToListMethodName) && methodNode.desc.equals("(L" + listClassName + ";[L" + enumEnchantmentTypeClassName + ";)V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.INVOKEVIRTUAL) { 
                        invokevirtualCount++;
                        if (invokevirtualCount == 2) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "getMaxLevel", "(L" + enchantmentClassName + ";)I", false));
                            methodNode.instructions.insert(currentInsn, nodesList); 
                            methodNode.instructions.remove(currentInsn);
                            isSuccessful = true;                        
                            break;
                        }
                    }
                }    
                break;
            }
        }
        return isSuccessful;    
    }

    private boolean pathcMCContainerRepair(ClassNode classNode) {
        String
        updateRepairOutputMethodName = ECCorePlugin.isObfuscated() ? "e" : "updateRepairOutput",
                enchantmentClassName = ECCorePlugin.isObfuscated() ? "agv" : "net/minecraft/enchantment/Enchantment",
                        itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack";
        boolean 
        isSuccessful0 = false,
        isSuccessful1 = false,  
        isSuccessful2 = false,
        isSuccessful3 = false;  
        int 
        istoreCount = 0,
        ifneCount = 0,
        invokestaticCount = 0;
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(updateRepairOutputMethodName) && methodNode.desc.equals("()V")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.ISTORE) {    
                        istoreCount++;
                        if (istoreCount == 20) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 11));
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "canApply", "(L" + enchantmentClassName + ";L" + itemStackClassName + ";)Z", false));
                            nodesList.add(new VarInsnNode(Opcodes.ISTORE, 14));
                            methodNode.instructions.insert(currentInsn, nodesList); 
                            isSuccessful0 = true;
                        }
                    } else if (currentInsn.getOpcode() == Opcodes.IFNE) {
                        ifneCount++;
                        if (ifneCount == 7) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "isCompatibleWith", "(L" + enchantmentClassName + ";L" + enchantmentClassName + ";)Z", false));
                            methodNode.instructions.insertBefore(currentInsn.getPrevious(), nodesList); 
                            methodNode.instructions.remove(currentInsn.getPrevious());
                            isSuccessful1 = true;
                        }
                    } else if (currentInsn.getOpcode() == Opcodes.IF_ICMPLE) {
                        InsnList nodesList = new InsnList();   
                        nodesList.add(new InsnNode(Opcodes.ICONST_0));
                        nodesList.add(new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) currentInsn).label));
                        methodNode.instructions.insertBefore(currentInsn.getPrevious().getPrevious().getPrevious(), nodesList); 
                        isSuccessful2 = true;
                    } else if (currentInsn.getOpcode() == Opcodes.INVOKESTATIC) {
                        invokestaticCount++;
                        if (invokestaticCount == 7) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 11));
                            nodesList.add(new VarInsnNode(Opcodes.ILOAD, 13));
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "ceilMaxLevel", "(L" + enchantmentClassName + ";I)I", false));
                            nodesList.add(new VarInsnNode(Opcodes.ISTORE, 13));
                            methodNode.instructions.insertBefore(currentInsn.getPrevious().getPrevious().getPrevious(), nodesList); 
                            isSuccessful3 = true;                        
                            break;
                        }
                    }
                }    
                break;
            }
        }
        return isSuccessful0 && isSuccessful1 && isSuccessful2 && isSuccessful3;    
    }

    private boolean pathcMCItemStack(ClassNode classNode) {
        String
        getTooltipMethodName = ECCorePlugin.isObfuscated() ? "a" : "getTooltip",
                itemStackClassName = ECCorePlugin.isObfuscated() ? "adz" : "net/minecraft/item/ItemStack",
                        entityPlayerClassName = ECCorePlugin.isObfuscated() ? "zs" : "net/minecraft/entity/player/EntityPlayer",
                                listClassName = "java/util/List"; 
        boolean isSuccessful = false;   
        int ifneCount = 0;
        AbstractInsnNode currentInsn;

        for (MethodNode methodNode : classNode.methods) {               
            if (methodNode.name.equals(getTooltipMethodName) && methodNode.desc.equals("(L" + entityPlayerClassName + ";Z)L" + listClassName + ";")) {                         
                Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();              
                while (insnIterator.hasNext()) {                        
                    currentInsn = insnIterator.next();                  
                    if (currentInsn.getOpcode() == Opcodes.IFNE) {    
                        ifneCount++;
                        if (ifneCount == 4) {
                            InsnList nodesList = new InsnList();   
                            nodesList.add(new VarInsnNode(Opcodes.ILOAD, 5));
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            nodesList.add(new VarInsnNode(Opcodes.ALOAD, 3));
                            nodesList.add(new InsnNode(Opcodes.ICONST_0));
                            nodesList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS_CLASS, "modifyItemStackTooltip", "(IL" + itemStackClassName + ";L" + listClassName + ";Z)V", false));
                            nodesList.add(new InsnNode(Opcodes.ICONST_0));
                            nodesList.add(new JumpInsnNode(Opcodes.IFEQ, ((JumpInsnNode) currentInsn).label));
                            methodNode.instructions.insertBefore(currentInsn.getPrevious().getPrevious().getPrevious(), nodesList); 
                            isSuccessful = true;                        
                            break;
                        }
                    }
                }    
                break;
            }
        }
        return isSuccessful;
    }
}
