package com.cole.lootall.client;

import com.cole.lootall.Config;
import com.cole.lootall.LootAll;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class LootAllConfigGui extends GuiConfig {
    public LootAllConfigGui(GuiScreen parent) {
        super(parent, elements(), LootAll.MODID, false, false, "Loot All");
    }

    private static List<IConfigElement> elements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        Configuration config = Config.getConfig();
        for (String category : config.getCategoryNames()) {
            list.add(new ConfigElement(config.getCategory(category)));
        }
        return list;
    }
}
