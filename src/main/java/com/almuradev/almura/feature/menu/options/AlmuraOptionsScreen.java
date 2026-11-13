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
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.button.CycleButton;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.Slider;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Almura's options, as on 1.12: two columns of settings under the title, and Done below the longer one.
 *
 * <p>The HUD's settings work now. The rest belong to features not yet ported to 4.0 (entity names and health bars, the guide, extended view,
 * the render distances of chests, signs, item frames, caches and name tags, the optimized defaults); they show greyed out, at 1.12's
 * defaults, until those features are.</p>
 */
final class AlmuraOptionsScreen implements Screen {

    private static final float CONTROL_WIDTH = 150f, CONTROL_HEIGHT = 20f, PADDING = 5f, COLUMN_GAP = 35f, DONE_WIDTH = 200f;
    private static final float TITLE_TOP = 20f, DONE_GAP = 10f;
    private static final int LATER_GRAY = 0xFFA0A0A0;

    private @Nullable Slider opacity;

    @Override
    public Text title() {
        return Text.translatable("almura.menu_button.options");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        final Label title = factory.label(this.title());

        // The left column: the HUD and what it shows, then the toggles of features to come
        final CycleButton<HudType> hud = factory.<HudType>cycleButton("HUD", type -> type == HudType.ORIGIN ? "Origin" : "Vanilla")
            .setItems(List.of(HudType.values()));
        hud.selectItem(AlmuraClientConfig.originHud() ? HudType.ORIGIN : HudType.VANILLA);
        hud.setSize(CONTROL_WIDTH, CONTROL_HEIGHT);
        hud.onEvent(Selection.SELECTED, event -> hud.selected().ifPresent(type -> {
            AlmuraClientConfig.set(AlmuraClientConfig.HUD, type);
            this.updateOpacity();
        }));

        final StackPanel left = factory.stackPanel().setSpacing(PADDING)
            .add(later(factory.button("Load Optimized Defaults").setSize(CONTROL_WIDTH, CONTROL_HEIGHT)))
            .add(hud)
            .add(check(factory, "Display World Compass Widget", AlmuraClientConfig.DISPLAY_WORLD_COMPASS_WIDGET))
            .add(check(factory, "Display Location Widget", AlmuraClientConfig.DISPLAY_LOCATION_WIDGET))
            .add(check(factory, "Display FPS / TPS", AlmuraClientConfig.DISPLAY_PERFORMANCE_STATS)
                .setTooltip(Text.of("Shown under the coordinates of the location widget")))
            .add(check(factory, "Display Numeric HUD Values", AlmuraClientConfig.DISPLAY_NUMERIC_HUD_VALUES))
            .add(laterCheck(factory, "Display Entity Names", true))
            .add(laterCheck(factory, "Display Entity Healthbars", true))
            .add(laterCheck(factory, "Disable Offhand Torch Placement", false))
            .add(laterCheck(factory, "Display Guide upon Login", true))
            .add(laterCheck(factory, "Enable/Disable Extended View", false));

        // The right column: the HUD's opacity, then the render distances to come
        this.opacity = factory.slider();
        this.opacity.setRange(0, 255).setStep(1).setValue(AlmuraClientConfig.get(AlmuraClientConfig.ORIGIN_HUD_OPACITY))
            .setLabel(value -> "HUD Opacity: " + (int) value)
            .setSize(CONTROL_WIDTH, CONTROL_HEIGHT);
        this.opacity.onEvent(Slider.COMMITTED, event -> AlmuraClientConfig.set(AlmuraClientConfig.ORIGIN_HUD_OPACITY, (int) Math.round(event.value())));
        this.updateOpacity();

        final StackPanel right = factory.stackPanel().setSpacing(PADDING)
            .add(this.opacity)
            .add(laterDistance(factory, "Chest Distance", 16))
            .add(laterDistance(factory, "Sign Text Distance", 16))
            .add(laterDistance(factory, "Item Frame Distance", 16))
            .add(laterDistance(factory, "Cache Distance", 16))
            .add(laterDistance(factory, "Player Name Distance", 8))
            .add(laterDistance(factory, "Enemy Name Distance", 8))
            .add(laterDistance(factory, "Animal Name Distance", 4));

        final StackPanel columns = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(COLUMN_GAP).add(left).add(right);

        final Button done = factory.button(Text.translatable("gui.done"))
            .setSize(DONE_WIDTH, CONTROL_HEIGHT)
            .onEvent(Button.PRESSED, event -> Minecraft.getInstance().gui.screen().onClose());

        // Under the title, Done under whichever column is longer
        final StackPanel page = factory.stackPanel().setSpacing(PADDING)
            .add(centred(title))
            .add(centred(columns))
            .add(centred(done).setMargin(0f, DONE_GAP - PADDING, 0f, 0f));
        page.setAnchor(Anchor.TOP_CENTER).setMargin(0f, TITLE_TOP, 0f, 0f);
        return factory.anchorPanel().add(page).setAnchor(Anchor.FILL);
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
        if (origin) {
            this.opacity.clearTooltip();
        } else {
            this.opacity.setTooltip(Text.of("Only available when Origin HUD is in use."));
        }
    }

    private static CheckBox check(final ComponentFactory factory, final String text, final ModConfigSpec.BooleanValue setting) {
        final CheckBox box = factory.checkBox(text).setState(AlmuraClientConfig.get(setting) ? CheckState.CHECKED : CheckState.UNCHECKED);
        box.onEvent(CheckBox.CHANGED, event -> AlmuraClientConfig.set(setting, event.value() == CheckState.CHECKED));
        return box;
    }

    // A setting of a feature not yet ported: greyed out, at 1.12's default
    private static CheckBox laterCheck(final ComponentFactory factory, final String text, final boolean checked) {
        // Grey text, as the theme draws a disabled check box's label like an enabled one's
        return later(factory.checkBox(Text.of(text).color(LATER_GRAY)).setState(checked ? CheckState.CHECKED : CheckState.UNCHECKED));
    }

    private static Slider laterDistance(final ComponentFactory factory, final String name, final int blocks) {
        final Slider slider = factory.slider();
        slider.setRange(0, 128).setStep(1).setValue(blocks)
            .setLabel(value -> name + ": " + ((int) value == 0 ? "Disabled" : String.valueOf((int) value)))
            .setSize(CONTROL_WIDTH, CONTROL_HEIGHT);
        return later(slider);
    }

    private static <T extends Component<T>> T later(final T component) {
        component.setEnabled(false);
        if (component instanceof final org.inspirenxe.strata.api.component.HasTooltip<?> tooltip) {
            tooltip.setTooltip(Text.translatable("almura.features.later"));
        }
        return component;
    }

    private static <T extends Component<T>> T centred(final T component) {
        return component.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    }
}
