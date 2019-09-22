package austeretony.enchcontrol.common.command;

public enum EnumCommandArg {

    HELP("help"),
    LIST_ALL("list-all", 1),
    FILE_ALL("file-all"),
    LIST_UNKNOWN("list-unknown", 1),
    FILE_UNKNOWN("file-unknown"),
    CLEAR("clear"),
    RELOAD("reload"),
    BACKUP("backup"),
    UPDATE("update"),
    INFO("info", 1),
    EVAL("eval", 1);

    private final String arg;

    private final int argsAmount;

    EnumCommandArg(String arg, int... argsAmount) {
        this.argsAmount = argsAmount.length > 0 ? argsAmount[0] : 0;
        this.arg = arg;
    }

    public int getArgsAmount() {
        return this.argsAmount + 1;
    }

    public String getProcessingArgument(String... commandArgs) {
        return commandArgs[this.getArgsAmount() - 1];
    }

    public static EnumCommandArg get(String... commandArgs) {
        if (commandArgs.length == 0)
            return null;
        for (EnumCommandArg arg : values())
            if (arg.arg.equals(commandArgs[0]) && commandArgs.length == arg.getArgsAmount())
                return arg;
        return null;
    }

    @Override
    public String toString() {
        return this.arg;
    }
}
