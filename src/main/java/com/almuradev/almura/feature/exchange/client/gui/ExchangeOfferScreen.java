/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.InventoryAction;
import com.almuradev.almura.feature.exchange.client.gui.component.UIExchangeOfferContainer;
import com.almuradev.almura.shared.client.ui.component.UIForm;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class ExchangeOfferScreen extends SimpleScreen {

    @Inject private static ClientExchangeManager clientExchangeManager;

    private final Exchange exchange;
    private final List<InventoryAction> inventoryActions = new ArrayList<>();
    private final int limit;
    private UIExchangeOfferContainer offerContainer;
    private List<VanillaStack> pendingItems;

    public ExchangeOfferScreen(final ExchangeScreen parent, final Exchange exchange, final List<VanillaStack> pendingItems, final int limit) {
        super(parent, true);
        this.exchange = exchange;
        this.pendingItems = pendingItems;
        this.limit = limit;
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        // Form
        final UIForm form = new UIForm(this, 400, 325, I18n.format("almura.title.exchange.offer"));
        form.setZIndex(10); // Fixes issue overlapping draws from parent
        form.setBackgroundAlpha(255);

        // OK/Cancel buttons
        final UIButton buttonOk = new UIButtonBuilder(this)
            .width(40)
            .text(I18n.format("almura.button.ok"))
            .x(1)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::transact)
            .build("button.ok");
        final UIButton buttonCancel = new UIButtonBuilder(this)
            .width(40)
            .text(I18n.format("almura.button.cancel"))
            .x(getPaddedX(buttonOk, 2, Anchor.RIGHT))
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::close)
            .build("button.cancel");

        // Swap container
        final NonNullList<ItemStack> mainInventory = Minecraft.getMinecraft().player.inventory.mainInventory;
        final int totalItemsForSale = this.exchange.getForSaleItemsFor(Minecraft.getMinecraft().player.getUniqueID()).map(List::size).orElse(0);
        this.offerContainer = new UIExchangeOfferContainer(this, getPaddedWidth(form), getPaddedHeight(form) - 20,
                Text.of(TextColors.WHITE, I18n.format("almura.text.exchange.inventory")),
                Text.of(TextColors.WHITE, I18n.format("almura.text.exchange.unlisted_items")),
                mainInventory.size(),
                this.limit,
                totalItemsForSale);
        this.offerContainer.register(this);

        // Populate offer container
        final List<VanillaStack> inventoryOffers = new ArrayList<>();
        mainInventory.stream().filter(i -> !i.isEmpty() && i.getItem() != null).forEach(i -> inventoryOffers.add(new BasicVanillaStack(i)));

        this.offerContainer.setItems(this.pendingItems, UIDualListContainer.SideType.RIGHT);
        this.offerContainer.setItems(inventoryOffers, UIDualListContainer.SideType.LEFT);

        form.add(this.offerContainer, buttonOk, buttonCancel);

        addToScreen(form);
    }

    @Subscribe
    private void onTransactionComplete(UIExchangeOfferContainer.TransactionCompletedEvent event) {
        final InventoryAction.Direction direction = event.targetSide == UIDualListContainer.SideType.LEFT
                ? InventoryAction.Direction.TO_INVENTORY
                : InventoryAction.Direction.TO_LISTING;
        final InventoryAction.Direction oppositeDirection = event.targetSide == UIDualListContainer.SideType.LEFT
                ? InventoryAction.Direction.TO_LISTING
                : InventoryAction.Direction.TO_INVENTORY;

        // Filter out relevant actions
        final List<InventoryAction> filteredActions = this.inventoryActions.stream()
                .filter(a -> UIExchangeOfferContainer.TransferType.isStackEqualIgnoreSize(a.getStack(), event.stack) &&
                        a.getDirection() == oppositeDirection)
                .collect(Collectors.toList());

        // Determine what we need to remove
        int removed = 0;
        int toRemoveCount = event.stack.getQuantity();

        // If we can still remove more, iterate through all remaining stacks and attempt to pull what we can
        if (toRemoveCount > 0) {
            for (InventoryAction action : filteredActions) {
                // Stop if we don't have anymore to remove
                if (toRemoveCount <= 0) {
                    break;
                }

                // Determine how much we are taking
                final int toTake = Math.min(action.getStack().getQuantity(), toRemoveCount);
                removed += toTake;
                toRemoveCount -= toTake;

                // Take the amount
                action.getStack().setQuantity(action.getStack().getQuantity() - toTake);
                if (action.getStack().isEmpty()) {
                    this.inventoryActions.remove(action);
                }
            }
        }

        // If the items we've removed is less than the items we need to add then continue
        // Otherwise we'll balance to a net zero as this means that the items were once added from one direction to another.
        if (removed < event.stack.getQuantity()) {
            final int toAdd = event.stack.getQuantity() - removed;

            // Add a new action or add the quantity to an existing one.
            final InventoryAction existingAction = this.inventoryActions.stream()
                .filter(a -> UIExchangeOfferContainer.TransferType.isStackEqualIgnoreSize(a.getStack(), event.stack) && a.getDirection() == direction)
                .findAny()
                .orElse(null);

            if (existingAction == null) {
                final InventoryAction newAction = new InventoryAction(direction, event.stack);
                newAction.getStack().setQuantity(toAdd);
                this.inventoryActions.add(newAction);
            } else {
                existingAction.getStack().setQuantity(existingAction.getStack().getQuantity() + event.stack.getQuantity());
            }
        }
    }

    private void transact() {
        if (!this.inventoryActions.isEmpty()) {
            clientExchangeManager.updateListItems(this.exchange.getId(), this.inventoryActions);
        }
        this.close();
    }
}
