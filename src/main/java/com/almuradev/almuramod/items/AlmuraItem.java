package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class AlmuraItem extends Item {
   
    // Fake Item Registry
    public AlmuraItem(String name) {
        registerAlmuraItem(name, false, null, 1);
    }
    
    // Real Item Registry
    public AlmuraItem(String name, boolean showInCreativeTab, CreativeTabs creativeTabName, int maxStackSize) {
        registerAlmuraItem(name, showInCreativeTab, creativeTabName, maxStackSize);
    }
    
    public void registerAlmuraItem(String name, boolean showInCreativeTab, CreativeTabs creativeTabName, int maxStackSize) {
        if (showInCreativeTab) {
            setCreativeTab(creativeTabName);
        }
        System.out.println(name);
        this.setUnlocalizedName(name);        
        GameRegistry.registerItem(this, name);
    }
}
