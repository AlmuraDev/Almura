/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client.gui;

import com.almuradev.almura.shared.client.ui.FontColors;
import com.almuradev.almura.shared.client.ui.component.UIDynamicList;
import com.almuradev.almura.shared.client.ui.component.button.UIButtonBuilder;
import com.almuradev.almura.shared.client.ui.screen.SimpleScreen;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIBackgroundContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class UISwapContainer<T> extends UIBackgroundContainer {

    private final UIButton buttonToLeft, buttonAllToLeft, buttonToRight, buttonAllToRight;
    private final UIBackgroundContainer middleContainer;
    private final UIDynamicList<T> leftDynamicList, rightDynamicList;
    private final BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory, rightComponentFactory;
    @Nullable private final Consumer<UISwapContainer<T>> onInitializeConsumer, onUpdateConsumer;
    @Nullable private final BiConsumer<UISwapContainer<T>, ContainerSide> onTransactionConsumer, onPopulateConsumer;
    private int leftItemLimit, rightItemLimit;

    @SuppressWarnings("deprecation")
    private UISwapContainer(MalisisGui gui, int width, int height,
            Text leftTitle,
            Text rightTitle,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory,
            BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> rightComponentFactory,
            @Nullable Consumer<UISwapContainer<T>> onInitializeConsumer,
            @Nullable BiConsumer<UISwapContainer<T>, ContainerSide> onTransactionConsumer,
            @Nullable BiConsumer<UISwapContainer<T>, ContainerSide> onPopulateConsumer,
            @Nullable Consumer<UISwapContainer<T>> onUpdateConsumer) {
        super(gui, width, height);

        // Assign factories
        this.leftComponentFactory = leftComponentFactory;
        this.rightComponentFactory = rightComponentFactory;

        // Assign consumers
        this.onInitializeConsumer = onInitializeConsumer;
        this.onPopulateConsumer = onPopulateConsumer;
        this.onTransactionConsumer = onTransactionConsumer;
        this.onUpdateConsumer = onUpdateConsumer;

        this.setBorder(FontColors.WHITE, 1, 185);
        this.setBackgroundAlpha(0);

        // Create left container
        final UIBackgroundContainer leftContainer = new UIBackgroundContainer(gui, (this.width - 30) / 2, UIComponent.INHERITED);
        leftContainer.setBackgroundAlpha(0);
        leftContainer.setPadding(4, 4);
        leftContainer.setTopPadding(20);

        final UILabel leftContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(leftTitle));
        leftContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.leftDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
        this.leftDynamicList.setItemComponentFactory(this.leftComponentFactory);
        this.leftDynamicList.setItemSpacing(1);
        this.leftDynamicList.setCanDeselect(true);
        this.leftDynamicList.setName("list.left");
        this.leftDynamicList.register(this);

        leftContainer.add(leftContainerLabel, this.leftDynamicList);

        // Create right container
        final UIBackgroundContainer rightContainer = new UIBackgroundContainer(gui, (this.width - 30) / 2, UIComponent.INHERITED);
        rightContainer.setBackgroundAlpha(0);
        rightContainer.setPadding(4, 4);
        rightContainer.setTopPadding(20);
        rightContainer.setAnchor(Anchor.TOP | Anchor.RIGHT);

        final UILabel rightContainerLabel = new UILabel(gui, TextSerializers.LEGACY_FORMATTING_CODE.serialize(rightTitle));
        rightContainerLabel.setPosition(0, -15, Anchor.TOP | Anchor.CENTER);

        this.rightDynamicList = new UIDynamicList<>(gui, UIComponent.INHERITED, UIComponent.INHERITED);
        this.rightDynamicList.setPosition(0, 0, Anchor.TOP | Anchor.RIGHT);
        this.rightDynamicList.setItemComponentFactory(this.rightComponentFactory);
        this.rightDynamicList.setItemSpacing(1);
        this.rightDynamicList.setCanDeselect(true);
        this.rightDynamicList.setName("list.right");
        this.rightDynamicList.register(this);

        rightContainer.add(rightContainerLabel, this.rightDynamicList);

        // Create middle container
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
                .onClick(() -> this.transferMany(ContainerSide.RIGHT))
                .enabled(false)
                .build("button.all_to_right");

        this.buttonToRight = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonAllToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("->")
                .tooltip("Move to right container")
                .onClick(() -> this.transferOne(ContainerSide.RIGHT))
                .enabled(false)
                .build("button.to_right");

        this.buttonToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonToRight, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("<-")
                .tooltip("Move to left container")
                .onClick(() -> this.transferOne(ContainerSide.LEFT))
                .enabled(false)
                .build("button.to_left");

        this.buttonAllToLeft = new UIButtonBuilder(gui)
                .size(20)
                .position(0, SimpleScreen.getPaddedY(this.buttonToLeft, 2))
                .anchor(Anchor.TOP | Anchor.CENTER)
                .text("|<-")
                .tooltip("Move all to left container")
                .onClick(() -> this.transferMany(ContainerSide.LEFT))
                .enabled(false)
                .build("button.all_to_left");

        this.middleContainer.add(this.buttonToRight, this.buttonAllToRight, this.buttonToLeft, this.buttonAllToLeft);

        this.add(leftContainer, this.middleContainer, rightContainer);

        if (this.onInitializeConsumer != null) {
            this.onInitializeConsumer.accept(this);
        }
    }

    @Override
    public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick) {
        super.drawBackground(renderer, mouseX, mouseY, partialTick);

        final int startX = this.width / 2;
        final int halfHeight = (this.height - this.middleContainer.getHeight() - (this.borderSize * 2)) / 2;
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
    private void onItemSelect(UIDynamicList.SelectEvent<T> event) {
        this.updateControls(event.getNewValue(), this.getTargetFromList(event.getComponent()));
    }

    @Subscribe
    private void onItemsChanged(UIDynamicList.ItemsChangedEvent<T> event) {
        this.updateControls(null, this.getTargetFromList(event.getComponent()));;
    }

    public UISwapContainer<T> setItemLimit(int limit, ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            this.leftItemLimit = limit;
        } else {
            this.rightItemLimit = limit;
        }

        return this;
    }

    public UISwapContainer<T> setItems(List<T> list, ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            this.leftDynamicList.setItems(list);
        } else {
            this.rightDynamicList.setItems(list);
        }

        if (this.onPopulateConsumer != null) {
            this.onPopulateConsumer.accept(this, target);
        }

        return this;
    }

    public Collection<T> getItems(ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            return this.leftDynamicList.getItems();
        }

        return this.rightDynamicList.getItems();
    }

    private void transferOne(ContainerSide target) {
        final UIDynamicList<T> targetList = this.getListFromSide(target);
        final UIDynamicList<T> sourceList = this.getOpposingListFromSide(target);
        final T item = sourceList.getSelectedItem();
        targetList.addItem(item);
        sourceList.removeItem(item);

        if (this.onTransactionConsumer != null) {
            this.onTransactionConsumer.accept(this, target);
        }
    }

    private void transferMany(ContainerSide target) {
        final UIDynamicList<T> targetList = this.getListFromSide(target);
        final UIDynamicList<T> sourceList = this.getOpposingListFromSide(target);
        Stream<T> limitedItemStream = Lists.newArrayList(sourceList.getItems()).stream();
        if (target == ContainerSide.LEFT && this.leftItemLimit > 0) {
            limitedItemStream = limitedItemStream.limit(Math.max(0, this.leftItemLimit - targetList.getItems().size()));
        } else if (target == ContainerSide.RIGHT && this.rightItemLimit > 0) {
            limitedItemStream = limitedItemStream.limit(Math.max(0, this.rightItemLimit - targetList.getItems().size()));
        }

        final List<T> limitedItemList = limitedItemStream.collect(Collectors.toList());
        targetList.addItems(limitedItemList);
        sourceList.removeItems(limitedItemList);

        this.updateControls(null, target);

        if (this.onTransactionConsumer != null) {
            this.onTransactionConsumer.accept(this, target);
        }
    }

    private UIDynamicList<T> getListFromSide(ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            return this.leftDynamicList;
        }

        return this.rightDynamicList;
    }

    private UIDynamicList<T> getOpposingListFromSide(ContainerSide target) {
        if (target == ContainerSide.LEFT) {
            return this.rightDynamicList;
        }

        return this.leftDynamicList;
    }

    private ContainerSide getTargetFromList(UIDynamicList<T> list) {
        return list.getName().equalsIgnoreCase("list.left") ? ContainerSide.LEFT : ContainerSide.RIGHT;
    }

    private void updateControls(@Nullable T selectedValue, ContainerSide containerSide) {
        final boolean isSelected = selectedValue != null;
        final boolean isLeftFull = this.leftItemLimit > 0 && this.getListFromSide(ContainerSide.LEFT).getItems().size() == this.leftItemLimit;
        final boolean isRightFull = this.rightItemLimit > 0 && this.getListFromSide(ContainerSide.RIGHT).getItems().size() == this.rightItemLimit;

        this.buttonToLeft.setEnabled(containerSide == ContainerSide.RIGHT && isSelected && !isLeftFull);
        this.buttonToRight.setEnabled(containerSide == ContainerSide.LEFT && isSelected && !isRightFull);
        this.buttonAllToLeft.setEnabled(!this.getListFromSide(ContainerSide.RIGHT).getItems().isEmpty() && !isLeftFull);
        this.buttonAllToRight.setEnabled(!this.getListFromSide(ContainerSide.LEFT).getItems().isEmpty() && !isRightFull);

        // Unregister and re-register to avoid firing this event when deselecting from the other list
        this.leftDynamicList.unregister(this);
        this.leftDynamicList.setSelectedItem(null);
        this.leftDynamicList.register(this);
        this.rightDynamicList.unregister(this);
        this.rightDynamicList.setSelectedItem(null);
        this.rightDynamicList.register(this);

        if (this.onUpdateConsumer != null) {
            this.onUpdateConsumer.accept(this);
        }
    }

    public static <S> Builder<S> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> leftComponentFactory, rightComponentFactory;
        @Nullable private Consumer<UISwapContainer<T>> onInitializeConsumer, onUpdateConsumer;
        @Nullable private BiConsumer<UISwapContainer<T>, ContainerSide> onTransactionConsumer, onPopulateConsumer;
        private Text leftTitle, rightTitle;
        private int leftItemLimit, rightItemLimit;

        public Builder<T> componentFactory(BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> factory) {
            this.componentFactory(factory, ContainerSide.LEFT);
            return this.componentFactory(factory, ContainerSide.RIGHT);
        }

        public Builder<T> componentFactory(BiFunction<MalisisGui, T, ? extends UIDynamicList.ItemComponent<?>> factory, ContainerSide containerSide) {
            if (containerSide == ContainerSide.LEFT) {
                this.leftComponentFactory = factory;
            } else {
                this.rightComponentFactory = factory;
            }
            return this;
        }

        public Builder<T> onInitialize(@Nullable Consumer<UISwapContainer<T>> consumer) {
            this.onInitializeConsumer = consumer;
            return this;
        }

        public Builder<T> onPopulate(@Nullable BiConsumer<UISwapContainer<T>, ContainerSide> consumer) {
            this.onPopulateConsumer = consumer;
            return this;
        }

        public Builder<T> onTransaction(@Nullable BiConsumer<UISwapContainer<T>, ContainerSide> consumer) {
            this.onTransactionConsumer = consumer;
            return this;
        }

        public Builder<T> onUpdate(@Nullable Consumer<UISwapContainer<T>> consumer) {
            this.onUpdateConsumer = consumer;
            return this;
        }

        public Builder<T> title(String leftTitle, String rightTitle) {
            this.title(leftTitle, ContainerSide.LEFT);
            return this.title(rightTitle, ContainerSide.RIGHT);
        }

        public Builder<T> title(String title, ContainerSide containerSide) {
            return this.title(Text.of(TextColors.WHITE, title), containerSide);
        }

        public Builder<T> title(Text leftTitle, Text rightTitle) {
            this.title(leftTitle, ContainerSide.LEFT);
            return this.title(rightTitle, ContainerSide.RIGHT);
        }

        public Builder<T> title(Text title, ContainerSide containerSide) {
            if (containerSide == ContainerSide.LEFT) {
                this.leftTitle = title;
            } else {
                this.rightTitle = title;
            }
            return this;
        }

        public Builder<T> itemLimit(int limit) {
            this.itemLimit(limit, ContainerSide.LEFT);
            return this.itemLimit(limit, ContainerSide.RIGHT);
        }

        public Builder<T> itemLimit(int leftLimit, int rightLimit) {
            this.itemLimit(leftLimit, ContainerSide.LEFT);
            return this.itemLimit(rightLimit, ContainerSide.RIGHT);
        }

        /**
         * Sets the item limit for the specified containerSide
         * @param limit The value to limit both lists to, 0 or less specifies unlimited
         * @param containerSide The containerSide to target the change at
         * @return The builder
         */
        public Builder<T> itemLimit(int limit, ContainerSide containerSide) {
            if (containerSide == ContainerSide.LEFT) {
                this.leftItemLimit = limit;
            } else {
                this.rightItemLimit = limit;
            }
            return this;
        }

        public UISwapContainer<T> build(SimpleScreen gui, int width, int height) {
            return new UISwapContainer<>(gui, width, height,
                    this.leftTitle,
                    this.rightTitle,
                    this.leftComponentFactory,
                    this.rightComponentFactory,
                    this.onInitializeConsumer,
                    this.onPopulateConsumer,
                    this.onTransactionConsumer,
                    this.onUpdateConsumer)
                    .setItemLimit(this.leftItemLimit, ContainerSide.LEFT)
                    .setItemLimit(this.rightItemLimit, ContainerSide.RIGHT);
        }
    }

    public enum ContainerSide {
        LEFT,
        RIGHT
    }
}
