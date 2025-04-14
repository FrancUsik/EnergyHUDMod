package com.example.energyhud;

import com.example.energyhud.client.HiTechHUDRenderer;
import com.example.energyhud.network.PacketHandler;
import com.example.energyhud.network.PacketEnergyData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
public class EnergyHUDMod {

    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD";
    public static final String VERSION = "1.0";

    @Mod.Instance
    public static EnergyHUDMod INSTANCE;

    public static SimpleNetworkWrapper NETWORK;

    @SidedProxy(clientSide = "com.example.energyhud.client.ClientProxy", serverSide = "com.example.energyhud.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        // Регистрируем пакет передачи данных об энергии (ID = 0)
        NETWORK.registerMessage(PacketEnergyData.Handler.class, PacketEnergyData.class, 0, Side.CLIENT);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(); // Регистрируем HUD только на клиенте
    }
}
