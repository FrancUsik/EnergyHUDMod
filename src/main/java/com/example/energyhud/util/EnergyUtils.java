package com.example.energyhud.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyUtils {

    public static double getEnergy(TileEntity tile) {
        if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, null);
            if (storage != null) {
                return storage.getEnergyStored();
            }
        }

        return 0;
    }

    public static double getMaxEnergy(TileEntity tile) {
        if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, null);
            if (storage != null) {
                return storage.getMaxEnergyStored();
            }
        }

        return 0;
    }
}
