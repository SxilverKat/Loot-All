package com.sxilverr.lootall.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class TransferFeedback {
    private static final int DURATION = 60;
    private static final int FADE = 10;

    private static Component message;
    @Nullable
    private static Component transfer;
    private static int ticks;

    public static void show(Component messageText, @Nullable Component transferText) {
        message = messageText;
        transfer = transferText;
        ticks = DURATION;
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    public static final LayeredDraw.Layer OVERLAY = (graphics, deltaTracker) -> {
        if (ticks <= 0 || message == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int alpha = ticks >= FADE ? 255 : ticks * 255 / FADE;
        if (alpha < 8) {
            return;
        }
        int color = (alpha << 24) | 0xFFFFFF;
        int messageY = height - 72;
        graphics.drawString(mc.font, message, (width - mc.font.width(message)) / 2, messageY, color, true);
        if (transfer != null) {
            Component line = Component.translatable("message.lootall.transferred", transfer);
            float scale = 0.75F;
            float lineWidth = mc.font.width(line) * scale;
            graphics.pose().pushPose();
            graphics.pose().translate((width - lineWidth) / 2.0F, messageY + 11.0F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(mc.font, line, 0, 0, color, true);
            graphics.pose().popPose();
        }
    };
}
