/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import com.almuradev.almura.feature.accessory.AccessoryCatalog;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ClientboundOpen;
import com.almuradev.almura.feature.menu.AlmuraScreens;
import com.almuradev.almura.feature.menu.InGameScreens;
import com.almuradev.almura.feature.title.client.PlayerPreview;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ComboBox;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Paint;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Features menu's Accessories, as 3.5's {@code AccessoryScreen}: the accessory and its style at the left (with previous and next, how
 * many styles there are, and what is worn), the player wearing the chosen style at the right, and Wear and Take Off, which leave the window
 * open. "Hide labels" leaves the name, title and health bar off the preview.
 */
final class AccessoryScreen implements Screen {

    private static final float WIDTH = 320f, HEIGHT = 200f, LEFT_WIDTH = 140f, COMBO_WIDTH = 130f, BUTTON_HEIGHT = 16f, MARGIN = 10f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GOLD = 0xFFFFAA00;
    private static final Paint LINE = Paint.solid(0xB9FFFFFF);

    // Kept while the game runs, as 3.5's
    private static boolean hideLabels;

    private final List<AccessoryType> allowed = new ArrayList<>();
    private final Map<String, String> worn;
    private @Nullable ComboBox<AccessoryType> typeSelect;
    private @Nullable ComboBox<String> styleSelect;
    private @Nullable Label countLabel, statusLabel;
    private @Nullable Button previous, next, wear, takeOff;

    AccessoryScreen(final ClientboundOpen message) {
        for (final String id : message.allowed()) {
            final AccessoryType type = AccessoryType.byId(id);
            if (type != null) {
                this.allowed.add(type);
            }
        }
        this.worn = new HashMap<>(message.worn());
    }

