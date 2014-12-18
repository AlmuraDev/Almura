/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack;

import com.google.common.collect.Lists;

import java.util.List;

public class PackKeys {
    //NON-SPECIFIC-NODE
    public static final DefaultedKey<String> DATE = new DefaultedKey<>("date", "");
    public static final DefaultedKey<String> TYPE = new DefaultedKey<>("type", "");
    public static final DefaultedKey<String> TITLE = new DefaultedKey<>("title", "");
    public static final DefaultedKey<String> IDENTIFIER = new DefaultedKey<>("identifier", "");
    public static final DefaultedKey<? extends List<String>> TOOLTIP = new DefaultedKey<>("tooltip", Lists.<String>newArrayList());
    public static final DefaultedKey<String> TEXTURE = new DefaultedKey<>("texture", "");
    public static final DefaultedKey<? extends List<String>> TEXTURE_COORDINATES = new DefaultedKey<>("coordinates", Lists.<String>newArrayList());
    public static final DefaultedKey<String> SHAPE = new DefaultedKey<>("shape", "");
    public static final DefaultedKey<String> CREATIVE_TAB = new DefaultedKey<>("creative-tab", "other");
    public static final DefaultedKey<Boolean> SHOW_IN_CREATIVE_TAB = new DefaultedKey<>("show-in-creative-tab", Boolean.TRUE);
    public static final DefaultedKey<Float> HARDNESS = new DefaultedKey<>("hardness", Float.MIN_VALUE);
    public static final DefaultedKey<Float> RESISTANCE = new DefaultedKey<>("resistance", Float.MIN_VALUE);
    public static final DefaultedKey<String> AMOUNT = new DefaultedKey<>("amount", "");
    public static final DefaultedKey<Float> DAMAGE = new DefaultedKey<>("damage", 0f);
    public static final DefaultedKey<Integer> LEVEL_REQUIRED = new DefaultedKey<>("level-required", Integer.MIN_VALUE);
    public static final DefaultedKey<Boolean> ENABLED = new DefaultedKey<>("enabled", Boolean.FALSE);
    public static final DefaultedKey<Float> HEALTH_CHANGE = new DefaultedKey<>("health-change", Float.MIN_VALUE);
    public static final DefaultedKey<Float> EXHAUSTION_CHANGE = new DefaultedKey<>("exhaustion-change", Float.MIN_VALUE);
    public static final DefaultedKey<String> CHANCE = new DefaultedKey<>("chance", "");
    //CONSUMPTION NODE
    public static final DefaultedKey<String> NODE_CONSUMPTION = new DefaultedKey<>("consumption", "");
    public static final DefaultedKey<Float> SATURATION_CHANGE = new DefaultedKey<>("saturation-change", 1f);
    public static final DefaultedKey<Boolean> ALWAYS_EDIBLE = new DefaultedKey<>("always-edible", Boolean.FALSE);
    public static final DefaultedKey<Boolean> WOLF_FAVORITE = new DefaultedKey<>("wolf-favorite", Boolean.FALSE);
    //RENDER NODE
    public static final DefaultedKey<String> NODE_RENDER = new DefaultedKey<>("render", "");
    public static final DefaultedKey<Boolean> NORMAL_CUBE = new DefaultedKey<>("normal-cube", Boolean.TRUE);
    public static final DefaultedKey<Boolean> OPAQUE = new DefaultedKey<>("opaque", Boolean.FALSE);
    //RECIPES NODE
    public static final DefaultedKey<String> NODE_RECIPES = new DefaultedKey<>("recipes", "");
    public static final DefaultedKey<? extends List<String>> INGREDIENTS = new DefaultedKey<>("ingredients", Lists.<String>newArrayList());
    //LIGHT NODE
    public static final DefaultedKey<String> NODE_LIGHT = new DefaultedKey<>("light", "");
    public static final DefaultedKey<Float> EMISSION = new DefaultedKey<>("emission", 1f);
    public static final DefaultedKey<Integer> OPACITY = new DefaultedKey<>("opacity", 0);
    public static final DefaultedKey<String> REQUIRED = new DefaultedKey<>("required", "");
    public static final DefaultedKey<Integer> MIN = new DefaultedKey<>("min", 1);
    public static final DefaultedKey<Integer> MAX = new DefaultedKey<>("max", 1);
    //ROTATION NODE
    public static final DefaultedKey<String> NODE_ROTATE = new DefaultedKey<>("rotate", "");
    public static final DefaultedKey<Boolean> DEFAULT_ROTATE = new DefaultedKey<>("default-rotate", Boolean.TRUE);
    public static final DefaultedKey<Boolean> DEFAULT_MIRROR_ROTATE = new DefaultedKey<>("default-mirror", Boolean.FALSE);
    public static final DefaultedKey<String> DIRECTION = new DefaultedKey<>("direction", "");
    public static final DefaultedKey<Float> ANGLE = new DefaultedKey<>("angle", 0f);
    public static final DefaultedKey<String> DIRECTION_X = new DefaultedKey<>("x", "none");
    public static final DefaultedKey<String> DIRECTION_Y = new DefaultedKey<>("y", "none");
    public static final DefaultedKey<String> DIRECTION_Z = new DefaultedKey<>("z", "none");
    //COLLISION NODE
    public static final DefaultedKey<String> NODE_COLLISION = new DefaultedKey<>("collision", "");
    public static final DefaultedKey<String> SOURCES = new DefaultedKey<>("sources", "");
    //GRASS NODE
    public static final DefaultedKey<String> NODE_GRASS = new DefaultedKey<>("grass", "");
    //BREAK NODE
    public static final DefaultedKey<String> NODE_BREAK = new DefaultedKey<>("break", "");
    public static final DefaultedKey<String> TOOLS = new DefaultedKey<>("tools", "");

    public static class DefaultedKey<T> {
        private final String key;
        private final T value;

        public DefaultedKey(String key, T value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public T getDefaultValue() {
            return value;
        }
    }
}

