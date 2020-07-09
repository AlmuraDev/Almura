package com.almuradev.almura.feature.moc;

import com.almuradev.core.event.Witness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MocDrops implements Witness {

    boolean debug = false;

    @SubscribeEvent
    public void onGetDrops(final LivingDropsEvent event) {
        // This class was built to modify the drops list of MoCreature animals to fit the requirements of Almura.
        String entityName = event.getEntity().getName();
        Entity entity = event.getEntity();
        int minimum = 4;
        int quantityFound = 0;
        ItemStack dropStack = null;

        if (debug) {
            System.out.println("Name: " + entityName);
            System.out.println("PreDrops: " + event.getDrops());
        }

        // Turkey
        if (entityName.equalsIgnoreCase("turkey")) {
            minimum = 4;

            for (EntityItem item : event.getDrops()) {
                if (item.getItem().item.getRegistryName().toString().equalsIgnoreCase("mocreatures:turkeyraw")) {
                    quantityFound++;
                }
            }

            if (minimum - quantityFound > 0) {
                // Turkey's have a built in which we will use for our crafting recipe
                dropStack = GameRegistry.makeItemStack("mocreatures:turkeyraw", 0, minimum - quantityFound, null);
            }
        }

        // Boar
        if (entityName.equalsIgnoreCase("boar")) {
            minimum = 2;

            for (EntityItem item : event.getDrops()) {
                if (item.getItem().item.getRegistryName().toString().equalsIgnoreCase("minecraft:porkchop")) {
                    quantityFound++;
                }
            }

            if (minimum - quantityFound > 0) {
                // Turkey's have a built in which we will use for our crafting recipe
                dropStack = GameRegistry.makeItemStack("minecraft:porkchop", 0, minimum - quantityFound, null);
            }

            // Additional stack
            ItemStack additionalStack = GameRegistry.makeItemStack("almura:normal/ingredient/porkbelly_raw", 0, 3, null);
            if (additionalStack != null) {
                EntityItem entityAdditionalItem = new EntityItem(entity.world, entity.posX, entity.posY + (double) 0.0F, entity.posZ, additionalStack);
                event.getDrops().add(entityAdditionalItem);
            }
        }

        // Duck
        if (entityName.equalsIgnoreCase("duck")) {
            minimum = 3;

            // Ducks getDrops list doesn't contain the following item, no need to search existing drops.
            // Todo: this item still needs to be created on Almura's end
            //dropStack = GameRegistry.makeItemStack("almura:normal/ingredient/raw_duck", 0, minimum - quantityFound, null);

        }

        // Finish up and create additional drops
        if (dropStack != null && ((minimum - quantityFound) > 0)) {
            EntityItem entityItem = new EntityItem(entity.world, entity.posX, entity.posY + (double) 0.0F, entity.posZ, dropStack);
            event.getDrops().add(entityItem);
        }
    }
}
