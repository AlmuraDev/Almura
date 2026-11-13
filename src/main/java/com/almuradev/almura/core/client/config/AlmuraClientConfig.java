/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Almura's client settings, in {@code config/almura-client.toml}, changed in game from the Almura options screen (Options, then
 * "Almura..."). The settings of features not yet ported to 4.0 join them as those features are.
 */
public final class AlmuraClientConfig {

    /**
     * The HUD drawn in game: Almura's (Origin), or vanilla's.
     */
    public enum HudType {
        ORIGIN,
        VANILLA
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<HudType> HUD;
    public static final ModConfigSpec.IntValue ORIGIN_HUD_OPACITY;
    public static final ModConfigSpec.BooleanValue DISPLAY_WORLD_COMPASS_WIDGET;
    public static final ModConfigSpec.BooleanValue DISPLAY_LOCATION_WIDGET;
    public static final ModConfigSpec.BooleanValue DISPLAY_PERFORMANCE_STATS;
    public static final ModConfigSpec.BooleanValue DISPLAY_NUMERIC_HUD_VALUES;
    public static final ModConfigSpec.BooleanValue ENHANCED_DEBUG;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");
        HUD = builder.comment("The HUD to use: ORIGIN (Almura's) or VANILLA").defineEnum("type", HudType.ORIGIN);
        ORIGIN_HUD_OPACITY = builder.comment("How opaque the Origin HUD's panels are. Range: 0-255")
            .defineInRange("origin-hud-opacity", 255, 0, 255);
        DISPLAY_WORLD_COMPASS_WIDGET = builder.comment("Shows the world name and compass at the top middle of the Origin HUD")
            .define("display-world-compass-widget", true);
        DISPLAY_LOCATION_WIDGET = builder.comment("Shows the coordinates and clock at the top right of the Origin HUD")
            .define("display-location-widget", true);
        DISPLAY_PERFORMANCE_STATS = builder.comment("Shows the FPS and server TPS under the coordinates of the Origin HUD")
            .define("display-performance-stats", true);
        DISPLAY_NUMERIC_HUD_VALUES = builder.comment("Shows the numbers on the Origin HUD's bars")
            .define("display-numeric-hud-values", true);
        ENHANCED_DEBUG = builder.comment("Shows Almura's debug panels in place of vanilla's debug text while the debug screen (F3) is open")
            .define("enhanced-debug", true);
        builder.pop();
        SPEC = builder.build();
    }

    private AlmuraClientConfig() {
    }

    /**
     * @return Whether Almura's HUD (Origin) is the one drawn
     */
    public static boolean originHud() {
        return !SPEC.isLoaded() || HUD.get() == HudType.ORIGIN;
    }

    /**
     * Reads a setting, or its default before the file has loaded.
     */
    public static boolean get(final ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static int get(final ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    /**
     * Changes a setting and writes the file.
     */
    public static <T> void set(final ModConfigSpec.ConfigValue<T> value, final T to) {
        value.set(to);
        value.save();
    }
}
