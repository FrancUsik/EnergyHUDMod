package com.example.energyhud;

import com.example.energyhud.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
public class EnergyHUDMod {

    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD";
    public static final String VERSION = "1.0";

    @Mod.Instance
    public static EnergyHUDMod INSTANCE;

    @SidedProxy(clientSide = "com.example.energyhud.client.ClientProxy", serverSide = "com.example.energyhud.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Регистрируем все пакеты
        PacketHandler.registerMessages();
    }
}
