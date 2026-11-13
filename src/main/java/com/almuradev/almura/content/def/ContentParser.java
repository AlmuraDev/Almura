/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.def;

import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.Box;
import com.almuradev.almura.content.def.ContentDefs.DropDef;
import com.almuradev.almura.content.def.ContentDefs.EffectDef;
import com.almuradev.almura.content.def.ContentDefs.FoodDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.def.ContentDefs.StateDef;
import com.almuradev.almura.content.source.ContentSource;
import com.almuradev.almura.content.source.ContentSource.ContentFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads the content files' 1.12 JSON into {@link ContentDefs}, keeping the meanings 1.12's processors gave each key (see
 * {@code legacy/.../content/type}). A file that can't be read is left out with a problem noted in the source.
 */
public final class ContentParser {

    public static final String DEFAULT_STATE = "normal", NORTH = "facing=north";

    private final ContentSource source;

    public ContentParser(final ContentSource source) {
        this.source = source;
    }

    public List<ItemGroupDef> itemGroups() {
        final List<ItemGroupDef> groups = new ArrayList<>();
        for (final ContentFile file : this.source.files("item_group")) {
            groups.add(new ItemGroupDef(file.id(), string(file.json(), "icon")));
        }
        return groups;
    }

    /**
     * @param kinds the item kinds to read ({@code normal}, {@code food}); the rest are left for later
     */
    public List<ItemDef> items(final Set<String> kinds) {
        final List<ItemDef> items = new ArrayList<>();
        for (final ContentFile file : this.source.files("item")) {
            final String kind = kind(file);
            if (!kinds.contains(kind)) {
                continue;
            }
            final JsonObject json = file.json();
            String container = null;
            int containerDamage = 0;
            final JsonElement containerElement = json.get("container");
            if (containerElement != null && containerElement.isJsonPrimitive()) {
                container = containerElement.getAsString();
            } else if (containerElement != null && containerElement.isJsonObject()) {
                container = string(containerElement.getAsJsonObject(), "item");
                containerDamage = integer(containerElement.getAsJsonObject(), "damage", 0);
            }
            items.add(new ItemDef(file.id(), kind, string(json, "item_group"), integer(json, "max_stack_size", 0), integer(json, "durability", 0),
                container, containerDamage, json.has("apply") ? this.food(json) : null));
        }
        return items;
    }

    private FoodDef food(final JsonObject json) {
        final JsonObject apply = json.getAsJsonObject("apply");
        final int[] nutrition = range(apply.get("food_level_change"), 0);
        final List<EffectDef> effects = new ArrayList<>();
        final JsonElement potions = json.get("potion_effects");
        if (potions != null && potions.isJsonArray()) {
            for (final JsonElement element : potions.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                final JsonObject effect = element.getAsJsonObject();
                final int[] chance = range(effect.get("chance"), 100);
                effects.add(new EffectDef(strings(effect.get("potion")), chance[0], chance[1], integer(effect, "duration", 0),
                    integer(effect, "amplifier", 0), bool(effect, "ambient", false), bool(effect, "show_particles", true)));
            }
        }
        return new FoodDef(nutrition[0], nutrition[1], (float) number(apply, "saturation_change", 0), (float) number(apply, "health_change", 0),
            integer(json, "duration_ticks", 32), bool(json, "always_edible", false), effects);
    }

    /**
     * @param kinds the block kinds to read ({@code normal}, {@code horizontal}); the rest are left for later
     */
    public List<BlockDef> blocks(final Set<String> kinds) {
        final List<BlockDef> blocks = new ArrayList<>();
        for (final ContentFile file : this.source.files("block")) {
            final String kind = kind(file);
            if (!kinds.contains(kind)) {
                continue;
            }
            final JsonObject json = file.json();
            final JsonObject state = this.defaultState(file);
            if (state == null) {
                continue;
            }
            blocks.add(new BlockDef(file.id(), kind, stringOr(json, "material", "minecraft:rock"), string(json, "map_color"), string(json, "sound"),
                string(json, "item_group"), strings(json.get("effective_tools")), string(json, "render_layer"), this.state(state),
                string(json, "block"), string(json, "single")));
        }
        return blocks;
    }

    /**
     * The state that defines the block, with what it inherits from its parents ({@code parent}: another state's name) filled in.
     */
    private @Nullable JsonObject defaultState(final ContentFile file) {
        final JsonObject states = file.json().getAsJsonObject("state");
        if (states == null || states.isEmpty()) {
            return new JsonObject();
        }
        final String name = states.has(DEFAULT_STATE) ? DEFAULT_STATE : states.has(NORTH) ? NORTH : states.keySet().iterator().next();
        final JsonObject resolved = new JsonObject();
        final Set<String> seen = new HashSet<>();
        String current = name;
        while (current != null && seen.add(current)) {
            final JsonElement element = states.get(current);
            if (element == null || !element.isJsonObject()) {
                this.source.problem(file.file(), "State '" + current + "' is missing");
                break;
            }
            for (final Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (!entry.getKey().equals("parent") && !resolved.has(entry.getKey())) {
                    resolved.add(entry.getKey(), entry.getValue());
                }
            }
            current = string(element.getAsJsonObject(), "parent");
        }
        return resolved;
    }

