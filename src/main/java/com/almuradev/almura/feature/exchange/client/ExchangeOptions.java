/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.ExchangeSort;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The player's exchange options, as 3.5's client config: whether their own listings are left out of searches, and the order a search
 * starts in (item name, price or seller). Kept in {@code config/almura/exchange-client.json}.
 */
final class ExchangeOptions {

    static final String ITEM_NAME = "item_name", PRICE = "price", SELLER = "seller";

    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve(Almura.ID).resolve("exchange-client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean loaded;
    private static boolean hideOwnListings;
    private static String defaultSort = ITEM_NAME;

    private ExchangeOptions() {
    }

    static boolean hideOwnListings() {
        load();
        return hideOwnListings;
    }

    static String defaultSort() {
        load();
        return defaultSort;
    }

    /**
     * @return the order a search starts in, as 3.5's
     */
    static ExchangeSort defaultSortType() {
        return switch (defaultSort()) {
            case PRICE -> ExchangeSort.PRICE_ASC;
            case SELLER -> ExchangeSort.SELLER_ASC;
            default -> ExchangeSort.ITEM_NAME_ASC;
        };
    }

    static void set(final boolean hideOwn, final String sort) {
        hideOwnListings = hideOwn;
        defaultSort = sort;
        loaded = true;
        final JsonObject json = new JsonObject();
        json.addProperty("hide-own-listings", hideOwn);
        json.addProperty("default-sort", sort);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Almura.LOGGER.warn("Couldn't save the exchange options to {}", FILE, e);
        }
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        if (!Files.isRegularFile(FILE)) {
            return;
        }
        try {
            final JsonObject json = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), JsonObject.class);
            if (json != null) {
                hideOwnListings = json.has("hide-own-listings") && json.get("hide-own-listings").getAsBoolean();
                if (json.has("default-sort")) {
                    defaultSort = json.get("default-sort").getAsString();
                }
            }
        } catch (final IOException | RuntimeException e) {
            Almura.LOGGER.warn("Couldn't read the exchange options in {}", FILE, e);
        }
    }
}
