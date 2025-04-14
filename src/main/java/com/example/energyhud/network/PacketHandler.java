package com.example.energyhud.network;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper("energyhudmod");

    public static void registerMessages() {
        // Packet ID должен быть уникален, начиная с 0
        INSTANCE.registerMessage(PacketEnergyData.Handler.class, PacketEnergyData.class, 0, Side.CLIENT);
    }
}
