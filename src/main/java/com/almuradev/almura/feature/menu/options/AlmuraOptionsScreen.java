/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.menu.options;

import com.almuradev.almura.core.client.config.AlmuraClientConfig;
import com.almuradev.almura.core.client.config.AlmuraClientConfig.HudType;
import com.almuradev.almura.feature.menu.AlmuraScreens;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.button.CycleButton;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Almura's options, as 1.12's, in an Almura window: the HUD and what it shows, gameplay, names and health bars over players, monsters and
 * animals, and the render distances of chests, sign text, item frames and caches.
 * Opened from vanilla's options ("Almura...") and the mod list's Config button; Done and the window's x go back there.
 */
final class AlmuraOptionsScreen implements Screen {

    private static final float COLUMN_WIDTH = 200f, COLUMN_GAP = 6f, SECTION_GAP = 6f, ROW_GAP = 3f, CONTROL_HEIGHT = 16f, SECTION_PADDING = 4f;
    private static final float DONE_WIDTH = 50f, MARGIN = 10f;
    // The window's padding at its sides (3 each, the Almura theme's)
    private static final float WINDOW_SIDES = 6f;
    private static final int HEADING = 0xFFFFAA00;
    private static final Paint BOX = Paint.solid(0x23FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);

    private @Nullable Slider opacity;

