/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.title.database.TitleQueries;
import com.almuradev.almura.feature.title.network.ClientboundAvailableTitlesResponsePacket;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitleBulkPacket;
import com.almuradev.almura.feature.title.network.ClientboundSelectedTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundTitlesRegistryPacket;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseUtils;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.title.tables.records.TitleSelectRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Results;
import org.slf4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.text.Text;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerTitleManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final ServerNotificationManager serverNotificationManager;
    private final DatabaseManager databaseManager;

    private final Map<String, Title> titles = new HashMap<>();
    private final Map<UUID, Set<Title>> availableTitles = new HashMap<>();
    private final Map<UUID, Title> selectedTitles = new HashMap<>();

    @Inject
    public ServerTitleManager(final PluginContainer container, final Scheduler scheduler, final Logger logger,
            @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final ServerNotificationManager serverNotificationManager,
            final DatabaseManager databaseManager) {
        this.container = container;
        this.scheduler = scheduler;
        this.logger = logger;
        this.network = network;
        this.serverNotificationManager = serverNotificationManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onServerStarting(final GameStartingServerEvent event) {
        this.loadTitles();
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {

        // Clear everything out for joining player
        this.selectedTitles.remove(player.getUniqueId());

        // Send available titles to joiing player, send as delayed here in order to prevent race condition.
        // Todo: Zidane; why is a race condition of this.getAvailableTitlesFor() return null if not ran within scheduled task at this point.
        this.scheduler
                .createTaskBuilder()
                .delayTicks(5)
                .execute(() -> {
                    final Set<Title> availableTitles = this.getAvailableTitlesFor(player).orElse(null);
                    if (availableTitles != null) {
                        this.network.sendTo(player, new ClientboundAvailableTitlesResponsePacket(availableTitles));
                    }
                })
                .submit(this.container);


        // Send titles to joiner
        this.network.sendTo(player, new ClientboundTitlesRegistryPacket(
                        this.titles
                                .values()
                                .stream()
                                .filter(title -> !title.isHidden())
                                .collect(Collectors.toSet())
                )
        );

        // Send selected titles to joiner
        this.network.sendTo(player, new ClientboundSelectedTitleBulkPacket(
                        this.selectedTitles
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getId()))
                )
        );

        // Cache available titles for the joiner
        this.calculateAvailableTitlesFor(player);

        // Query database for selected title for joiner
        this.scheduler.createTaskBuilder()
                .async()
                .execute(() -> {
                    try (final DSLContext context = this.databaseManager.createContext(true)) {
                        final TitleSelectRecord record = TitleQueries
                                .createFetchSelectedTitleFor(player.getUniqueId())
                                .build(context)
                                .keepStatement(false)
                                .fetchOne();

                        if (record != null) {
                            final String titleId = record.getTitle();

                            this.scheduler
                                    .createTaskBuilder()
                                    .delayTicks(5)
                                    .execute(() -> {
                                        final Title selectedTitle = this.getTitle(titleId).orElse(null);

                                        if (this.verifySelectedTitle(player, selectedTitle)) {
                                            this.selectedTitles.put(player.getUniqueId(), selectedTitle);

                                            // Send everyone joiner's selected title
                                            this.network.sendToAll(new ClientboundSelectedTitlePacket(player.getUniqueId(), titleId));
                                        }
                                    })
                                    .submit(this.container);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(this.container);
    }

    public Optional<Title> getTitle(final String titleId) {
        return Optional.ofNullable(this.titles.get(titleId));
    }

    public Optional<Set<Title>> getAvailableTitlesFor(final Player holder) {
        checkNotNull(holder);

        return Optional.ofNullable(this.availableTitles.get(holder.getUniqueId()));
    }

    public boolean loadTitles() {

        this.availableTitles.clear();

        this.logger.info("Querying database for titles, please wait...");

        this.scheduler.createTaskBuilder()
                .async()
                .execute(() -> {
                    try (final DSLContext context = this.databaseManager.createContext(true)) {
                        final Results results = TitleQueries
                                .createFetchAllTitles()
                                .build(context)
                                .keepStatement(false)
                                .fetchMany();

                        final Map<String, Title> titles = new HashMap<>();

                        results.forEach(result -> {
                            for (Record record : result) {
                                final String id = record.getValue(com.almuradev.generated.title.tables.Title.TITLE.ID);
                                final Timestamp created = record.getValue(com.almuradev.generated.title.tables.Title.TITLE.CREATED);
                                final UUID creator = DatabaseUtils.fromBytes(record.getValue(com.almuradev.generated.title.tables.Title.TITLE
                                        .CREATOR));
                                final String name = record.getValue(com.almuradev.generated.title.tables.Title.TITLE.NAME);
                                final String permission = record.getValue(com.almuradev.generated.title.tables.Title.TITLE.PERMISSION);
                                final boolean isHidden = record.getValue(com.almuradev.generated.title.tables.Title.TITLE.IS_HIDDEN);
                                final String content = record.getValue(com.almuradev.generated.title.tables
                                        .Title.TITLE.CONTENT);

                                titles.put(id, new Title(created, creator, id, name, permission, isHidden, content));
                            }
                        });

                        this.scheduler.createTaskBuilder()
                                .execute(() -> {
                                    this.titles.clear();

                                    if (!titles.isEmpty()) {
                                        this.titles.putAll(titles);
                                    }

                                    this.logger.info("Loaded {} title(s).", this.titles.size());

                                    // Re-send titles to everyone
                                    this.network.sendToAll(new ClientboundTitlesRegistryPacket(this.titles.isEmpty() ? null : new HashSet<>(this.titles
                                            .values())));

                                    // Re-calculate available titles
                                    Sponge.getServer().getOnlinePlayers().forEach(player -> {
                                        this.calculateAvailableTitlesFor(player);

                                        this.verifySelectedTitle(player, null);
                                    });


                                })
                                .submit(this.container);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(this.container);

        return true;
    }

    public void calculateAvailableTitlesFor(final Player player) {
        checkNotNull(player);

        System.out.println("HasPerms: " + player.hasPermission("almura.title.admin"));

        this.availableTitles.remove(player.getUniqueId());

        if (this.titles.isEmpty()) {
            return;
        }

        final Set<Title> availableTitles = this.titles
                .entrySet()
                .stream()
                .filter(kv -> {
                    if (kv.getValue().isHidden()) {
                        System.out.println("Check 1");
                        return player.hasPermission(Almura.ID + ".title.admin");
                    }
                    System.out.println("Check 2");
                    return player.hasPermission(kv.getValue().getId());
                })
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(HashSet::new));

        if (availableTitles.isEmpty()) {
            System.out.println("Available Titles SM empty");
            return;
        }
        System.out.println("Returned List of Titles");
        this.availableTitles.put(player.getUniqueId(), availableTitles);
    }

    public boolean verifySelectedTitle(final Player player, @Nullable Title selectedTitle) {
        checkNotNull(player);

        boolean remove = false;

        if (selectedTitle == null) {
            selectedTitle = this.selectedTitles.get(player.getUniqueId());

            if (selectedTitle == null) {
                remove = true;
            }
        }

        if (!remove) {
            final Title foundTitle = this.titles.get(selectedTitle.getId());

            if (foundTitle == null) {
                remove = true;
            }

            if (!remove && selectedTitle.isHidden()) {
                remove = true;
            }

            if (!remove && !player.hasPermission(selectedTitle.getPermission())) {
                remove = true;
            }
        }

        if (remove) {
            this.scheduler.createTaskBuilder()
                    .async()
                    .execute(() -> {
                                try (final DSLContext context = this.databaseManager.createContext(true)) {
                                    TitleQueries
                                            .createDeleteSelectedTitleFor(player.getUniqueId())
                                            .build(context)
                                            .execute();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                    )
                    .submit(this.container);
        }

        return !remove;
    }

    public void setSelectedTitleFor(final Player player, @Nullable final String titleId) {
        checkNotNull(player);

        boolean remove = titleId == null;

        Title title;

        if (!remove) {
            title = this.getTitle(titleId).orElse(null);

            if (title != null) {
                final Set<Title> availableTitles = this.getAvailableTitlesFor(player).orElse(null);
                if (availableTitles == null || !availableTitles.contains(title)) {
                    this.network.sendTo(player, new ClientboundTitlesRegistryPacket(new HashSet<>(this.titles.values())));
                    this.network.sendTo(player, new ClientboundAvailableTitlesResponsePacket(availableTitles));
                } else {

                    final Title previousTitle = this.selectedTitles.remove(player.getUniqueId());

                    this.selectedTitles.put(player.getUniqueId(), title);

                    this.network.sendToAll(new ClientboundSelectedTitlePacket(player.getUniqueId(), titleId));

                    this.scheduler.createTaskBuilder()
                            .async()
                            .execute(() -> {
                                try (final DSLContext context = this.databaseManager.createContext(true)) {
                                    if (previousTitle != null) {
                                        TitleQueries
                                                .createDeleteSelectedTitleFor(player.getUniqueId())
                                                .build(context)
                                                .execute();

                                        TitleQueries
                                                .createInsertSelectedTitleHistoryFor(player.getUniqueId(), previousTitle.getId())
                                                .build(context)
                                                .execute();
                                    }

                                    TitleQueries
                                            .createInsertSelectedTitleFor(player.getUniqueId(), titleId)
                                            .build(context)
                                            .execute();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            })
                            .submit(this.container);
                }
            } else {
                remove = true;
            }
        }

        if (remove) {
            this.scheduler.createTaskBuilder()
                    .async()
                    .execute(() -> {
                                try (final DSLContext context = this.databaseManager.createContext(true)) {
                                    TitleQueries
                                            .createDeleteSelectedTitleFor(player.getUniqueId())
                                            .build(context)
                                            .execute();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                    )
                    .submit(this.container);
        }
    }

    public void addTitle(final Player player, final String id, final String name, final String permission, final String content) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);
        checkNotNull(content);

        if (!player.hasPermission(Almura.ID + ".title.create")) {
            serverNotificationManager.sendPopupNotification(player, Text.of("Title Manager"), Text.of("Insufficient Permission!, Title addition failed."),5);
            return;
        }

        if (this.getTitle(id).isPresent()) {
            serverNotificationManager.sendPopupNotification(player, Text.of("Title Manager"), Text.of("This Title already exists!"),5);

            this.network.sendTo(player, new ClientboundTitlesRegistryPacket(
                            this.titles
                                    .values()
                                    .stream()
                                    .filter(title -> !title.isHidden())
                                    .collect(Collectors.toSet())
                    )
            );
        } else {
            this.scheduler
                    .createTaskBuilder()
                    .async()
                    .execute(() -> {
                        try (final DSLContext context = this.databaseManager.createContext(true)) {
                            final int result = TitleQueries
                                    .createInsertTitle(player.getUniqueId(), id, name, permission, content)
                                    .build(context)
                                    .keepStatement(false)
                                    .execute();

                            final Runnable runnable;

                            if (result == 0) {
                                runnable = () -> {
                                    serverNotificationManager.sendPopupNotification(player, Text.of("Title Manager"), Text.of("Thread execution to add Title to database Failed!"),5);
                                };
                            } else {
                                runnable = this::loadTitles;
                            }

                            this.scheduler
                                    .createTaskBuilder()
                                    .execute(runnable)
                                    .submit(this.container);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    })
                    .submit(this.container);
        }
    }

    public void modifyTitle(final Player player, final String id, final String name, final String permission, final String content) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);
        checkNotNull(content);

        if (!player.hasPermission(Almura.ID + ".title.modify")) {
            // TODO Dockter, handle this
            return;
        }

        final Title title = this.getTitle(id).orElse(null);

        if (title == null) {
            // TODO Dockter, we're in a desync...either send them a notification that title modify failed as it doesn't exist or remove this TODO

            this.network.sendTo(player, new ClientboundTitlesRegistryPacket(
                            this.titles
                                    .values()
                                    .stream()
                                    .filter(t -> !t.isHidden())
                                    .collect(Collectors.toSet())
                    )
            );
        } else {
            this.scheduler.createTaskBuilder()
                    .async()
                    .execute(() -> {
                        try (final DSLContext context = this.databaseManager.createContext(true)) {
                            final int result = TitleQueries
                                    .createUpdateTitle(id, name, permission, content)
                                    .build(context)
                                    .keepStatement(false)
                                    .execute();

                            final Runnable runnable;

                            if (result == 0) {
                                runnable = () -> {
                                    // TODO Dockter, send a notification down to the player that modify failed
                                    System.err.println("Modify failed!");
                                };
                            } else {
                                runnable = this::loadTitles;
                            }

                            this.scheduler
                                    .createTaskBuilder()
                                    .execute(runnable)
                                    .submit(this.container);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    })
                    .submit(this.container);
        }
    }
    public void setTitleVisibility(final Player player, final String id, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);

        if (!this.getTitle(id).isPresent()) {
            // TODO Dockter, we're in a desync...either send them a notification that title visibility changing failed as it doesn't exist or remove this TODO

            this.network.sendTo(player, new ClientboundTitlesRegistryPacket(
                            this.titles
                                    .values()
                                    .stream()
                                    .filter(title -> !title.isHidden())
                                    .collect(Collectors.toSet())
                    )
            );
        } else {
            this.scheduler
                    .createTaskBuilder()
                    .async()
                    .execute(() -> {
                        try (final DSLContext context = this.databaseManager.createContext(true)) {
                            final int result = TitleQueries
                                    .createSetTitleHidden(id, isHidden)
                                    .build(context)
                                    .keepStatement(false)
                                    .execute();

                            final Runnable runnable;

                            if (result == 0) {
                                runnable = () -> {
                                    // TODO Dockter, send a notification down to the player that changing title visibility failed
                                    System.err.println("Modify failed!");
                                };
                            } else {
                                runnable = this::loadTitles;
                            }

                            this.scheduler
                                    .createTaskBuilder()
                                    .execute(runnable)
                                    .submit(this.container);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    })
                    .submit(this.container);
        }
    }
}
