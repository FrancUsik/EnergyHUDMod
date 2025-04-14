package com.example.energyhud.network;

import com.example.energyhud.EnergyHUDMod;
import com.example.energyhud.network.packets.EnergyDataPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketRegistry
{
    public static void registerPackets() {
        // Ensure that we are registering packets correctly with the network
        SimpleNetworkWrapper network = EnergyHUDMod.network;
        network.registerMessage(EnergyDataPacket.Handler.class, EnergyDataPacket.class, 0, Side.CLIENT);
    }
}
