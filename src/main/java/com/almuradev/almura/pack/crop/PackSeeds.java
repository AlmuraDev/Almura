package com.almuradev.almura.pack.crop;

import com.almuradev.almura.pack.ContentPack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemSeeds;

public class PackSeeds extends ItemSeeds {

    public PackSeeds(ContentPack pack, String identifier, String textureName, boolean showInCreativeTab,
                     String creativeTabName, Block crop, Block soil) {
        super(crop, soil);
    }
}
