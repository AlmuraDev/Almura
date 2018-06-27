/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPropertyBar;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.UISimpleList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.container.UIListContainer;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ExchangeOfferScreen extends SimpleScreen {

    private final int maxOfferSlots = ThreadLocalRandom.current().nextInt(2, 5);
    private UIPropertyBar progressBar;

    public ExchangeOfferScreen(ExchangeScreen parent) {
        super(parent, true);
    }

    @Override
    public void construct() {
        this.guiscreenBackground = false;

        // Form
        final UIFormContainer form = new UIFormContainer(this, 400, 325, "Offer");
        form.setZIndex(10); // Fixes issues with text on combobox from parent, doesn't help with UIImages from parent
        form.setMovable(true);
        form.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        form.setBackgroundAlpha(215);
        form.setBorder(FontColors.WHITE, 1, 185);
        form.setPadding(4, 4);
        form.setTopPadding(20);

        // OK/Cancel buttons
        final UIButton buttonOk = new UIButtonBuilder(this)
                .width(40)
                .text("OK")
                .x(1)
                .anchor(Anchor.BOTTOM | Anchor.RIGHT)
                .onClick(this::close)
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
        final SwapContainer swapContainer = new SwapContainer(this, getPaddedWidth(form), getPaddedHeight(form) - 20,
                Text.of(TextColors.WHITE, "Inventory"), Text.of(TextColors.WHITE, "Offers"),
                (sc) -> {
                    this.progressBar.setAmount(MathUtil.convertToRange(sc.pendingOffers.size(), 0, this.maxOfferSlots, 0f, 1f));
                    this.progressBar.setText(Text.of(sc.pendingOffers.size(), "/", this.maxOfferSlots));
                });

        form.add(this.progressBar, swapContainer, buttonOk, buttonCancel);

        addToScreen(form);
    }

    private final class SwapContainer extends UIBackgroundContainer {

        private final List<ExchangeScreen.MockOffer> pendingOffers = new ArrayList<>();
        private final List<ExchangeScreen.MockOffer> inventoryOffers = new ArrayList<>();
        private final UIButton buttonToLeft, buttonAllToLeft, buttonToRight, buttonAllToRight;
        private final UIBackgroundContainer middleContainer;
        private final UISimpleList<ExchangeScreen.MockOffer> leftItemList, rightItemList;
        private final Consumer<SwapContainer> onActionConsumer;

        @SuppressWarnings("deprecation")
        public SwapContainer(MalisisGui gui, int width, int height, Text leftContainerTitle, Text rightContainerTitle, Consumer<SwapContainer>
                onActionConsumer) {
            super(gui, width, height);

            this.onActionConsumer = onActionConsumer;

            this.setBorder(FontColors.WHITE, 1, 185);
            this.setBackgroundAlpha(0);

            // Left container
            final UIBackgroundContainer leftContainer = new UIBackgroundContainer(gui, (this.width - 30) / 2, UIComponent.INHERITED);
            leftContainer.setBackgroundAlpha(0);
            leftContainer.setPadding(4, 4);
            leftContainer.setTopPadding(20);

            final UILabel leftContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(leftContainerTitle));
            leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

            this.leftItemList = new UISimpleList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
            this.leftItemList.setComponentFactory(InventoryListElement::new);
            this.leftItemList.setElementSpacing(1);
            this.leftItemList.setName("list.left");
            this.leftItemList.register(this);

            // THIS IS TEST CODE
            final EntityPlayer player = Minecraft.getMinecraft().player;
            this.inventoryOffers.addAll(player.inventory.mainInventory.stream()
                    .filter(i -> i.item != Items.AIR)
                    .map(i -> new ExchangeScreen.MockOffer((ItemStack) (Object) i, player))
                    .collect(Collectors.toList()));

            this.leftItemList.setElements(this.inventoryOffers);
            // NO LONGER TEST CODE

            leftContainer.add(leftContainerLabel, this.leftItemList);

            // Right container
            final UIBackgroundContainer rightContainer = new UIBackgroundContainer(gui, (this.width - 30) / 2, UIComponent.INHERITED);
            rightContainer.setBackgroundAlpha(0);
            rightContainer.setPadding(4, 4);
            rightContainer.setTopPadding(20);
            rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

            final UILabel rightContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(rightContainerTitle));
            rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

            this.rightItemList = new UISimpleList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
            this.rightItemList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
            this.rightItemList.setComponentFactory(InventoryListElement::new);
            this.rightItemList.setElementSpacing(1);
            this.rightItemList.setName("list.right");
            this.rightItemList.register(this);

            rightContainer.add(rightContainerLabel, this.rightItemList);

            // Middle container
            this.middleContainer = new UIBackgroundContainer(gui, 28, 92);
            this.middleContainer.setPadding(0, 3);
            this.middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
            this.middleContainer.setBorder(FontColors.WHITE, 1, 185);
            this.middleContainer.setBackgroundAlpha(0);

            this.buttonAllToRight = new UIButtonBuilder(gui)
                    .size(20)
                    .position(0, 0)
                    .anchor(Anchor.TOP | Anchor.CENTER)
                    .text("->|")
                    .tooltip("Move all to right container")
                    .onClick(() -> this.migrateAll(this.inventoryOffers, this.pendingOffers, maxOfferSlots - this.pendingOffers.size()))
                    .enabled(false)
                    .build("button.all_to_right");

            this.buttonToRight = new UIButtonBuilder(gui)
                    .size(20)
                    .position(0, getPaddedY(this.buttonAllToRight, 2))
                    .anchor(Anchor.TOP | Anchor.CENTER)
                    .text("->")
                    .tooltip("Move to right container")
                    .onClick(() -> this.migrateOne(this.inventoryOffers, this.pendingOffers, this.leftItemList.getSelected()))
                    .enabled(false)
                    .build("button.to_right");

            this.buttonToLeft = new UIButtonBuilder(gui)
                    .size(20)
                    .position(0, getPaddedY(this.buttonToRight, 2))
                    .anchor(Anchor.TOP | Anchor.CENTER)
                    .text("<-")
                    .tooltip("Move to left container")
                    .onClick(() -> this.migrateOne(this.pendingOffers, this.inventoryOffers, this.rightItemList.getSelected()))
                    .enabled(false)
                    .build("button.to_left");

            this.buttonAllToLeft = new UIButtonBuilder(gui)
                    .size(20)
                    .position(0, getPaddedY(this.buttonToLeft, 2))
                    .anchor(Anchor.TOP | Anchor.CENTER)
                    .text("|<-")
                    .tooltip("Move all to left container")
                    .onClick(() -> this.migrateAll(this.pendingOffers, this.inventoryOffers, this.pendingOffers.size()))
                    .enabled(false)
                    .build("button.all_to_left");

            this.middleContainer.add(this.buttonToRight, this.buttonAllToRight, this.buttonToLeft, this.buttonAllToLeft);

            this.add(leftContainer, this.middleContainer, rightContainer);

            this.updateControls(null, null);
        }

        @Override
        public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
            super.drawBackground(renderer, mouseX, mouseY, partialTick);

            final int startX = this.width / 2;
            final int halfHeight = ((this.height - this.middleContainer.getHeight()) - (this.borderSize * 2)) / 2;
            int startY = 1;

            // Draw: top -> middle_section
            renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
            startY += halfHeight;

            // Skip: middle_section
            startY += this.middleContainer.getHeight();

            // Draw: middle_section -> bottom
            renderer.drawRectangle(startX, startY, 50, 1, halfHeight, FontColors.WHITE, 185);
        }

        @Subscribe
        private void onElementSelect(UIListContainer.SelectEvent<ExchangeScreen.MockOffer> event) {
            this.updateControls(event.getNewValue(), event.getComponent());
        }

        private void migrateOne(List<ExchangeScreen.MockOffer> fromList, List<ExchangeScreen.MockOffer> toList, ExchangeScreen.MockOffer offer) {
            toList.add(offer);
            fromList.remove(offer);
            this.updateControls(null, null);
        }

        private void migrateAll(List<ExchangeScreen.MockOffer> fromList, List<ExchangeScreen.MockOffer> toList, int max) {
            final List<ExchangeScreen.MockOffer> limitedItemList = fromList.stream()
                    .limit(max)
                    .collect(Collectors.toList());
            toList.addAll(limitedItemList);
            fromList.removeAll(limitedItemList);
            this.updateControls(null, null);
        }

        private void updateControls(@Nullable ExchangeScreen.MockOffer selectedValue, @Nullable UIListContainer container) {
            final boolean isSelected = selectedValue != null;
            final boolean isLeft = container != null && container.getName().equalsIgnoreCase("list.left");
            final boolean isOffersFull = this.pendingOffers.size() == maxOfferSlots;

            this.buttonToLeft.setEnabled(!isLeft && isSelected);
            this.buttonToRight.setEnabled(isLeft && isSelected && !isOffersFull);
            this.buttonAllToLeft.setEnabled(!this.pendingOffers.isEmpty());
            this.buttonAllToRight.setEnabled(!this.inventoryOffers.isEmpty() && !isOffersFull);

            this.leftItemList.setElements(this.inventoryOffers);
            this.rightItemList.setElements(this.pendingOffers);

            // Unregister and re-register to avoid firing this event when deselecting from the other list
            this.leftItemList.unregister(this);
            this.leftItemList.select(null);
            this.leftItemList.register(this);
            this.rightItemList.unregister(this);
            this.rightItemList.select(null);
            this.rightItemList.register(this);

            this.onActionConsumer.accept(this);
        }
    }

    private static final class InventoryListElement extends ExchangeScreen.ExchangeListElement<ExchangeScreen.MockOffer> {
        private UIImage image;
        private UILabel itemLabel;

        private InventoryListElement(final MalisisGui gui, final ExchangeScreen.MockOffer tag) {
            super(gui, tag);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui, final ExchangeScreen.MockOffer tag) {
            // Add components
            final net.minecraft.item.ItemStack fakeStack = ItemStackUtil.toNative(tag.item);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIImage(gui, fakeStack);
            this.image.setPosition(0, 0, Anchor.LEFT | Anchor.MIDDLE);
            this.image.setTooltip(new UISaneTooltip(gui, String.join("\n", fakeStack.getTooltip(player, useAdvancedTooltips
                    ? ITooltipFlag.TooltipFlags.ADVANCED
                    : ITooltipFlag.TooltipFlags.NORMAL))));

            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final int maxItemTextWidth = fontRenderer.getStringWidth("999999999999999999");

            // Limit item name to prevent over drawing
            final StringBuilder itemTextBuilder = new StringBuilder();
            for (char c : (tag.item.getTranslation().get()).toCharArray()) {
                final int textWidth = fontRenderer.getStringWidth(itemTextBuilder.toString() + c);
                if (textWidth > maxItemTextWidth + 4) {
                    itemTextBuilder.replace(itemTextBuilder.length() - 3, itemTextBuilder.length(), "...");
                    break;
                }
                itemTextBuilder.append(c);
            }
            this.itemLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(Text.of(TextColors.WHITE, itemTextBuilder.toString())));
            this.itemLabel.setPosition(getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            this.add(this.image, this.itemLabel);
        }
    }
}
