/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.convert.ContentPack;
import com.almuradev.almura.content.convert.ContentPackBuilder;
import com.almuradev.almura.content.def.ContentDefs.BlockDef;
import com.almuradev.almura.content.def.ContentDefs.ItemDef;
import com.almuradev.almura.content.def.ContentDefs.ItemGroupDef;
import com.almuradev.almura.content.def.ContentParser;
import com.almuradev.almura.content.pack.ContentPacks;
import com.almuradev.almura.content.registry.ContentRegistration;
import com.almuradev.almura.content.source.ContentSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Almura's content (see {@code docs/content-loader.md}): read from {@code <game>/assets/fs_5} as on 1.12 (or the folder named by the
 * {@code almura.content} system property) when the mod is constructed, registered before the registries freeze, and carried to the game
 * in the {@code almura_content} pack. On both sides: a client and a server must hold the same content.
 */
public final class AlmuraContent {

    // The kinds of content loaded so far; the rest follow as they are ported
    public static final Set<String> ITEM_KINDS = Set.of("normal", "food");
    public static final Set<String> BLOCK_KINDS = Set.of("normal", "horizontal");

    private AlmuraContent() {
    }

    public static Path root() {
        final String property = System.getProperty("almura.content");
        return property != null ? Path.of(property) : FMLPaths.GAMEDIR.get().resolve("assets").resolve("fs_5");
    }

    public static void register(final IEventBus modBus) {
        final Path root = root();
        if (!Files.isDirectory(root.resolve("assets"))) {
            Almura.LOGGER.warn("No Almura content at {}: no Almura blocks or items will be loaded", root.toAbsolutePath());
            return;
        }
        final ContentSource source = ContentSource.at(root);
        final ContentParser parser = new ContentParser(source);
        final List<ItemGroupDef> groups = parser.itemGroups();
        final List<ItemDef> items = parser.items(ITEM_KINDS);
        final List<BlockDef> blocks = parser.blocks(BLOCK_KINDS);
        source.problems().forEach(problem -> Almura.LOGGER.warn("Content: {}", problem));
        Almura.LOGGER.info("Loaded Almura content from {}: {} creative tabs, {} items, {} blocks", root.toAbsolutePath(), groups.size(),
            items.size(), blocks.size());

        modBus.register(new ContentRegistration(groups, items, blocks));
        ContentPacks.register(modBus, () -> {
            final ContentPackBuilder builder = new ContentPackBuilder(source);
            final ContentPack pack = builder.buildAssets(groups, items, blocks);
            builder.problems().forEach(problem -> Almura.LOGGER.debug("Content assets: {}", problem));
            Almura.LOGGER.info("Built Almura's content assets: {} files", pack.paths().size());
            return pack;
        }, () -> {
            // Built once the registries are whole, so recipes naming unknown items are left out rather than failing to load
            final ContentPackBuilder builder = new ContentPackBuilder(source);
            final ContentPack pack = builder.buildData(blocks, id -> BuiltInRegistries.ITEM.containsKey(Identifier.parse(id)));
            builder.problems().forEach(problem -> Almura.LOGGER.debug("Content data: {}", problem));
            Almura.LOGGER.info("Built Almura's content data: {} files ({} problems, see the debug log)", pack.paths().size(),
                builder.problems().size());
            return pack;
        });
    }
}
