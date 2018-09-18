/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.store.basic.BasicStore;
import com.almuradev.almura.feature.store.basic.listing.BasicBuyingItem;
import com.almuradev.almura.feature.store.basic.listing.BasicSellingItem;
import com.almuradev.almura.feature.store.database.StoreQueries;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.listing.StoreItem;
import com.almuradev.almura.feature.store.network.ClientboundStoreItemsResponsePacket;
import com.almuradev.almura.feature.store.network.ClientboundStoreGuiResponsePacket;
import com.almuradev.almura.feature.store.network.ClientboundStoresRegistryPacket;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseQueue;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.store.tables.StoreBuyingItem;
import com.almuradev.generated.store.tables.StoreBuyingItemData;
import com.almuradev.generated.store.tables.StoreSellingItem;
import com.almuradev.generated.store.tables.StoreSellingItemData;
import com.google.inject.Inject;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
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
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
public final class ServerStoreManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;
    private final ServerNotificationManager notificationManager;

    private final Map<String, Store> stores = new HashMap<>();
    private final List<UUID> playerSpecificInitiatorIds = new ArrayList<>();

    @Inject
    public ServerStoreManager(final PluginContainer container, final Scheduler scheduler, final Logger logger, @ChannelId(NetworkConfig.CHANNEL)
        final ChannelBinding.IndexedMessageChannel network, final DatabaseManager databaseManager, final ServiceManager serviceManager, final
        ServerNotificationManager notificationManager) {

        this.container = container;
        this.scheduler = scheduler;
        this.logger = logger;
        this.network = network;
        this.databaseManager = databaseManager;
        this.serviceManager = serviceManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener
    public void onServerStarting(final GameStartingServerEvent event) {
        this.logger.info("Querying database for stores, please wait...");

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(this::loadStores)
            .submit(this.container);
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        this.syncStoreRegistryTo(player);
    }

    public Optional<Store> getStore(final String id) {
        checkNotNull(id);

        return Optional.ofNullable(this.stores.get(id));
    }

    /**
     * Store
     */

    private void loadStores() {

        final Map<String, Store> stores = new HashMap<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = StoreQueries
                .createFetchAllStores()
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> {
                for (Record record : result) {
                    final String id = record.getValue(com.almuradev.generated.store.tables.Store.STORE.ID);
                    final Timestamp created = record.getValue(com.almuradev.generated.store.tables.Store.STORE.CREATED);
                    final UUID creator = SerializationUtil.uniqueIdFromBytes(record.getValue(com.almuradev.generated.store.tables.Store.STORE.CREATOR));
                    final String name = record.getValue(com.almuradev.generated.store.tables.Store.STORE.NAME);
                    final String permission = record.getValue(com.almuradev.generated.store.tables.Store.STORE.PERMISSION);
                    final boolean isHidden = record.getValue(com.almuradev.generated.store.tables.Store.STORE.IS_HIDDEN);

                    this.logger.info("Loaded store '{}' ({})", name, id);

                    stores.put(id, new BasicStore(id, created.toInstant(), creator, name, permission, isHidden));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.scheduler
            .createTaskBuilder()
            .execute(() -> {
                this.stores.clear();

                this.stores.putAll(stores);

                if (this.stores.isEmpty()) {
                    // TODO TEST CODE, remove before going live
                    final BasicStore store = new BasicStore("almura.store.test", Instant.now(), FeatureConstants.UNKNOWN_OWNER, "Test "
                        + "Store", "almura.store.test", false);
                    this.stores.put("almura.store.test", store);

                    // Yes, I am purposely running this sync
                    try (final DSLContext context1 = this.databaseManager.createContext(true)) {
                        StoreQueries
                            .createInsertStore(store.getCreated(), store.getCreator(), store.getId(), store.getName(),
                                store.getPermission(), store.isHidden())
                            .build(context1)
                            .keepStatement(false)
                            .execute();

                        this.logger.info("Loaded store '{}' ({})", store.getName(), store.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                this.logger.info("Loaded [{}] store(s).", this.stores.size());

                this.stores.values().forEach(IngameFeature::syncCreatorNameToUniqueId);

                Sponge.getServer().getOnlinePlayers().forEach(this::syncStoreRegistryTo);
            })
            .submit(this.container);
    }

    private void syncStoreRegistryTo(final Player player) {
        this.network.sendTo(player, new ClientboundStoresRegistryPacket(
                this.stores
                    .values()
                    .stream()
                    .filter(store -> {
                        if (!store.isHidden()) {
                            return true;
                        }

                        return player.hasPermission(Almura.ID + ".store.admin");
                    })
                    .collect(Collectors.toSet())
            )
        );
    }

    public void openStoreManage(final Player player) {
        checkNotNull(player);

        this.network.sendTo(player, new ClientboundStoreGuiResponsePacket(StoreGuiType.MANAGE));
    }

    void openStoreSpecific(final Player player, final Store store) {
        checkNotNull(player);
        checkNotNull(store);

        this.network.sendTo(player, new ClientboundStoreGuiResponsePacket(StoreGuiType.SPECIFIC, store.getId()));

        if (!store.isLoaded()) {
            this.playerSpecificInitiatorIds.add(player.getUniqueId());

            this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, store.getId(), () -> {
                this.loadSellingItems(store);

                this.loadBuyingItems(store);

                this.scheduler
                    .createTaskBuilder()
                    .execute(() -> {
                        store.setLoaded(true);

                        final Iterator<UUID> iter = this.playerSpecificInitiatorIds.iterator();
                        while (iter.hasNext()) {
                            final UUID uniqueId = iter.next();
                            iter.remove();

                            final Player p = Sponge.getServer().getPlayer(uniqueId).orElse(null);
                            if (p != null && p.isOnline() && !p.isRemoved()) {
                                final List<SellingItem> sellingItems = store.getSellingItems();
                                this.network.sendTo(p, new ClientboundStoreItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));

                                final List<BuyingItem> buyingItems = store.getBuyingItems();
                                this.network.sendTo(player, new ClientboundStoreItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, buyingItems));
                            }
                        }
                    })
                    .submit(this.container);
            });
        } else {
            final List<SellingItem> sellingItems = store.getSellingItems();
            this.network.sendTo(player, new ClientboundStoreItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));

            final List<BuyingItem> buyingItems = store.getBuyingItems();
            this.network.sendTo(player, new ClientboundStoreItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, buyingItems));
        }
    }

    public void handleStoreAdd(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".store.add")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
                + "to add stores!"), 5);
            return;
        }

        if (this.getStore(id).isPresent()) {
            this.logger.error("Player '{}' attempted to add store '{}' but it already exists. Syncing store registry...", player.getName(),
                id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final UUID creator = player.getUniqueId();

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Instant created = Instant.now();

                    final int result = StoreQueries
                        .createInsertStore(created, creator, id, name, permission, isHidden)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        this.logger.error("Player '{}' submitted a new store '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final BasicStore basicStore = new BasicStore(id, created, creator, name, permission, isHidden);
                            basicStore.syncCreatorNameToUniqueId();

                            this.stores.put(id, basicStore);

                            Sponge.getServer().getOnlinePlayers().forEach(this::syncStoreRegistryTo);
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleStoreModify(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".store.modify")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
                + "to modify stores!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to modify store '{}' but the server has no knowledge of it. Syncing store registry...",
                player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final int result = StoreQueries
                        .createUpdateStore(id, name, permission, isHidden)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        this.logger.error("Player '{}' submitted a modified store '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.loadStores();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleStoreDelete(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".store.delete")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
                + "to delete stores!"), 5);
            return;
        }

        if (!this.getStore(id).isPresent()) {
            this.logger.error("Player '{}' attempted to delete store '{}' but the server has no knowledge of it. Syncing store registry...",
                player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final int result = StoreQueries
                        .createDeleteStore(id)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        this.logger.error("Player '{}' submitted a deleted store '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.loadStores();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * Selling Items
     */
    private void loadSellingItems(final Store store) {
        checkNotNull(store);

        this.logger.info("Querying selling items for store '{}' ({}), please wait...", store.getName(), store.getId());

        final List<SellingItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = StoreQueries
                .createFetchSellingItemsAndDataFor(store.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> items.addAll(this.parseSellingItemsFrom(result)));

            store.clearSellingItems();

            items.sort(Comparator.comparingInt(SellingItem::getIndex));

            store.putSellingItems(items.isEmpty() ? null : items);

            this.logger.info("Loaded [{}] selling item(s) for store '{}' ({}).", items.size(), store.getName(), store.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<SellingItem> parseSellingItemsFrom(final Result<Record> result) {
        final List<SellingItem> items = new ArrayList<>();

        for (final Record record : result) {
            final Integer recNo = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.REC_NO);

            final String domain = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.DOMAIN);
            final String path = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.PATH);

            final ResourceLocation location = new ResourceLocation(domain, path);

            final Item item = ForgeRegistries.ITEMS.getValue(location);

            if (item == null) {
                this.logger.error("Unknown item for domain '{}' and path '{}' found at record number '{}'. Skipping... (Did you remove a mod?)",
                    domain, path, recNo);
                continue;
            }

            final Timestamp created = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.CREATED);
            final Integer quantity = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.QUANTITY);
            final Integer metadata = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.METADATA);
            final BigDecimal price = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.PRICE);
            final Integer index = record.getValue(StoreSellingItem.STORE_SELLING_ITEM.INDEX);
            final byte[] compoundData = record.getValue(StoreSellingItemData.STORE_SELLING_ITEM_DATA.DATA);

            NBTTagCompound compound = null;
            if (compoundData != null) {
                try {
                    compound = SerializationUtil.compoundFromBytes(compoundData);
                } catch (IOException e) {
                    this.logger.error("Malformed item data found at record number '{}'. Skipping...", recNo);
                    continue;
                }
            }

            final BasicSellingItem basicSellingItem = new BasicSellingItem(recNo, created.toInstant(), item, quantity, metadata, price, index,
                compound);

            items.add(basicSellingItem);
        }

        return items;
    }

    /**
     * BuyingItems
     */

    private void loadBuyingItems(final Store store) {
        checkNotNull(store);

        this.logger.info("Querying buying items for store '{}' ({}), please wait...", store.getName(), store.getId());

        final List<BuyingItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = StoreQueries
                .createFetchBuyingItemsAndDataFor(store.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> items.addAll(this.parseBuyingItemsFor(result)));

            store.clearBuyingItems();

            items.sort(Comparator.comparingInt(BuyingItem::getIndex));

            store.putBuyingItems(items.isEmpty() ? null : items);

            this.logger.info("Loaded [{}] buying item(s) for store '{}' ({}).", items.size(), store.getName(), store.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<BuyingItem> parseBuyingItemsFor(final Result<Record> result) {
        final List<BuyingItem> items = new ArrayList<>();

        for (final Record record : result) {
            final Integer recNo = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.REC_NO);

            final String domain = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.DOMAIN);
            final String path = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.PATH);

            final ResourceLocation location = new ResourceLocation(domain, path);

            final Item item = ForgeRegistries.ITEMS.getValue(location);

            if (item == null) {
                this.logger.error("Unknown item for domain '{}' and path '{}' found at record number '{}'. Skipping... (Did you remove a mod?)",
                    domain, path, recNo);
                continue;
            }

            final Timestamp created = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.CREATED);
            final Integer quantity = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.QUANTITY);
            final Integer metadata = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.METADATA);
            final BigDecimal price = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.PRICE);
            final Integer index = record.getValue(StoreBuyingItem.STORE_BUYING_ITEM.INDEX);
            final byte[] compoundData = record.getValue(StoreBuyingItemData.STORE_BUYING_ITEM_DATA.DATA);

            NBTTagCompound compound = null;
            if (compoundData != null) {
                try {
                    compound = SerializationUtil.compoundFromBytes(compoundData);
                } catch (IOException e) {
                    this.logger.error("Malformed item data found at record number '{}'. Skipping...", recNo);
                    continue;
                }
            }

            final BasicBuyingItem basicBuyingItem = new BasicBuyingItem(recNo, created.toInstant(), item, quantity, metadata, price, index,
                compound);

            items.add(basicBuyingItem);
        }

        return items;
    }
}
