package com.example.energyhud.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncEnergy implements IMessage {

    private BlockPos pos;
    private double energy;
    private double maxEnergy;

    public PacketSyncEnergy() {}

    public PacketSyncEnergy(BlockPos pos, double energy, double maxEnergy) {
        this.pos = pos;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeDouble(energy);
        buf.writeDouble(maxEnergy);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        energy = buf.readDouble();
        maxEnergy = buf.readDouble();
    }

    public static class Handler implements IMessageHandler<PacketSyncEnergy, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncEnergy message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ClientEnergyCache.update(message.pos, message.energy, message.maxEnergy);
            });
            return null;
        }
    }
}
