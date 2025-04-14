package franc.energyhud.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class EnergyDataPacket implements IMessage {
    private BlockPos pos;
    private long energyStored;
    private long maxEnergy;

    public EnergyDataPacket() {}

    public EnergyDataPacket(BlockPos pos, long energyStored, long maxEnergy) {
        this.pos = pos;
        this.energyStored = energyStored;
        this.maxEnergy = maxEnergy;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.energyStored = buf.readLong();
        this.maxEnergy = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeLong(energyStored);
        buf.writeLong(maxEnergy);
    }

    public static class Handler implements IMessageHandler<EnergyDataPacket, IMessage> {
        @Override
        public IMessage onMessage(EnergyDataPacket message, MessageContext ctx) {
            // Клиент: обрабатываем в главном потоке
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EnergyDataHolder.INSTANCE.updateData(
                        message.pos,
                        message.energyStored,
                        message.maxEnergy
                );
            });
            return null;
        }
    }
}
