package com.example.energyhud.network;

import com.example.energyhud.util.EnergyUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyRequestHandler {

    public static void handle(BlockPos pos, EntityPlayerMP player) {
        World world = player.world;

        if (!world.isBlockLoaded(pos)) return;

        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return;

        double energy = EnergyUtils.getEnergy(tile);
        double maxEnergy = EnergyUtils.getMaxEnergy(tile);

        PacketSyncEnergy packet = new PacketSyncEnergy(pos, energy, maxEnergy);
        PacketHandler.INSTANCE.sendTo(packet, player);
    }
}
