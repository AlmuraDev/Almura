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
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleFilterRequestPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsSaleStatusPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseQueue;
import com.almuradev.almura.shared.feature.store.Store;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.axs.tables.Axs;
import com.almuradev.generated.axs.tables.AxsForSaleItem;
import com.almuradev.generated.axs.tables.AxsListItem;
import com.almuradev.generated.axs.tables.AxsListItemData;
import com.almuradev.generated.axs.tables.records.AxsForSaleItemRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemRecord;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jooq.DSLContext;
import org.jooq.Query;
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
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

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

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ServerExchangeManager extends Witness.Impl implements Witness.Lifecycle {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final DatabaseManager databaseManager;
    private final ServiceManager serviceManager;
    private final ServerNotificationManager notificationManager;

    private final Map<String, Exchange> exchanges = new HashMap<>();

    @Inject
    public ServerExchangeManager(final PluginContainer container, final Scheduler scheduler, final Logger logger, @ChannelId(NetworkConfig.CHANNEL)
        final ChannelBinding.IndexedMessageChannel network, final DatabaseManager databaseManager, final ServiceManager serviceManager,
        final ServerNotificationManager notificationManager) {
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
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(this::loadExchanges)
            .submit(this.container);
    }

    @Listener
    public void onClientConnectionEventJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        this.syncExchangeRegistryTo(player);
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

                    this.logger.info("Loaded exchange '{}' ({})", name, id);

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
                    // TODO I might automatically create the Global Exchange (almura.exchange.global) by default (if no others are found when loading from db).
                    final BasicExchange exchange = new BasicExchange("almura.exchange.global", Instant.now(), Store.UNKNOWN_OWNER, "Global "
                        + "Exchange", "almura.exchange.global", false);
                    this.exchanges.put("almura.exchange.global", exchange);

                    // Yes, I am purposely running this sync
                    try (final DSLContext context1 = this.databaseManager.createContext(true)) {
                        ExchangeQueries
                            .createInsertExchange(exchange.getCreated(), exchange.getCreator(), exchange.getId(), exchange.getName(),
                                exchange.getPermission(), exchange.isHidden())
                            .build(context1)
                            .keepStatement(false)
                            .execute();

                        this.logger.info("Loaded exchange '{}' ({})", exchange.getName(), exchange.getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                this.logger.info("Loaded [{}] exchange(s).", this.exchanges.size());

                this.exchanges.values().forEach(axs -> ((BasicExchange) axs).refreshCreatorName());

                Sponge.getServer().getOnlinePlayers().forEach(this::syncExchangeRegistryTo);
            })
            .submit(this.container);
    }

    public void handleExchangeManage(final Player player) {
        checkNotNull(player);

        if (!player.hasPermission(Almura.ID + ".exchange.manage")) {
            // TODO Notification
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.MANAGE));
    }

    private final List<UUID> playerSpecificInitiatorIds = new ArrayList<>();

    public void handleExchangeSpecific(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".exchange.open")) {
            // TODO Notification
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null || !player.hasPermission(axs.getPermission())) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.SPECIFIC, id));

        if (!axs.isLoaded()) {
            this.playerSpecificInitiatorIds.add(player.getUniqueId());

            this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, id, () -> {
                this.loadListItems(axs);

                this.loadForSaleItems(axs);

                this.scheduler
                    .createTaskBuilder()
                    .execute(() -> {
                        axs.setLoaded(true);

                        final Iterator<UUID> iter = this.playerSpecificInitiatorIds.iterator();
                        while (iter.hasNext()) {
                            final UUID uniqueId = iter.next();
                            iter.remove();

                            final Player p = Sponge.getServer().getPlayer(uniqueId).orElse(null);
                            if (p != null && p.isOnline() && !p.isRemoved()) {
                                final List<ListItem> listItems = axs.getListItemsFor(p.getUniqueId()).orElse(null);
                                this.network.sendTo(p, new ClientboundListItemsResponsePacket(axs.getId(), listItems));

                                if (listItems != null && !listItems.isEmpty()) {
                                    final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(p.getUniqueId()).orElse(null);
                                    if (forSaleItems != null && !forSaleItems.isEmpty()) {
                                        this.network.sendTo(p, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems));
                                    }
                                }

                                this.network.sendTo(p, new ClientboundForSaleFilterRequestPacket(axs.getId()));
                            }
                        }
                    })
                    .submit(this.container);
            });
        } else {
            final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), listItems));

            if (listItems != null && !listItems.isEmpty()) {
                final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
                if (forSaleItems != null && !forSaleItems.isEmpty()) {
                    this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems));
                }
            }

            this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
        }
    }

    public void handleExchangeSpecificOffer(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            this.syncExchangeRegistryTo(player);
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.SPECIFIC_OFFER, id, this.getListingsLimit(player)));
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

            this.syncExchangeRegistryTo(player);
            return;
        }
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Instant created = Instant.now();

                    final int result = ExchangeQueries
                        .createInsertExchange(created, player.getUniqueId(), id, name, permission, isHidden)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        // TODO Notification
                        return;
                    }

                    final BasicExchange basicExchange = new BasicExchange(id, created, player.getUniqueId(), name, permission, isHidden);
                    basicExchange.refreshCreatorName();

                    this.exchanges.put(id, basicExchange);

                    Sponge.getServer().getOnlinePlayers().forEach(this::syncExchangeRegistryTo);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleExchangeModify(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".exchange.modify")) {
            // TODO Send Notification
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final int result = ExchangeQueries
                        .createUpdateExchange(id, name, permission, isHidden)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        // TODO Notification
                    } else {
                        this.loadExchanges();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);

    }

    public void handleExchangeDelete(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!this.getExchange(id).isPresent()) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

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
                        // TODO Notification
                    } else {
                        this.loadExchanges();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * ListItem
     */

    public void loadListItems(final Exchange axs) {
        checkNotNull(axs);

        this.logger.info("Querying items for Exchange [{}], please wait...", axs.getId());

        final List<ListItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchListItemsAndDataFor(axs.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> {
                for (final Record record : result) {
                    final Integer recNo = record.getValue(AxsListItem.AXS_LIST_ITEM.REC_NO);

                    final String rawItemId = record.getValue(AxsListItem.AXS_LIST_ITEM.ITEM_TYPE);

                    final ResourceLocation location = SerializationUtil.fromString(rawItemId);

                    if (location == null) {
                        this.logger.warn("Malformed item id found when loading from database. Id is [{}], record number is [{}]. Skipping...",
                            rawItemId, recNo);
                        continue;
                    }

                    final Item item = ForgeRegistries.ITEMS.getValue(location);

                    if (item == null) {
                        this.logger.warn("Unknown item found when loading from database. Registry name is [{}], record number is [{}]. Skipping"
                            + "... (Did you remove a mod?)", rawItemId, recNo);
                        continue;
                    }

                    final Timestamp created = record.getValue(AxsListItem.AXS_LIST_ITEM.CREATED);
                    final UUID seller = SerializationUtil.uniqueIdFromBytes(record.getValue(AxsListItem.AXS_LIST_ITEM.SELLER));
                    final Integer quantity = record.getValue(AxsListItem.AXS_LIST_ITEM.QUANTITY);
                    final Integer metadata = record.getValue(AxsListItem.AXS_LIST_ITEM.METADATA);
                    final Integer index = record.getValue(AxsListItem.AXS_LIST_ITEM.INDEX);
                    final byte[] compoundData = record.getValue(AxsListItemData.AXS_LIST_ITEM_DATA.DATA);

                    NBTTagCompound compound = null;
                    if (compoundData != null) {
                        try {
                            compound = SerializationUtil.compoundFromBytes(compoundData);
                        } catch (IOException e) {
                            this.logger.error("Malformed item data found when loading from database. Record number is [{}]. Skipping...",
                                recNo);
                            continue;
                        }
                    }

                    final BasicListItem basicListItem =
                        new BasicListItem(recNo, created.toInstant(), seller, item, quantity, metadata, index, compound);

                    basicListItem.refreshSellerName();

                    items.add(basicListItem);
                }
            });

            axs.clearListItems();
            axs.clearForSaleItems();

            final Map<UUID, List<ListItem>> itemsByOwner = items.stream().collect(Collectors.groupingBy(ListItem::getSeller));
            itemsByOwner.forEach((key, value) -> value.sort(Comparator.comparingInt(ListItem::getIndex)));

            axs.putListItems(itemsByOwner.isEmpty() ? null : itemsByOwner);

            this.logger.info("Loaded [{}] list item(s) for Exchange [{}].", items.size(), axs.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleModifyListItems(final Player player, final String id, final List<InventoryAction> actions) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(actions);
        checkState(!actions.isEmpty());

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;

        List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);

        // Inventory -> Listing
        final List<InventoryAction> toListingActions =
            actions.stream().filter(a -> a.getDirection() == InventoryAction.Direction.TO_LISTING).collect(Collectors.toList());

        // TODO Listing limits (listing amounts)
        for (int i = 0; i < toListingActions.size(); i++) {
            final VanillaStack stack = toListingActions.get(i).getStack();

            int amountLeft = stack.getQuantity();

            for (int j = 0; j < serverPlayer.inventory.mainInventory.size(); j++) {
                final ItemStack next = serverPlayer.inventory.getStackInSlot(j);

                if (ItemStack.areItemsEqual(stack.asRealStack(), next)) {
                    final int toRemove = Math.min(next.getCount(), amountLeft);
                    next.setCount(next.getCount() - toRemove);
                    amountLeft -= toRemove;

                    if (next.getCount() <= 0) {
                        serverPlayer.inventory.removeStackFromSlot(j);
                    }
                }

                if (amountLeft == 0) {
                    break;
                }
            }

            if (amountLeft > 0) {
                // TODO Player asked to create a listing for an amount they do not have so we have taken what we could. Maybe send them a
                // TODO notification and tell them what we didn't take.
            }

            final int listingQuantity = stack.getQuantity() - amountLeft;

            if (listItems == null) {
                listItems = new ArrayList<>();
                axs.putListItemsFor(player.getUniqueId(), listItems);
            }

            ListItem found = null;

            for (final ListItem listItem : listItems) {
                if (ItemStack.areItemsEqual(stack.asRealStack(), listItem.asRealStack()) && ItemStack.areItemStackTagsEqual(stack.asRealStack(),
                    listItem.asRealStack())) {
                    found = listItem;
                    break;
                }
            }

            if (found != null) {
                found.setQuantity(found.getQuantity() + listingQuantity);
            } else {
                found = new BasicListItem(BasicListItem.NEW_ROW, Instant.now(), player.getUniqueId(), stack.getItem(), listingQuantity,
                    stack.getMetadata(), i, stack.getCompound());
                ((BasicListItem) found).refreshSellerName();
                listItems.add(found);
            }
        }

        // Listing -> Inventory
        final List<InventoryAction> toInventoryActions =
            actions.stream().filter(a -> a.getDirection() == InventoryAction.Direction.TO_INVENTORY).collect(Collectors.toList());

        if ((listItems == null || listItems.isEmpty()) && !toInventoryActions.isEmpty()) {
            this.logger.warn("Player [{}] attempted to move listings back to the inventory but the server knows of no listings for them. This could"
                + " be a de-sync or an exploit. Printing stacks...", player.getName());
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), null));
            return;
        }

        final List<VanillaStack> unknownListings = new ArrayList<>();
        final List<VanillaStack> inventoryRejections = new ArrayList<>();
        final List<ListItem> listingRemovals = new ArrayList<>();

        for (final InventoryAction action : toInventoryActions) {
            final VanillaStack stack = action.getStack();

            ListItem found = null;

            for (final ListItem listItem : listItems) {
                if (ItemStack.areItemsEqual(stack.asRealStack(), listItem.asRealStack()) && ItemStack.areItemStackTagsEqual(stack.asRealStack(),
                    listItem.asRealStack())) {
                    found = listItem;
                    break;
                }
            }

            if (found == null) {
                unknownListings.add(stack);
                continue;
            }

            if (found.getQuantity() < stack.getQuantity()) {
                inventoryRejections.add(stack);
                continue;
            }

            final int limit = stack.getItem().getItemStackLimit(stack.asRealStack());
            final int divisor = stack.getQuantity() / limit;
            final int remainder = stack.getQuantity() % limit;

            int amountAdded = 0;

            for (int i = 0; i < divisor; i++) {
                final VanillaStack copyStack = stack.copy();
                copyStack.setQuantity(limit);
                final ItemStack realCopyStack = copyStack.asRealStack().copy();
                serverPlayer.inventory.addItemStackToInventory(realCopyStack);
                amountAdded += copyStack.getQuantity() - realCopyStack.getCount();
            }

            if (remainder > 0) {
                final VanillaStack copyStack = stack.copy();
                copyStack.setQuantity(remainder);
                final ItemStack realCopyStack = copyStack.asRealStack().copy();
                serverPlayer.inventory.addItemStackToInventory(realCopyStack);
                amountAdded += copyStack.getQuantity() - realCopyStack.getCount();
            }

            final int amountRejected = stack.getQuantity() - amountAdded;
            if (amountRejected <= 0) {
                listingRemovals.add(found);
                listItems.remove(found);
            } else {
                found.setQuantity(amountRejected);

                final VanillaStack rejectedStack = stack.copy();
                rejectedStack.setQuantity(amountAdded);
                inventoryRejections.add(rejectedStack);
            }
        }

        if (!unknownListings.isEmpty()) {
            this.logger.warn("Player [{}] attempted to move the following item listings back to their inventory but the "
                + "Exchange knows of no such listings. This could be a de-sync or an exploit. Printing stacks...",
                player.getName());
            this.logger.warn(unknownListings.toString());
        }

        if (!inventoryRejections.isEmpty()) {
            // TODO Send down a notification explaining their inventory ran out of space and the remaining remained a listing
        }

        final List<ListItem> listItemsRef = listItems;

        // TODO Unsure if I want to do this here but it is quite necessary to cap for sale quantities..
        listItemsRef
            .stream()
            .filter(item -> item.getForSaleItem().isPresent())
            .map(item -> item.getForSaleItem().get())
            .forEach(forSaleItem -> ((BasicForSaleItem) forSaleItem).setQuantityRemaining(Math.min(forSaleItem.getQuantityRemaining(),
                forSaleItem.getListItem().getQuantity())));

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {

                    // Insertions/Updates
                    final List<Query> batchUpdates = new ArrayList<>();

                    final Iterator<ListItem> iter = listItemsRef.iterator();
                    while (iter.hasNext()) {

                        final ListItem item = iter.next();

                        // Insertions
                        if (item.getRecord() == BasicListItem.NEW_ROW) {

                            // TODO Can't keepstatement this, need to make sure it doesn't cache connections.
                            final Result<AxsListItemRecord> result = ExchangeQueries
                                .createInsertItem(axs.getId(), item.getCreated(), item.getSeller(), item.getItem(), item.getQuantity(),
                                    item.getMetadata(), item.getIndex())
                                .build(context)
                                .fetch();

                            if (result.isEmpty()) {
                                this.logger.error("Failed to submit an item listing to the database and therefore it will be discarded. This "
                                    + "generally means there is an issue connecting to the database or an issue with the database itself. Printing "
                                    + "contents of the listing...");
                                this.logger.error(item.toString());
                                iter.remove();
                                continue;
                            }

                            final Integer recNo = result.getValue(0, AxsListItem.AXS_LIST_ITEM.REC_NO);

                            ((BasicListItem) item).setRecord(recNo);

                            final NBTTagCompound compound = item.getCompound();
                            if (compound == null) {
                                continue;
                            }

                            ExchangeQueries
                                .createInsertItemData(recNo, compound)
                                .build(context)
                                .keepStatement(false)
                                .execute();
                        } else {

                            batchUpdates.add(ExchangeQueries
                                .createUpdateListItem(item.getRecord(), item.getQuantity(), item.getIndex())
                                .build(context)
                                .keepStatement(false)
                            );
                        }
                    }

                    // Updates
                    int[] batchedResults = context
                        .batch(batchUpdates)
                        .execute();

                    for (int i = 0; i < batchedResults.length; i++) {
                        if (batchedResults[i] == 0) {
                            this.logger.warn("A query apart of a batch returned a 0 result which means the listing was not found. "
                                + "Normally this means a de-sync between the server and the database. Please report the query contents as follows to"
                                + " an AlmuraDev developer.");
                            this.logger.warn(batchUpdates.get(i).getSQL());
                        }
                    }

                    final List<Query> batchRemovals = new ArrayList<>();

                    // Deletions (soft)
                    listingRemovals.forEach(item -> batchRemovals.add(ExchangeQueries
                        .createUpdateListItemIsHidden(item.getRecord(), true)
                        .build(context)
                        .keepStatement(false))
                    );

                    batchedResults = context
                        .batch(batchRemovals).execute();

                    for (int i = 0; i < batchedResults.length; i++) {
                        if (batchedResults[i] == 0) {
                            this.logger.warn("A soft removal query apart of a batch returned a 0 result which means the listing was not found. "
                                + "Normally this means a de-sync between the server and the database. Please report the query contents as follows to"
                                + " an AlmuraDev developer.");
                            this.logger.warn(batchRemovals.get(i).getSQL());
                        }
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), listItemsRef));

                            if (!listItemsRef.isEmpty()) {
                                final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
                                if (forSaleItems != null && !forSaleItems.isEmpty()) {
                                    this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems));
                                }
                            }

                            this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
                        })
                        .submit(this.container);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * ForSaleItem
     */

    public void loadForSaleItems(final Exchange axs) {
        checkNotNull(axs);

        this.logger.info("Querying for sale items for Exchange [{}], please wait...", axs.getId());

        final List<ForSaleItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchForSaleItemsFor(axs.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            // We're async here, we have to get a copy of the map else iteration will be synchronized for the entirety of the below..
            final Map<UUID, List<ListItem>> listItems = new HashMap<>(axs.getListItems());

            results.forEach(result -> result.forEach(record -> {
                final Integer recNo = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.REC_NO);
                final Integer itemRecNo = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.LIST_ITEM);
                ListItem found = null;

                for (final List<ListItem> itemList : listItems.values()) {

                    found = itemList
                        .stream()
                        .filter(item -> item.getRecord() == itemRecNo)
                        .findAny()
                        .orElse(null);

                    if (found != null) {
                        break;
                    }
                }

                if (found == null) {
                    this.logger.error("A for sale listing is being loaded but the listing no longer exists. This is a de-sync between the database "
                        + "and the server or somehow an entity has tampered with the structure of the database. The item list id is [{}] and the "
                        + "record number is [{}]. Report to an AlmuraDev developer ASAP.", itemRecNo, recNo);
                } else {
                    final Timestamp created = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.CREATED);
                    int quantityRemaining = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.QUANTITY_REMAINING);
                    final BigDecimal price = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.PRICE);

                    if (quantityRemaining <= 0) {
                        this.logger.error("A for sale listing is being loaded but the quantity remaining is zero or below. An entity has tampered "
                            + "with the contents of the database. The record number is [{}]. This row will be set to hidden.", recNo);

                        ExchangeQueries
                            .createUpdateForSaleItemIsHidden(recNo, true)
                            .build(context)
                            .keepStatement(false)
                            .execute();

                    } else {
                        if (quantityRemaining > found.getQuantity()) {
                            this.logger.warn("A for sale listing is being loaded but the quantity remaining is less than the actual listing. An "
                                    + "entity has tampered with the contents of the database. The record number is [{}], the quantity remaining is "
                                    + "[{}] and the listing's quantity is [{}]. The quantity remaining will be adjusted to match.", recNo,
                                quantityRemaining, found.getQuantity());

                            final int dbQuantityRemaining = quantityRemaining;
                            quantityRemaining = found.getQuantity();

                            ExchangeQueries
                                .createUpdateForSaleItemQuantityRemaining(recNo, dbQuantityRemaining)
                                .build(context)
                                .keepStatement(false)
                                .execute();
                        }

                        final BasicForSaleItem basicForSaleItem = new BasicForSaleItem((BasicListItem) found, recNo, created.toInstant(),
                            quantityRemaining, price);
                        ((BasicListItem) found).setForSaleItem(basicForSaleItem);

                        items.add(basicForSaleItem);
                    }
                }
            }));

            axs.clearForSaleItems();

            axs.putForSaleItems(items.isEmpty() ? null : items.stream().collect(Collectors.groupingBy(forSaleItem -> forSaleItem
                .getListItem().getSeller())));

            this.logger.info("Loaded [{}] for sale item(s) for Exchange [{}].", items.size(), axs.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleForSaleFilter(final Player player, final String id, final String filter) {
        checkNotNull(player);
        checkNotNull(id);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        // TODO Parse filter or more determine filter format

        // TODO TEST CODE
        this.network.sendTo(player, new ClientboundForSaleItemsResponsePacket(axs.getId(),
            axs.getForSaleItems().entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream).collect(Collectors.toList())));
    }

    public void handleListForSaleItem(final Player player, final String id, final int listItemRecNo, final BigDecimal price) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null || listItems.isEmpty()) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        if (found.getForSaleItem().isPresent()) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
        if (forSaleItems != null && forSaleItems.size() + 1 > this.getListingsLimit(player)) {
            // TODO Notification
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Instant created = Instant.now();

                    // TODO Can't keepstatement this, need to make sure it doesn't cache connections.
                    final AxsForSaleItemRecord record = ExchangeQueries
                        .createInsertForSaleItem(created, found.getRecord(), found.getQuantity(), price)
                        .build(context)
                        .fetchOne();

                    if (record == null) {
                        // TODO Logger
                        return;
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final BasicForSaleItem basicForSaleItem = new BasicForSaleItem((BasicListItem) found, record.getRecNo(),
                                created, record.getQuantityRemaining(), record.getPrice());

                            List<ForSaleItem> forSaleItemsRef = forSaleItems;
                            if (forSaleItemsRef == null) {
                                forSaleItemsRef = new ArrayList<>();
                                axs.putForSaleItemsFor(player.getUniqueId(), forSaleItemsRef);
                            }

                            ((BasicListItem) found).setForSaleItem(basicForSaleItem);

                            forSaleItemsRef.add(basicForSaleItem);

                            this.network.sendToAll(new ClientboundForSaleFilterRequestPacket(axs.getId()));

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(),
                                Lists.newArrayList(basicForSaleItem)));
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleDelistForSaleItem(final Player player, final String id, final int listItemRecNo) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null || listItems.isEmpty()) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        if (forSaleItem == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
        if (forSaleItems == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final int result = ExchangeQueries
                        .createUpdateForSaleItemIsHidden(forSaleItem.getRecord(), true)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        // TODO Logger
                        return;
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            if (!forSaleItems.remove(forSaleItem)) {
                                // TODO Notification
                                // TODO Resync
                                return;
                            }

                            ((BasicListItem) found).setForSaleItem(null);

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems));

                            this.network.sendToAll(new ClientboundForSaleFilterRequestPacket(axs.getId()));
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleAdjustPriceForSaleItem(final Player player, final String id, final int listItemRecNo, final BigDecimal price) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        if (forSaleItem == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final int result = ExchangeQueries
                        .createUpdateForSaleItemPrice(forSaleItem.getRecord(), price)
                        .build(context)
                        .execute();

                    if (result == 0) {
                        // TODO Logger
                        return;
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            ((BasicForSaleItem) forSaleItem).setPrice(price);

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), Lists.newArrayList(forSaleItem)));
                            this.network.sendToAll(new ClientboundForSaleFilterRequestPacket(axs.getId()));
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * Transaction
     */

    public void handleTransaction(final Player player, final String id, final int listItemRecNo, final int quantity) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkState(quantity > 0);

        final EconomyService economyService = this.serviceManager.provide(EconomyService.class).orElse(null);
        if (economyService == null) {
            // TODO Notification
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            // TODO Notification
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            // TODO Notification
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        if (forSaleItem == null) {
            // TODO Notification
            // TODO Resync
            return;
        }

        if (forSaleItem.getQuantityRemaining() < quantity) {
            // TODO Notification
            // TODO Resync
            return;
        }

        final BigDecimal balance = account.getBalance(economyService.getDefaultCurrency());
        final BigDecimal price = forSaleItem.getPrice();
        final double total = price.doubleValue() * quantity;

        if (total > balance.doubleValue()) {
            // TODO Notification
            return;
        }
    }

    private void syncExchangeRegistryTo(final Player player) {
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

    private int getListingsLimit(final Player player) {
        // TODO Need to determine what controls this ultimately, 100 for now.
        return 100;
    }
}
