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
    private static final ResourceLocation HUD_GRAPH = new ResourceLocation("energyhud", "textures/gui/energy_graph.png");

    private static final int HUD_WIDTH = 128;
    private static final int HUD_HEIGHT = 64;

    private double lastEnergy = -1;
    private double displayedDelta = 0;
    private long lastDeltaUpdate = 0;
    private final long deltaUpdateInterval = 250;

    private final LinkedList<Double> deltaHistory = new LinkedList<>();
    private final int historySize = 40;

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

        // delta calculation with interpolation
        long now = System.currentTimeMillis();
        if (lastEnergy >= 0) {
            double dt = (now - lastDeltaUpdate) / 1000.0;
            if (now - lastDeltaUpdate >= deltaUpdateInterval) {
                double rawDelta = (energy - lastEnergy) / Math.max(dt, 0.01);
                displayedDelta += (rawDelta - displayedDelta) * 0.25; // interpolate
                lastDeltaUpdate = now;

                // update history
                if (deltaHistory.size() >= historySize) deltaHistory.removeFirst();
                deltaHistory.add(displayedDelta);
            }
        }
        lastEnergy = energy;

        ScaledResolution res = new ScaledResolution(mc);
        int leftX = res.getScaledWidth() / 2 - HUD_WIDTH / 2;
        int topY = 10;

        // Background
        mc.getTextureManager().bindTexture(HUD_BACKGROUND);
        drawTexturedRect(leftX, topY, HUD_WIDTH, HUD_HEIGHT, HUD_WIDTH, HUD_HEIGHT);

        // Frame
        mc.getTextureManager().bindTexture(HUD_FRAME);
        drawTexturedRect(leftX, topY, HUD_WIDTH, HUD_HEIGHT, HUD_WIDTH, HUD_HEIGHT);

        // ICON ENERGY
        drawIconExact(ICON_ENERGY, leftX + 6, topY + 6);
        mc.fontRenderer.drawStringWithShadow(
                formatNumber(energy) + " / " + formatNumber(max) + " RF",
                leftX + 26, topY + 9, 0x00FFFF
        );

        // ICON DELTA
        drawIconExact(ICON_DELTA, leftX + 6, topY + 26);
        String deltaText = "Δ: " + formatNumber(displayedDelta) + " RF/s";
        int color = displayedDelta > 0.01 ? 0x55FF55 : displayedDelta < -0.01 ? 0xFF5555 : 0xAAAAAA;
        mc.fontRenderer.drawStringWithShadow(deltaText, leftX + 26, topY + 29, color);

        // Graph on Shift
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            mc.getTextureManager().bindTexture(HUD_GRAPH);
            drawGraph(leftX + 8, topY + 48, 112, 10);
        }
    }

    private void drawTexturedRect(int x, int y, int w, int h, int texW, int texH) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, texW, texH);
    }

    private void drawIconExact(ResourceLocation texture, int x, int y) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedRect(x, y, 16, 16, 16, 16);
    }

    private void drawGraph(int x, int y, int width, int height) {
        if (deltaHistory.isEmpty()) return;
        GlStateManager.disableTexture2D();
        GlStateManager.color(0f, 1f, 0f, 1f);
        double max = deltaHistory.stream().mapToDouble(Math::abs).max().orElse(1);
        for (int i = 0; i < deltaHistory.size() - 1; i++) {
            double val1 = deltaHistory.get(i) / max;
            double val2 = deltaHistory.get(i + 1) / max;
            int x1 = x + i * width / historySize;
            int x2 = x + (i + 1) * width / historySize;
            int y1 = y + height / 2 - (int)(val1 * height / 2);
            int y2 = y + height / 2 - (int)(val2 * height / 2);
            drawLine(x1, y1, x2, y2);
        }
        GlStateManager.enableTexture2D();
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        GlStateManager.glBegin(1); // GL_LINES
        GlStateManager.glVertex3f(x1, y1, 0);
        GlStateManager.glVertex3f(x2, y2, 0);
        GlStateManager.glEnd();
    }

    private String formatNumber(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        value = Math.abs(value);
        while (value >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }
}
