package com.francusik.energyhud;

import com.francusik.energyhud.client.HiTechHUDRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

@Mod(modid = EnergyHUDMod.MODID, name = EnergyHUDMod.NAME, version = EnergyHUDMod.VERSION)
@Mod.EventBusSubscriber
public class EnergyHUDMod {
    public static final String MODID = "energyhud";
    public static final String NAME = "Energy HUD Mod";
    public static final String VERSION = "1.0";

    private static float lastEnergy = -1;
    private static long lastUpdate = 0;
    private static final Deque<Float> energyHistory = new ArrayDeque<>();
    private static final int HISTORY_SIZE = 60;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) return;

        TileEntity tile = mc.world.getTileEntity(mc.objectMouseOver.getBlockPos());
        if (tile == null) return;

        IEnergyStorage energyStorage = tile.getCapability(CapabilityEnergy.ENERGY, null);
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

        // история обновляется всегда
        float percent = maxEnergy > 0 ? currentEnergy / maxEnergy : 0f;
        if (energyHistory.size() >= HISTORY_SIZE) energyHistory.pollFirst();
        energyHistory.addLast(percent);

        lastEnergy = currentEnergy;
        lastUpdate = now;

        HiTechHUDRenderer.renderHUD(currentEnergy, maxEnergy, deltaRF, new ArrayList<>(energyHistory));
    }
}