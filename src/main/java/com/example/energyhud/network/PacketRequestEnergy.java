package com.example.energyhud.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestEnergy implements IMessage {

    private BlockPos pos;

    public PacketRequestEnergy() {}

    public PacketRequestEnergy(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketRequestEnergy, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestEnergy message, MessageContext ctx) {
            ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                EnergyRequestHandler.handle(message.pos, ctx.getServerHandler().player);
            });
            return null;
        }
    }

    public BlockPos getPos() {
        return pos;
    }
}
