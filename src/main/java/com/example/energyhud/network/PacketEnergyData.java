package com.example.energyhud.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEnergyData implements IMessage {
    public int energy;

    public PacketEnergyData() {}

    public PacketEnergyData(int energy) {
        this.energy = energy;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketEnergyData, IMessage> {
        @Override
        public IMessage onMessage(PacketEnergyData message, MessageContext ctx) {
            // Здесь логика получения на клиенте
            return null;
        }
    }
}
