package com.example.energyhud;

import com.example.energyhud.client.HiTechHUDRenderer;
import com.example.energyhud.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = EnergyHUDMod.MODID, name = "Energy HUD", version = "1.0")
public class EnergyHUDMod {
    public static final String MODID = "energyhud";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler.registerPackets();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new HiTechHUDRenderer());
    }
}
