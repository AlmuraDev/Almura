/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.loader;

import com.almuradev.almura.asm.mixin.accessors.client.MinecraftAccessor;
import com.almuradev.almura.asm.mixin.accessors.client.block.BlockAccessor;
import com.almuradev.content.ContentConfig;
import com.almuradev.content.ContentType;
import com.almuradev.core.event.Witness;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.inject.Injector;
import net.kyori.indigo.DetailedReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public final class RootContentLoader implements Witness {
    private static final boolean FORCE_SEARCH_JARS = Boolean.getBoolean(ContentConfig.SYSTEM_PROPERTY_PREFIX + "loader.searchJars");
    public static final String FILESYSTEM_ASSETS = "fs_4";
    public static final String MANAGED_ASSETS = "managed";
    private final Path assets;
    private final Injector injector;
    private final Logger logger;
    private final Set<ContentType> types;
    private final AssetState state;
    private final List<SearchEntry> entries = new ArrayList<>();
    private final MutableGraph<JarSearchEntry> graph = GraphBuilder.directed().build();
    @Inject private RecipeManager recipeManager; // HACK

    @Inject
    public RootContentLoader(@Named("assets") final Path assets, final Injector injector, final Logger logger, final Set<ContentType> types) {
        this.assets = assets;
        this.injector = injector;
        this.logger = logger;
        this.types = types;
        this.state = AssetState.resolve(logger, assets);
    }

    // TODO(kashike); find a better way to do this
    @Listener
    @SideOnly(Side.CLIENT)
    public void insertResourcePack(final GamePreInitializationEvent event) {
        final Minecraft client = Minecraft.getMinecraft();
        for (final AbstractFileSystemSearchEntry qrp : this.entries(AbstractFileSystemSearchEntry.class)) {
            ((MinecraftAccessor) client).accessor$getDefaultResourcePacks().add(new DirectoryResourcePack(qrp));
        }
        client.refreshResources();
    }

    /**
     * Queue a search for content in the filesystem {@code path}.
     *
     * @param path the path
     */
    public void searchFileSystem(final Path path) {
        if (!SearchableSearchEntry.exists(this.logger, path)) {
            return;
        }
        this.pushFileSystem(path);
    }

    private void pushFileSystem(final Path path) {
        this.entries.add(new FileSystemSearchEntry(path));
    }

    /**
     * Queue a search for content in each JAR found in {@code path}.
     *
     * @param path the jars
     */
    public void searchJars(final Path path) {
        for (final AssetStateEntry entry : this.state.entries.values()) {
            this.searchJar(entry.resolve(path));
        }
    }

    private void searchJar(final Path path) {
        if (!SearchableSearchEntry.exists(this.logger, path) || !this.shouldLoadJar(path)) {
            return;
        }
        this.pushJar(path);
    }

    private boolean shouldLoadJar(final Path path) {
        if (FORCE_SEARCH_JARS || !((boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            return true;
        }
        this.logger.info("Skipping asset JAR '{}' - JAR loading is not supported in a deobfuscated environment.", path);
        return false;
    }

    private void pushJar(final Path path) {
        final AssetStateEntry state = this.state.entry(path);
        final URI uri = URI.create("jar:" + path.toUri());
        final JarSearchEntry entry = new JarSearchEntry(state, uri);
        for (final SearchEntry other : this.entries) {
            if (other instanceof JarSearchEntry) {
                this.graph.putEdge((JarSearchEntry) other, entry);
            }
        }
        this.entries.add(entry);
    }

    private void resolve() {
        final List<JarSearchEntry> jars = this.entries(JarSearchEntry.class);
        if (!jars.isEmpty()) {
            this.resolveJars(jars, this.assets.resolve(MANAGED_ASSETS));
        }

        final List<SearchableSearchEntry> searchable = this.entries(SearchableSearchEntry.class);
        for (final SearchableSearchEntry entry : searchable) {
            entry.search(this.injector, this.logger, this.types);
        }

        this.logger.debug("Processing queued content...");
        for (final ContentType type : this.types) {
            this.logger.debug("    Processing queued '{}' content...", type.id());
            this.process(type);
        }

        this.state.write(this.logger, this.assets);
    }

    private void process(final ContentType type) {
        try {
            type.loader(this.injector).process();
        } catch (final DetailedReportedException e) {
            this.logger.error("{}:\n {}", e.getMessage(), e.report().toString());
        }
    }

    private void resolveJars(final List<JarSearchEntry> jars, final Path target) {
        this.entries.add(0, new ManagedFileSystemEntry(target.resolve(ContentConfig.ASSETS_DIRECTORY)));

        for (final JarSearchEntry jar : jars) {
            jar.loadJson(this.logger);
        }

        for (final JarSearchEntry jar : jars) {
            for (final JarSearchEntry predecessor : this.graph.predecessors(jar)) {
                predecessor.filter(jar);
            }
        }

        for (final JarSearchEntry jar : jars) {
            if (jar.canRevert(this.logger) && jar.state.previous != null) {
                this.logger.debug("Beginning rollback of '{}' to '{}'", jar.state.id, jar.state.previous.id);
                jar.revert(this.logger, target);
            }
        }

        for (final JarSearchEntry jar : jars) {
            if (jar.canCopy(this.logger)) {
                jar.copy(this.logger, target);
                jar.remove(this.logger, target);
            }
        }
    }

    private <T> List<T> entries(final Class<T> type) {
        final List<T> entries = new ArrayList<>();
        for (final SearchEntry entry : this.entries) {
            if (type.isInstance(entry)) {
                entries.add(type.cast(entry));
            }
        }
        return entries;
    }

    /**
     * Load content.
     */
    public void load() {
        this.resolve();

        this.logger.debug("Loading content...");
        for (final ContentType type : this.types) {
            this.logger.debug("    Loading '{}' content...", type.id());
            try {
                final int loaded = type.loader(this.injector).load();
                this.logger.debug("        Loaded '{}' asset{}", loaded, loaded == 1 ? "" : "s");
            } catch (final IOException e) {
                this.logger.error("Encountered an exception while searching for '{}' content", type.id());
            } catch (final DetailedReportedException e) {
                this.logger.error("{}:\n {}", e.getMessage(), e.report().toString());
            }
        }
    }

    @SubscribeEvent
    public void recipes(final RegistryEvent.Register<IRecipe> event) {
        try {
            this.recipeManager.load();
        } catch (final IOException e) {
            this.logger.error("Encountered an exception while loading recipes");
        }
    }
}
