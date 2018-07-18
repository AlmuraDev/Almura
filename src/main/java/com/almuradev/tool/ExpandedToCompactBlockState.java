/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public final class ExpandedToCompactBlockState {
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private static final JsonParser PARSER = new JsonParser();
    private static final String DEFAULTS = "defaults";
    private static final String FORGE_MARKER = "forge_marker";
    private static final String VARIANTS = "variants";
    private static final String INVENTORY_VARIANT = "inventory";
    private static final String MODEL = "model";

    public static void main(final String[] args) throws IOException {
        if(args.length < 1) {
            System.err.println("Usage: <source json> [target json]");
            System.err.println("    source json will be overwritten if a target json is not provided");
            return;
        }
        final Path sourcePath = Paths.get(args[0]);
        final Path targetPath = args.length > 1 ? Paths.get(args[1]) : sourcePath;
        new ExpandedToCompactBlockState().transform(sourcePath, targetPath);
    }

    public void transform(final Path sourcePath, final Path targetPath) throws IOException {
        final JsonObject source;
        try(final InputStream is = Files.newInputStream(sourcePath)) {
            source = PARSER.parse(new InputStreamReader(is)).getAsJsonObject();
        }

        final JsonObject target = new JsonObject();

        target.addProperty(FORGE_MARKER, 1);
        target.add(DEFAULTS, source.getAsJsonObject(DEFAULTS));

        final JsonObject sourceVariants = source.getAsJsonObject(VARIANTS);
        final JsonObject targetVariants = new JsonObject();

        final KeyStack keyStack = new KeyStack();
        for(final Map.Entry<String, JsonElement> entry : sourceVariants.entrySet()) {
            final String key = entry.getKey();
            final JsonElement value = entry.getValue();
            if(key.equalsIgnoreCase(INVENTORY_VARIANT)) {
                targetVariants.add(key, value);
            } else {
                keyStack.push(key);
                this.visit(keyStack, value.getAsJsonObject(), targetVariants);
            }
        }

        target.add(VARIANTS, targetVariants);

        Files.write(targetPath, GSON.toJson(target).getBytes(StandardCharsets.UTF_8));
    }

    private void visit(final KeyStack keyStack, final JsonObject sourceVariant, final JsonObject targetVariants) {
        for(final Map.Entry<String, JsonElement> entry : sourceVariant.entrySet()) {
            keyStack.push(entry.getKey());
            final JsonElement value = entry.getValue();
            if(value.isJsonObject()) {
                if(value.getAsJsonObject().has(MODEL)) {
                    final String key = keyStack.toString();
                    System.out.println("Found variant at " + keyStack.key() + ", moving to " + key);
                    targetVariants.add(key, value);
                } else {
                    this.visit(keyStack, value.getAsJsonObject(), targetVariants);
                }
            }
            keyStack.pop();
        }
    }

    private static class KeyStack {
        final Stack<String> stack = new Stack<>();

        void push(final String string) {
            this.stack.push(string);
        }

        void pop() {
            this.stack.pop();
        }

        private String key() {
            return this.stack.toString();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final Iterator<String> it = this.stack.iterator();
            while(it.hasNext()) {
                sb.append(it.next()).append('=').append(it.next());
                if(it.hasNext()) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }
    }
}
