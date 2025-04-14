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

    private static final ResourceLocation ICON_ENERGY = new ResourceLocation("energyhud", "textures/gui/icon_energy.png");
    private static final ResourceLocation ICON_DELTA = new ResourceLocation("energyhud", "textures/gui/icon_delta.png");
    private static final ResourceLocation HUD_FRAME = new ResourceLocation("energyhud", "textures/gui/frame.png");
    private static final ResourceLocation HUD_BACKGROUND = new ResourceLocation("energyhud", "textures/gui/hud_hitech.png");

    private double lastEnergy = -1;
    private double displayedDelta = 0;
    private long lastUpdate = 0;
    private long lastDeltaCalcTime = 0;

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

        // Обновляем дельту раз в 250 мс
        if (now - lastDeltaCalcTime >= 250) {
            if (lastUpdate > 0) {
                double dt = (now - lastUpdate) / 1000.0;
                double targetDelta = (energy - lastEnergy) / Math.max(dt, 0.001);
                // Интерполяция дельты
                displayedDelta = lerp(displayedDelta, targetDelta, 0.25);
            }
            lastDeltaCalcTime = now;
        }

        lastUpdate = now;
        lastEnergy = energy;

        // Рендер
        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;
        int leftX = centerX - 64;

        // HUD background
        mc.getTextureManager().bindTexture(HUD_BACKGROUND);
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY, 0, 0, 128, 64, 128, 64);

        // Frame overlay
        mc.getTextureManager().bindTexture(HUD_FRAME);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY, 0, 0, 128, 64, 128, 64);

        // ICON ENERGY
        drawIconExact(ICON_ENERGY, leftX + 6, topY + 6);
        mc.fontRenderer.drawStringWithShadow(
                formatNumber(energy) + " / " + formatNumber(max) + " RF",
                leftX + 26, topY + 9, 0x00FFFF
        );

        // ICON DELTA
        drawIconExact(ICON_DELTA, leftX + 6, topY + 26);
        String deltaText = "Δ: " + formatNumber(displayedDelta) + " RF/s";
        mc.fontRenderer.drawStringWithShadow(
                deltaText,
                leftX + 26, topY + 29,
                displayedDelta > 0 ? 0x55FF55 : (displayedDelta < 0 ? 0xFF5555 : 0xAAAAAA)
        );
    }

    private void drawIconExact(ResourceLocation texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(
                x, y, 0, 0, 16, 16, 16, 16
        );
    }

    private String formatNumber(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        while (Math.abs(value) >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
