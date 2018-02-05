package com.almuradev.almura.feature.crafting;

import com.almuradev.almura.shared.event.Witness;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemReturnHelper implements Witness {

    private boolean debug = true;

    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.craftMatrix instanceof InventoryCrafting)) {
            return;
        }

        for (int i = 0; i < 9; i++) {  // Scan the crafting matrix.
            if (event.craftMatrix.getStackInSlot(i) != null) {
                almuraItemCrafted(event.player, event.craftMatrix.getStackInSlot(i).getUnlocalizedName(), event.craftMatrix.getStackInSlot(i));
            }
        }
    }

    @SuppressWarnings("unused")
    private void almuraItemCrafted(EntityPlayer player, String name, ItemStack currentStack) {
        int damage = 0;
        ItemStack reUseItem = null;
        if (debug) {
            System.out.println("ItemReturnHelper: Name = " + name.toUpperCase());
        }
        switch (name.toUpperCase()) {
            case "ITEM.ALMURA.NORMAL.TOOL.GRINDER":
                reUseItem = new ItemStack(currentStack.getItem(), 1, currentStack.getMetadata());
                //damage = 1;  Not configurable yet.
                break;

            case "ITEM.ALMURA.NORMAL.INGREDIENTS.VINEGAR":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_BEEF":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_CHICKEN":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_FISH":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_MILK":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_TOMATO":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.BROTH_VEGETABLE":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.PASTA_WHITESAUCE":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.PASTA_REDSAUCE":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.VEGETABLEOIL":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.MATERIALS_SODAWATER":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.MAYONAISE":
                reUseItem = GameRegistry.makeItemStack("almura:norma/ingredient/glass_cruet", 0, 1, null);
                break;

            case "ITEM.ALMURA.NORMAL.INGREDIENTS.PEANUTBUTTER":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.JELLY_ORANGE":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.JELLY_APPLE":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.SALT":
            case "ITEM.ALMURA.NORMAL.INGREDIENTS.PEPPER":
                reUseItem = GameRegistry.makeItemStack("almura:norma/ingredient/glass_jar", 0, 1, null);
                break;

            default:
                return; //Exit this method because an above wasn't found.
        }

        if (reUseItem == null) {
            if (debug) {
                System.out.println("ItemReturnHelper: ItemStack is null");
            }
            return;
        }

        if (damage > 0) {
            reUseItem.damageItem(damage, player);
            if (!(reUseItem.getMetadata() < reUseItem.getMaxDamage())) {
                if (debug) {
                    System.out.println("ItemReturnHelper: Item Doesn't have a max damage value so return nothing");
                }
                return; // Item durability of returned exceeds maximum.
            }
        }

        boolean found = false;
        // Search player Inventory for a stack that already exists that matches what is about to be created.
        for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
            if (player.inventory.getStackInSlot(j) != null && player.inventory.getStackInSlot(j).isItemEqual(reUseItem)) {
                if (player.inventory.getStackInSlot(j).getCount() < player.inventory.getStackInSlot(j).getMaxStackSize()) {
                    player.inventory.getStackInSlot(j).setCount(player.inventory.getStackInSlot(j).getCount() + 1);
                    found = true;
                }
            }
        }

        if (!found) {
            // Nothing found, try and put it into the players inventory as a new stack.
            if (!player.inventory.addItemStackToInventory(reUseItem)) {
                // Couldn't put stack in inventory, its full.  Spawn EntityItem on ground with reUseItem stack.
                if (!player.world.isRemote) {
                    // Only create entities on the server else you'll get ghosts.
                    EntityItem item = new EntityItem(player.world, player.posX, player.posY, player.posZ, reUseItem);
                    player.world.spawnEntity(item);
                }
            }
        }
    }
}
