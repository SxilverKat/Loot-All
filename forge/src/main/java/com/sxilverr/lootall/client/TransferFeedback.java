package com.sxilverr.lootall.client;

import com.sxilverr.lootall.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
//? if >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;*/
//?}
//? if >=1.19 {
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
//?} else if >=1.17 {
/*import net.minecraftforge.client.gui.IIngameOverlay;*/
//?}

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

    //? if >=1.20 {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, width, height) -> render(graphics, width, height);
    //?} else if >=1.19 {
    /*public static final IGuiOverlay OVERLAY = (gui, poseStack, partialTick, width, height) -> render(poseStack, width, height);
    *///?} else if >=1.17 {
    /*public static final IIngameOverlay OVERLAY = (gui, poseStack, partialTick, width, height) -> render(poseStack, width, height);
    *///?}

    //? if >=1.20 {
    public static void render(GuiGraphics graphics, int width, int height) {
        if (ticks <= 0 || message == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        int alpha = ticks >= FADE ? 255 : ticks * 255 / FADE;
        if (alpha < 8) {
            return;
        }
        int color = (alpha << 24) | 0xFFFFFF;
        int messageY = height - 72;
        graphics.drawString(mc.font, message, (width - mc.font.width(message)) / 2, messageY, color, true);
        if (transfer != null) {
            Component line = Text.translatable("message.lootall.transferred", transfer);
            float scale = 0.75F;
            float lineWidth = mc.font.width(line) * scale;
            graphics.pose().pushPose();
            graphics.pose().translate((width - lineWidth) / 2.0F, messageY + 11.0F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(mc.font, line, 0, 0, color, true);
            graphics.pose().popPose();
        }
    }
    //?} else {
    /*public static void render(PoseStack poseStack, int width, int height) {
        if (ticks <= 0 || message == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        int alpha = ticks >= FADE ? 255 : ticks * 255 / FADE;
        if (alpha < 8) {
            return;
        }
        int color = (alpha << 24) | 0xFFFFFF;
        int messageY = height - 72;
        mc.font.drawShadow(poseStack, message, (width - mc.font.width(message)) / 2.0F, messageY, color);
        if (transfer != null) {
            Component line = Text.translatable("message.lootall.transferred", transfer);
            float scale = 0.75F;
            float lineWidth = mc.font.width(line) * scale;
            poseStack.pushPose();
            poseStack.translate((width - lineWidth) / 2.0F, messageY + 11.0F, 0.0F);
            poseStack.scale(scale, scale, 1.0F);
            mc.font.drawShadow(poseStack, line, 0, 0, color);
            poseStack.popPose();
        }
    }
    *///?}
}
