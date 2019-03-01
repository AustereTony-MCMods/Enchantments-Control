package austeretony.enchcontrol.client.reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientReference {

    @SideOnly(Side.CLIENT)
    public static Minecraft getMinecraft() {
        return Minecraft.getMinecraft();
    }

    @SideOnly(Side.CLIENT)
    public static GameSettings getGameSettings() {
        return Minecraft.getMinecraft().gameSettings;
    }

    @SideOnly(Side.CLIENT)
    public static EntityPlayer getClientPlayer() {
        return getMinecraft().player;
    }
}
