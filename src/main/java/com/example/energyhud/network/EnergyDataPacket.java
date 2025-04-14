package com.example.energyhud.network.packets;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class EnergyDataPacket implements IMessage {
    private int energyLevel;

    public EnergyDataPacket() { }

    public EnergyDataPacket(int energyLevel) {
        this.energyLevel = energyLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Чтение уровня энергии из буфера
        energyLevel = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Запись уровня энергии в буфер
        buf.writeInt(energyLevel);
    }

    public int getEnergyLevel() {
        return energyLevel;
    }

    public static class Handler implements net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler<EnergyDataPacket, IMessage> {
        @Override
        public IMessage onMessage(EnergyDataPacket message, net.minecraftforge.fml.common.network.simpleimpl.MessageContext ctx) {
            // Применение уровня энергии
            // Пример обработки: логирование или обновление HUD на клиенте
            System.out.println("Получен пакет с уровнем энергии: " + message.getEnergyLevel());

            // Вернуть null, так как не нужно отправлять ответный пакет
            return null;
        }
    }
}
