package com.sxilverr.lootall.network;

import com.sxilverr.lootall.Config;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Config.MOD_ID)
public class LootAllNetwork {
    private static final String PROTOCOL = "1";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToServer(LootAllPacket.TYPE, LootAllPacket.CODEC, LootAllPacket::handle);
        registrar.playToServer(SetBlockTargetPacket.TYPE, SetBlockTargetPacket.CODEC, SetBlockTargetPacket::handle);
        registrar.playToServer(ClearTargetPacket.TYPE, ClearTargetPacket.CODEC, ClearTargetPacket::handle);
        registrar.playToServer(SetItemTargetPacket.TYPE, SetItemTargetPacket.CODEC, SetItemTargetPacket::handle);
        registrar.playToClient(LootFeedbackPacket.TYPE, LootFeedbackPacket.CODEC, LootFeedbackPacket::handle);
    }
}
