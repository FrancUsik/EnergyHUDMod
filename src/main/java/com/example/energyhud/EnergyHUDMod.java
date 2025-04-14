package com.francusik.energyhud;

import com.francusik.energyhud.client.HiTechHUDRenderer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.Deque;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
public class EnergyHUDMod {
    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD Mod";
    public static final String VERSION = "1.0";

    private float lastEnergy = -1;
    private long lastUpdate = 0;
    private final Deque<Float> energyHistory = new ArrayDeque<>();
    private static final int HISTORY_SIZE = 60; // 60 записей = ~3 секунды

    @Mod.EventHandler
    public void init(net.minecraftforge.fml.common.event.FMLInitializationEvent event) {
        // init if needed
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        TileEntity tile = mc.world.getTileEntity(mc.objectMouseOver.getBlockPos());
        if (tile == null) return;

        IEnergyStorage energyStorage = tile.getCapability(Capabilities.ELECTRICITY_CAPABILITY, null);
        if (energyStorage == null) return;

        float currentEnergy = energyStorage.getEnergyStored();
        float maxEnergy = energyStorage.getMaxEnergyStored();

        long now = System.currentTimeMillis();
        float deltaRF = 0f;

        if (lastEnergy >= 0 && now > lastUpdate) {
            float delta = (currentEnergy - lastEnergy);
            float seconds = (now - lastUpdate) / 1000f;
            deltaRF = delta / seconds;
        }

        lastEnergy = currentEnergy;
        lastUpdate = now;

        // Обновляем историю каждые 500 мс
        if (energyHistory.isEmpty() || now - lastUpdate > 500) {
            float percent = maxEnergy > 0 ? currentEnergy / maxEnergy : 0f;
            if (energyHistory.size() >= HISTORY_SIZE) energyHistory.pollFirst();
            energyHistory.addLast(percent);
        }

        HiTechHUDRenderer.renderHUD(currentEnergy, maxEnergy, deltaRF, new ArrayList<>(energyHistory));
    }
}