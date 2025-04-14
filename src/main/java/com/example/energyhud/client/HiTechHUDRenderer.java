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
    private static final ResourceLocation GRAPH_OVERLAY = new ResourceLocation("energyhud", "textures/gui/energy_graph.png");

    private double lastEnergy = -1;
    private long lastUpdate = 0;
    private double smoothedDelta = 0;
    private final LinkedList<Double> deltaHistory = new LinkedList<>();

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
            double rawDelta = (energy - lastEnergy) / Math.max(dt, 0.01);

            smoothedDelta = smoothedDelta * 0.85 + rawDelta * 0.15;

            deltaHistory.add(smoothedDelta);
            if (deltaHistory.size() > 100) deltaHistory.removeFirst();
        }
        lastUpdate = now;
        lastEnergy = energy;

        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;
        int leftX = centerX - 64;

        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(HUD_BACKGROUND);
        drawTexturedRect(leftX, topY, 128, 64);

        mc.getTextureManager().bindTexture(HUD_FRAME);
        drawTexturedRect(leftX, topY, 128, 64);

        drawIconExact(ICON_ENERGY, leftX + 6, topY + 6);
        mc.fontRenderer.drawStringWithShadow(
                formatNumber(energy) + " / " + formatNumber(max) + " RF",
                leftX + 26, topY + 9, 0x00FFFF
        );

        drawIconExact(ICON_DELTA, leftX + 6, topY + 26);
        String deltaText = "Δ: " + formatNumber(smoothedDelta) + " RF/s";
        mc.fontRenderer.drawStringWithShadow(
                deltaText,
                leftX + 26, topY + 29,
                smoothedDelta > 0 ? 0x55FF55 : 0xFF5555
        );

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            mc.getTextureManager().bindTexture(GRAPH_OVERLAY);
            drawTexturedRect(leftX, topY + 46, 128, 16);
        }
        GlStateManager.disableBlend();
    }

    private void drawTexturedRect(int x, int y, int width, int height) {
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    private void drawIconExact(ResourceLocation texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
    }

    private String formatNumber(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        double absValue = Math.abs(value);
        while (absValue >= 1000 && index < suffixes.length - 1) {
            absValue /= 1000;
            value /= 1000;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }
}
