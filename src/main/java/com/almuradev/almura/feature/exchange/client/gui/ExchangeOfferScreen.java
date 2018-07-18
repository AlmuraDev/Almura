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
import com.almuradev.almura.shared.client.ui.component.UIFormContainer;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
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
                .onClick(this::lightenPockets)
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
                ExchangeScreen.BaseItemComponent::new,
                ExchangeScreen.BaseItemComponent::new);
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

    private void lightenPockets() {
        this.close();
    }
}
