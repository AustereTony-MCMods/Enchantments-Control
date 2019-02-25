package austeretony.enchcontrol.common.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("Enchantments Control Core")
@MCVersion("1.12.2")
@TransformerExclusions({"austeretony.enchcontrol.common.core"})
public class ECCorePlugin implements IFMLLoadingPlugin {

    private static boolean isObfuscated;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ECClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isObfuscated = (boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static boolean isObfuscated() {
        return isObfuscated;
    }
}
