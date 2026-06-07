package com.cole.lootall.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TransferFeedback {
    private static final int DURATION = 60;
    private static final int FADE = 10;

    private static ITextComponent message;
    private static ITextComponent transfer;
    private static int ticks;

    public static void show(ITextComponent messageText, ITextComponent transferText) {
        message = messageText;
        transfer = transferText;
        ticks = DURATION;
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        if (ticks <= 0 || message == null) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.hideGUI) {
            return;
        }
        int alpha = ticks >= FADE ? 255 : ticks * 255 / FADE;
        if (alpha < 8) {
            return;
        }
        int color = (alpha << 24) | 0xFFFFFF;
        ScaledResolution resolution = event.getResolution();
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        FontRenderer font = mc.fontRenderer;

        String messageText = message.getFormattedText();
        int messageY = height - 72;
        font.drawStringWithShadow(messageText, (width - font.getStringWidth(messageText)) / 2.0F, messageY, color);

        if (transfer != null) {
            ITextComponent line = new TextComponentTranslation("message.lootall.transferred", transfer);
            String lineText = line.getFormattedText();
            float scale = 0.75F;
            float lineWidth = font.getStringWidth(lineText) * scale;
            GlStateManager.pushMatrix();
            GlStateManager.translate((width - lineWidth) / 2.0F, messageY + 11.0F, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            font.drawStringWithShadow(lineText, 0, 0, color);
            GlStateManager.popMatrix();
        }
    }
}