    private StateDef state(final JsonObject json) {
        Box shape = null, collision = null;
        boolean noCollision = false;
        final JsonObject aabb = object(json, "aabb");
        if (aabb != null) {
            shape = box(aabb.has("shape") ? object(aabb, "shape") : aabb);
            collision = box(object(aabb, "collision"));
            noCollision = aabb.get("collision") != null && aabb.get("collision").isJsonPrimitive() && number(aabb, "collision", 0) < 0;
        }
        final JsonObject light = object(json, "light");
        final List<DropDef> drops = new ArrayList<>();
        final JsonObject action = object(json, "action");
        if (action != null && action.get("destroy") != null && action.get("destroy").isJsonArray()) {
            for (final JsonElement destroy : action.getAsJsonArray("destroy")) {
                if (destroy.isJsonObject() && destroy.getAsJsonObject().get("drop") instanceof final JsonArray dropArray) {
                    for (final JsonElement drop : dropArray) {
                        if (drop.isJsonObject() && string(drop.getAsJsonObject(), "item") != null) {
                            final JsonObject dropJson = drop.getAsJsonObject();
                            final int[] amount = range(dropJson.get("amount"), 1);
                            final JsonObject bonus = object(dropJson, "bonus");
                            drops.add(new DropDef(string(dropJson, "item"), amount[0], amount[1], bonus == null ? 0 : integer(bonus, "amount", 0),
                                bonus == null ? 0 : integer(bonus, "chance", 0)));
                        }
                    }
                }
            }
        }
        return new StateDef((float) number(json, "hardness", Double.NaN), (float) number(json, "resistance", Double.NaN),
            json.has("opaque") ? bool(json, "opaque", true) : null, light == null ? 0f : (float) number(light, "emission", 0), shape, collision,
            noCollision, string(json, "block_face_shape"), drops);
    }

    private static @Nullable Box box(final @Nullable JsonObject json) {
        if (json == null || !(json.get("min") instanceof final JsonArray min) || !(json.get("max") instanceof final JsonArray max)
            || min.size() < 3 || max.size() < 3) {
            return null;
        }
        return new Box(min.get(0).getAsDouble(), min.get(1).getAsDouble(), min.get(2).getAsDouble(), max.get(0).getAsDouble(),
            max.get(1).getAsDouble(), max.get(2).getAsDouble());
    }

    /**
     * The type folder below {@code item} or {@code block}: the first part of the id's path.
     */
    static String kind(final ContentFile file) {
        final int slash = file.path().indexOf('/');
        return slash < 0 ? "" : file.path().substring(0, slash);
    }

    /**
     * A number, or a {@code {min, max}} range, as {@code [min, max]}.
     */
    static int[] range(final @Nullable JsonElement element, final int otherwise) {
        if (element == null || element.isJsonNull()) {
            return new int[] {otherwise, otherwise};
        }
        if (element.isJsonPrimitive()) {
            final int value = (int) Math.round(element.getAsDouble());
            return new int[] {value, value};
        }
        final JsonObject object = element.getAsJsonObject();
        final int min = (int) Math.round(number(object, "min", otherwise));
        return new int[] {min, (int) Math.round(number(object, "max", min))};
    }

    static List<String> strings(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        if (element.isJsonPrimitive()) {
            return List.of(element.getAsString());
        }
        final List<String> strings = new ArrayList<>();
        if (element.isJsonArray()) {
            for (final JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonPrimitive()) {
                    strings.add(item.getAsString());
                }
            }
        }
        return strings;
    }

    static @Nullable JsonObject object(final JsonObject json, final String key) {
        final JsonElement element = json == null ? null : json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    static @Nullable String string(final JsonObject json, final String key) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    static String stringOr(final JsonObject json, final String key, final String otherwise) {
        final String value = string(json, key);
        return value == null ? otherwise : value;
    }

    static double number(final JsonObject json, final String key, final double otherwise) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber() ? element.getAsDouble() : otherwise;
    }

    static int integer(final JsonObject json, final String key, final int otherwise) {
        return (int) Math.round(number(json, key, otherwise));
    }

    static boolean bool(final JsonObject json, final String key, final boolean otherwise) {
        final JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : otherwise;
    }
}
