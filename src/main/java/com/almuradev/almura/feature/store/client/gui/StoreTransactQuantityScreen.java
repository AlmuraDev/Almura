/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store.client.gui;

import com.almuradev.almura.feature.store.Store;
import com.almuradev.almura.feature.store.client.ClientStoreManager;
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class StoreTransactQuantityScreen extends BasicScreen {

  @Inject private static ClientStoreManager storeManager;

  private final Store store;
  private final StoreItem transactItem;
  private final StoreScreen.SideType sideType;
  private final int maxTransactable;

  private UIButton buttonTransact, buttonCancel;
  private UILabel quantityLabel, perItemLabel, perItemValueLabel, totalLabel, totalValueLabel;
  private BasicTextBox quantityTextBox;
  private BasicForm form;

  public StoreTransactQuantityScreen(final StoreScreen parent, final Store store, final StoreItem transactItem, StoreScreen.SideType sideType) {
    super(parent, true);

    this.store = store;
    this.transactItem = transactItem;
    this.sideType = sideType;

    // Establish the max transactable (is that even a word?)
    // If the item quantity is -1 then it is infinite and we'll allow up to 9999
    final int itemQuantity = this.transactItem.getQuantity() == -1 ? 9999 : this.transactItem.getQuantity();
    if (this.sideType == StoreScreen.SideType.BUY) {
      this.maxTransactable = itemQuantity;
    } else {
      // Establish how much we have of this item in the player's main inventory
      final int heldQuantity = Minecraft.getMinecraft().player.inventory.mainInventory
        .stream()
        .filter(i -> ItemStack.areItemsEqual(i, this.transactItem.asRealStack()))
        .mapToInt(ItemStack::getCount)
        .sum();

      // Use the lower of the two values
      this.maxTransactable = Math.min(heldQuantity, itemQuantity);
    }
  }

  @Override
  public void construct() {
    this.guiscreenBackground = false;

    this.form = new BasicForm(this, 130, 90, I18n.format("almura.feature.exchange.title.enter_a_quantity"));
    this.form.setZIndex(10); // Fixes issue overlapping draws from parent
    this.form.setBackgroundAlpha(255);

    this.quantityLabel = new UILabel(this, I18n.format("almura.feature.common.text.quantity") + ":");
    this.quantityLabel.setFontOptions(FontColors.WHITE_FO);
    this.quantityLabel.setPosition(0, 2);

    this.quantityTextBox = new BasicTextBox(this, "1");
    this.quantityTextBox.setAcceptsReturn(false);
    this.quantityTextBox.setOnEnter(tb -> this.transact());
    this.quantityTextBox.setSize(45, 0);
    this.quantityTextBox.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
    this.quantityTextBox.setFilter(s -> String.valueOf(MathUtil.squashi(Integer.valueOf(s.replaceAll("[^\\d]", "")), 1, this.maxTransactable)));
    this.quantityTextBox.setPosition(0, 0);
    this.quantityTextBox.register(this);

    this.perItemLabel = new UILabel(this, "Per:");
    this.perItemLabel.setPosition(0, BasicScreen.getPaddedY(this.quantityTextBox, 8));
    this.perItemLabel.setFontOptions(FontColors.WHITE_FO);

    this.perItemValueLabel = new UILabel(this, "");
    this.perItemValueLabel.setPosition(0, this.perItemLabel.getY(), Anchor.TOP | Anchor.RIGHT);
    this.perItemValueLabel.setFontOptions(FontColors.WHITE_FO);

    this.totalLabel = new UILabel(this, I18n.format("almura.feature.common.text.total") + ":");
    this.totalLabel.setPosition(0, BasicScreen.getPaddedY(this.perItemLabel, 4));
    this.totalLabel.setFontOptions(FontColors.WHITE_FO);

    this.totalValueLabel = new UILabel(this, "");
    this.totalValueLabel.setPosition(0, this.totalLabel.getY(), Anchor.TOP | Anchor.RIGHT);
    this.totalValueLabel.setFontOptions(FontColors.WHITE_FO);

    this.buttonTransact = new UIButtonBuilder(this)
      .text(I18n.format("almura.feature.common.button." + sideType.name().toLowerCase()))
      .width(40)
      .position(-2, -2)
      .anchor(Anchor.RIGHT | Anchor.BOTTOM)
      .onClick(this::transact)
      .build("button.transact");

    this.buttonCancel = new UIButtonBuilder(this)
      .text(I18n.format("almura.button.cancel"))
      .width(40)
      .position(BasicScreen.getPaddedX(this.buttonTransact, 2, Anchor.RIGHT), -2)
      .anchor(Anchor.RIGHT | Anchor.BOTTOM)
      .onClick(this::close)
      .build("button.cancel");

    this.form.add(this.quantityLabel, this.quantityTextBox,
      this.perItemLabel, this.perItemValueLabel,
      this.totalLabel, this.totalValueLabel,
      this.buttonCancel, this.buttonTransact);

    this.updateControls(this.quantityTextBox.getText());

    this.addToScreen(this.form);

    this.quantityTextBox.focus();
    this.quantityTextBox.selectAll();
  }

  @Subscribe
  private void onTextChange(final ComponentEvent.ValueChange<UITextField, String> event) {
    this.updateControls(event.getNewValue());
  }

  private boolean validate(final String rawValue) {
    try {
      final int value = Integer.parseInt(rawValue);

      this.perItemValueLabel.setText(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(this.transactItem.getPrice()));
      this.totalValueLabel.setText(FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(
        this.transactItem.getPrice().doubleValue() * value));

      return value > 0 && value <= this.maxTransactable;
    } catch (final NumberFormatException e) {
      return false;
    }
  }

  private void updateControls(final String rawValue) {
    this.buttonTransact.setEnabled(this.validate(rawValue));
  }

  private void transact() {
    if (this.sideType == StoreScreen.SideType.BUY) {
      storeManager.buy(this.store.getId(), this.transactItem.getRecord(), Integer.valueOf(this.quantityTextBox.getText()));
    } else {
      storeManager.sell(this.store.getId(), this.transactItem.getRecord(), Integer.valueOf(this.quantityTextBox.getText()));
    }
    this.close();
  }
}
