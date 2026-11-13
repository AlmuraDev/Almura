/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.crafting;

import com.almuradev.content.type.block.type.crop.CropBlock;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryEmpty;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

/**
 * Extra animal loot and harvest tweaks. Items handed back after crafting or eating (bottles, bowls, cruets, damaged knives, ...) are
 * Forge container items, configured with the content loader's {@code container} key.
 */
public class ItemReturnHelper implements Witness {

    @SubscribeEvent
    public void lootLoad(LootTableLoadEvent lootTableEvent) {
        if (lootTableEvent.getName().toString().equals("minecraft:entities/chicken")) {
            if (!GameRegistry.makeItemStack("almura:normal/ingredient/chickenleg_raw", 0, 1, null).isEmpty()) {
                LootEntry chicken_entry = new LootEntryItem(GameRegistry.makeItemStack("almura:normal/ingredient/chickenleg_raw", 0, 1, null).item, 100, 1, new LootFunction[0], new LootCondition[0], "chickenleg_raw");
                LootPool chicken_pool = new LootPool(new LootEntry[]{chicken_entry}, new LootCondition[0], new RandomValueRange(2), new RandomValueRange(0, 1), "chicken_table");
                lootTableEvent.getTable().addPool(chicken_pool);
            }
        }
        if (lootTableEvent.getName().toString().equals("minecraft:entities/cow")) {
            if (!GameRegistry.makeItemStack("almura:normal/ingredient/roastbeef_raw", 0, 1, null).isEmpty()) {
                LootEntry cow_entry = new LootEntryItem(GameRegistry.makeItemStack("almura:normal/ingredient/roastbeef_raw", 0, 1, null).item, 100, 1, new LootFunction[0], new LootCondition[0], "roastbeef_raw");
                LootPool cow_pool = new LootPool(new LootEntry[]{cow_entry}, new LootCondition[0], new RandomValueRange(2), new RandomValueRange(0, 1), "cow_table");
                lootTableEvent.getTable().addPool(cow_pool);
            }
        }
        if (lootTableEvent.getName().toString().equals("minecraft:entities/pig")) {
            if (!GameRegistry.makeItemStack("almura:normal/ingredient/porkbelly_raw", 0, 1, null).isEmpty()) {
                LootEntry pig0_entry = new LootEntryItem(GameRegistry.makeItemStack("almura:normal/ingredient/porkbelly_raw", 0, 1, null).item, 100, 1, new LootFunction[0], new LootCondition[0], "porkbelly_raw");
                LootPool pig0_pool = new LootPool(new LootEntry[]{pig0_entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0, 1), "pig_table0");
                lootTableEvent.getTable().addPool(pig0_pool);
            }

            if (!GameRegistry.makeItemStack("almura:normal/ingredient/hamshank", 0, 1, null).isEmpty()) {
                LootEntry pig1_entry = new LootEntryItem(GameRegistry.makeItemStack("almura:normal/ingredient/hamshank", 0, 1, null).item, 100, 1, new LootFunction[0], new LootCondition[0], "hamshank");
                LootPool pig1_pool = new LootPool(new LootEntry[]{pig1_entry}, new LootCondition[0], new RandomValueRange(2), new RandomValueRange(0, 1), "pig_table1");
                lootTableEvent.getTable().addPool(pig1_pool);
            }
        }
        if (lootTableEvent.getName().toString().equals("minecraft:entities/sheep")) {
            if (!GameRegistry.makeItemStack("almura:normal/ingredient/lambchop_raw", 0, 1, null).isEmpty()) {
                LootEntry sheep_entry = new LootEntryItem(GameRegistry.makeItemStack("almura:normal/ingredient/lambchop_raw", 0, 1, null).item, 100, 1, new LootFunction[0], new LootCondition[0], "lambchop_raw");
                LootPool sheep_pool = new LootPool(new LootEntry[]{sheep_entry}, new LootCondition[0], new RandomValueRange(2), new RandomValueRange(0, 1), "sheep_table");
                lootTableEvent.getTable().addPool(sheep_pool);
            }
        }
        // Wolves: raw wolf meat, 1-2 (adults only: children never drop loot)
        if (lootTableEvent.getName().toString().equals("minecraft:entities/wolf")) {
            final Item wolfMeat = almuraItem("normal/ingredient/wolfmeat_raw");
            if (wolfMeat != null) {
                final LootEntry entry = new LootEntryItem(wolfMeat, 100, 1, new LootFunction[0], new LootCondition[0], "wolfmeat_raw");
                lootTableEvent.getTable().addPool(new LootPool(new LootEntry[]{entry}, new LootCondition[0], new RandomValueRange(1, 2),
                        new RandomValueRange(0, 1), "wolf_table"));
            }
        }
        // Creepers: now and then (one in four) a Creeper Steak, the joke food that hurts whoever eats it
        if (lootTableEvent.getName().toString().equals("minecraft:entities/creeper")) {
            final Item creeperSteak = almuraItem("food/food/creepersteak");
            if (creeperSteak != null) {
                final LootEntry steak = new LootEntryItem(creeperSteak, 1, 0, new LootFunction[0], new LootCondition[0], "creepersteak");
                final LootEntry nothing = new LootEntryEmpty(3, 0, new LootCondition[0], "creepersteak_none");
                lootTableEvent.getTable().addPool(new LootPool(new LootEntry[]{steak, nothing}, new LootCondition[0], new RandomValueRange(1),
                        new RandomValueRange(0), "creeper_table"));
            }
        }
        // Fishing: clams join the catch (about one in eight fish), for clam chowder and pescatore pasta
        if (lootTableEvent.getName().toString().equals("minecraft:gameplay/fishing/fish")) {
            final Item clam = almuraItem("normal/ingredient/clam");
            final LootPool main = lootTableEvent.getTable().getPool("main");
            if (clam != null && main != null) {
                main.addEntry(new LootEntryItem(clam, 15, 0, new LootFunction[0], new LootCondition[0], "clam"));
            }
        }
    }

    @Nullable
    private static Item almuraItem(final String path) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("almura", path));
    }

    // Sunflower Destroy Action Modifier & charging the player food/drink for harvesting custom crops.
    @SubscribeEvent
    public void onHarvestBlock(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
        if (block instanceof CropBlock) {
            EntityPlayer player = event.getHarvester();
            if (player != null)
                // This is how we make players use more food/drink...
                player.addExhaustion(0.01F);
        }

        if (block instanceof BlockDoublePlant) {
            if (block.getMetaFromState(event.getState()) == 0) { // 0 = Sunflower Type.
                final ItemStack drop = GameRegistry.makeItemStack("almura:food/food/sunflowerseed", 0, 16, null);
                if (drop != ItemStack.EMPTY) {
                    event.getDrops().clear(); // Destroy the sunflower return.
                    event.getDrops().add(drop); //Return Sunflower seeds, yeh!
                }
            }
        }
    }
}
