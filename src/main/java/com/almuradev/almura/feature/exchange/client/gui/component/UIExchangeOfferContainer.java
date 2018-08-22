/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.feature.hud.screen.origin.component.panel.UIPropertyBar;
import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.item.VirtualStack;
import com.almuradev.almura.shared.util.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIExchangeOfferContainer extends UIDualListContainer<VanillaStack> {

    private final int leftLimit, rightLimit, listedItems;

    private UIButton buttonSingle, buttonStack, buttonType, buttonAll;
    private UILabel labelDirection;
    private UIPropertyBar leftPropertyBar, rightPropertyBar;
    private SideType targetSide = SideType.RIGHT;

    public UIExchangeOfferContainer(final MalisisGui gui, final int width, final int height, final Text leftTitle, final Text rightTitle,
            final BiFunction<MalisisGui, VanillaStack, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            final BiFunction<MalisisGui, VanillaStack, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory,
            final int leftLimit,
            final int rightLimit,
            final int listedItems) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);

        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;
        this.listedItems = listedItems;

        this.construct(gui);
    }

    @Override
    protected void construct(final MalisisGui gui) {
        // Inventory
        this.leftDynamicList = new UIItemList(gui, true, this.leftLimit, 64, UIComponent.INHERITED, UIComponent.INHERITED);
        this.leftDynamicList.register(this);

        // Unlisted Items
        this.rightDynamicList = new UIItemList(gui, false, this.rightLimit, Integer.MAX_VALUE, 0, 0);
        this.rightDynamicList.register(this);

        // Call our parent construct
        super.construct(gui);

        // Set the sizes
        this.leftDynamicList.setSize(UIComponent.INHERITED, SimpleScreen.getPaddedHeight(this.leftContainer) - 22);
        this.rightDynamicList.setSize(UIComponent.INHERITED, SimpleScreen.getPaddedHeight(this.rightContainer) - 22);

        // Add property bars
        this.leftPropertyBar = new UIPropertyBar(gui, SimpleScreen.getPaddedWidth(this.leftContainer), 15);
        this.leftPropertyBar.setColor(org.spongepowered.api.util.Color.ofRgb(0, 130, 0).getRgb());
        this.leftPropertyBar.setPosition(-1, -1, Anchor.BOTTOM | Anchor.LEFT);
        this.leftPropertyBar.setText(Text.of(0, "/", this.leftLimit));

        this.rightPropertyBar = new UIPropertyBar(gui, SimpleScreen.getPaddedWidth(this.rightContainer), 15);
        this.rightPropertyBar.setColor(org.spongepowered.api.util.Color.ofRgb(0, 130, 0).getRgb());
        this.rightPropertyBar.setPosition(-1, -1, Anchor.BOTTOM | Anchor.LEFT);
        this.rightPropertyBar.setText(Text.of(0, "/", this.rightLimit));

        this.leftContainer.add(this.leftPropertyBar);
        this.rightContainer.add(this.rightPropertyBar);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        if (this.targetSide == null) {
            this.targetSide = SideType.RIGHT;
        }

        final UIContainer<?> middleContainer = new UIContainer(gui, 38, 111);
        middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        middleContainer.setBorder(FontColors.WHITE, 1, 185);
        middleContainer.setBackgroundAlpha(0);

        this.buttonSingle = new UIButtonBuilder(gui)
                .size(20)
                .position(0, 3)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("1")
                .tooltip(I18n.format("almura.tooltip.exchange.move_single", this.targetSide.toString().toLowerCase()))
                .onClick(() -> TransferType.SINGLE.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.one");

        this.buttonStack = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonSingle, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("S")
                .tooltip(I18n.format("almura.tooltip.exchange.move_stack", this.targetSide.toString().toLowerCase()))
                .onClick(() -> TransferType.STACK.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.stack");

        this.labelDirection = new UILabel(gui, "->");
        this.labelDirection.setFontOptions(FontColors.WHITE_FO);
        this.labelDirection.setPosition(0, SimpleScreen.getPaddedY(this.buttonStack, 6), Anchor.TOP | Anchor.CENTER);

        this.buttonType = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.labelDirection, 6))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("T")
                .tooltip(I18n.format("almura.tooltip.exchange.move_type", this.targetSide.toString().toLowerCase()))
                .onClick(() -> TransferType.TYPE.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.type");

        this.buttonAll = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonType, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("A")
                .tooltip(I18n.format("almura.tooltip.exchange.move_all", this.targetSide.toString().toLowerCase()))
                .onClick(() -> TransferType.ALL.transfer(this, this.targetSide, null))
                .enabled(false)
                .build("button.all");

        middleContainer.add(this.buttonSingle, this.buttonStack, this.labelDirection, this.buttonType, this.buttonAll);

        return middleContainer;
    }

    @Override
    protected void updateControls(@Nullable final VanillaStack selectedValue, final SideType targetSide) {
        this.labelDirection.setText(targetSide == SideType.LEFT ? "<-" : "->");

        // Left property bar
        final int leftSize = this.leftDynamicList.getSize();
        this.leftPropertyBar.setAmount(MathUtil.convertToRange(leftSize, 0, this.leftLimit, 0f, 1f));
        this.leftPropertyBar.setText(Text.of(leftSize, "/", this.leftLimit));

        // Right progress bar
        final int rightSize = this.rightDynamicList.getSize();
        this.rightPropertyBar.setAmount(MathUtil.convertToRange(rightSize, 0, this.rightLimit, 0f, 1f));
        this.rightPropertyBar.setText(Text.of(rightSize + this.listedItems, "/", this.rightLimit));

        super.updateControls(selectedValue, targetSide);
    }

    private void setDirection(final SideType targetSide) {
        // Invert the target targetSide
        this.targetSide = targetSide;
        this.updateControls(null, this.targetSide);
    }

    @Subscribe
    private void onItemSelect(final UIDynamicList.SelectEvent<VanillaStack> event) {
        final boolean isSelectionValid = event.getNewValue() != null;
        if (isSelectionValid) {
            // Target the opposite list when selecting an item
            this.setDirection(this.getOpposingSideFromList(event.getComponent()));
        }

        buttonSingle.setEnabled(isSelectionValid);
        buttonSingle.setTooltip(I18n.format("almura.tooltip.exchange.move_single", this.targetSide.toString().toLowerCase()));
        buttonStack.setEnabled(isSelectionValid);
        buttonStack.setTooltip(I18n.format("almura.tooltip.exchange.move_stack", this.targetSide.toString().toLowerCase()));
        buttonType.setEnabled(isSelectionValid);
        buttonType.setTooltip(I18n.format("almura.tooltip.exchange.move_type", this.targetSide.toString().toLowerCase()));
        buttonAll.setEnabled(isSelectionValid);
        buttonAll.setTooltip(I18n.format("almura.tooltip.exchange.move_all", this.targetSide.toString().toLowerCase()));
    }

    @Subscribe
    private void onPopulate(final UIDualListContainer.PopulateEvent<VanillaStack> event) {
        if (this.leftDynamicList.getSize() > 0) {
            this.leftDynamicList.setSelectedItem(this.leftDynamicList.getItems().get(0));
        } else if (this.rightDynamicList.getSize() > 0) {
            this.rightDynamicList.setSelectedItem(this.rightDynamicList.getItems().get(0));
        }
    }

    interface ITransferType {
        void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack);
    }

    public enum TransferType implements ITransferType {
        SINGLE {
            @Override
            public void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack) {
                this.transfer(component, targetSide, sourceStack, 1);
            }
        },
        STACK {
            @Override
            public void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack) {
                if (sourceStack == null) {
                    return;
                }
                this.transfer(component, targetSide, sourceStack, sourceStack.getQuantity());
            }
        },
        TYPE {
            @Override
            public void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack) {
                if (sourceStack == null) {
                    return;
                }

                component.getOpposingListFromSide(targetSide).getItems()
                        .stream()
                        .filter(i -> i != null && !i.isEmpty() && ItemHandlerHelper.canItemStacksStack(i.asRealStack(), sourceStack.asRealStack()))
                        .collect(Collectors.toList())
                        .forEach(i -> STACK.transfer(component, targetSide, i));
            }
        },
        ALL {
            @Override
            public void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack) {
                new ArrayList<>(component.getOpposingListFromSide(targetSide).getItems()).forEach(i -> STACK.transfer(component, targetSide, i));
            }
        };

        protected void transfer(final UIExchangeOfferContainer component, final SideType targetSide, @Nullable final VanillaStack sourceStack,
                final int toTransferCount) {
            if (sourceStack == null) {
                return;
            }

            // Store our lists
            final UIItemList sourceList = (UIItemList) component.getOpposingListFromSide(targetSide);
            final UIItemList targetList = (UIItemList) component.getListFromSide(targetSide);

            // Store a copy of the source stack before we modify it
            final VanillaStack transactionStack = sourceStack.copy();

            final VanillaStack toInsertStack = sourceStack.copy();
            toInsertStack.setQuantity(toTransferCount);

            int amountLeft = toTransferCount;
            // Attempt to insert the item into the target list
            for (int i = 0; i < targetList.getSlots(); i++) {
                // Ensure we aren't going over the limit
                if (targetList.getSize() >= component.rightLimit - component.listedItems) {
                    break;
                }

                final ItemStack resultStack = targetList.insertItem(i, toInsertStack.asRealStack(), false);

                amountLeft -= toInsertStack.getQuantity() - resultStack.getCount();
                toInsertStack.setQuantity(amountLeft);

                final VanillaStack slotStack = targetList.getItem(i);

                if (slotStack != null) {
                    slotStack.setQuantity(slotStack.asRealStack().getCount());
                }

                if (amountLeft <= 0) {
                    break;
                }
            }

            final VanillaStack insertResultStack = toInsertStack.copy();
            insertResultStack.setQuantity(amountLeft);

            // Attempt to extract the items from the source list
            int amountToExtract = toTransferCount - insertResultStack.getQuantity();

            if (amountToExtract > 0) {
                final int sourceSlot = sourceList.getItems().indexOf(sourceStack);
                amountToExtract -= sourceList.extractItem(sourceSlot, amountToExtract, false).getCount();

                sourceStack.setQuantity(sourceStack.asRealStack().getCount());

                // Check again if we still need to continue
                if (amountToExtract > 0) {
                    for (int i = 0; i < sourceList.getSize(); i++) {
                        if (!ItemHandlerHelper.canItemStacksStack(sourceList.getStackInSlot(i), sourceStack.asRealStack())) {
                            continue;
                        }

                        amountToExtract -= sourceList.extractItem(i, amountToExtract, false).getCount();

                        // Update the vanilla stack
                        final VanillaStack slotStack = sourceList.getStackFromSlot(i);
                        slotStack.setQuantity(slotStack.asRealStack().getCount());

                        if (amountToExtract <= 0) {
                            break;
                        }
                    }
                }
            }

            transactionStack.setQuantity(toTransferCount - amountLeft);

            component.fireEvent(new TransactionCompletedEvent<>(component, targetSide, transactionStack));
            component.fireEvent(new UIDynamicList.ItemsChangedEvent<>(sourceList));
            component.fireEvent(new UIDynamicList.ItemsChangedEvent<>(targetList));

            // See if we can reselect the same stack
            if (!sourceStack.isEmpty()) {
                sourceList.setSelectedItem(sourceStack);
            } else { // Otherwise select the first in the source list or the target list if none are available in the source
                if (sourceList.getSize() > 0) {
                    sourceList.setSelectedItem(sourceList.getItems().get(0));
                } else if (targetList.getSize() > 0) {
                    targetList.setSelectedItem(targetList.getItems().get(0));
                }
            }
        }

        public static boolean isStackEqualIgnoreSize(@Nullable VirtualStack a, @Nullable VirtualStack b) {
            if (a == null || b == null) {
                return false;
            }
            return net.minecraft.item.ItemStack.areItemsEqual(a.asRealStack(), b.asRealStack()) && net.minecraft.item.ItemStack
                .areItemStackTagsEqual(a.asRealStack(), b.asRealStack());
        }
    }

    public static class TransactionCompletedEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final SideType targetSide;
        public final VanillaStack stack;

        TransactionCompletedEvent(final UIDualListContainer<T> component, final SideType targetSide, final VanillaStack stack) {
            super(component);
            this.targetSide = targetSide;
            this.stack = stack;
        }
    }
}
