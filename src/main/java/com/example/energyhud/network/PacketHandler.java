package com.example.energyhud.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("energyhud");

    public static void registerPackets() {
        int id = 0;

        INSTANCE.registerMessage(PacketRequestEnergy.Handler.class, PacketRequestEnergy.class, id++, Side.SERVER);
        INSTANCE.registerMessage(PacketSyncEnergy.Handler.class, PacketSyncEnergy.class, id++, Side.CLIENT);
    }
}
