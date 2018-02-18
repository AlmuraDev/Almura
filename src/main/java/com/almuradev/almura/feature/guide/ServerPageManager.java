/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.guide;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.guide.network.ClientboundGuideOpenResponsePacket;
import com.almuradev.almura.feature.guide.network.ClientboundPageListingsPacket;
import com.almuradev.almura.feature.guide.network.GuideOpenType;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.event.Witness;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerPageManager extends Witness.Impl implements Witness.Lifecycle {

    private final Game game;
    private final Logger logger;
    private final Path configRoot;
    private final Path pageRoot;
    private final ChannelBinding.IndexedMessageChannel network;
    private final Map<String, Page> pages = new HashMap<>();

    @com.google.inject.Inject
    private static ServerNotificationManager manager;

    @Inject
    public ServerPageManager(final Game game, Logger logger, @ConfigDir(sharedRoot = false) final Path configRoot, final @ChannelId(NetworkConfig
            .CHANNEL) ChannelBinding.IndexedMessageChannel network) {
        this.game = game;
        this.logger = logger;
        this.configRoot = configRoot.resolve(GuideConfig.DIR_GUIDE);
        this.pageRoot = this.configRoot.resolve(GuideConfig.DIR_GUIDE_PAGES);
        this.network = network;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (!player.hasPermission("almura.guide.open_at_login")) {
            return;
        }

        openGuideForPlayer(player, GuideOpenType.PLAYER_LOGGED_IN, null);
    }

    public void openGuideForPlayer(Player player, GuideOpenType type, String pageName) {
        this.network.sendTo(player, new ClientboundGuideOpenResponsePacket(
                type, // Specifies what called the open request.
                player.hasPermission("almura.guide.add"),
                player.hasPermission("almura.guide.remove"),
                player.hasPermission("almura.guide.modify")));

        final Map<String, Page> pagesToSend = this.getAvailablePagesFor(player);
        if (pagesToSend.size() > 0) {

            final List<PageListEntry> playerListings = pagesToSend.entrySet().stream().map(entry -> new PageListEntry(entry.getKey(), entry.getValue().getName())).collect(Collectors.toList());
            final PageListEntry allPages = playerListings.stream().findFirst().orElse(null);

            if (pageName != null) {
                final Page page = getPage(pageName).orElse(null);
                this.network.sendTo(player, new ClientboundPageListingsPacket(playerListings, page == null ? allPages.getId() : page.getId()));
            } else {
                this.network.sendTo(player, new ClientboundPageListingsPacket(playerListings, allPages == null ? null : allPages.getId()));
            }
        }
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) throws IOException {
        this.loadPages();
    }

    private boolean loadPages() throws IOException {
        this.pages.clear();

        if (Files.notExists(this.configRoot)) {
            Files.createDirectories(this.configRoot);
        }

        if (Files.notExists(this.pageRoot)) {
            Files.createDirectories(this.pageRoot);
        }

        final PageWalker pageWalker = new PageWalker(this.logger);
        Files.walkFileTree(this.pageRoot, pageWalker);

        pageWalker.found.forEach((pageFile) -> {
            final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(pageFile);
            try {
                final String id = pageFile.getFileName().toString().split("\\.")[0];

                final ConfigurationNode rootNode = loader.load();
                final int index = rootNode.getNode(GuideConfig.INDEX).getInt(0);
                final String name = rootNode.getNode(GuideConfig.NAME).getString(id);
                final String title = rootNode.getNode(GuideConfig.TITLE).getString("");

                final ConfigurationNode lastModifiedNode = rootNode.getNode(GuideConfig.LastModified.LAST_MODIFIED);
                final String lastModifiedModifierRaw = lastModifiedNode.getNode(GuideConfig.LastModified.MODIFIER).getString("");

                UUID lastModifiedModifier;
                try {
                    lastModifiedModifier = UUID.fromString(lastModifiedModifierRaw);
                } catch (Exception ex) {
                    lastModifiedModifier = new UUID(0L, 0L);
                }

                final String lastModifiedTimeRaw = lastModifiedNode.getString(GuideConfig.LastModified.TIME);

                Instant lastModifiedTime;
                try {
                    lastModifiedTime = Instant.parse(lastModifiedTimeRaw);
                } catch (Exception ex) {
                    lastModifiedTime = Instant.now();
                }

                final ConfigurationNode createdNode = rootNode.getNode(GuideConfig.Created.CREATED);
                final String createdCreatorRaw = createdNode.getNode(GuideConfig.Created.CREATOR).getString("");

                UUID createdCreator;
                try {
                    createdCreator = UUID.fromString(createdCreatorRaw);
                } catch (Exception ex) {
                    createdCreator = new UUID(0L, 0L);
                }

                final String createdTimeRaw = lastModifiedNode.getString(GuideConfig.Created.TIME);

                Instant createdTime;
                try {
                    createdTime = Instant.parse(createdTimeRaw);
                } catch (Exception ex) {
                    createdTime = Instant.now();
                }

                final String content = rootNode.getNode(GuideConfig.CONTENT).getString("");

                final Page page = new Page(id, createdCreator, createdTime);
                page.setIndex(index);
                page.setName(name);
                page.setLastModifier(lastModifiedModifier);
                page.setLastModified(lastModifiedTime);
                page.setContent(content);

                this.logger.info("Loaded '" + id + "' page.");

                this.pages.put(id, page);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.logger.info("Loaded " + this.pages.size() + " page(s).");

        return true;
    }

    private ConfigurationLoader<CommentedConfigurationNode> createLoader(Path path) {
        checkNotNull(path);

        return HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(ConfigurationOptions.defaults())
                .setRenderOptions(
                        ConfigRenderOptions.defaults()
                                .setFormatted(true)
                                .setComments(true)
                                .setOriginComments(false)
                )
                .build();
    }

    public Map<String, Page> getAvailablePagesFor(Player player) {
        checkNotNull(player);

        return this.pages.entrySet()
                .stream()
                .filter(entry -> player.hasPermission("almura.guide.page." + entry.getKey()))
                .sorted(Comparator.comparingInt(p -> p.getValue().getIndex()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) ->
                {
                    throw new IllegalStateException(String.format("Duplicate key %s", k));
                }, LinkedHashMap::new));
    }

    public Optional<Page> getPage(String id) {
        return Optional.ofNullable(this.pages.get(id));
    }

    public boolean loadAndSyncPages() throws IOException {
        final boolean loaded = this.loadPages();
        if (loaded) {
            this.game.getServer().getOnlinePlayers().forEach((player) -> {
                this.network.sendTo(player, new ClientboundPageListingsPacket(this.getAvailablePagesFor(player).entrySet().stream().map(entry ->
                        new PageListEntry(entry.getKey(), entry.getValue().getName())).collect(Collectors.toList()), null));
            });
        }

        return loaded;
    }

    public void addPage(Page page) {
        checkNotNull(page);

        this.pages.put(page.getId(), page);
    }

    public void deletePage(String id) {
        checkNotNull(id);

        if (this.pages.remove(id) != null) {

            final Path path = this.pageRoot.resolve(id + GuideConfig.EXT_CONFIG_PAGE);

            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void savePage(Page page, boolean notify) {
        checkNotNull(page);

        final Path path = this.pageRoot.resolve(page.getId() + GuideConfig.EXT_CONFIG_PAGE);
        final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(path);
        final ConfigurationNode rootNode = loader.createEmptyNode();

        rootNode.getNode(GuideConfig.INDEX).setValue(page.getIndex());
        rootNode.getNode(GuideConfig.NAME).setValue(page.getName());

        final ConfigurationNode lastModifiedNode = rootNode.getNode(GuideConfig.LastModified.LAST_MODIFIED);
        lastModifiedNode.getNode(GuideConfig.LastModified.MODIFIER).setValue(page.getLastModifier().toString());
        lastModifiedNode.getNode(GuideConfig.LastModified.TIME).setValue(page.getLastModified().toString());

        final ConfigurationNode createdNode = rootNode.getNode(GuideConfig.Created.CREATED);
        createdNode.getNode(GuideConfig.Created.CREATOR).setValue(page.getCreator().toString());
        createdNode.getNode(GuideConfig.Created.TIME).setValue(page.getCreated().toString());

        // Packet sends up as sectional, since I am a nice guy I'll let them save as ampersand
        rootNode.getNode(GuideConfig.CONTENT).setValue(Page.asFriendlyText(page.getContent()));

        if (notify) {
            for (final Player player : this.game.getServer().getOnlinePlayers()) {
                manager.sendPopupNotification(player, Text.of("Guide Update"), Text.of("The Guide: (" + page.getName() + ") has been updated!"), 10);
            }
        }

        try {
            loader.save(rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final class PageWalker implements FileVisitor<Path> {

        private final Logger logger;
        private final Set<Path> found = new HashSet<>();

        PageWalker(Logger logger) {
            this.logger = logger;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path fileName = file.getFileName();
            if (fileName.toString().endsWith(".conf")) {
                this.found.add(file);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
            if (exception != null && !(exception instanceof NoSuchFileException)) {
                this.logger.error("Encountered an exception while visiting file '{}'", file, exception);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
            if (exception != null && !(exception instanceof NoSuchFileException)) {
                this.logger.error("Encountered an exception while visiting directory '{}'", directory, exception);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}
