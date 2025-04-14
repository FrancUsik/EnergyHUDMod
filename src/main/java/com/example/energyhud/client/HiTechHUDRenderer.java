package com.francusik.energyhud.client;

import com.francusik.energyhud.api.EnergyHUDFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.List;

public class HiTechHUDRenderer {
    private static final ResourceLocation HUD_TEXTURE = new ResourceLocation("energyhud", "textures/gui/hud_hitech.png");
    private static final ResourceLocation FRAME_TEXTURE = new ResourceLocation("energyhud", "textures/gui/frame.png");
    private static final ResourceLocation ICON_ENERGY = new ResourceLocation("energyhud", "textures/gui/icon_energy.png");
    private static final ResourceLocation ICON_DELTA = new ResourceLocation("energyhud", "textures/gui/icon_delta.png");

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.00");

    public static void renderHUD(float currentEnergy, float maxEnergy, float deltaRF, List<Float> history) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);

        int hudWidth = 128;
        int hudHeight = 64;

        int x = res.getScaledWidth() / 2 - hudWidth / 2;
        int y = 10;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();

        // HUD background
        mc.getTextureManager().bindTexture(HUD_TEXTURE);
        drawTexturedRect(x, y, hudWidth, hudHeight, 0, 0, 128, 64);

        // Frame overlay
        mc.getTextureManager().bindTexture(FRAME_TEXTURE);
        drawTexturedRect(x, y, hudWidth, hudHeight, 0, 0, 128, 64);

        // Icon: Energy
        mc.getTextureManager().bindTexture(ICON_ENERGY);
        drawTexturedRect(x + 6, y + 6, 16, 16, 0, 0, 16, 16);

        // Icon: Delta
        mc.getTextureManager().bindTexture(ICON_DELTA);
        drawTexturedRect(x + 6, y + 28, 16, 16, 0, 0, 16, 16);

        // Energy text
        String energyStr = FORMAT.format(currentEnergy) + " / " + FORMAT.format(maxEnergy) + " RF";
        mc.fontRenderer.drawStringWithShadow(energyStr, x + 26, y + 10, 0x00FFFF);

        // Delta text
        String deltaStr = (deltaRF >= 0 ? "+" : "") + FORMAT.format(deltaRF) + " RF/s";
        int deltaColor = deltaRF > 0 ? 0x00FF00 : deltaRF < 0 ? 0xFF5555 : 0xAAAAAA;
        mc.fontRenderer.drawStringWithShadow(deltaStr, x + 26, y + 32, deltaColor);

        // Energy progress bar (gradient)
        float percent = Math.min(currentEnergy / maxEnergy, 1.0f);
        int barWidth = (int) (percent * (hudWidth - 12));
        drawGradientBar(x + 6, y + 54, barWidth, 6, 0xFF00FF00, 0xFF0099FF);

        // Shift: draw history graph
        if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown() && history != null && history.size() > 1) {
            drawHistoryGraph(x + 6, y + 64, 116, 20, history);
        }

        GlStateManager.popMatrix();
    }

    private static void drawTexturedRect(int x, int y, int width, int height, int u, int v, int texW, int texH) {
        Tessellator tessellator = Tessellator.getInstance();
        GL11.glColor4f(1F, 1F, 1F, 1F);
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellator.getBuffer().pos(x, y + height, 0).tex(0, 1).endVertex();
        tessellator.getBuffer().pos(x + width, y + height, 0).tex(1, 1).endVertex();
        tessellator.getBuffer().pos(x + width, y, 0).tex(1, 0).endVertex();
        tessellator.getBuffer().pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

    private static void drawGradientBar(int x, int y, int width, int height, int colorStart, int colorEnd) {
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int a1 = (colorStart >> 24) & 0xFF;
        int r1 = (colorStart >> 16) & 0xFF;
        int g1 = (colorStart >> 8) & 0xFF;
        int b1 = colorStart & 0xFF;

        int a2 = (colorEnd >> 24) & 0xFF;
        int r2 = (colorEnd >> 16) & 0xFF;
        int g2 = (colorEnd >> 8) & 0xFF;
        int b2 = colorEnd & 0xFF;

        tessellator.getBuffer().pos(x, y + height, 0).color(r1, g1, b1, a1).endVertex();
        tessellator.getBuffer().pos(x + width, y + height, 0).color(r2, g2, b2, a2).endVertex();
        tessellator.getBuffer().pos(x + width, y, 0).color(r2, g2, b2, a2).endVertex();
        tessellator.getBuffer().pos(x, y, 0).color(r1, g1, b1, a1).endVertex();

        tessellator.draw();
    }

    private static void drawHistoryGraph(int x, int y, int width, int height, List<Float> history) {
        if (history.isEmpty()) return;
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < history.size(); i++) {
            float value = history.get(i);
            float px = x + (i / (float) history.size()) * width;
            float py = y + height - (value * height);
            tessellator.getBuffer().pos(px, py, 0).color(0, 255, 255, 255).endVertex();
        }

        tessellator.draw();
    }
}