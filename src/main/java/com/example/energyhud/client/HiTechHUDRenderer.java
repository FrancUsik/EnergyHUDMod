
package com.example.energyhud.client;

import com.example.energyhud.network.ClientEnergyCache;
import com.example.energyhud.network.PacketHandler;
import com.example.energyhud.network.PacketRequestEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HiTechHUDRenderer {

    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation("energyhud", "textures/gui/hud_hitech.png");
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation("energyhud", "textures/gui/frame.png");
    private static final ResourceLocation ICON_ENERGY = new ResourceLocation("energyhud", "textures/gui/icon_energy.png");
    private static final ResourceLocation ICON_DELTA = new ResourceLocation("energyhud", "textures/gui/icon_delta.png");

    private double lastEnergy = -1;
    private long lastUpdate = 0;
    private double delta = 0;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null || mc.world == null) return;

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        PacketHandler.INSTANCE.sendToServer(new PacketRequestEnergy(pos));
        ClientEnergyCache.EnergyData data = ClientEnergyCache.get(pos);
        if (data == null || data.maxEnergy <= 0) return;

        double energy = data.energy;
        double max = data.maxEnergy;

        long now = System.currentTimeMillis();
        if (lastUpdate != 0) {
            double dt = (now - lastUpdate) / 1000.0;
            double newDelta = (energy - lastEnergy) / Math.max(dt, 0.01);
            if (Math.abs(newDelta) > 0.1) delta = newDelta;
        }
        lastUpdate = now;
        lastEnergy = energy;

        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;

        GlStateManager.color(1f, 1f, 1f, 1f);

        mc.getTextureManager().bindTexture(HUD_TEXTURE);
        mc.ingameGUI.drawTexturedModalRect(centerX - 128, topY, 0, 0, 256, 64);

        mc.getTextureManager().bindTexture(ICON_ENERGY);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(centerX - 112, topY + 8, 0, 0, 16, 16, 64, 64);

        mc.getTextureManager().bindTexture(ICON_DELTA);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(centerX - 112, topY + 28, 0, 0, 16, 16, 64, 64);

        mc.fontRenderer.drawStringWithShadow("⚡ " + formatNumber(energy) + " / " + formatNumber(max), centerX - 90, topY + 10, 0x00FFFF);
        mc.fontRenderer.drawStringWithShadow("Δ " + formatNumber(delta) + " RF/s", centerX - 90, topY + 30, delta >= 0 ? 0x55FF55 : 0xFF5555);
    }

    private String formatNumber(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        while (value >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }
}
