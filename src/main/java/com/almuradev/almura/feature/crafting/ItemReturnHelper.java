/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.crafting;

import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemReturnHelper implements Witness {

    private boolean debug = false;

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
    }

    // Sunflower Destroy Action Modifier.
    @SubscribeEvent
    public void onHarvestBlock(BlockEvent.HarvestDropsEvent event) {
        Block block = event.getState().getBlock();
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

    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.craftMatrix instanceof InventoryCrafting)) {
            return;
        }

        for (int i = 0; i < 9; i++) {  // Scan the crafting matrix.
            if (event.craftMatrix.getStackInSlot(i) != null) {
                returnReusableItem(event.player, event.craftMatrix.getStackInSlot(i).getTranslationKey(), event.craftMatrix.getStackInSlot(i), debug);
            }
        }
    }

    public static void returnReusableItem(EntityPlayer player, String name, ItemStack currentStack, boolean debug) {
        int damage = 0;
        ItemStack reusableItem;
        if (debug) {
            System.out.println("ItemReturnHelper: Name = " + name.toUpperCase());
        }
        switch (name.toUpperCase()) {
            case "ITEM.ALMURA.NORMAL.TOOL.GRINDER":
                reusableItem = new ItemStack(currentStack.getItem(), 1, currentStack.getMetadata());
                damage = 1;
                break;

            case "ITEM.ALMURA.NORMAL.INGREDIENT.VINEGAR":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.YEAST":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_BEEF":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_CHICKEN":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_FISH":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_MILK":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_TOMATO":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.BROTH_VEGETABLE":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.CREAM":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.PASTA_WHITESAUCE":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.PASTA_REDSAUCE":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.VEGETABLEOIL":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.MATERIALS_SODAWATER":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.MAYONAISE":
                reusableItem = GameRegistry.makeItemStack("almura:normal/ingredient/glass_cruet", 0, 1, null);
                break;

            case "ITEM.ALMURA.NORMAL.INGREDIENT.PEANUTBUTTER":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.JELLY_ORANGE":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.JELLY_APPLE":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.SALT":
            case "ITEM.ALMURA.NORMAL.INGREDIENT.PEPPER":
                reusableItem = GameRegistry.makeItemStack("almura:normal/ingredient/glass_jar", 0, 1, null);
                break;

            case "ITEM.ALMURA.FOOD.DRINK.BEER_BEER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_BOREALIS":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_COWTAIL":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_DELVERSLIGHT":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_FTBROS_BEER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_FTBROS_LAGER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_FTBROS_PORTER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_FTBROS_STOUT":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_HARDGINGER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_HIGHLANDS":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_IRONBELLY":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_LAVAPAIL":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_NETHER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_PIGTROTTER":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_RIVERSIDE":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_ROCKBOTTOM":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_SOUTHERNGOLD":
            case "ITEM.ALMURA.FOOD.DRINK.BEER_WITHERSHINS":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_BLOODYMARY":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_COSMOPOLITAN":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_DAIQUIRI":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_FUZZYNAVEL":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_GIBSON":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_GIMLET":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_GINTONIC":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_GREYHOUND":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_HIGHBALL":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_HURRICANE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_KAMIKAZE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_KIR":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_KIRROYALE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_LEMONDROP":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MAITAI":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MARGARITA":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MARTINI":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MELONSOUR":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MIMOSA":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MINTJULEP":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_MOJITO":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_PARADISE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_PINACOLADA":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_ROBROY":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_ROSE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_RUMCOLA":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_SALTYDOG":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_SCREWDRIVER":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_SEABREEZE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_SEXONTHEBEACH":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_SIDECAR":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_TEQUILASUNRISE":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_VODKAGIMLET":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_WHISKEYSOUR":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_WHITELADY":
            case "ITEM.ALMURA.FOOD.DRINK.MIXED_ZOMBIE":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_BUMBO":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_GROG":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_MEAD":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_SPIDERBITE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_APPLE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_BANANA":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_BLACKBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_BLUEBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_CANTALOUPE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_CARROT":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_CHERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_CONCORD_GRAPE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_CRANBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_DRAGONFRUIT":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_GRAPEFRUIT":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_HONEYDEW":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_KIWI":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_LEMON":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_LIME":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_MIXEDGRAPE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_MIXEDVEGETABLE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_ORANGE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_PEACH":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_PINEAPPLE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_PLUM":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_RASPBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_STRAWBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_TOMATO":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_WATERMELON":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_WHITEGRAPE":
            case "ITEM.ALMURA.FOOD.DRINK.JUICE_YUMMYBERRY":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_APPLECIDER":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_EGGNOG":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_LEMONADE":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_LIMEADE":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_COLA":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_GINGERALE":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_GRAPE":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_LEMONLIME":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_ORANGE":
            case "ITEM.ALMURA.FOOD.DRINK.NONALC_SODA_ROOTBEER":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_HARDAPPLECIDER":
            case "ITEM.ALMURA.FOOD.DRINK.OTHER_SPIKEDEGGNOG":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_ABSINTHE":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_BRANDY":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_CASSIS":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_GIN":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_MELONLIQUEUR":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_PORT":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_RUM":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_SHERRY":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_TEQUILA":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_TRIPLESEC":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_VERMOUTH":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_VODKA":
            case "ITEM.ALMURA.FOOD.DRINK.SPIRIT_WHISKEY":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_OTHER_RICE":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_FIREBRAND":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_NETHERTEARS":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_PRIORY":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_PRIORY79":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_PRIORY84":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_SPICEDRUBY":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_SWEETWATER":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_RED_TABLE":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_ROSE_PRIORY":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_ROSE_TABLE":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_ROSE_TRIPLEROSE":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_DESSERT":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_FEYFIRE":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_ICEBRAND":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_PRIORY":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_SILVERDEW":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_SPARKLING":
            case "ITEM.ALMURA.FOOD.DRINK.WINE_WHITE_TABLE":
                reusableItem = GameRegistry.makeItemStack("minecraft:glass_bottle", 0, 1, null);
                break;

            default:
                return; //Exit this method because an above wasn't found.
        }

        if (reusableItem == ItemStack.EMPTY) {
            if (debug) {
                System.out.println("ItemReturnHelper: ItemStack is EMPTY");
            }
            return;
        }

        if (damage > 0) {
            reusableItem.damageItem(damage, player);
            if (!(reusableItem.getMetadata() < reusableItem.getMaxDamage())) {
                if (debug) {
                    System.out.println("ItemReturnHelper: Item Doesn't have a max damage value so return nothing");
                }
                return; // Item durability of returned exceeds maximum.
            }
        }

        boolean found = false;
        // Search player Inventory for a stack that already exists that matches what is about to be created.
        for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
            if (player.inventory.getStackInSlot(j) != null && player.inventory.getStackInSlot(j).isItemEqual(reusableItem)) {
                if (player.inventory.getStackInSlot(j).getCount() < player.inventory.getStackInSlot(j).getMaxStackSize()) {
                    player.inventory.getStackInSlot(j).setCount(player.inventory.getStackInSlot(j).getCount() + 1);
                    found = true;
                }
            }
        }

        if (!found) {
            // Nothing found, try and put it into the players inventory as a new stack.
            if (!player.inventory.addItemStackToInventory(reusableItem)) {
                // Couldn't put stack in inventory, its full.  Spawn EntityItem on ground with reUseItem stack.
                if (!player.world.isRemote) {
                    // Only create entities on the server else you'll get ghosts.
                    EntityItem item = new EntityItem(player.world, player.posX, player.posY, player.posZ, reusableItem);
                    player.world.spawnEntity(item);
                }
            }
        }
    }
}
