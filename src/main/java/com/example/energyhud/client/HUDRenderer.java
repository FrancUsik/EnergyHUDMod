package com.example.energyhud.client;

import com.example.energyhud.network.ClientEnergyCache;
import com.example.energyhud.network.PacketHandler;
import com.example.energyhud.network.PacketRequestEnergy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.Queue;

public class HUDRenderer {

    private static final int MAX_HISTORY = 60;

    private final Queue<Double> energyDeltaHistory = new LinkedList<>();
    private double lastEnergy = -1;
    private long lastUpdateTime = 0;
    private double delta = 0;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null || mc.world == null) return;

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        PacketHandler.INSTANCE.sendToServer(new PacketRequestEnergy(pos));
        ClientEnergyCache.EnergyData data = ClientEnergyCache.get(pos);
        if (data == null || data.maxEnergy <= 0) return;

        double energy = data.energy;
        double maxEnergy = data.maxEnergy;

        long currentTime = System.currentTimeMillis();
        if (lastUpdateTime != 0) {
            double deltaT = (currentTime - lastUpdateTime) / 1000.0;
            double rawDelta = (energy - lastEnergy) / Math.max(deltaT, 0.01);
            if (Math.abs(rawDelta) > 0.01) delta = rawDelta;
        }

        lastUpdateTime = currentTime;
        lastEnergy = energy;

        energyDeltaHistory.add(delta);
        if (energyDeltaHistory.size() > MAX_HISTORY) energyDeltaHistory.poll();

        ScaledResolution res = new ScaledResolution(mc);
        int centerX = res.getScaledWidth() / 2;
        int topY = 10;

        int boxWidth = 180;
        int boxHeight = 80;

        drawNeonBox(centerX - boxWidth / 2, topY, boxWidth, boxHeight, 0x6600FFFF);

        int textX = centerX - boxWidth / 2 + 10;
        int textY = topY + 8;

        mc.fontRenderer.drawStringWithShadow("§b⚡ Энергия ⚡", centerX - 30, textY, 0x00FFFF);
        textY += 12;

        drawEnergyBar(centerX - 70, textY + 8, 140, 12, energy, maxEnergy);
        textY += 20;

        String energyText = formatNumber(energy) + " / " + formatNumber(maxEnergy) + " RF";
        mc.fontRenderer.drawStringWithShadow(energyText, centerX - 50, textY, 0xFFFFFF);
        textY += 10;

        int deltaColor = delta > 0 ? 0x55FF55 : delta < 0 ? 0xFF5555 : 0xAAAAAA;
        String deltaText = "∆: " + formatNumber(delta) + " RF/s";
        mc.fontRenderer.drawStringWithShadow(deltaText, centerX - 50, textY, deltaColor);

        if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            drawDeltaGraph(centerX - 70, topY + 60, 140, 20);
        }
    }

    private void drawNeonBox(int x, int y, int width, int height, int color) {
        drawRect(x, y, x + width, y + 1, color);
        drawRect(x, y + height - 1, x + width, y + height, color);
        drawRect(x, y, x + 1, y + height, color);
        drawRect(x + width - 1, y, x + width, y + height, color);
    }

    private void drawEnergyBar(int x, int y, int width, int height, double energy, double max) {
        int barWidth = (int) ((energy / max) * width);
        int color = 0xFF00FFFF;
        drawRect(x, y, x + barWidth, y + height, color);
    }

    private void drawDeltaGraph(int x, int y, int width, int height) {
        Double[] history = energyDeltaHistory.toArray(new Double[0]);
        double maxDelta = 0;
        for (double d : history) maxDelta = Math.max(maxDelta, Math.abs(d));
        if (maxDelta < 0.01) return;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor4f(0f, 1f, 1f, 1f);
        for (int i = 0; i < history.length; i++) {
            double d = history[i];
            float nx = x + ((float) i / MAX_HISTORY) * width;
            float ny = y + height / 2 - (float) ((d / maxDelta) * (height / 2));
            GL11.glVertex2f(nx, ny);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawRect(int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.Gui.drawRect(x1, y1, x2, y2, color);
    }

    private String formatNumber(double value) {
        String[] suffixes = {"RF", "kRF", "MRF", "GRF", "TRF"};
        int index = 0;
        while (value >= 1000 && index < suffixes.length - 1) {
            value /= 1000.0;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }
}
