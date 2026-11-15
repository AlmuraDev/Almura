/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client;

import com.almuradev.almura.feature.store.StoreSide;
import com.almuradev.almura.feature.store.network.StoreMessages.ItemView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.minecraft.MinecraftText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Items as the store screens show and find them, as 3.5's {@code ItemFinders}: what the player holds, a store's listings, and each creative
 * tab's items for store admins to list.
 */
final class StoreItems {

    /**
     * A place to find items to list, for the admin pane's Type: the store's own items, the player's inventory, or a creative tab.
     */
    record Finder(Text name, Supplier<List<ItemStack>> items) {
    }

    private StoreItems() {
    }

    /**
     * The admin pane's places to find items: the store's, the inventory's, then each creative tab's (by name).
     */
    static List<Finder> finders(final String storeId) {
        final List<Finder> finders = new ArrayList<>();
        finders.add(new Finder(Text.translatable("almura.feature.store.itemfinder.store"), () -> storeItems(storeId)));
        finders.add(new Finder(Text.translatable("almura.feature.store.itemfinder.inventory"), StoreItems::inventoryItems));
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), false, player.level().registryAccess());
        }
        CreativeModeTabs.allTabs().stream()
            .filter(tab -> tab.getType() == CreativeModeTab.Type.CATEGORY)
            .sorted(Comparator.comparing(tab -> tab.getDisplayName().getString()))
            .forEach(tab -> finders.add(new Finder(MinecraftText.of(tab.getDisplayName()), () -> List.copyOf(tab.getDisplayItems()))));
        return finders;
    }

    /**
     * The store's listed items, one of each.
     */
    private static List<ItemStack> storeItems(final String storeId) {
        final List<ItemStack> items = new ArrayList<>();
        for (final StoreSide side : StoreSide.values()) {
            for (final ItemView view : ClientStore.items(storeId, side)) {
                if (items.stream().noneMatch(item -> ItemStack.isSameItemSameComponents(item, view.stack()))) {
                    items.add(view.stack().copyWithCount(1));
                }
            }
        }
        return items;
    }

    /**
     * What the player holds (hotbar and main), one of each.
     */
    private static List<ItemStack> inventoryItems() {
        final List<ItemStack> items = new ArrayList<>();
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return items;
        }
        for (final ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && items.stream().noneMatch(item -> ItemStack.isSameItemSameComponents(item, stack))) {
                items.add(stack.copyWithCount(1));
            }
        }
        return items;
    }

    /**
     * @return how many of the item the player holds (hotbar and main)
     */
    static int held(final ItemStack item) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        int held = 0;
        for (final ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (ItemStack.isSameItemSameComponents(stack, item)) {
                held += stack.getCount();
            }
        }
        return held;
    }

    /**
     * @return the item's tooltip, as its slot in an inventory would show it
     */
    static Text tooltip(final ItemStack stack) {
        final Minecraft minecraft = Minecraft.getInstance();
        final List<Component> lines = minecraft.player == null ? List.of(stack.getHoverName())
            : stack.getTooltipLines(Item.TooltipContext.of(minecraft.player.level()), minecraft.player,
                minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
        return Text.join(Text.of("\n"), lines.stream().map(MinecraftText::of).toList());
    }

    /**
     * @return the item's name, shortened with "..." to fit the width with the suffix after it
     */
    static String fitName(final ItemStack stack, final String suffix, final int width) {
        final Font font = Minecraft.getInstance().font;
        final String name = stack.getHoverName().getString();
        if (font.width(name + suffix) <= width) {
            return name;
        }
        final StringBuilder fitted = new StringBuilder();
        for (final char c : name.toCharArray()) {
            if (font.width(fitted.toString() + c + "..." + suffix) > width) {
                break;
            }
            fitted.append(c);
        }
        return fitted + "...";
    }
}
