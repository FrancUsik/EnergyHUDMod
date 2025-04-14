package franc.energyhud.network;

import net.minecraft.util.math.BlockPos;

public class EnergyDataHolder {
    public static final EnergyDataHolder INSTANCE = new EnergyDataHolder();

    private BlockPos lastPos;
    private long energyStored;
    private long maxEnergy;

    public void updateData(BlockPos pos, long stored, long max) {
        this.lastPos = pos;
        this.energyStored = stored;
        this.maxEnergy = max;
    }

    public BlockPos getLastPos() {
        return lastPos;
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getMaxEnergy() {
        return maxEnergy;
    }
}
