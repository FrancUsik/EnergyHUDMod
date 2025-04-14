package com.example.energyhud;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.SidedProxy;
import com.example.energyhud.network.PacketRegistry;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
public class EnergyHUDMod {
    // Запрос на доступ к сети через регистрацию пакетов
    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD";
    public static final String VERSION = "1.0";

    @Mod.Instance
    public static EnergyHUDMod instance;

    @SidedProxy(clientSide = "com.example.energyhud.ClientProxy", serverSide = "com.example.energyhud.ServerProxy")
    public static IProxy proxy;

    @Mod.EventHandler
    public void commonSetup(FMLCommonSetupEvent event) {
        // Место для регистрации общих настроек мода
        System.out.println("Common setup for " + MODID);
        PacketRegistry.registerPackets();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Init phase for " + MODID);
    }

    // Регистрация событий
    @Mod.EventBusSubscriber(modid = MODID)
    public static class EventHandler {
        @SubscribeEvent
        public static void onServerStarting(FMLServerStartingEvent event) {
            // Код, который нужно выполнить при запуске сервера
            System.out.println("Server starting for " + MODID);
        }

        @SubscribeEvent
        public static void onPlayerLogin(PlayerLoggedInEvent event) {
            // Код, который нужно выполнить при входе игрока
            System.out.println("Player logged in for " + MODID);
        }
    }
}
