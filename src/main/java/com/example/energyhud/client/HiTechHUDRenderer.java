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
    public void onRender(RenderGameOverlayEvent.Post event) {
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

        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(HUD_TEXTURE);

        int hudWidth = 256;
        int hudHeight = 128;

        mc.ingameGUI.drawTexturedModalRect(centerX - hudWidth / 2, topY, 0, 0, hudWidth, hudHeight);

        // Иконка энергии
        mc.getTextureManager().bindTexture(ICON_ENERGY);
        drawIcon(centerX - 110, topY + 20, 64, 64);

        // Иконка дельты
        mc.getTextureManager().bindTexture(ICON_DELTA);
        drawIcon(centerX - 110, topY + 50, 64, 64);

        // Текст энергии
        String energyStr = format(energy) + " / " + format(max) + " RF";
        mc.fontRenderer.drawStringWithShadow(energyStr, centerX - 80, topY + 25, 0x00FFFF);

        // Текст дельты
        int deltaColor = delta > 0 ? 0x55FF55 : delta < 0 ? 0xFF5555 : 0xAAAAAA;
        String deltaStr = "Δ: " + format(delta) + " RF/t";
        mc.fontRenderer.drawStringWithShadow(deltaStr, centerX - 80, topY + 55, deltaColor);
    }

    private void drawIcon(int x, int y, int texSize, int drawSize) {
        Minecraft.getMinecraft().ingameGUI.drawModalRectWithCustomSizedTexture(x, y, 0, 0, drawSize, drawSize, texSize, texSize);
    }

    private String format(double value) {
        String[] suffixes = {"", "k", "M", "G"};
        int index = 0;
        while (Math.abs(value) >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }
        return String.format("%.2f %s", value, suffixes[index]);
    }
}
