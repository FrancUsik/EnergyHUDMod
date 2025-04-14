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
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;

public class HiTechHUDRenderer {

    private static final ResourceLocation ICON_ENERGY = new ResourceLocation("energyhud", "textures/gui/icon_energy.png");
    private static final ResourceLocation ICON_DELTA = new ResourceLocation("energyhud", "textures/gui/icon_delta.png");
    private static final ResourceLocation HUD_FRAME = new ResourceLocation("energyhud", "textures/gui/frame.png");
    private static final ResourceLocation HUD_BACKGROUND = new ResourceLocation("energyhud", "textures/gui/hud_hitech.png");
    private static final ResourceLocation ENERGY_GRAPH = new ResourceLocation("energyhud", "textures/gui/energy_graph.png");

    private double smoothedDelta = 0;
    private double lastDeltaEnergy = -1;
    private long lastDeltaUpdate = 0;

    private LinkedList<Double> deltaHistory = new LinkedList<>();
    private static final int HISTORY_LIMIT = 100;

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

        if (lastDeltaEnergy >= 0 && now - lastDeltaUpdate >= 250) {
            double dt = (now - lastDeltaUpdate) / 1000.0;
            double rawDelta = (energy - lastDeltaEnergy) / Math.max(dt, 0.01);
            smoothedDelta = interpolate(smoothedDelta, rawDelta, 0.5);

            deltaHistory.addFirst(smoothedDelta);
            if (deltaHistory.size() > HISTORY_LIMIT) deltaHistory.removeLast();

            lastDeltaEnergy = energy;
            lastDeltaUpdate = now;
        }

        if (lastDeltaEnergy < 0) {
            lastDeltaEnergy = energy;
            lastDeltaUpdate = now;
        }

        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;
        int leftX = centerX - 64;

        // HUD background
        mc.getTextureManager().bindTexture(HUD_BACKGROUND);
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY, 0, 0, 128, 64, 128, 64);

        // HUD frame
        mc.getTextureManager().bindTexture(HUD_FRAME);
        mc.ingameGUI.drawModalRectWithCustomSizedTexture(leftX, topY, 0, 0, 128, 64, 128, 64);

        // ICON ENERGY (16x16)
        drawIconExact(ICON_ENERGY, leftX + 6, topY + 6);
        mc.fontRenderer.drawStringWithShadow(
                formatNumber(energy) + " / " + formatNumber(max) + " RF",
                leftX + 26, topY + 9, 0x00FFFF
        );

        // ICON DELTA (16x16)
        drawIconExact(ICON_DELTA, leftX + 6, topY + 26);
        String deltaText = "Δ: " + formatNumber(smoothedDelta) + " RF/s";
        mc.fontRenderer.drawStringWithShadow(
                deltaText, leftX + 26, topY + 29,
                smoothedDelta > 0 ? 0x55FF55 : 0xFF5555
        );

        // Draw delta history graph (optional: hold Shift)
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            drawGraph(leftX + 6, topY + 48, 116, 12);
        }
    }

    private void drawIconExact(ResourceLocation texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(
                x, y, 0, 0, 16, 16, 64, 64
        );
    }

    private void drawGraph(int x, int y, int w, int h) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ENERGY_GRAPH);
        GlStateManager.color(1f, 1f, 1f, 0.6f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, 512, 128);
    }

    private String formatNumber(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        while (Math.abs(value) >= 1000 && index < suffixes.length - 1) {
            value /= 1000.0;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }

    private double interpolate(double from, double to, double factor) {
        return from + (to - from) * factor;
    }
}