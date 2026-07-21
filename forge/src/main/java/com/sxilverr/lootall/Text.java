package com.sxilverr.lootall;

import net.minecraft.network.chat.Component;
//? if <1.19 {
/*import net.minecraft.network.chat.TranslatableComponent;*/
//?}

public final class Text {
    private Text() {
    }

    public static Component translatable(String key, Object... args) {
        //? if >=1.19 {
        return Component.translatable(key, args);
        //?} else {
        /*return new TranslatableComponent(key, args);*/
        //?}
    }
}
