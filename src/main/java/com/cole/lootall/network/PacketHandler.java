package com.cole.lootall.network;

import com.cole.lootall.LootAll;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(LootAll.MODID);

    private PacketHandler() {
    }

    public static void register() {
        INSTANCE.registerMessage(LootAllMessage.Handler.class, LootAllMessage.class, 0, Side.SERVER);
        INSTANCE.registerMessage(SetBlockTargetMessage.Handler.class, SetBlockTargetMessage.class, 1, Side.SERVER);
        INSTANCE.registerMessage(ClearTargetMessage.Handler.class, ClearTargetMessage.class, 2, Side.SERVER);
        INSTANCE.registerMessage(SetItemTargetMessage.Handler.class, SetItemTargetMessage.class, 3, Side.SERVER);
        INSTANCE.registerMessage(LootFeedbackMessage.Handler.class, LootFeedbackMessage.class, 4, Side.CLIENT);
    }
}
