package com.cole.lootall;

import com.cole.lootall.network.PacketHandler;
import com.cole.lootall.proxy.CommonProxy;
import com.cole.lootall.server.ServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = LootAll.MODID, name = LootAll.NAME, version = LootAll.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", useMetadata = true,
        guiFactory = "com.cole.lootall.client.GuiFactory")
public class LootAll {
    public static final String MODID = "lootall";
    public static final String NAME = "Loot All";
    public static final String VERSION = "1.1.0";

    @SidedProxy(clientSide = "com.cole.lootall.proxy.ClientProxy",
            serverSide = "com.cole.lootall.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        PacketHandler.register();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        proxy.init(event);
    }
}
