/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.items;

import com.almuradev.almura.items.wands.ChunkPopulateWand;

import com.almuradev.almura.items.wands.ChunkRegenWand;
import com.almuradev.almura.Almura;
import com.almuradev.almura.items.wands.ExplosionWand;
import com.almuradev.almura.items.wands.FireballWand;
import com.almuradev.almura.items.wands.InformationWand;
import com.almuradev.almura.items.wands.LightningWand;
import com.almuradev.almura.items.wands.MetadataWand;
import com.almuradev.almura.items.wands.WireframeWand;
import com.almuradev.almura.tabs.Tabs;

public class Items {

    public static final ExplosionWand WAND_EXPLOSION = new ExplosionWand("wand.explosion", "Explosion Wand", "explosion_wand", Tabs.TAB_TOOLS, 5);
    public static final FireballWand WAND_FIREBALL = new FireballWand("wand.fireball", "Fireball Wand", "fireball_wand", Tabs.TAB_TOOLS);
    public static final InformationWand WAND_INFORMATION = new InformationWand("wand.information", "information_wand", "information_wand", Tabs.TAB_TOOLS);    
    public static final LightningWand WAND_LIGHTNING = new LightningWand("wand.lightning", "Lightning Wand", "lightning_wand", Tabs.TAB_TOOLS);
    public static final MetadataWand WAND_METADATA = new MetadataWand("wand.metadata", "Metadata Wand", "metadata_wand", Tabs.TAB_TOOLS);
    public static final ExplosionWand WAND_NUCLEAR = new ExplosionWand("wand.nuclear", "Nuclear Wand", "nuclear_wand", Tabs.TAB_TOOLS, 60);
    public static final WireframeWand WAND_WIREFRAME = new WireframeWand("wand.wireframe", "Wireframe Wand", "wireframe_wand", Tabs.TAB_TOOLS);
    public static final ChunkRegenWand WAND_CHUNKREGEN = new ChunkRegenWand("wand.chunkregen", "ChunkRegen Wand", "chunkregen_wand", Tabs.TAB_TOOLS);
    public static final ChunkPopulateWand WAND_CHUNKPOPULATE = new ChunkPopulateWand("wand.chunkpopulate", "Chunk Populate Wand", "explosion_wand", Tabs.TAB_TOOLS);
    public static final FarmsAlmanac FARMERS_ALMANAC = new FarmsAlmanac("almanac.farmers", "Farmers Almanac", "farmers_almanac", Tabs.TAB_TOOLS);

    public static void fakeStaticLoad() {
        Almura.INTERNAL_PACK.addItem(WAND_EXPLOSION);
        Almura.INTERNAL_PACK.addItem(WAND_FIREBALL);
        Almura.INTERNAL_PACK.addItem(WAND_INFORMATION);
        Almura.INTERNAL_PACK.addItem(WAND_LIGHTNING);
        Almura.INTERNAL_PACK.addItem(WAND_METADATA);
        Almura.INTERNAL_PACK.addItem(WAND_NUCLEAR);
        Almura.INTERNAL_PACK.addItem(WAND_WIREFRAME);
        Almura.INTERNAL_PACK.addItem(WAND_CHUNKREGEN);
        Almura.INTERNAL_PACK.addItem(WAND_CHUNKPOPULATE);
        
        Almura.INTERNAL_PACK.addItem(FARMERS_ALMANAC);
    }
}
