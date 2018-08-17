/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import static com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen.DEFAULT_DECIMAL_FORMAT;
import static com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen.MILLION;
import static com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen.withSuffix;

import com.almuradev.almura.feature.exchange.client.ClientExchangeManager;
import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.InventoryAction;
import com.almuradev.almura.feature.exchange.client.gui.component.UIExchangeOfferContainer;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPropertyBar;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIComplexImage;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.UIExpandingLabel;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;

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
    private UIPropertyBar progressBar;
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
        final UIFormContainer form = new UIFormContainer(this, 400, 325, "Offer");
        form.setZIndex(10); // Fixes issue with combobox behind the form drawing text over the form
        form.setMovable(true);
        form.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        form.setBackgroundAlpha(255);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setPadding(4, 4);
        form.setTopPadding(20);

        // OK/Cancel buttons
        final UIButton buttonOk = new UIButtonBuilder(this)
            .width(40)
            .text("OK")
            .x(1)
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::transact)
            .build("button.ok");
        final UIButton buttonCancel = new UIButtonBuilder(this)
            .width(40)
            .text("Cancel")
            .x(getPaddedX(buttonOk, 2, Anchor.RIGHT))
            .anchor(Anchor.BOTTOM | Anchor.RIGHT)
            .onClick(this::close)
            .build("button.cancel");

        this.progressBar = new UIPropertyBar(this, getPaddedWidth(form) - buttonOk.getWidth() - buttonCancel.getWidth() - 4, 15);
        this.progressBar.setColor(Color.ofRgb(0, 130, 0).getRgb());
        this.progressBar.setPosition(-1, -1, Anchor.BOTTOM | Anchor.LEFT);
        this.progressBar.setText(Text.of(0, "/", this.limit));

        // Swap container
        this.offerContainer = new UIExchangeOfferContainer(this, getPaddedWidth(form), getPaddedHeight(form) - 20,
            Text.of(TextColors.WHITE, "Inventory"),
            Text.of(TextColors.WHITE, "Unlisted Items"),
            OfferItemComponent::new,
            OfferItemComponent::new);
        this.offerContainer.setItemLimit(this.limit - this.getUsedLimit(), UIDualListContainer.SideType.RIGHT);
        this.offerContainer.register(this);

        // Populate offer container
        final List<VanillaStack> inventoryOffers = new ArrayList<>();
        Minecraft.getMinecraft().player.inventory.mainInventory.stream().filter(i -> !i.isEmpty()).forEach(i -> inventoryOffers.add(new BasicVanillaStack(i)));

        this.offerContainer.setItems(this.pendingItems, UIDualListContainer.SideType.RIGHT);
        this.offerContainer.setItems(inventoryOffers, UIDualListContainer.SideType.LEFT);

        this.updateProgressBar();

        form.add(this.progressBar, this.offerContainer, buttonOk, buttonCancel);

        addToScreen(form);
    }

    @Subscribe
    private void onTransaction(UIExchangeOfferContainer.TransactionCompletedEvent event) {
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
        if (removed < event.stack.getQuantity()) { // To Inventory logic
            final int toadd = event.stack.getQuantity() - removed;
            if (direction == InventoryAction.Direction.TO_INVENTORY) {

                // Add a new action or add the quantity to an existing one.
                final InventoryAction existingAction = this.inventoryActions.stream()
                        .filter(a -> UIExchangeOfferContainer.TransferType.isStackEqualIgnoreSize(a.getStack(), event.stack) && a.getDirection() == direction).findAny().orElse(null);

                if (existingAction == null) {
                    final InventoryAction newAction = new InventoryAction(direction, event.stack);
                    newAction.getStack().setQuantity(toadd);
                    this.inventoryActions.add(newAction);
                } else {
                    existingAction.getStack().setQuantity(existingAction.getStack().getQuantity() + event.stack.getQuantity());
                }
            } else { // To Listing logic
                this.inventoryActions.add(new InventoryAction(direction, event.stack));
            }
        }

        this.updateProgressBar();
    }

    private void transact() {
        if (!this.inventoryActions.isEmpty()) {
            clientExchangeManager.updateListItems(this.exchange.getId(), this.inventoryActions);
        }
        this.close();
    }

    private int getForSaleCount() {
        return ((ExchangeScreen) this.parent.get()).listItemList.getItems()
                .stream()
                .filter(i -> i.getForSaleItem().isPresent())
                .collect(Collectors.toList()).size();
    }

    private int getUsedLimit() {
        return this.offerContainer.getItems(UIDualListContainer.SideType.RIGHT).size() + this.getForSaleCount();
    }

    private void updateProgressBar() {
        final int size = this.getUsedLimit();
        this.progressBar.setAmount(MathUtil.convertToRange(size, 0, this.limit, 0f, 1f));
        this.progressBar.setText(Text.of(size, "/", this.limit));
    }

    private static class OfferItemComponent extends UIDynamicList.ItemComponent<VanillaStack> {

        private UIComplexImage image;
        private UIExpandingLabel itemLabel;
        private int lastKnownQuantity;
        private String itemLabelText;
        private String itemQuantityText;

        public OfferItemComponent(final MalisisGui gui, final VanillaStack stack) {
            super(gui, stack);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui) {
            this.setSize(0, 24);

            // Add components
            final net.minecraft.item.ItemStack fakeStack = this.item.asRealStack();
            fakeStack.setCount(1);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIComplexImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final int maxItemTextWidth = fontRenderer.getStringWidth("999999999999999999") + 4;

            // Limit item name to prevent over drawing
            String displayName = fakeStack.getDisplayName();
            if (fontRenderer.getStringWidth(displayName) > maxItemTextWidth) {
                final StringBuilder displayNameBuilder = new StringBuilder();
                for (char c : fakeStack.getDisplayName().toCharArray()) {
                    final int textWidth = fontRenderer.getStringWidth(displayNameBuilder.toString() + c);
                    if (textWidth > maxItemTextWidth) {
                        displayNameBuilder.replace(displayNameBuilder.length() - 3, displayNameBuilder.length(), "...");
                        break;
                    }
                    displayNameBuilder.append(c);
                }
                displayName = displayNameBuilder.toString();
            }

            this.itemLabelText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.WHITE, displayName));
            this.itemQuantityText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                    Text.of(TextColors.GRAY, " x ", withSuffix(this.item.getQuantity())));

            this.itemLabel = new UIExpandingLabel(gui, this.itemLabelText + this.itemQuantityText);
            this.itemLabel.setPosition(SimpleScreen.getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            if (this.item.getQuantity() >= (int) MILLION) {
                this.itemLabel.setTooltip(new UISaneTooltip(gui, DEFAULT_DECIMAL_FORMAT.format(item.getQuantity())));
            }

            this.lastKnownQuantity = this.item.getQuantity();

            this.add(this.image, this.itemLabel);
        }

        @Override
        @SuppressWarnings("deprecation")
        public void drawForeground(final GuiRenderer renderer, final int mouseX, final int mouseY, final float partialTick) {
            // Update the item label if the quantity has changed to reflect the new quantity
            if (this.lastKnownQuantity != this.item.getQuantity()) {
                this.itemQuantityText = TextSerializers.LEGACY_FORMATTING_CODE.serialize(
                        Text.of(TextColors.GRAY, " x ", withSuffix(this.item.getQuantity())));
                this.itemLabel.setText(this.itemLabelText + this.itemQuantityText);
                this.lastKnownQuantity = this.item.getQuantity();
            }
            super.drawForeground(renderer, mouseX, mouseY, partialTick);
        }
    }
}
