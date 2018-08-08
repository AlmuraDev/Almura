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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SideOnly(Side.CLIENT)
public class ExchangeOfferScreen extends SimpleScreen {

    private final List<VanillaStack> toList = new ArrayList<>();
    private final List<VanillaStack> toInventory = new ArrayList<>();
    private final int maxOfferSlots = ThreadLocalRandom.current().nextInt(1, 5); // TODO: Server must instruct client to open this screen with the
                                                                                 // appropriate limit
    private UIExchangeOfferContainer offerContainer;
    private UIPropertyBar progressBar;

    public ExchangeOfferScreen(ExchangeScreen parent) {
        super(parent, true);
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
        this.progressBar.setText(Text.of(0, "/", this.maxOfferSlots));

        // Swap container
        this.offerContainer = new UIExchangeOfferContainer(this, getPaddedWidth(form), getPaddedHeight(form) - 20,
            Text.of(TextColors.WHITE, "Inventory"),
            Text.of(TextColors.WHITE, "Held Items"),
            OfferItemComponent::new,
            OfferItemComponent::new);
        this.offerContainer.setItemLimit(this.maxOfferSlots, UIDualListContainer.SideType.RIGHT);
        this.offerContainer.register(this);

        // Populate swap container
        final List<VanillaStack> inventoryOffers = new ArrayList<>();
        Minecraft.getMinecraft().player.inventory.mainInventory.stream().filter(i -> !i.isEmpty()).forEach(i -> inventoryOffers.add(new BasicVanillaStack(i)));

        this.offerContainer.setItems(inventoryOffers, UIDualListContainer.SideType.LEFT);

        final int size = this.offerContainer.getItems(UIDualListContainer.SideType.RIGHT).size();
        this.progressBar.setAmount(MathUtil.convertToRange(size, 0, this.maxOfferSlots, 0f, 1f));
        this.progressBar.setText(Text.of(size, "/", this.maxOfferSlots));

        form.add(this.progressBar, this.offerContainer, buttonOk, buttonCancel);

        addToScreen(form);
    }

    @Subscribe
    private void onTransaction(UIExchangeOfferContainer.TransactionCompletedEvent event) {
        final List<VanillaStack> targetList = event.targetSide == UIDualListContainer.SideType.LEFT ? this.toInventory : this.toList;

        final VanillaStack stack = targetList.stream()
                .filter(t -> ItemStackComparators.IGNORE_SIZE.compare((ItemStack) (Object) t.asRealStack(),
                        (ItemStack) (Object) event.stack.asRealStack()) == 0)
                .findAny()
                .orElseGet(() -> {
                    final VanillaStack newStack = event.stack.copy();
                    newStack.setQuantity(0);
                    targetList.add(newStack);
                    return newStack;
                });

        stack.setQuantity(stack.getQuantity() + event.quantity);

        if (stack.getQuantity() <= 0) {
            targetList.remove(stack);
        }

        final int size = offerContainer.getItems(UIDualListContainer.SideType.RIGHT).size();
        this.progressBar.setAmount(MathUtil.convertToRange(size, 0, this.maxOfferSlots, 0f, 1f));
        this.progressBar.setText(Text.of(size, "/", this.maxOfferSlots));

        System.out.println(">>> Side: " + event.targetSide);
        System.out.println(Arrays.toString(targetList.toArray()));
        System.out.println("==========");
    }

    private void transact() {
        // Only add for now
        if (parent.isPresent() && parent.get() instanceof ExchangeScreen) {
            //final ExchangeScreen excParent = (ExchangeScreen) parent.get();

            //excParent.forSaleList.clearItems();
            //excParent.forSaleList.setItems(new ArrayList<>(this.offerContainer.getItems(UIDualListContainer.SideType.RIGHT)));
        }
        this.close();
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
