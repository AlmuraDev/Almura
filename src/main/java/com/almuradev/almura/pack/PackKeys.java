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
    public static final DefaultedKey<Boolean> ROTATE = new DefaultedKey<>("rotate", Boolean.TRUE);
    public static final DefaultedKey<Boolean> MIRROR_ROTATE = new DefaultedKey<>("mirror-rotate", Boolean.FALSE);
    public static final DefaultedKey<Float> HARDNESS = new DefaultedKey<>("hardness", Float.MIN_VALUE);
    public static final DefaultedKey<Float> RESISTANCE = new DefaultedKey<>("resistance", Float.MIN_VALUE);
    public static final DefaultedKey<Integer> AMOUNT = new DefaultedKey<>("amount", Integer.MIN_VALUE);
    public static final DefaultedKey<Integer> INTEGER_DAMAGE = new DefaultedKey<>("damage", Integer.MIN_VALUE);
    public static final DefaultedKey<Float> FLOAT_DAMAGE = new DefaultedKey<>("damage", Float.MIN_VALUE);
    public static final DefaultedKey<Integer> LEVEL_REQUIRED = new DefaultedKey<>("level-required", Integer.MIN_VALUE);
    public static final DefaultedKey<Boolean> ENABLED = new DefaultedKey<>("enabled", Boolean.FALSE);
    public static final DefaultedKey<Float> HEALTH_CHANGE = new DefaultedKey<>("health-change", Float.MIN_VALUE);
    //CONSUMPTION NODE
    public static final DefaultedKey<String> NODE_CONSUMPTION = new DefaultedKey<>("consumption", "");
    public static final DefaultedKey<Float> SATURATION_CHANGE = new DefaultedKey<>("saturation-change", Float.MIN_VALUE);
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
    public static final DefaultedKey<Float> EMISSION = new DefaultedKey<>("emission", Float.MIN_VALUE);
    public static final DefaultedKey<Integer> OPACITY = new DefaultedKey<>("opacity", Integer.MIN_VALUE);


    public static final String SEED = "seed";

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

