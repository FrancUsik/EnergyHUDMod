package com.example.energyhud.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEnergyData implements IMessage {

    private int energy;
    private int maxEnergy;
    private int delta;

    public PacketEnergyData() {}

    public PacketEnergyData(int energy, int maxEnergy, int delta) {
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.delta = delta;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
        buf.writeInt(maxEnergy);
        buf.writeInt(delta);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
        maxEnergy = buf.readInt();
        delta = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketEnergyData, IMessage> {
        @Override
        public IMessage onMessage(PacketEnergyData message, MessageContext ctx) {
            // Запускаем на клиенте
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                // Здесь можешь положить данные в свой HUD
                // Например: EnergyHUDClientData.update(message.energy, message.maxEnergy, message.delta);
            });
            return null;
        }
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getDelta() {
        return delta;
    }
}
