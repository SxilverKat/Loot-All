package com.cole.lootall.proxy;

import com.cole.lootall.client.ClientEvents;
import com.cole.lootall.client.KeyBindings;
import com.cole.lootall.client.TransferFeedback;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        KeyBindings.register();
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        MinecraftForge.EVENT_BUS.register(new TransferFeedback());
    }

    @Override
    public void showFeedback(final ITextComponent message, final ITextComponent transfer) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            @Override
            public void run() {
                TransferFeedback.show(message, transfer);
            }
        });
    }
}
