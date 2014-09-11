package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
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
        this.setUnlocalizedName(name);
        setTextureName(AlmuraMod.MOD_ID + ":" + name);     
        GameRegistry.registerItem(this, name);
        
    }
   
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("almuramod:"+this.getUnlocalizedName().substring(5));       
    }
}
