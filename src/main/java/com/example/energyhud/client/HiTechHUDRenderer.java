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

        GlStateManager.enableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        // --- HUD фон ---
        mc.getTextureManager().bindTexture(HUD_TEXTURE);
        mc.ingameGUI.drawTexturedModalRect(centerX - 128, topY, 0, 0, 256, 64);

        // --- Иконки (отображаются как 16x16 из 64x64 PNG) ---
        drawIcon(centerX - 120, topY + 5, ICON_ENERGY, 64, 64, 16, 16);
        drawIcon(centerX - 120, topY + 25, ICON_DELTA, 64, 64, 16, 16);

        // --- Текст ---
        String energyStr = formatNumber(energy) + " / " + formatNumber(max) + " RF";
        String deltaStr = "0\": " + formatNumber(delta) + " RF/s";
        int deltaColor = delta > 0 ? 0x55FF55 : delta < 0 ? 0xFF5555 : 0xFFFFAA00;

        mc.fontRenderer.drawStringWithShadow(energyStr, centerX - 95, topY + 8, 0x00FFFF);
        mc.fontRenderer.drawStringWithShadow(deltaStr, centerX - 95, topY + 28, deltaColor);

        GlStateManager.disableBlend();
    }

    private void drawIcon(int x, int y, ResourceLocation texture, int texWidth, int texHeight, int drawWidth, int drawHeight) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(
                x, y, 0, 0, drawWidth, drawHeight, texWidth, texHeight
        );
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
