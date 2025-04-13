package com.example.energyhud.network;

import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientEnergyCache {

    public static class EnergyData {
        public final double energy;
        public final double maxEnergy;

        public EnergyData(double energy, double maxEnergy) {
            this.energy = energy;
            this.maxEnergy = maxEnergy;
        }
    }

    private static final Map<BlockPos, EnergyData> energyDataMap = new ConcurrentHashMap<>();

    public static void update(BlockPos pos, double energy, double maxEnergy) {
        energyDataMap.put(pos, new EnergyData(energy, maxEnergy));
    }

    public static EnergyData get(BlockPos pos) {
        return energyDataMap.get(pos);
    }

    public static boolean isSupported(BlockPos pos) {
        // Пока считаем, что если данные есть — значит поддерживается
        return energyDataMap.containsKey(pos);
    }
}
