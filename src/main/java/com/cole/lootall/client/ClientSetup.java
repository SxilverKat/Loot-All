package com.cole.lootall.client;

import com.cole.lootall.LootAll;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LootAll.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(LootAll.MODID, "loot_transfer_feedback"),
                TransferFeedback.OVERLAY);
    }
}
