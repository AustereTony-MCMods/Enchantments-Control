package austeretony.enchcontrol.common.command;

import java.math.BigDecimal;

import com.udojava.evalex.Expression;

import austeretony.enchcontrol.common.config.ConfigLoader;
import austeretony.enchcontrol.common.config.EnumConfigSettings;
import austeretony.enchcontrol.common.config.EnumEnchantmentListType;
import austeretony.enchcontrol.common.enchantment.EnchantmentWrapper;
import austeretony.enchcontrol.common.main.ECMain;
import austeretony.enchcontrol.common.main.EnumChatMessages;
import austeretony.enchcontrol.common.reference.CommonReference;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandEC extends CommandBase {

    public static final String 
    NAME = "ec", 
    USAGE = "/ec <arg>, type </ec help> for available arguments.";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE; 
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender instanceof EntityPlayerMP && CommonReference.isOpped((EntityPlayerMP) sender);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException { 
        EnumCommandECArgs arg;
        if ((arg = EnumCommandECArgs.get(args)) == null)         
            throw new WrongUsageException(this.getUsage(sender));   
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);  
        switch(arg) {
        case HELP:
            EnumChatMessages.COMMAND_EC_HELP.sendMessage(player);
            break;
        case LIST_ALL:
            if (!this.validAction(player, false, false, false, false)) break;
            EnumChatMessages.COMMAND_EC_LIST_ALL.sendMessage(player, arg.getProcessingArgument(args));
            break;
        case FILE_ALL:
            if (!this.validAction(player, true, false, false, true)) break;
            ConfigLoader.createEnchantmentsListFile(EnumEnchantmentListType.ALL);
            EnumChatMessages.COMMAND_EC_FILE_ALL.sendMessage(player);
            break;
        case LIST_UNKNOWN:
            if (!this.validAction(player, false, false, true, false)) break;
            EnumChatMessages.COMMAND_EC_LIST_UNKNOWN.sendMessage(player, arg.getProcessingArgument(args));
            break;
        case FILE_UNKNOWN:
            if (!this.validAction(player, true, false, true, true)) break;
            ConfigLoader.createEnchantmentsListFile(EnumEnchantmentListType.UNKNOWN);
            EnumChatMessages.COMMAND_EC_FILE_UNKNOWN.sendMessage(player);
            break;
        case CLEAR:
            if (!this.validAction(player, true, true, false, true)) break;
            ConfigLoader.clear();
            EnumChatMessages.COMMAND_EC_CLEAR.sendMessage(player);
            break;
        case RELOAD:
            if (!this.validAction(player, true, true, false, true)) break;
            EnchantmentWrapper.clearData();
            ConfigLoader.load();
            this.wrap();
            EnumChatMessages.COMMAND_EC_RELOAD.sendMessage(player);
            break;
        case BACKUP:
            if (!this.validAction(player, true, true, false, true)) break;
            ConfigLoader.backup();
            EnumChatMessages.COMMAND_EC_BACKUP.sendMessage(player);
            break;
        case UPDATE:
            if (!this.validAction(player, true, true, true, true)) break;
            ConfigLoader.save();
            EnumChatMessages.COMMAND_EC_UPDATE.sendMessage(player);
            break;
        case INFO:
            if (!this.validAction(player, false, false, false, false)) break;
            EnumChatMessages.COMMAND_EC_INFO.sendMessage(player, arg.getProcessingArgument(args));
            break;
        case EVAL:
            if (!this.validAction(player, false, false, false, false)) break;
            this.calculate(player, arg.getProcessingArgument(args));
            break;
        }
    }

    private boolean validAction(EntityPlayer player, boolean checkExternalConfig, boolean checkDebugMode, boolean checkUnknown, boolean checkDedicatedServer) {
        if (checkExternalConfig && !EnumConfigSettings.EXTERNAL_CONFIG.isEnabled()) {
            EnumChatMessages.COMMAND_EC_ERR_EXTERNAL_CONFIG_DISABLED.sendMessage(player);
            return false;
        } else if (checkDebugMode && !EnumConfigSettings.DEBUG_MODE.isEnabled()) {
            EnumChatMessages.COMMAND_EC_ERR_DEBUG_MODE_DISABLED.sendMessage(player);
            return false;
        } else if (checkUnknown && EnchantmentWrapper.UNKNOWN.isEmpty()) {
            EnumChatMessages.COMMAND_EC_ERR_NO_UNKNOWN_ENCHANTMENTS.sendMessage(player);
            return false;
        } else if (checkDedicatedServer && ConfigLoader.runningAtDedicatedServer) {
            EnumChatMessages.COMMAND_EC_ERR_RUNNING_AT_DEDICATED_SERVER.sendMessage(player);
            return false;
        }
        return true;
    }

    private void wrap() {
        ECMain.LOGGER.info("Applying enchantments settings...");
        for (Enchantment enchantment : Enchantment.REGISTRY)
            EnchantmentWrapper.get(enchantment);//getter will wrap enchantment and apply settings
    }

    private void calculate(EntityPlayer player, String eval) {
        try {
            BigDecimal result = new Expression(eval).eval();
            EnumChatMessages.COMMAND_EC_EVAL.sendMessage(player, eval, String.valueOf(result.intValue()));
        } catch (Exception exception) {
            EnumChatMessages.COMMAND_EC_ERR_WRONG_EXPRESSION.sendMessage(player, eval);
        }
    }
}