    @Override
    public Text title() {
        return Text.translatable("almura.menu_button.options");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        // ---- HUD
        final CycleButton<HudType> hud = factory.<HudType>cycleButton("HUD", type -> type == HudType.ORIGIN ? "Origin" : "Vanilla")
            .setItems(List.of(HudType.values()));
        hud.selectItem(AlmuraClientConfig.originHud() ? HudType.ORIGIN : HudType.VANILLA);
        hud.setHeight(CONTROL_HEIGHT);
        hud.setTooltip(Text.of("Origin is Almura's HUD; Vanilla is Minecraft's"));
        hud.onEvent(Selection.SELECTED, event -> hud.selected().ifPresent(type -> {
            AlmuraClientConfig.set(AlmuraClientConfig.HUD, type);
            this.updateOpacity();
        }));
        final Slider opacity = factory.slider();
        opacity.setRange(0, 255).setStep(1).setValue(AlmuraClientConfig.get(AlmuraClientConfig.ORIGIN_HUD_OPACITY))
            .setLabel(value -> "HUD Opacity: " + (int) value);
        opacity.setHeight(CONTROL_HEIGHT);
        opacity.onEvent(Slider.COMMITTED, event -> AlmuraClientConfig.set(AlmuraClientConfig.ORIGIN_HUD_OPACITY, (int) Math.round(event.value())));
        this.opacity = opacity;
        this.updateOpacity();
        final CheckBox locationWidget = check(factory, "Location Widget", AlmuraClientConfig.DISPLAY_LOCATION_WIDGET,
            "The coordinates and clock at the top right");
        final Component<?> hudSection = section(factory, "HUD",
            hud, opacity,
            check(factory, "World & Compass Widget", AlmuraClientConfig.DISPLAY_WORLD_COMPASS_WIDGET, "The world name and compass at the top middle"),
            locationWidget,
            check(factory, "FPS / TPS", AlmuraClientConfig.DISPLAY_PERFORMANCE_STATS, "Shown under the coordinates of the location widget"),
            check(factory, "Numeric HUD Values", AlmuraClientConfig.DISPLAY_NUMERIC_HUD_VALUES, "The numbers on the HUD's bars"));

        // ---- Gameplay
        final Component<?> gameplaySection = section(factory, "Gameplay",
            check(factory, "Display Guide upon Login", AlmuraClientConfig.DISPLAY_GUIDE_ON_LOGIN, "Opens the Guide a few seconds after joining a server"),
            check(factory, "Flickering Lights", AlmuraClientConfig.FLICKERING_LIGHTS, "Off makes flickering lights shine steadily"),
            check(factory, "Disable Offhand Torch Placement", AlmuraClientConfig.DISABLE_OFFHAND_TORCH_PLACEMENT,
                "Torches held in the off hand aren't placed"));

        // ---- Names and health bars
        final Component<?> labelsSection = section(factory, "Names & Health Bars",
            check(factory, "Display Entity Names", AlmuraClientConfig.DISPLAY_NAMES, "Names over players, monsters and animals"),
            check(factory, "Display Entity Healthbars", AlmuraClientConfig.DISPLAY_HEALTHBARS, "A health bar under their names"),
            distance(factory, "Player Names", AlmuraClientConfig.PLAYER_NAME_RENDER_DISTANCE, "Off", "How far away a player's name and health show"),
            distance(factory, "Enemy Names", AlmuraClientConfig.ENEMY_NAME_RENDER_DISTANCE, "Off", "How far away a monster's name and health show"),
            distance(factory, "Animal Names", AlmuraClientConfig.ANIMAL_NAME_RENDER_DISTANCE, "Off", "How far away an animal's name and health show"));

        // ---- Render distances
        final Component<?> distancesSection = section(factory, "Render Distances",
            distance(factory, "Chests", AlmuraClientConfig.CHEST_RENDER_DISTANCE, "No limit", "How far away chests are drawn"),
            distance(factory, "Sign Text", AlmuraClientConfig.SIGN_TEXT_RENDER_DISTANCE, "No limit", "How far away a sign's text is drawn"),
            distance(factory, "Item Frames", AlmuraClientConfig.ITEM_FRAME_RENDER_DISTANCE, "No limit", "How far away item frames are drawn"),
            distance(factory, "Caches", AlmuraClientConfig.CACHE_RENDER_DISTANCE, "No limit", "How far away a cache's item and count are drawn"));

        final StackPanel left = column(factory).add(hudSection).add(gameplaySection);
        final StackPanel right = column(factory).add(labelsSection).add(distancesSection);
        final StackPanel columns = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(COLUMN_GAP).add(left).add(right);

        // ---- The bottom row: Done at the right
        final Button done = factory.button(Text.translatable("gui.done")).setSize(DONE_WIDTH, CONTROL_HEIGHT)
            .onEvent(Button.PRESSED, event -> close());
        final AnchorPanel bottom = factory.anchorPanel()
            .add(done.setAnchor(Anchor.MIDDLE_RIGHT));
        bottom.setHeight(CONTROL_HEIGHT);

        // The gap above the bottom row is the window's padding under it (3), so the row sits evenly
        final StackPanel page = factory.stackPanel().setSpacing(3f).add(columns)
            .add(bottom.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(page.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .setWidth(Math.min(COLUMN_WIDTH * 2 + COLUMN_GAP + WINDOW_SIDES, minecraft.getWindow().getGuiScaledWidth() - MARGIN))
            .setAnchor(Anchor.CENTER);
        // Its x goes back where the options were opened from, as Done does
        InGameScreens.closesScreen(window, AlmuraOptionsScreen::close);
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    private static void close() {
        Minecraft.getInstance().gui.screen().onClose();
    }

    /**
     * The opacity only applies to Almura's HUD, so it's greyed out on vanilla's, as on 1.12.
     */
    private void updateOpacity() {
        if (this.opacity == null) {
            return;
        }
        final boolean origin = AlmuraClientConfig.originHud();
        this.opacity.setEnabled(origin);
        this.opacity.setTooltip(Text.of(origin ? "How opaque the Origin HUD's panels are" : "Only available when Origin HUD is in use."));
    }

    // ---- Building blocks

    private static StackPanel column(final ComponentFactory factory) {
        final StackPanel column = factory.stackPanel().setSpacing(SECTION_GAP);
        column.setWidth(COLUMN_WIDTH);
        return column;
    }

    /**
     * A boxed section: its heading in gold over a line, then its controls, each as wide as the box.
     */
    private static Component<?> section(final ComponentFactory factory, final String heading, final Component<?>... controls) {
        final StackPanel rows = factory.stackPanel().setSpacing(ROW_GAP);
        rows.add(stretch(factory.label(Text.of(heading).color(HEADING))));
        rows.add(stretch(factory.separator()));
        for (final Component<?> control : controls) {
            control.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
            rows.add(control);
        }
        final AnchorPanel box = factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(SECTION_PADDING)
            .add(rows.setAnchor(Anchor.TOP_LEFT).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP));
        return box.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }

    private static CheckBox check(final ComponentFactory factory, final String text, final ModConfigSpec.BooleanValue setting, final String tooltip) {
        final CheckBox box = factory.checkBox(text).setBoxSize(AlmuraScreens.CHECKBOX_SIZE)
            .setState(AlmuraClientConfig.get(setting) ? CheckState.CHECKED : CheckState.UNCHECKED);
        box.setTooltip(Text.of(tooltip));
        box.onEvent(CheckBox.CHANGED, event -> AlmuraClientConfig.set(setting, event.value() == CheckState.CHECKED));
        return box;
    }

    /**
     * A distance in blocks, 0 to 128; 0 shows {@code zero} ("Off" for names, "No limit" for the rest).
     */
    private static Slider distance(final ComponentFactory factory, final String name, final ModConfigSpec.IntValue setting, final String zero,
        final String tooltip) {
        final Slider slider = factory.slider();
        slider.setRange(0, 128).setStep(1).setValue(AlmuraClientConfig.get(setting))
            .setLabel(value -> name + ": " + ((int) value == 0 ? zero : (int) value + " blocks"));
        slider.setHeight(CONTROL_HEIGHT);
        slider.setTooltip(Text.of(tooltip + " (0: " + zero.toLowerCase() + ")"));
        slider.onEvent(Slider.COMMITTED, event -> AlmuraClientConfig.set(setting, (int) Math.round(event.value())));
        return slider;
    }

    private static <T extends Component<T>> T stretch(final T component) {
        return component.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
    }
}
