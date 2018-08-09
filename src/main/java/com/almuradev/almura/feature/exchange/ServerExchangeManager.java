/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.database.ExchangeQueries;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.InventoryAction;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseQueue;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.almura.shared.feature.store.Store;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.axs.tables.Axs;
import com.almuradev.generated.axs.tables.AxsItem;
import com.almuradev.generated.axs.tables.AxsListItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerExchangeManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final DatabaseManager databaseManager;
    private final ServerNotificationManager notificationManager;

    private final Map<String, Exchange> exchanges = new HashMap<>();

    @Inject
    public ServerExchangeManager(final PluginContainer container, final Scheduler scheduler, final Logger logger, final @ChannelId(NetworkConfig
        .CHANNEL) ChannelBinding.IndexedMessageChannel network, final DatabaseManager databaseManager,
        final ServerNotificationManager notificationManager) {
        this.container = container;
        this.scheduler = scheduler;
        this.logger = logger;
        this.network = network;
        this.databaseManager = databaseManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onServerStarting(final GameStartingServerEvent event) {
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(this::loadExchanges)
            .submit(this.container);
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {

        // Send exchanges to joiner
        this.network.sendTo(player, new ClientboundExchangeRegistryPacket(
                this.exchanges
                    .values()
                    .stream()
                    .filter(axs -> {
                        if (!axs.isHidden()) {
                            return true;
                        }

                        return player.hasPermission(Almura.ID + ".exchange.admin");
                    })
                    .collect(Collectors.toSet())
            )
        );
    }

    public Optional<Exchange> getExchange(final String id) {
        checkNotNull(id);

        return Optional.ofNullable(this.exchanges.get(id));
    }

    /**
     * Exchange
     */

    public void loadExchanges() {

        this.logger.info("Querying database for exchanges, please wait...");

        final Map<String, Exchange> exchanges = new HashMap<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchAllExchanges()
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> {
                for (Record record : result) {
                    final String id = record.getValue(Axs.AXS.ID);
                    final Timestamp created = record.getValue(Axs.AXS.CREATED);
                    final UUID creator = SerializationUtil.uniqueIdFromBytes(record.getValue(Axs.AXS.CREATOR));
                    final String name = record.getValue(Axs.AXS.NAME);
                    final String permission = record.getValue(Axs.AXS.PERMISSION);
                    final boolean isHidden = record.getValue(Axs.AXS.IS_HIDDEN);

                    exchanges.put(id, new BasicExchange(id, created.toInstant(), creator, name, permission, isHidden));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.scheduler
            .createTaskBuilder()
            .execute(() -> {
                this.exchanges.clear();

                this.exchanges.putAll(exchanges);

                if (this.exchanges.isEmpty()) {
                    // TODO Begin Test Code
                    // TODO I might automatically create the Global Exchange (almura.global) by default (if no others are found when loading from db)
                    final BasicExchange exchange = new BasicExchange("almura.global", Instant.now(), Store.UNKNOWN_OWNER, "Exchange",
                        "almura.exchange.global", false);
                    this.exchanges.put("almura.global", exchange);

                    // Yes, I am purposely running this sync
                    try (final DSLContext context1 = this.databaseManager.createContext(true)) {
                        final int result = ExchangeQueries
                            .createInsertExchange(exchange.getCreator(), exchange.getId(), exchange.getName(), exchange.getPermission(), exchange
                                .isHidden())
                            .build(context1)
                            .keepStatement(false)
                            .execute();

                        if (result == 0) {
                            Thread.dumpStack();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                this.logger.info("Loaded {} exchange(s).", this.exchanges.size());

                this.exchanges.values().forEach(axs -> ((BasicExchange) axs).refreshCreatorName());

                // Re-send exchanges to everyone
                this.network.sendToAll(new ClientboundExchangeRegistryPacket(this.exchanges.isEmpty() ? null : new HashSet<>(this.exchanges.values
                    ())));
            })
            .submit(this.container);
    }

    public void handleExchangeManage(final Player player) {
        if (!player.hasPermission(Almura.ID + ".exchange.manage")) {
            // TODO Dockter
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.MANAGE, null));
    }

    public void handleExchangeSpecific(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".exchange.open")) {
            // TODO Dockter
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null || !player.hasPermission(axs.getPermission())) {
            // TODO Dockter

            this.network.sendTo(player, new ClientboundExchangeRegistryPacket(
                this.exchanges
                    .values()
                    .stream()
                    .filter(a -> {
                        if (!a.isHidden()) {
                            return true;
                        }

                        return player.hasPermission(Almura.ID + ".exchange.admin");
                    })
                    .collect(Collectors.toSet()))
            );
        } else {
            this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.SPECIFIC, id));

            this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, id, () -> {
                this.loadListItems(axs);

                this.scheduler
                    .createTaskBuilder()
                    .execute(() ->
                        Sponge.getServer().getOnlinePlayers().forEach(p ->
                            axs.getListItemsFor(p.getUniqueId()).ifPresent(items ->
                                this.network.sendTo(p, new ClientboundListItemsResponsePacket(axs.getId(), items)))));
            });
        }
    }

    public void handleExchangeAdd(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".exchange.create")) {
            this.notificationManager.sendPopupNotification(player, Text.of("Exchange Manager"), Text.of("Insufficient Permission!, "
                + "Exchange addition failed."), 5);
            return;
        }

        if (this.getExchange(id).isPresent()) {
            this.notificationManager.sendPopupNotification(player, Text.of("Exchange Manager"), Text.of("This Exchange already "
                + "exists!"), 5);

            this.network.sendTo(player, new ClientboundExchangeRegistryPacket(
                    this.exchanges
                        .values()
                        .stream()
                        .filter(axs -> {
                            if (!axs.isHidden()) {
                                return true;
                            }

                            return player.hasPermission(Almura.ID + ".exchange.admin");
                        })
                        .collect(Collectors.toSet())
                )
            );
        } else {
            this.scheduler
                .createTaskBuilder()
                .async()
                .execute(() -> {
                    try (final DSLContext context = this.databaseManager.createContext(true)) {
                        final int result = ExchangeQueries
                            .createInsertExchange(player.getUniqueId(), id, name, permission, isHidden)
                            .build(context)
                            .keepStatement(false)
                            .execute();

                        if (result == 0) {
                            this.scheduler
                                .createTaskBuilder()
                                .execute(() -> this.notificationManager.sendPopupNotification(player, Text.of("Exchange Manager"), Text.of(
                                    "Thread execution to add Exchange to database failed!"), 5))
                                .submit(this.container);
                        } else {
                            this.loadExchanges();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(this.container);
        }
    }

    public void handleExchangeModify(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".exchange.modify")) {
            // TODO Dockter, handle this
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null) {
            // TODO Dockter, we're in a desync...either send them a notification that modify failed as it doesn't exist or remove this TODO

            this.network.sendTo(player, new ClientboundExchangeRegistryPacket(
                    this.exchanges
                        .values()
                        .stream()
                        .filter(a -> {
                            if (!a.isHidden()) {
                                return true;
                            }

                            return player.hasPermission(Almura.ID + ".axs.admin");
                        })
                        .collect(Collectors.toSet())
                )
            );
        } else {
            this.scheduler.createTaskBuilder()
                .async()
                .execute(() -> {
                    try (final DSLContext context = this.databaseManager.createContext(true)) {
                        final int result = ExchangeQueries
                            .createUpdateExchange(id, name, permission, isHidden)
                            .build(context)
                            .keepStatement(false)
                            .execute();

                        if (result == 0) {
                            this.scheduler
                                .createTaskBuilder()
                                .execute(() -> this.notificationManager.sendPopupNotification(player, Text.of("Exchange Manager"), Text.of(
                                    "Thread execution to modify Exchange in database failed!"), 5))
                                .submit(this.container);
                        } else {
                            this.loadExchanges();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(this.container);
        }
    }

    public void handleExchangeDelete(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!this.getExchange(id).isPresent()) {
            // TODO Dockter, we're in a desync...either send them a notification that deletion failed as it doesn't exist or remove this TODO
            this.network.sendTo(player, new ClientboundExchangeRegistryPacket(
                    this.exchanges
                        .values()
                        .stream()
                        .filter(axs -> {
                            if (!axs.isHidden()) {
                                return true;
                            }

                            return player.hasPermission(Almura.ID + ".exchange.admin");
                        })
                        .collect(Collectors.toSet())
                )
            );
        } else {
            this.scheduler
                .createTaskBuilder()
                .async()
                .execute(() -> {
                    try (final DSLContext context = this.databaseManager.createContext(true)) {
                        final int result = ExchangeQueries
                            .createDeleteExchange(id)
                            .build(context)
                            .keepStatement(false)
                            .execute();

                        if (result == 0) {
                            this.scheduler
                                .createTaskBuilder()
                                .execute(() -> this.notificationManager.sendPopupNotification(player, Text.of("Exchange Manager"), Text.of(
                                    "Thread execution to delete Exchange from database failed!"), 5))
                                .submit(this.container);
                        } else {
                            this.loadExchanges();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .submit(this.container);
        }
    }

    /**
     * ListItem
     */

    public void loadListItems(final Exchange axs) {

        this.logger.info("Querying items for Exchange [{}], please wait...");

        final List<ListItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchItemsFor(axs.getId())
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> result.forEach(record -> {
                final ResourceLocation location = SerializationUtil.fromString(record.getValue(AxsItem.AXS_ITEM.ITEM_TYPE));

                if (location == null) {
                    // TODO This is a malformed resource location
                } else {

                    final Item item = ForgeRegistries.ITEMS.getValue(location);

                    if (item == null) {
                        // TODO They've given us an item that isn't loaded (likely a mod that vanished)
                    } else {

                        final Integer recNo = record.getValue(AxsItem.AXS_ITEM.REC_NO);
                        final Timestamp created = record.getValue(AxsItem.AXS_ITEM.CREATED);
                        final UUID seller = SerializationUtil.uniqueIdFromBytes(record.getValue(AxsItem.AXS_ITEM.SELLER));
                        final Integer quantity = record.getValue(AxsItem.AXS_ITEM.QUANTITY);
                        final Integer metadata = record.getValue(AxsItem.AXS_ITEM.METADATA);
                        final BigDecimal price = record.getValue(AxsItem.AXS_ITEM.PRICE);
                        final Integer index = record.getValue(AxsItem.AXS_ITEM.INDEX);

                        items.add(new BasicListItem(recNo, created.toInstant(), seller, item, quantity, metadata, price, index));
                    }
                }
            }));

            items.sort(Comparator.comparingInt(ListItem::getIndex));

            axs.putListItems(items.isEmpty() ? null : items.stream().collect(Collectors.groupingBy(ListItem::getSeller)));

            this.logger.info("Loaded {} item(s) for Exchange [{}].", axs.getId(), items.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleModifyListItems(final Player player, final String id, final List<InventoryAction> actions) {
        checkNotNull(id);
        checkNotNull(actions);
        checkState(!actions.isEmpty());

        // TODO Buckle up, this will be a hell of a ride
    }

    /**
     * ForSaleItem
     */

    public void loadForSaleItems(final Exchange axs) {
        this.logger.info("Querying for sale items for Exchange [{}], please wait...");

        final List<ForSaleItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchListItemsFor(axs.getId())
                .build(context)
                .keepStatement(false)
                .fetchMany();

            // We're async here, we have to get a copy of the map else iteration will be synchronized for the entirety of the below..
            final Map<UUID, List<ListItem>> listItems = new HashMap<>(axs.getListItems());

            results.forEach(result -> result.forEach(record -> {
                final Integer itemRecNo = record.getValue(AxsListItem.AXS_LIST_ITEM.AXS_ITEM);
                ListItem found = null;

                for (final List<ListItem> itemList : listItems.values()) {

                    found = itemList.stream().filter(item -> item.getRecord() == itemRecNo).findAny().orElse(null);
                    if (found != null) {
                        break;
                    }
                }

                if (found == null) {
                    // TODO If we're here, this is a dead listing as the listing doesn't exist in the exchange
                } else {
                    final Timestamp created = record.getValue(AxsListItem.AXS_LIST_ITEM.CREATED);
                    final Integer quantity = record.getValue(AxsListItem.AXS_LIST_ITEM.QUANTITY);

                    items.add(new BasicForSaleItem((BasicListItem) found, created.toInstant(), quantity));
                }
            }));

            this.scheduler
                .createTaskBuilder()
                .execute(() -> {
                    axs.clearForSaleItems();

                    axs.putForSaleItems(items.isEmpty() ? null : items.stream().collect(Collectors.groupingBy(forSaleItem -> forSaleItem
                        .getListItem().getSeller())));
                })
                .submit(this.container);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
