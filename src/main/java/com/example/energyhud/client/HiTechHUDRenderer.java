
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
            if (Math.abs(newDelta) > 0.01) delta = newDelta;
        }
        lastUpdate = now;
        lastEnergy = energy;

        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;
        int leftX = centerX - 128;

        // HUD background
        mc.getTextureManager().bindTexture(HUD_BACKGROUND);
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY - 10, 0, 0, 256, 128, 256, 128);

        // Frame (optional overlay)
        mc.getTextureManager().bindTexture(HUD_FRAME);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY - 10, 0, 0, 256, 128, 256, 128);

        // Energy Icon and Text
        drawIcon(ICON_ENERGY, leftX + 10, topY, 16, 64);
        mc.fontRenderer.drawStringWithShadow(formatNumber(energy) + " / " + formatNumber(max) + " RF", leftX + 30, topY + 4, 0x00FFFF);

        // Delta Icon and Text
        drawIcon(ICON_DELTA, leftX + 10, topY + 24, 16, 64);
        String deltaText = "Δ: " + formatNumber(delta) + " RF/s";
        mc.fontRenderer.drawStringWithShadow(deltaText, leftX + 30, topY + 28, delta > 0 ? 0x55FF55 : 0xFF5555);
    }

    private void drawIcon(ResourceLocation texture, int x, int y, int drawSize, int textureSize) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, drawSize, drawSize, textureSize, textureSize);
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
