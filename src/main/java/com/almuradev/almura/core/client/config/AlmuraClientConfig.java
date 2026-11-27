/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.core.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

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
    public static final ModConfigSpec.BooleanValue DISPLAY_LOCATOR_BAR;
    public static final ModConfigSpec.BooleanValue ENHANCED_DEBUG;
    public static final ModConfigSpec.BooleanValue DISPLAY_GUIDE_ON_LOGIN;
    public static final ModConfigSpec.BooleanValue DISPLAY_OTHER_PLAYER_GUIDES;
    public static final ModConfigSpec.BooleanValue HIDE_GUIDE_FORMATTER_BAR;
    public static final ModConfigSpec.BooleanValue FLICKERING_LIGHTS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HUD_SKILLS;
    public static final ModConfigSpec.BooleanValue DISPLAY_NAMES;
    public static final ModConfigSpec.BooleanValue DISPLAY_HEALTHBARS;
    public static final ModConfigSpec.IntValue PLAYER_NAME_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue ENEMY_NAME_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue ANIMAL_NAME_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue CHEST_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue SIGN_TEXT_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue ITEM_FRAME_RENDER_DISTANCE;
    public static final ModConfigSpec.IntValue CACHE_RENDER_DISTANCE;
    public static final ModConfigSpec.BooleanValue DISABLE_OFFHAND_TORCH_PLACEMENT;

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
        DISPLAY_LOCATOR_BAR = builder.comment("Shows vanilla's locator bar (the directions of nearby players) above the hotbar; off shows the experience bar there instead")
            .define("display-locator-bar", true);
        ENHANCED_DEBUG = builder.comment("Shows Almura's debug panels in place of vanilla's debug text while the debug screen (F3) is open")
            .define("enhanced-debug", true);
        HUD_SKILLS = builder.comment("The skills shown in the bottom right of the Origin HUD (chosen in the Features menu's Skills), as 3.5's")
            .defineList("hud-skills", List.of(), () -> "", value -> value instanceof String);
        builder.pop();
        builder.push("guide");
        DISPLAY_GUIDE_ON_LOGIN = builder.comment("Opens the guide a few seconds after joining a server that opens it then")
            .define("display-guide-on-login", true);
        DISPLAY_OTHER_PLAYER_GUIDES = builder.comment("Lists other players' guides in the guide, for its administrators")
            .define("display-other-player-guides", false);
        HIDE_GUIDE_FORMATTER_BAR = builder.comment("Hides the guide's formatter bar for those who may change its pages")
            .define("hide-formatter-bar", false);
        builder.pop();
        builder.push("labels");
        DISPLAY_NAMES = builder.comment("Shows the names of players, monsters and animals over them (within their render distances), as 3.5's")
            .define("display-names", true);
        DISPLAY_HEALTHBARS = builder.comment("Shows a health bar under the names of players, monsters and animals (within their render distances)")
            .define("display-healthbars", true);
        PLAYER_NAME_RENDER_DISTANCE = builder.comment("How far away (blocks) a player's name and health bar show. 0 hides them")
            .defineInRange("player-name-render-distance", 8, 0, 128);
        ENEMY_NAME_RENDER_DISTANCE = builder.comment("How far away (blocks) a monster's name and health bar show. 0 hides them")
            .defineInRange("enemy-name-render-distance", 8, 0, 128);
        ANIMAL_NAME_RENDER_DISTANCE = builder.comment("How far away (blocks) an animal's name and health bar show. 0 hides them")
            .defineInRange("animal-name-render-distance", 4, 0, 128);
        builder.pop();
        builder.push("render-distance");
        CHEST_RENDER_DISTANCE = builder.comment("How far away (blocks) chests are drawn. 0 for no limit (vanilla's)")
            .defineInRange("chest-render-distance", 16, 0, 128);
        SIGN_TEXT_RENDER_DISTANCE = builder.comment("How far away (blocks) a sign's text is drawn. 0 for no limit (vanilla's)")
            .defineInRange("sign-text-render-distance", 16, 0, 128);
        ITEM_FRAME_RENDER_DISTANCE = builder.comment("How far away (blocks) item frames are drawn. 0 for no limit (vanilla's)")
            .defineInRange("item-frame-render-distance", 16, 0, 128);
        CACHE_RENDER_DISTANCE = builder.comment("How far away (blocks) a cache's item and text are drawn. 0 for no limit")
            .defineInRange("cache-render-distance", 16, 0, 128);
        builder.pop();
        builder.push("gameplay");
        DISABLE_OFFHAND_TORCH_PLACEMENT = builder.comment("Stops torches held in the off hand from being placed")
            .define("disable-offhand-torch-placement", false);
        builder.pop();
        builder.push("lighting");
        FLICKERING_LIGHTS = builder.comment("Lets the content's flickering lights flicker (near the player, on this client only)")
            .define("flickering-lights", true);
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

    /**
     * @return the skills shown on the HUD, or none before the file has loaded
     */
    public static List<String> hudSkills() {
        return SPEC.isLoaded() ? List.copyOf(HUD_SKILLS.get()) : List.of();
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
