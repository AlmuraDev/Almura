package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class AlmuraItem extends Item {
   
    // Fake Item Registry
    public AlmuraItem(String name) {
        registerAlmuraItem(name, false, null);
    }
    
    // Real Item Registry
    public AlmuraItem(String name, boolean showInCreativeTab, CreativeTabs creativeTabName) {
        registerAlmuraItem(name, showInCreativeTab, creativeTabName);
    }
    
    public void registerAlmuraItem(String name, boolean showInCreativeTab, CreativeTabs creativeTabName) {
        if (showInCreativeTab) {
            setCreativeTab(creativeTabName);
        }

        GameRegistry.registerItem(this, name);
    }
}
