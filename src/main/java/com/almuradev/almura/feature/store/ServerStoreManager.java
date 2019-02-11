/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.store;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.feature.store.basic.BasicStore;
import com.almuradev.almura.feature.store.basic.listing.BasicBuyingItem;
import com.almuradev.almura.feature.store.basic.listing.BasicSellingItem;
import com.almuradev.almura.feature.store.database.StoreQueries;
import com.almuradev.almura.feature.store.listing.BuyingItem;
import com.almuradev.almura.feature.store.listing.SellingItem;
import com.almuradev.almura.feature.store.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.store.network.ServerboundListItemsRequestPacket;
import com.almuradev.almura.feature.store.network.ClientboundStoreGuiResponsePacket;
import com.almuradev.almura.feature.store.network.ClientboundStoresRegistryPacket;
import com.almuradev.almura.feature.store.network.ServerboundModifyItemsPacket;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseQueue;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.store.tables.StoreBuyingItem;
import com.almuradev.generated.store.tables.StoreBuyingItemData;
import com.almuradev.generated.store.tables.StoreSellingItem;
import com.almuradev.generated.store.tables.StoreSellingItemData;
import com.almuradev.generated.store.tables.records.StoreBuyingItemDataRecord;
import com.almuradev.generated.store.tables.records.StoreBuyingItemRecord;
import com.almuradev.generated.store.tables.records.StoreSellingItemDataRecord;
import com.almuradev.generated.store.tables.records.StoreSellingItemRecord;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Results;
import org.slf4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
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
        } catch (final SQLException e) {
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
                    } catch (final SQLException e) {
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

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to manage stores!"), 5);
            return;
        }

        this.network.sendTo(player, new ClientboundStoreGuiResponsePacket(StoreGuiType.MANAGE));
    }

    void openStoreSpecific(final Player player, final Store store) {
        checkNotNull(player);
        checkNotNull(store);

        if (!player.hasPermission(store.getPermission())) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to open this store!"), 5);
            return;
        }

        this.network.sendTo(player, new ClientboundStoreGuiResponsePacket(StoreGuiType.SPECIFIC, store.getId(),
          player.hasPermission(Almura.ID + ".store.admin")));

        if (!store.isLoaded()) {
            this.playerSpecificInitiatorIds.add(player.getUniqueId());

            this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, store.getId(), () -> {
                this.loadSellingItems(store);

                this.loadBuyingItems(store);

                this.scheduler
                    .createTaskBuilder()
                    .execute(() -> {
                        store.setLoaded(true);

                        final Iterator<UUID> iterator = this.playerSpecificInitiatorIds.iterator();
                        while (iterator.hasNext()) {
                            final UUID uniqueId = iterator.next();
                            iterator.remove();

                            final Player p = Sponge.getServer().getPlayer(uniqueId).orElse(null);
                            if (p != null && p.isOnline() && !p.isRemoved()) {
                                this.network.sendTo(p, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING,
                                    store.getSellingItems()));

                                this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING,
                                    store.getBuyingItems()));
                            }
                        }
                    })
                    .submit(this.container);
            });
        } else {
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING,
                store.getSellingItems()));

            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, store.getBuyingItems()));
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
                } catch (final SQLException e) {
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
                } catch (final SQLException e) {
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
                } catch (final SQLException e) {
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

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleListSellingItems(final Player player, final String id,
        final List<ServerboundListItemsRequestPacket.ListCandidate> candidates) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to list items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to list selling items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {

                    final Map<StoreSellingItemRecord, VanillaStack> inserted = new HashMap<>();

                    for (final ServerboundListItemsRequestPacket.ListCandidate candidate : candidates) {

                        final VanillaStack stack = candidate.stack;
                        final int index = candidate.index;
                        final BigDecimal price = candidate.price;

                        final StoreSellingItemRecord itemRecord = StoreQueries
                            .createInsertSellingItem(store.getId(), Instant.now(), stack.getItem(), stack.getQuantity(), stack.getMetadata(), index,
                                price)
                            .build(context)
                            .fetchOne();

                        if (itemRecord == null) {
                            this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                                Text.of("Critical error encountered, check the server console for more details!"));
                            this.logger.error("Player '{}' submitted a new selling item for store '{}' to the database but it failed. "
                                + "Discarding changes and printing stack...", player.getName(), id);
                            this.printStacksToConsole(Lists.newArrayList(stack));
                            continue;
                        }

                        final NBTTagCompound compound = stack.getCompound();
                        if (compound != null) {

                            StoreSellingItemDataRecord dataRecord = null;

                            try {
                                dataRecord = StoreQueries
                                    .createInsertSellingItemData(itemRecord.getRecNo(), compound)
                                    .build(context)
                                    .fetchOne();
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }

                            if (dataRecord == null) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                                    Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' submitted data for selling item record '{}' for store '{}' but it failed. "
                                    + "Discarding changes...", player.getName(), itemRecord.getRecNo(), id);

                                StoreQueries
                                    .createDeleteSellingItem(itemRecord.getRecNo())
                                    .build(context)
                                    .execute();
                            }
                        }

                        inserted.put(itemRecord, stack);
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {

                            for (final Map.Entry<StoreSellingItemRecord, VanillaStack> entry : inserted.entrySet()) {
                                final StoreSellingItemRecord record = entry.getKey();
                                final VanillaStack stack = entry.getValue();

                                final BasicSellingItem item = new BasicSellingItem(record.getRecNo(), record.getCreated().toInstant(),
                                    stack.getItem(), stack.getQuantity(), stack.getMetadata(), record.getPrice(), record.getIndex(),
                                    stack.getCompound());

                                store.getSellingItems().add(item);
                            }

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING,
                                store.getSellingItems()));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleModifySellingItems(final Player player, final String id, final List<ServerboundModifyItemsPacket.ModifyCandidate> candidates) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(candidates);

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to modify listed items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to modify selling items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final List<SellingItem> sellingItems = store.getSellingItems();
        if (sellingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to modify selling items for store '{}' but the server knows of no "
                + " selling items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));
            return;
        }

        final List<ServerboundModifyItemsPacket.ModifyCandidate> copyCandidates = Lists.newArrayList(candidates);
        final Iterator<ServerboundModifyItemsPacket.ModifyCandidate> iter = copyCandidates.iterator();

        while (iter.hasNext()) {
            final ServerboundModifyItemsPacket.ModifyCandidate candidate = iter.next();

            final SellingItem found = sellingItems
                .stream()
                .filter(v -> v.getRecord() == candidate.recNo)
                .findAny()
                .orElse(null);

            if (found == null) {
                this.logger.error("Player '{}' attempted to modify selling item '{}' for store '{}' but the server knows of no knowledge of it. "
                    + "This could be a de-sync or an exploit. Discarding...", player.getName(), candidate.recNo, store.getId());
                iter.remove();
            }
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final List<Query> toUpdate = new ArrayList<>();

                    for (final ServerboundModifyItemsPacket.ModifyCandidate candidate : copyCandidates) {
                        toUpdate.add(StoreQueries
                            .createUpdateSellingItem(candidate.recNo, candidate.quantity, candidate.index, candidate.price)
                            .build(context));
                    }

                    context.batch(toUpdate).execute();

                    final Results results = StoreQueries
                        .createFetchSellingItemsAndDataFor(store.getId(), false)
                        .build(context)
                        .fetchMany();

                    final List<SellingItem> finalSellingItems = new ArrayList<>();
                    results.forEach(result -> finalSellingItems.addAll(this.parseSellingItemsFrom(result)));

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            store.putSellingItems(finalSellingItems);

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING,
                                finalSellingItems));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleDelistSellingItems(final Player player, final String id, final List<Integer> recNos) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(recNos);
        checkState(!recNos.isEmpty());

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to unlist items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to de-list selling items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final List<SellingItem> sellingItems = store.getSellingItems();
        if (sellingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to de-list selling items for store '{}' but the server knows of no "
                + " buying items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));
            return;
        }

        final List<Integer> copyRecNos = Lists.newArrayList(recNos);
        final Iterator<Integer> iter = copyRecNos.iterator();

        while (iter.hasNext()) {
            final Integer recNo = iter.next();

            final SellingItem found = sellingItems
                .stream()
                .filter(v -> v.getRecord() == recNo)
                .findAny()
                .orElse(null);

            if (found == null) {
                this.logger.error("Player '{}' attempted to de-list selling item '{}' for store '{}' but the server knows of no knowledge of it. "
                    + "This could be a de-sync or an exploit. Discarding...", player.getName(), recNo, store.getId());
                iter.remove();
            }
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final List<Query> toUpdate = new ArrayList<>();

                    for (final Integer recNo : recNos) {
                        toUpdate.add(StoreQueries
                            .createUpdateSellingItemIsHidden(recNo, true)
                            .build(context));
                    }

                    context.batch(toUpdate).execute();

                    final Results results = StoreQueries
                        .createFetchSellingItemsAndDataFor(store.getId(), false)
                        .build(context)
                        .fetchMany();

                    final List<SellingItem> finalSellingItems = new ArrayList<>();
                    results.forEach(result -> finalSellingItems.addAll(this.parseSellingItemsFrom(result)));

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            store.putSellingItems(finalSellingItems);

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING,
                                finalSellingItems));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
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
                this.logger.error("Unknown selling item for domain '{}' and path '{}' found at record number '{}'. Skipping... (Did you remove a "
                        + "mod?)",
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
                } catch (final IOException e) {
                    this.logger.error("Malformed selling item data found at record number '{}'. Skipping...", recNo);
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
     * Buying Items
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

        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleListBuyingItems(final Player player, final String id, final List<ServerboundListItemsRequestPacket.ListCandidate>
        candidates) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to list items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to list buying items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {

                    final Map<StoreBuyingItemRecord, VanillaStack> inserted = new HashMap<>();

                    for (final ServerboundListItemsRequestPacket.ListCandidate candidate : candidates) {

                        final VanillaStack stack = candidate.stack;
                        final int index = candidate.index;
                        final BigDecimal price = candidate.price;

                        final StoreBuyingItemRecord itemRecord = StoreQueries
                            .createInsertBuyingItem(store.getId(), Instant.now(), stack.getItem(), stack.getQuantity(), stack.getMetadata(), index,
                                price)
                            .build(context)
                            .fetchOne();

                        if (itemRecord == null) {
                            this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                                Text.of("Critical error encountered, check the server console for more details!"));
                            this.logger.error("Player '{}' submitted a new buying item for store '{}' to the database but it failed. "
                                + "Discarding changes and printing stack...", player.getName(), id);
                            this.printStacksToConsole(Lists.newArrayList(stack));
                            continue;
                        }

                        final NBTTagCompound compound = stack.getCompound();
                        if (compound != null) {

                            StoreBuyingItemDataRecord dataRecord = null;

                            try {
                                dataRecord = StoreQueries
                                    .createInsertBuyingItemData(itemRecord.getRecNo(), compound)
                                    .build(context)
                                    .fetchOne();
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }

                            if (dataRecord == null) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                                    Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' submitted data for buying item record '{}' for store '{}' but it failed. "
                                    + "Discarding changes...", player.getName(), itemRecord.getRecNo(), id);

                                StoreQueries
                                    .createDeleteBuyingItem(itemRecord.getRecNo())
                                    .build(context)
                                    .execute();
                            }
                        }

                        inserted.put(itemRecord, stack);
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {

                            for (final Map.Entry<StoreBuyingItemRecord, VanillaStack> entry : inserted.entrySet()) {
                                final StoreBuyingItemRecord record = entry.getKey();
                                final VanillaStack stack = entry.getValue();

                                final BasicBuyingItem item = new BasicBuyingItem(record.getRecNo(), record.getCreated().toInstant(),
                                    stack.getItem(), stack.getQuantity(), stack.getMetadata(), record.getPrice(), record.getIndex(),
                                    stack.getCompound());

                                store.getBuyingItems().add(item);
                            }

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING,
                                store.getBuyingItems()));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleModifyBuyingItems(final Player player, final String id, final List<ServerboundModifyItemsPacket.ModifyCandidate> candidates) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(candidates);

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to modify listed items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to modify buying items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final List<BuyingItem> buyingItems = store.getBuyingItems();
        if (buyingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to modify buying items for store '{}' but the server knows of no "
                + " buying items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, buyingItems));
            return;
        }

        final List<ServerboundModifyItemsPacket.ModifyCandidate> copyCandidates = Lists.newArrayList(candidates);
        final Iterator<ServerboundModifyItemsPacket.ModifyCandidate> iter = copyCandidates.iterator();

        while (iter.hasNext()) {
            final ServerboundModifyItemsPacket.ModifyCandidate candidate = iter.next();

            final BuyingItem found = buyingItems
                .stream()
                .filter(v -> v.getRecord() == candidate.recNo)
                .findAny()
                .orElse(null);

            if (found == null) {
                this.logger.error("Player '{}' attempted to modify buying item '{}' for store '{}' but the server knows of no knowledge of it. "
                    + "This could be a de-sync or an exploit. Discarding...", player.getName(), candidate.recNo, store.getId());
                iter.remove();
            }
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final List<Query> toUpdate = new ArrayList<>();

                    for (final ServerboundModifyItemsPacket.ModifyCandidate candidate : copyCandidates) {
                        toUpdate.add(StoreQueries
                            .createUpdateBuyingItem(candidate.recNo, candidate.quantity, candidate.index, candidate.price)
                            .build(context));
                    }

                    context.batch(toUpdate).execute();

                    final Results results = StoreQueries
                        .createFetchBuyingItemsAndDataFor(store.getId(), false)
                        .build(context)
                        .fetchMany();

                    final List<BuyingItem> finalBuyingItems = new ArrayList<>();
                    results.forEach(result -> finalBuyingItems.addAll(this.parseBuyingItemsFor(result)));

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            store.putBuyingItems(finalBuyingItems);

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING,
                                finalBuyingItems));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
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
                this.logger.error("Unknown buying item for domain '{}' and path '{}' found at record number '{}'. Skipping... (Did you remove a "
                        + "mod?)",
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
                } catch (final IOException e) {
                    this.logger.error("Malformed buying item data found at record number '{}'. Skipping...", recNo);
                    continue;
                }
            }

            final BasicBuyingItem basicBuyingItem = new BasicBuyingItem(recNo, created.toInstant(), item, quantity, metadata, price, index,
                compound);

            items.add(basicBuyingItem);
        }

        return items;
    }

    public void handleDelistBuyingItems(final Player player, final String id, final List<Integer> recNos) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(recNos);
        checkState(!recNos.isEmpty());

        if (!player.hasPermission(Almura.ID + ".store.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to unlist items!"), 5);
            return;
        }

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to de-list buying items for store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final List<BuyingItem> buyingItems = store.getBuyingItems();
        if (buyingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to de-list buying items for store '{}' but the server knows of no "
                + " buying items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, buyingItems));
            return;
        }

        final List<Integer> copyRecNos = Lists.newArrayList(recNos);
        final Iterator<Integer> iter = copyRecNos.iterator();

        while (iter.hasNext()) {
            final Integer recNo = iter.next();

            final BuyingItem found = buyingItems
                .stream()
                .filter(v -> v.getRecord() == recNo)
                .findAny()
                .orElse(null);

            if (found == null) {
                this.logger.error("Player '{}' attempted to de-list buying item '{}' for store '{}' but the server knows of no knowledge of it. "
                    + "This could be a de-sync or an exploit. Discarding...", player.getName(), recNo, store.getId());
                iter.remove();
            }
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final List<Query> toUpdate = new ArrayList<>();

                    for (final Integer recNo : recNos) {
                        toUpdate.add(StoreQueries
                            .createUpdateBuyingItemIsHidden(recNo, true)
                            .build(context));
                    }

                    context.batch(toUpdate).execute();

                    final Results results = StoreQueries
                        .createFetchBuyingItemsAndDataFor(store.getId(), false)
                        .build(context)
                        .fetchMany();

                    final List<BuyingItem> finalBuyingItems = new ArrayList<>();
                    results.forEach(result -> finalBuyingItems.addAll(this.parseBuyingItemsFor(result)));

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            store.putBuyingItems(finalBuyingItems);

                            this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING,
                                finalBuyingItems));
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * Transaction (Selling/Buying)
     */

    public void handleBuyingItemTransaction(final Player player, final String id, final int recNo, final int quantity) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(recNo >= 0);
        checkState(quantity >= 0);

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to buy an item from store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final EconomyService economyService = this.serviceManager.provide(EconomyService.class).orElse(null);
        if (economyService == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to buy an item from store '{}' but the economy service no longer exists. This is a "
                + "critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to buy an item from store '{}' but the economy service returned no account for them. "
                + "This is a critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        final List<BuyingItem> buyingItems = store.getBuyingItems();
        if (buyingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to buy an item from store '{}' but the server knows of no "
                + " buying items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, buyingItems));
            return;
        }

        final BuyingItem found = buyingItems
            .stream()
            .filter(v -> v.getRecord() == recNo)
            .findAny()
            .orElse(null);

        if (found == null) {
            this.logger.error("Player '{}' attempted to buy item '{}' from store '{}' but the server knows of no knowledge of it. "
                + "This could be a de-sync or an exploit. Discarding...", player.getName(), recNo, store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING, buyingItems));
            return;
        }

        final boolean infinite = found.getQuantity() == FeatureConstants.UNLIMITED;

        if (!infinite && found.getQuantity() < quantity) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                Text.of("There is not enough quantity left to fulfill your order!"));
            return;
        }

        final BigDecimal balance = account.getBalance(economyService.getDefaultCurrency());
        final BigDecimal price = found.getPrice();
        final double total = price.doubleValue() * quantity;

        if (total > balance.doubleValue()) {
            final String formattedTotal = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total);
            final String formattedBalance = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(balance.doubleValue());
            final String formattedDifference = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total - balance.doubleValue());
            this.notificationManager.sendWindowMessage(player, Text.of("Store"),
                Text.of("You attempted to purchase items totaling to ", TextColors.RED, formattedTotal, TextColors.RESET, " while you only have ",
                    TextColors.GREEN, formattedBalance, TextColors.RESET, ".", Text.NEW_LINE, Text.NEW_LINE, "You need ",
                    TextColors.LIGHT_PURPLE, formattedDifference, TextColors.RESET, " more!"));
            return;
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        final IItemHandler inventory = serverPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

        final BuyingItem copyStack = found.copy();
        copyStack.setQuantity(quantity);

        final ItemStack simulatedResultStack = ItemHandlerHelper.insertItemStacked(inventory, copyStack.asRealStack(), true);
        if (!simulatedResultStack.isEmpty()) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("You lack sufficient inventory space to "
                + "purchase these item(s)!"));
            return;
        }

        // Charge the buyer
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(store);

            account.withdraw(economyService.getDefaultCurrency(), BigDecimal.valueOf(total), frame.getCurrentCause());

            this.notificationManager.sendPopupNotification(player,
              Text.of(TextColors.GREEN, "Transaction Complete"),
              Text.of("Purchased ", TextColors.AQUA, quantity, TextColors.WHITE," x ", TextColors.YELLOW, found.asRealStack().getDisplayName(),
                TextColors.RESET, " for a total of ", TextColors.GOLD, "$", FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total)), 3);
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    int result;

                    if (!infinite) {
                        result = StoreQueries
                            .createUpdateBuyingItem(found.getRecord(), found.getQuantity() - quantity, found.getIndex(), found.getPrice())
                            .build(context)
                            .execute();

                        if (result == 0) {
                            // TODO It failed, message
                            return;
                        }
                    }

                    result = StoreQueries
                        .createInsertBuyingTransaction(Instant.now(), found.getRecord(), player.getUniqueId(), found.getPrice(), quantity)
                        .build(context)
                        .execute();

                    if (result == 0) {
                        // TODO It failed, message

                        StoreQueries
                            .createUpdateBuyingItem(found.getRecord(), found.getQuantity(), found.getIndex(), found.getPrice())
                            .build(context)
                            .execute();
                        return;
                    }

                    final List<BuyingItem> finalBuyingItems = new ArrayList<>();

                    if (!infinite) {
                        final Results results = StoreQueries
                            .createFetchBuyingItemsAndDataFor(store.getId(), false)
                            .build(context)
                            .fetchMany();
                        results.forEach(r -> finalBuyingItems.addAll(this.parseBuyingItemsFor(r)));
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            if (!infinite) {
                                store.putBuyingItems(finalBuyingItems);
                            }

                            final ItemStack resultStack = ItemHandlerHelper.insertItemStacked(inventory, copyStack.asRealStack(), false);
                            if (!resultStack.isEmpty()) {
                                // TODO Inventory changed awaiting DB and now we're full...could drop it on the ground? It is an off-case
                            }

                            if (!infinite) {
                                this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.BUYING,
                                    store.getBuyingItems()));
                            }
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleSellingItemTransaction(final Player player, final String id, final int recNo, final int quantity) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(recNo >= 0);
        checkState(quantity >= 0);

        final Store store = this.getStore(id).orElse(null);

        if (store == null) {
            this.logger.error("Player '{}' attempted to sell an item to store '{}' but the server has no knowledge of it. Syncing "
                + "store registry...", player.getName(), id);
            this.syncStoreRegistryTo(player);
            return;
        }

        final EconomyService economyService = this.serviceManager.provide(EconomyService.class).orElse(null);
        if (economyService == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to sell an to from store '{}' but the economy service no longer exists. This is a "
                + "critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to sell an item to store '{}' but the economy service returned no account for them. "
                + "This is a critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        final List<SellingItem> sellingItems = store.getSellingItems();
        if (sellingItems.isEmpty()) {
            this.logger.error("Player '{}' attempted to sell an item to store '{}' but the server knows of no "
                + "selling items for it. This could be a de-sync or an exploit.", player.getName(), store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));
            return;
        }

        final SellingItem found = sellingItems
            .stream()
            .filter(v -> v.getRecord() == recNo)
            .findAny()
            .orElse(null);

        if (found == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to sell item '{}' to store '{}' but the server knows of no knowledge of it. This could be a "
                + "de-sync or an exploit. Discarding...", player.getName(), recNo, store.getId());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, sellingItems));
            return;
        }

        final boolean infinite = found.getQuantity() == FeatureConstants.UNLIMITED;

        if (!infinite && found.getQuantity() < quantity) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("There is not enough quantity left to sell "
                + "your product!"));
            return;
        }

        final BigDecimal price = found.getPrice();
        final double total = price.doubleValue() * quantity;

        final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        final IItemHandler inventory = serverPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

        final SellingItem copyStack = found.copy();
        copyStack.setQuantity(quantity);

        int amountLeft = copyStack.getQuantity();

        for (int j = 0; j < inventory.getSlots(); j++) {
            final ItemStack slotStack = inventory.getStackInSlot(j);

            if (ItemHandlerHelper.canItemStacksStack(slotStack, copyStack.asRealStack())) {
                amountLeft -= inventory.extractItem(j, amountLeft, false).getCount();
            }

            if (amountLeft <= 0) {
                break;
            }
        }

        if (amountLeft != 0) {
            this.notificationManager.sendWindowMessage(player, Text.of("Store"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to sell item '{}' to store '{}' but they do not have enough inventory quantity. "
                + "This could be a de-sync or an exploit. Discarding...", player.getName(), recNo, store.getId());
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    int result;

                    if (!infinite) {
                        result = StoreQueries
                            .createUpdateSellingItem(found.getRecord(), found.getQuantity() - quantity, found.getIndex(), found.getPrice())
                            .build(context)
                            .execute();

                        if (result == 0) {
                            // TODO It failed, message
                            return;
                        }
                    }

                    result = StoreQueries
                        .createInsertSellingTransaction(Instant.now(), found.getRecord(), player.getUniqueId(), found.getPrice(), quantity)
                        .build(context)
                        .execute();

                    if (result == 0) {
                        // TODO It failed, message

                        StoreQueries
                            .createUpdateSellingItem(found.getRecord(), found.getQuantity(), found.getIndex(), found.getPrice())
                            .build(context)
                            .execute();
                        return;
                    }

                    final List<SellingItem> finalSellingItems = new ArrayList<>();

                    if (!infinite) {
                        final Results results = StoreQueries
                            .createFetchSellingItemsAndDataFor(store.getId(), false)
                            .build(context)
                            .fetchMany();
                        results.forEach(r -> finalSellingItems.addAll(this.parseSellingItemsFrom(r)));
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            if (!infinite) {
                                store.putSellingItems(finalSellingItems);
                            }

                            // Pay the seller
                            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                                frame.pushCause(store);

                                account.deposit(economyService.getDefaultCurrency(), BigDecimal.valueOf(total), frame.getCurrentCause());

                                this.notificationManager.sendPopupNotification(player,
                                  Text.of(TextColors.GREEN, "Transaction Complete"),
                                  Text.of("Sold ", TextColors.AQUA, quantity, TextColors.WHITE," x ", TextColors.YELLOW, found.asRealStack().getDisplayName(),
                                    TextColors.RESET, " for a total of ", TextColors.GOLD, "$", FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total)), 3);
                            }

                            if (!infinite) {
                                this.network.sendToAll(new ClientboundListItemsResponsePacket(store.getId(), StoreItemSegmentType.SELLING, store
                                    .getSellingItems()));
                            }
                        })
                        .submit(this.container);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    private void printStacksToConsole(final List<VanillaStack> stacks) {
        // TODO
    }
}