    @Override
    public Text title() {
        return Text.of("Accessories");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Button close = button(factory, "Close", 50f, this::close);
        final AnchorPanel content = factory.anchorPanel().add(close.setAnchor(Anchor.BOTTOM_RIGHT));
        if (this.allowed.isEmpty()) {
            content.add(factory.label(Text.of("You don't have any accessories you can wear.").color(GRAY)).setAnchor(Anchor.CENTER));
        } else {
            final ComboBox<AccessoryType> typeSelect = factory.<AccessoryType>comboBox(type -> Text.of(type.displayName()));
            typeSelect.setItems(this.allowed);
            typeSelect.setSize(COMBO_WIDTH, BUTTON_HEIGHT);
            this.typeSelect = typeSelect;
            // Each style by its readable name, as 3.5's
            final ComboBox<String> styleSelect = factory.comboBox(style -> {
                final AccessoryType type = this.type();
                return Text.of(type == null ? style : AccessoryCatalog.label(type, style));
            });
            styleSelect.setSize(COMBO_WIDTH, BUTTON_HEIGHT);
            this.styleSelect = styleSelect;
            this.previous = button(factory, "<", 24f, () -> this.step(-1));
            this.next = button(factory, ">", 24f, () -> this.step(1));
            this.countLabel = factory.label("");
            this.statusLabel = factory.label("");
            final CheckBox hide = factory.checkBox(Text.of("Hide labels").color(WHITE)).setBoxSize(AlmuraScreens.CHECKBOX_SIZE)
                .setState(hideLabels ? CheckState.CHECKED : CheckState.UNCHECKED);
            hide.setTooltip(Text.of("Leaves your name, title and health bar off the preview"));
            hide.onEvent(CheckBox.CHANGED, event -> hideLabels = event.value() == CheckState.CHECKED);
            this.wear = button(factory, "Wear", 50f, this::wearSelected);
            this.takeOff = button(factory, "Take Off", 60f, this::takeOff);
            content
                .add(label(factory, "Accessory").setAnchor(Anchor.TOP_LEFT).setMargin(4f, 2f, 0f, 0f))
                .add(typeSelect.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 13f, 0f, 0f))
                .add(label(factory, "Style").setAnchor(Anchor.TOP_LEFT).setMargin(4f, 34f, 0f, 0f))
                .add(styleSelect.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 45f, 0f, 0f))
                .add(this.previous.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 66f, 0f, 0f))
                .add(this.next.setAnchor(Anchor.TOP_LEFT).setMargin(31f, 66f, 0f, 0f))
                .add(this.countLabel.setAnchor(Anchor.TOP_LEFT).setMargin(62f, 70f, 0f, 0f))
                .add(this.statusLabel.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 90f, 0f, 0f))
                .add(hide.setAnchor(Anchor.TOP_LEFT).setMargin(4f, 106f, 0f, 0f))
                .add(this.wear.setAnchor(Anchor.BOTTOM_LEFT))
                .add(this.takeOff.setAnchor(Anchor.BOTTOM_LEFT).setMargin(54f, 0f, 0f, 0f))
                // The controls apart from the player
                .add(factory.anchorPanel().setBackground(LINE).setWidth(1f).setAnchor(Anchor.FILL).setMargin(LEFT_WIDTH + 4f, 2f, 0f, BUTTON_HEIGHT + 6f)
                    .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH))
                // From near the top to just above the buttons (it takes the drags that turn the figure, so it must not cover Close); the
                // figure and its labels are centred in it
                .add(PlayerPreview.create(factory, WIDTH - LEFT_WIDTH - 22f, HEIGHT - 46f, 48, AccessoryScreen::previewName)
                    .setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 8f, 4f, 0f));
            typeSelect.onEvent(Selection.SELECTED, event -> this.typeChanged());
            styleSelect.onEvent(Selection.SELECTED, event -> this.styleChanged());
            typeSelect.select(0);
            this.typeChanged();
        }
        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        InGameScreens.closesScreen(window, this::close);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private @Nullable AccessoryType type() {
        return this.typeSelect == null ? null : this.typeSelect.selected().orElse(null);
    }

    private @Nullable String style() {
        return this.styleSelect == null ? null : this.styleSelect.selected().orElse(null);
    }

    /**
     * Another accessory: its styles, the one worn (or the first) selected.
     */
    private void typeChanged() {
        final AccessoryType type = this.type();
        if (type == null || this.styleSelect == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        this.styleSelect.setItems(styles);
        final String worn = this.worn.get(type.id());
        if (!styles.isEmpty()) {
            this.styleSelect.selectItem(worn != null && styles.contains(worn) ? worn : styles.getFirst());
        }
        this.styleChanged();
    }

    private void styleChanged() {
        final AccessoryType type = this.type();
        if (type == null) {
            return;
        }
        ClientAccessories.preview(type, this.style());
        this.update();
    }

    private void step(final int by) {
        final AccessoryType type = this.type();
        final String style = this.style();
        if (type == null || this.styleSelect == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        if (styles.isEmpty()) {
            return;
        }
        final int index = style == null ? 0 : Math.floorMod(styles.indexOf(style) + by, styles.size());
        this.styleSelect.selectItem(styles.get(index));
        this.styleChanged();
    }

    private void wearSelected() {
        final AccessoryType type = this.type();
        final String style = this.style();
        if (type != null && style != null) {
            ClientAccessories.select(type, style);
            this.worn.put(type.id(), style);
            this.update();
        }
    }

    private void takeOff() {
        final AccessoryType type = this.type();
        if (type != null) {
            ClientAccessories.select(type, "");
            this.worn.remove(type.id());
            ClientAccessories.preview(type, null);
            this.update();
        }
    }

    private void update() {
        final AccessoryType type = this.type();
        if (type == null || this.countLabel == null || this.statusLabel == null || this.styleSelect == null) {
            return;
        }
        final List<String> styles = AccessoryCatalog.variants(type);
        final String style = this.style();
        final int index = style == null ? -1 : styles.indexOf(style);
        this.countLabel.setText(Text.of(index < 0 ? "" : (index + 1) + " / " + styles.size()).color(GRAY));
        final String worn = this.worn.get(type.id());
        this.statusLabel.setText(Text.of(worn == null ? "Not wearing " + type.displayName()
            : "Wearing: " + AccessoryCatalog.label(type, worn)).color(GOLD));
        if (this.takeOff != null) {
            this.takeOff.setEnabled(worn != null);
        }
        if (this.wear != null) {
            this.wear.setEnabled(style != null && !style.equals(worn));
        }
    }

    private void close() {
        ClientAccessories.clearPreview();
        Minecraft.getInstance().gui.screen().onClose();
    }

    private static net.minecraft.network.chat.@Nullable Component previewName() {
        final Minecraft minecraft = Minecraft.getInstance();
        return hideLabels || minecraft.player == null ? null : minecraft.player.getDisplayName();
    }

    private static Label label(final ComponentFactory factory, final String text) {
        return factory.label(Text.of(text).color(WHITE));
    }

    private static Button button(final ComponentFactory factory, final String text, final float width, final Runnable press) {
        return factory.button(Text.of(text)).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }
}
