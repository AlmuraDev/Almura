/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.feature.exchange.MockOffer;
import com.almuradev.almura.feature.exchange.client.gui.component.UIExchangeOfferContainer;
import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPropertyBar;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIComplexImage;
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.UISaneTooltip;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.decoration.UILabel;
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
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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
        final UIExchangeOfferContainer offerContainer = new UIExchangeOfferContainer(this, getPaddedWidth(form), getPaddedHeight(form) - 20,
                Text.of(TextColors.WHITE, "Inventory"),
                Text.of(TextColors.WHITE, "Held Items"),
                InventoryItemComponent::new,
                InventoryItemComponent::new);
        offerContainer.setItemLimit(this.maxOfferSlots, UIDualListContainer.ContainerSide.RIGHT);
        offerContainer.register(this);

        // Populate swap container
        final EntityPlayer player = Minecraft.getMinecraft().player;
        offerContainer.setItems(player.inventory.mainInventory.stream()
                        .map(ItemStackUtil::fromNative)
                        .filter(i -> !i.isEmpty())
                        .map(i -> new MockOffer(i.copy(), player))
                        .collect(Collectors.toList()),
                UIDualListContainer.ContainerSide.LEFT);
        final int size = offerContainer.getItems(UIDualListContainer.ContainerSide.RIGHT).size();
        this.progressBar.setAmount(
                MathUtil.convertToRange(size, 0, this.maxOfferSlots, 0f, 1f));
        this.progressBar.setText(Text.of(size, "/", this.maxOfferSlots));

        form.add(this.progressBar, offerContainer, buttonOk, buttonCancel);

        addToScreen(form);
    }

    @Subscribe
    private void onUpdate(UIDualListContainer.UpdateEvent<UIDualListContainer<MockOffer>> event) {
        final int size = event.getComponent().getItems(UIDualListContainer.ContainerSide.RIGHT).size();
        this.progressBar.setAmount(MathUtil.convertToRange(size, 0, this.maxOfferSlots, 0f, 1f));
        this.progressBar.setText(Text.of(size, "/", this.maxOfferSlots));
    }

    private static final class InventoryItemComponent extends ExchangeScreen.ExchangeItemComponent<MockOffer> {
        private UIComplexImage image;
        private UILabel itemLabel;

        private InventoryItemComponent(final MalisisGui gui, final MockOffer tag) {
            super(gui, tag);
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void construct(final MalisisGui gui, final MockOffer tag) {
            // Add components
            final net.minecraft.item.ItemStack fakeStack = ItemStackUtil.toNative(tag.item);
            final EntityPlayer player = Minecraft.getMinecraft().player;
            final boolean useAdvancedTooltips = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

            this.image = new UIComplexImage(gui, fakeStack);
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
            this.itemLabel.setPosition(SimpleScreen.getPaddedX(this.image, 4), 0, Anchor.LEFT | Anchor.MIDDLE);

            this.add(this.image, this.itemLabel);
        }
    }
}
