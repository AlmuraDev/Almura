/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui.component;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.component.container.UIContainer;
import com.almuradev.almura.shared.client.ui.component.container.UIDualListContainer;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.item.VirtualStack;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UIExchangeOfferContainer extends UIDualListContainer<VanillaStack> {

    @Nullable private UIButton buttonSingle, buttonStack, buttonType, buttonAll, buttonDirection;
    private int leftItemLimit = -1, rightItemLimit = -1;
    private SideType targetSide = SideType.RIGHT;

    public UIExchangeOfferContainer(MalisisGui gui, int width, int height, Text leftTitle, Text rightTitle,
        BiFunction<MalisisGui, VanillaStack, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
        BiFunction<MalisisGui, VanillaStack, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory) {
        super(gui, width, height, leftTitle, rightTitle, leftComponentFactory, rightComponentFactory);

        this.leftDynamicList.register(this);
        this.rightDynamicList.register(this);
    }

    @Override
    protected UIContainer<?> createMiddleContainer(MalisisGui gui) {
        final UIContainer<?> middleContainer = new UIContainer(gui, 38, 129);
        middleContainer.setPosition(0, 0, Anchor.MIDDLE | Anchor.CENTER);
        middleContainer.setBorder(FontColors.WHITE, 1, 185);
        middleContainer.setBackgroundAlpha(0);

        this.buttonSingle = new UIButtonBuilder(gui)
                .size(20)
                .position(0, 3)
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("1")
                .tooltip("Move one of the selected item to the target container")
                .onClick(() -> TransferType.SINGLE.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.one");

        this.buttonStack = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonSingle, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("S")
                .tooltip("Move the selected stack of listItems to the target container")
                .onClick(() -> TransferType.STACK.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.stack");

        this.buttonDirection = new UIButtonBuilder(gui)
            .size(30)
            .position(0, SimpleScreen.getPaddedY(this.buttonStack, 4))
            .anchor(Anchor.TOP | Anchor.CENTER)
            .text("->")
            .tooltip("Send listItems to the right")
            .onClick(() -> this.setDirection(this.getOppositeSide(this.targetSide)))
            .build("button.direction");

        this.buttonType = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonDirection, 4))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("T")
                .tooltip("Moves all listItems of the selected type to the target container")
                .onClick(() -> TransferType.TYPE.transfer(this, this.targetSide, this.getOpposingListFromSide(this.targetSide).getSelectedItem()))
                .enabled(false)
                .build("button.type");

        this.buttonAll = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonType, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("A")
                .tooltip("Moves all listItems of the selected type to the target container")
                .onClick(() -> TransferType.ALL.transfer(this, this.targetSide, null))
                .build("button.all");

        middleContainer.add(this.buttonSingle, this.buttonStack, this.buttonDirection, this.buttonType, this.buttonAll);

        return middleContainer;
    }

    @Override
    protected void updateControls(@Nullable final VanillaStack selectedValue, final SideType targetSide) {
        this.buttonDirection.setText(targetSide == SideType.LEFT ? "<-" : "->");
        this.buttonDirection.setTooltip(String.format("Send listItems to the %s", this.targetSide == SideType.LEFT ? "left" : "right"));

        super.updateControls(selectedValue, targetSide);
    }

    public UIExchangeOfferContainer setItemLimit(int limit, SideType target) {
        if (target == SideType.LEFT) {
            this.leftItemLimit = limit;
        } else {
            this.rightItemLimit = limit;
        }

        return this;
    }

    private int getLimitFromSide(final SideType side) {
        if (side == SideType.LEFT) {
            return this.leftItemLimit;
        }

        return this.rightItemLimit;
    }

    private void setDirection(final SideType targetSide) {
        // Invert the target targetSide
        this.targetSide = targetSide;
        this.updateControls(null, this.targetSide);
    }

    @Subscribe
    private void onItemSelect(final UIDynamicList.SelectEvent<VanillaStack> event) {
        if (event.getNewValue() != null) {
            // Target the opposite list when selecting an item
            this.setDirection(this.getOpposingSideFromList(event.getComponent()));
        }

        final boolean isSelectionValid = event.getNewValue() != null;

        buttonSingle.setEnabled(isSelectionValid);
        buttonStack.setEnabled(isSelectionValid);
        buttonType.setEnabled(isSelectionValid);
        buttonAll.setEnabled(isSelectionValid);
    }

    @Subscribe
    private void onPopulate(final UIDualListContainer.PopulateEvent<VanillaStack> event) {
        if (event.side == SideType.LEFT) {
            final UIDynamicList<VanillaStack> currentList = this.getListFromSide(event.side);
            currentList.setSelectedItem(currentList.getItems().stream().findFirst().orElse(null));
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
                this.transfer(component, targetSide, sourceStack,
                        getFilteredStream(component.getOpposingListFromSide(targetSide).getItems(), sourceStack).mapToInt(VirtualStack::getQuantity).sum());
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
            final UIDynamicList<VanillaStack> sourceList = component.getOpposingListFromSide(targetSide);
            final UIDynamicList<VanillaStack> targetList = component.getListFromSide(targetSide);

            // Remove what we can from the source stack first
            final int initialRemoved = Math.min(sourceStack.getQuantity(), toTransferCount);
            sourceStack.setQuantity(sourceStack.getQuantity() - initialRemoved);
            if (sourceStack.isEmpty()) {
                sourceList.removeItem(sourceStack);
            }

            // Determine what we need to remove still
            int toRemoveCount = toTransferCount - initialRemoved;

            // If we can still remove more, iterate through all remaining stacks and attempt to pull what we can
            if (toRemoveCount > 0) {
                for (VanillaStack stack : new ArrayList<>(sourceList.getItems())) {
                    // Stop if we don't have anymore to remove
                    if (toRemoveCount <= 0) {
                        break;
                    }

                    // Only remove if the stacks are identical
                    if (!isStackEqualIgnoreSize(stack, sourceStack)) {
                        continue;
                    }

                    // Determine how much we are taking
                    final int toTake = Math.min(stack.getQuantity(), toRemoveCount);
                    toRemoveCount -= toTake;

                    // Take the amount
                    stack.setQuantity(stack.getQuantity() - toTake);
                    if (stack.isEmpty()) {
                        sourceList.removeItem(stack);
                    }
                }
            }

            // Add/merge to target
            final int limit = targetSide == SideType.RIGHT ? Integer.MAX_VALUE : sourceStack.asRealStack().getMaxStackSize();
            int toAddCount = toTransferCount;

            for (VanillaStack stack : targetList.getItems()) {
                // Stop if we don't have anymore to add
                if (toAddCount <= 0) {
                    break;
                }

                // Only add if we are not at our limit
                if (stack.getQuantity() == limit) {
                    continue;
                }

                // Only add if the stacks are identical
                if (!isStackEqualIgnoreSize(stack, sourceStack)) {
                    continue;
                }

                // Determine how much we are adding
                final int toAdd = Math.min(limit, toAddCount);
                toAddCount -= toAdd;

                // Add the amount
                stack.setQuantity(stack.getQuantity() + toAdd);
            }

            final int divisor = toAddCount / limit;
            final int remainder = toAddCount % limit;

            final ArrayList<VanillaStack> toAdd = new ArrayList<>();

            for (int i = 0; i < divisor; i++) {
                final VanillaStack copyStack = sourceStack.copy();
                copyStack.setQuantity(limit);
                toAdd.add(copyStack);
            }

            if (remainder != 0) {
                final VanillaStack copyStack = sourceStack.copy();
                copyStack.setQuantity(remainder);
                toAdd.add(copyStack);
            }

            targetList.addItems(toAdd);

            component.fireEvent(new TransactionCompletedEvent<>(component, targetSide, sourceStack, toTransferCount));
            component.fireEvent(new UIDynamicList.ItemsChangedEvent<>(sourceList));
            component.fireEvent(new UIDynamicList.ItemsChangedEvent<>(targetList));
        }

        protected static Stream<VanillaStack> getFilteredStream(List<VanillaStack> list, @Nullable VanillaStack sourceStack) {
            return list.stream().filter(s -> isStackEqualIgnoreSize(sourceStack, s));
        }

        protected static boolean isStackEqualIgnoreSize(@Nullable VanillaStack stack1, @Nullable VanillaStack stack2) {
            if (stack1 == null || stack2 == null) {
                return false;
            }

            final ItemStack spongeStack1 = (ItemStack) (Object) stack1.asRealStack();
            final ItemStack spongeStack2 = (ItemStack) (Object) stack2.asRealStack();

            return ItemStackComparators.IGNORE_SIZE.compare(spongeStack1, spongeStack2) == 0;
        }
    }

    public static class TransactionCompletedEvent<T> extends ComponentEvent<UIDualListContainer<T>> {

        public final SideType targetSide;
        public final VanillaStack stack;
        public final int quantity;

        TransactionCompletedEvent(final UIDualListContainer<T> component, final SideType targetSide, final VanillaStack stack, final int quantity) {
            super(component);
            this.targetSide = targetSide;
            this.stack = stack;
            this.quantity = quantity;
        }
    }
}
