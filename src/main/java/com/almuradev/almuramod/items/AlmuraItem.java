package com.almuradev.almuramod.items;

import com.almuradev.almuramod.AlmuraMod;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class AlmuraItem extends Item {
   
    /**
     * Create fake item for Creative Tabs images
     */
    public AlmuraItem(String name) {
        registerAlmuraItem(name, null, 1);
    }
    
    /**
     * Create items with generic parameters
     */
    public AlmuraItem(String name, CreativeTabs creativeTabName, int maxStackSize) {
        registerAlmuraItem(name, creativeTabName, maxStackSize);
    }
    
    public void registerAlmuraItem(String name, CreativeTabs creativeTabName, int maxStackSize) {
        if (creativeTabName != null) {
            setCreativeTab(creativeTabName);
        }

        this.setUnlocalizedName(name);
        setTextureName(AlmuraMod.MOD_ID + ":" + name);
        GameRegistry.registerItem(this, name);
    }
   
    /**
     * Setup item icon texture.
     */
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        this.itemIcon = par1IconRegister.registerIcon("almuramod:"+this.getUnlocalizedName().substring(5));       
    }
}
