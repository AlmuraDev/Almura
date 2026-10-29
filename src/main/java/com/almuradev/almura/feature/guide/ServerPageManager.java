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
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.TextUtil;
import com.almuradev.almura.shared.util.UchatUtil;
import com.almuradev.core.event.Witness;
import com.typesafe.config.ConfigRenderOptions;
import com.almuradev.almura.feature.permission.SingleplayerFeatures;
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Server guides live in guide/pages and are shown by permission (almura.guide.page.&lt;id&gt;). Player guides live in
 * guide/player_pages/&lt;owner&gt; and are shown to their owner and to guide admins (almura.guide.modify).
 *
 * <p>Player guide permissions: almura.guide.player.add to create them, the almura.guide.player.limit option for how many (default
 * {@link #DEFAULT_PLAYER_GUIDE_LIMIT}). Owners and guide admins may edit and remove them.</p>
 */
@Singleton
public final class ServerPageManager extends Witness.Impl implements Witness.Lifecycle {

    public static final String PERMISSION_ADMIN = "almura.guide.modify";
    public static final String PERMISSION_PLAYER_ADD = "almura.guide.player.add";
    public static final String OPTION_PLAYER_LIMIT = "almura.guide.player.limit";
    private static final int DEFAULT_PLAYER_GUIDE_LIMIT = 5;
    private static final int MAX_PLAYER_ID_LENGTH = 32;

    private final Game game;
    private final Logger logger;
    private final Path configRoot;
    private final Path pageRoot;
    private final Path playerPageRoot;
    private final ChannelBinding.IndexedMessageChannel network;
    private final Map<String, Page> pages = new HashMap<>();
    private final Map<UUID, String> ownerNames = new HashMap<>();
    private final PluginContainer container;

    @com.google.inject.Inject
    private static ServerNotificationManager manager;

    @Inject
    public ServerPageManager(final Game game, Logger logger, @ConfigDir(sharedRoot = false) final Path configRoot, final @ChannelId(NetworkConfig
        .CHANNEL) ChannelBinding.IndexedMessageChannel network, final PluginContainer container) {
        this.game = game;
        this.logger = logger;
        this.configRoot = configRoot.resolve(GuideConfig.DIR_GUIDE);
        this.pageRoot = this.configRoot.resolve(GuideConfig.DIR_GUIDE_PAGES);
        this.playerPageRoot = this.configRoot.resolve(GuideConfig.DIR_GUIDE_PLAYER_PAGES);
        this.network = network;
        this.container = container;
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void onPlayerJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
        if (!player.hasPermission("almura.guide.open_at_login") || SingleplayerFeatures.blocked() || player.health().get() == 0) {
            return;
        }

        Task.builder()
            .delayTicks(100) // Give GP time to register the user as its not at tick zero.
            .execute(t -> openGuideForPlayer(player, GuideOpenType.PLAYER_LOGGED_IN, null))
            .submit(this.container);
    }

    public void openGuideForPlayer(Player player, GuideOpenType type, String pageName) {
        this.network.sendTo(player, this.createOpenResponse(player, type, false));

        final List<PageListEntry> playerListings = this.getListingsFor(player);
        if (playerListings.size() > 0) {
            final PageListEntry allPages = playerListings.stream().findFirst().orElse(null);

            if (pageName != null) {
                final Page page = getPage(pageName).orElse(null);
                this.network.sendTo(player, new ClientboundPageListingsPacket(playerListings, page == null ? allPages.getId() : page.getId()));
            } else {
                this.network.sendTo(player, new ClientboundPageListingsPacket(playerListings, allPages == null ? null : allPages.getId()));
            }
        }
    }

    public ClientboundGuideOpenResponsePacket createOpenResponse(final Player player, final GuideOpenType type, final boolean requestedFromClient) {
        return new ClientboundGuideOpenResponsePacket(
            type, // Specifies what called the open request.
            player.hasPermission("almura.guide.add"),
            player.hasPermission("almura.guide.remove"),
            player.hasPermission(PERMISSION_ADMIN),
            player.hasPermission(PERMISSION_PLAYER_ADD),
            requestedFromClient);
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) throws IOException {
        this.loadPages();
    }

    private boolean loadPages() throws IOException {
        this.pages.clear();
        this.ownerNames.clear();

        if (Files.notExists(this.configRoot)) {
            Files.createDirectories(this.configRoot);
        }

        if (Files.notExists(this.pageRoot)) {
            Files.createDirectories(this.pageRoot);
        }

        if (Files.notExists(this.playerPageRoot)) {
            Files.createDirectories(this.playerPageRoot);
        }

        final PageWalker pageWalker = new PageWalker(this.logger);
        Files.walkFileTree(this.pageRoot, pageWalker);
        pageWalker.found.forEach(pageFile -> this.loadPage(pageFile, null));

        // Player guides: a folder per owner
        int playerPages = 0;
        try (final DirectoryStream<Path> owners = Files.newDirectoryStream(this.playerPageRoot, Files::isDirectory)) {
            for (final Path ownerDir : owners) {
                final UUID owner;
                try {
                    owner = UUID.fromString(ownerDir.getFileName().toString());
                } catch (IllegalArgumentException ex) {
                    this.logger.warn("Skipping player guide folder '{}', it is not named after a player's UUID.", ownerDir);
                    continue;
                }

                final PageWalker ownerWalker = new PageWalker(this.logger);
                Files.walkFileTree(ownerDir, ownerWalker);
                for (final Path pageFile : ownerWalker.found) {
                    if (this.loadPage(pageFile, owner)) {
                        playerPages++;
                    }
                }
            }
        }

        this.logger.info("Loaded [" + (this.pages.size() - playerPages) + "] page(s) and [" + playerPages + "] player page(s).");

        return true;
    }

    private boolean loadPage(final Path pageFile, @Nullable final UUID owner) {
        final ConfigurationLoader<CommentedConfigurationNode> loader = this.createLoader(pageFile);
        try {
            final String fileId = pageFile.getFileName().toString().split("\\.")[0];
            final String id = owner == null ? fileId : playerPageId(owner, fileId);

            final ConfigurationNode rootNode = loader.load();
            final int index = rootNode.getNode(GuideConfig.INDEX).getInt(0);
            final String name = rootNode.getNode(GuideConfig.NAME).getString(fileId);

            final ConfigurationNode lastModifiedNode = rootNode.getNode(GuideConfig.LastModified.LAST_MODIFIED);
            final UUID lastModifiedModifier = parseUuid(lastModifiedNode.getNode(GuideConfig.LastModified.MODIFIER).getString(""));
            final Instant lastModifiedTime = parseInstant(lastModifiedNode.getNode(GuideConfig.LastModified.TIME).getString(""));

            final ConfigurationNode createdNode = rootNode.getNode(GuideConfig.Created.CREATED);
            final UUID createdCreator = parseUuid(createdNode.getNode(GuideConfig.Created.CREATOR).getString(""));
            final Instant createdTime = parseInstant(createdNode.getNode(GuideConfig.Created.TIME).getString(""));

            final String content = rootNode.getNode(GuideConfig.CONTENT).getString("");

            final Page page = new Page(id, createdCreator, createdTime, owner);
            page.setIndex(index);
            page.setName(name);
            page.setLastModifier(lastModifiedModifier);
            page.setLastModified(lastModifiedTime);
            page.setContent(content);

            this.logger.info("Loaded '" + id + "' page.");

            this.pages.put(id, page);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static UUID parseUuid(final String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception ex) {
            return new UUID(0L, 0L);
        }
    }

    private static Instant parseInstant(final String raw) {
        try {
            return Instant.parse(raw);
        } catch (Exception ex) {
            return Instant.now();
        }
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

    /**
     * Server guides the player has permission for, then player guides they may see: their own, then everyone else's for admins. Each by
     * index, then name.
     */
    public Map<String, Page> getAvailablePagesFor(Player player) {
        checkNotNull(player);

        final Comparator<Map.Entry<String, Page>> order = Comparator
            .<Map.Entry<String, Page>>comparingInt(entry -> !entry.getValue().isPlayerGuide() ? 0 : player.getUniqueId().equals(entry.getValue()
                .getOwner()) ? 1 : 2)
            .thenComparingInt(entry -> entry.getValue().getIndex())
            .thenComparing(entry -> plainName(entry.getValue().getName()), String.CASE_INSENSITIVE_ORDER);

        return this.pages.entrySet()
            .stream()
            .filter(entry -> this.canView(player, entry.getValue()))
            .sorted(order)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) ->
            {
                throw new IllegalStateException(String.format("Duplicate key %s", k));
            }, LinkedHashMap::new));
    }

    /**
     * @return The list entries of {@link #getAvailablePagesFor(Player)}
     */
    public List<PageListEntry> getListingsFor(final Player player) {
        return this.getAvailablePagesFor(player).values()
            .stream()
            .map(page -> page.isPlayerGuide()
                ? new PageListEntry(page.getId(), page.getName(), true, player.getUniqueId().equals(page.getOwner()), this.getOwnerName(page))
                : new PageListEntry(page.getId(), page.getName()))
            .collect(Collectors.toList());
    }

    public boolean isAdmin(final Player player) {
        return player.hasPermission(PERMISSION_ADMIN);
    }

    public boolean canView(final Player player, final Page page) {
        if (page.isPlayerGuide()) {
            return player.getUniqueId().equals(page.getOwner()) || this.isAdmin(player);
        }
        return player.hasPermission("almura.guide.page." + page.getId());
    }

    public boolean canModify(final Player player, final Page page) {
        if (page.isPlayerGuide()) {
            return player.getUniqueId().equals(page.getOwner()) || this.isAdmin(player);
        }
        return player.hasPermission("almura.guide.modify." + page.getId());
    }

    public boolean canRemove(final Player player, final Page page) {
        if (page.isPlayerGuide()) {
            return player.getUniqueId().equals(page.getOwner()) || this.isAdmin(player);
        }
        return player.hasPermission("almura.guide.remove." + page.getId());
    }

    /**
     * @return How many player guides the player may have: the almura.guide.player.limit option, or {@link #DEFAULT_PLAYER_GUIDE_LIMIT}
     */
    public int getPlayerGuideLimit(final Player player) {
        final String raw = player.getOption(OPTION_PLAYER_LIMIT).orElse(null);
        if (raw != null) {
            try {
                return Math.max(0, Integer.parseInt(raw.trim()));
            } catch (NumberFormatException ex) {
                this.logger.warn("Option '{}' of '{}' is '{}', not a number. Using {}.", OPTION_PLAYER_LIMIT, player.getName(), raw,
                    DEFAULT_PLAYER_GUIDE_LIMIT);
            }
        }
        return DEFAULT_PLAYER_GUIDE_LIMIT;
    }

    /**
     * @return The index that puts a new player guide at the bottom of its owner's guides
     */
    public int getNextPlayerGuideIndex(final UUID owner) {
        return this.pages.values().stream().filter(page -> owner.equals(page.getOwner())).mapToInt(Page::getIndex).max().orElse(0) + 1;
    }

    /**
     * Moves a page one place up (-1) or down (1) among the pages of its kind as the player sees them: server guides (admins), or the
     * player's own guides. Swaps its index with its neighbour's; if the indexes repeat, so the order can't be told apart, they are
     * numbered 1, 2, 3... first.
     */
    public void movePage(final Player player, final String id, final int direction) {
        final Page page = this.pages.get(id);
        if (page == null || (direction != -1 && direction != 1)) {
            return;
        }
        final boolean allowed = page.isPlayerGuide() ? player.getUniqueId().equals(page.getOwner()) : this.isAdmin(player);
        if (!allowed) {
            return;
        }

        final List<Page> group = this.getAvailablePagesFor(player).values()
            .stream()
            .filter(other -> page.isPlayerGuide() ? page.getOwner().equals(other.getOwner()) : !other.isPlayerGuide())
            .collect(Collectors.toList());
        final int from = group.indexOf(page);
        final int to = from + direction;
        if (from < 0 || to < 0 || to >= group.size()) {
            return;
        }

        final List<Page> changed = new ArrayList<>();
        if (group.stream().mapToInt(Page::getIndex).distinct().count() < group.size()) {
            for (int i = 0; i < group.size(); i++) {
                group.get(i).setIndex(i + 1);
            }
            changed.addAll(group);
        }

        final Page neighbour = group.get(to);
        final int index = page.getIndex();
        page.setIndex(neighbour.getIndex());
        neighbour.setIndex(index);
        if (changed.isEmpty()) {
            changed.add(page);
            changed.add(neighbour);
        }
        changed.forEach(changedPage -> this.savePage(changedPage, false));

        // Only the order changed: everyone keeps the page they have open (and any unsaved edits)
        this.network.sendTo(player, new ClientboundPageListingsPacket(this.getListingsFor(player), null, true));
        this.syncListingsToAll(player.getUniqueId(), true);
    }

    public long getPlayerGuideCount(final UUID owner) {
        return this.pages.values().stream().filter(page -> owner.equals(page.getOwner())).count();
    }

    /**
     * @return A page name without its color and style codes (names can be formatted in the details screen)
     */
    public static String plainName(final String name) {
        return name.replaceAll("(?i)[§&][0-9a-fk-or]", "");
    }

    public static String playerPageId(final UUID owner, final String fileId) {
        return GuideConfig.PLAYER_ID_PREFIX + owner + "/" + fileId;
    }

    /**
     * Picks an unused id for a new player guide from its name (letters, digits and underscores, safe as a file name).
     */
    public String newPlayerPageId(final UUID owner, final String name) {
        String base = plainName(name).toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) {
            base = "guide";
        }
        if (base.length() > MAX_PLAYER_ID_LENGTH) {
            base = base.substring(0, MAX_PLAYER_ID_LENGTH);
        }

        String fileId = base;
        for (int i = 2; this.pages.containsKey(playerPageId(owner, fileId)); i++) {
            fileId = base + "_" + i;
        }
        return playerPageId(owner, fileId);
    }

    private String getOwnerName(final Page page) {
        final UUID owner = page.getOwner();
        if (owner == null) {
            return "";
        }
        return this.ownerNames.computeIfAbsent(owner, uuid -> this.game.getServiceManager().provide(UserStorageService.class)
            .flatMap(service -> service.get(uuid))
            .map(User::getName)
            .orElse(uuid.toString().substring(0, 8)));
    }

    public Optional<Page> getPage(String id) {
        return Optional.ofNullable(this.pages.get(id));
    }

    public boolean loadAndSyncPages() throws IOException {
        final boolean loaded = this.loadPages();
        if (loaded) {
            this.syncListingsToAll(null);
        }

        return loaded;
    }

    /**
     * Sends every online player (but the excluded one) their own list of pages.
     */
    public void syncListingsToAll(@Nullable final UUID exclude) {
        this.syncListingsToAll(exclude, false);
    }

    /**
     * @param reorderOnly Only the order changed, players stay on the page they have open
     */
    public void syncListingsToAll(@Nullable final UUID exclude, final boolean reorderOnly) {
        this.game.getServer().getOnlinePlayers()
            .stream()
            .filter(player -> !player.getUniqueId().equals(exclude))
            .forEach(player -> this.network.sendTo(player, new ClientboundPageListingsPacket(this.getListingsFor(player), null, reorderOnly)));
    }

    public void addPage(Page page) {
        checkNotNull(page);

        this.pages.put(page.getId(), page);
    }

    public void deletePage(String id) {
        checkNotNull(id);

        final Page page = this.pages.remove(id);
        if (page != null) {
            try {
                Files.deleteIfExists(this.getPath(page));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Path getPath(final Page page) {
        if (page.isPlayerGuide()) {
            final String fileId = page.getId().substring(page.getId().lastIndexOf('/') + 1);
            return this.playerPageRoot.resolve(page.getOwner().toString()).resolve(fileId + GuideConfig.EXT_CONFIG_PAGE);
        }
        return this.pageRoot.resolve(page.getId() + GuideConfig.EXT_CONFIG_PAGE);
    }

    public void savePage(Page page, boolean notify) {
        checkNotNull(page);

        final Path path = this.getPath(page);
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
        rootNode.getNode(GuideConfig.CONTENT).setValue(TextUtil.asFriendlyText(page.getContent()));

        // Player guides are private: no announcement
        if (notify && !page.isPlayerGuide()) {
            for (final Player player : this.game.getServer().getOnlinePlayers()) {
                if (player.hasPermission("almura.guide.page." + page.getId())) {
                    manager.sendPopupNotification(player, Text.of("Guide Update"), Text.of("The Guide: (" + plainName(page.getName()), Text.of(TextColors.WHITE, ") has been updated!")), 10);

                }
            }
            UchatUtil.relayMessageToDiscord(":regional_indicator_s:", Text.of("The Guide: (" + plainName(page.getName()) + ") has been updated!").toPlain(), true);
        }

        try {
            Files.createDirectories(path.getParent());
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
