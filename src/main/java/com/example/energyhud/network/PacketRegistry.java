package com.example.energyhud.network;

import com.example.energyhud.EnergyHUDMod;  // Импортируем EnergyHUDMod
import com.example.energyhud.network.EnergyDataPacket;  // Импортируем EnergyDataPacket
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;


public class PacketRegistry {

    public static void registerPackets() {
        // Регистрируем все пакеты
        EnergyHUDMod.network.registerMessage(EnergyDataPacket.Handler.class, EnergyDataPacket.class, 0, Side.CLIENT);
        // Добавь другие пакеты, если они есть
    }
}
