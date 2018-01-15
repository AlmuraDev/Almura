/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.content.mixin.MixinSupport;
import com.almuradev.content.type.item.definition.ItemDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RecipeManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    static final String DIRECTORY = "_recipes";
    private final Set<Path> sources = new HashSet<>();
    private final Logger logger;

    @Inject
    private RecipeManager(final Logger logger) {
        this.logger = logger;
    }

    public void push(final Path source) {
        this.sources.add(source);
    }

    public void load() throws IOException {
        this.logger.debug("Loading recipes...");
        for (final Path source : this.sources) {
            final Iterator<Path> it = Files.walk(source).iterator();
            while (it.hasNext()) {
                final Path path = it.next();
                if ("json".equals(FilenameUtils.getExtension(path.toString()))) {
                    final String id = FilenameUtils.removeExtension(source.relativize(path).toString()).replace("\\\\", "/");
                    try (final BufferedReader br = Files.newBufferedReader(path)) {
                        try {
                            final Object recipe = parseRecipe(JsonUtils.fromJson(GSON, br, JsonObject.class));
                            if (recipe instanceof IRecipe) {
                                ((IRecipe) recipe).setRegistryName(new ResourceLocation("almura", id));
                                GameData.register_impl((IRecipe) recipe);
                            } else if (recipe instanceof FurnaceRecipe) {
                                ((FurnaceRecipe) recipe).register();
                            }
                        } catch (final JsonParseException e) {
                            this.logger.error("Encountered an exception parsing recipe '{}'", path.toAbsolutePath().toString(), e);
                        }
                    }
                }
            }
        }
    }

    private static Object parseRecipe(final JsonObject object) {
        String type = JsonUtils.getString(object, "type");
        if ("crafting_shaped".equals(type)) {
            try {
                MixinSupport.useCountWithShapedRecipe = true;
                return ShapedRecipes.deserialize(object);
            } finally {
                MixinSupport.useCountWithShapedRecipe = false;
            }
        } else if ("crafting_shapeless".equals(type)) {
            return ShapelessRecipes.deserialize(object);
        } else if ("furnace".equals(type)) {
            return FurnaceRecipe.deserialize(object);
        }
        throw new JsonSyntaxException("Invalid or unsupported recipe type '" + type + "'");
    }

    private static class FurnaceRecipe {
        private final ItemDefinition input;
        private final ItemDefinition output;
        private final float experience;

        private FurnaceRecipe(final ItemDefinition input, final ItemDefinition output, final float experience) {
            this.input = input;
            this.output = output;
            this.experience = experience;
        }

        private void register() {
            FurnaceRecipes.instance().addSmeltingRecipe(this.input.create(), this.output.create(), this.experience);
        }

        public static FurnaceRecipe deserialize(final JsonObject object) {
            final ItemDefinition input = ItemDefinition.parse(object.get("input"));
            final ItemDefinition output = ItemDefinition.parse(object.get("output"));
            final float experience = JsonUtils.getFloat(object, "experience");
            return new FurnaceRecipe(input, output, experience);
        }
    }
}
