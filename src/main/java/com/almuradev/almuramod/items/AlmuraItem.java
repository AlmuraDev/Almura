package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class AlmuraItem extends Item {
    public AlmuraItem(String name, boolean showInCreativeTab) {
        if (showInCreativeTab) {
            setCreativeTab(AlmuraMod.CREATIVE_TAB);
        }

        GameRegistry.registerItem(this, name);
    }
}
