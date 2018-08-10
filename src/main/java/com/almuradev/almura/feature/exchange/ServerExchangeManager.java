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
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
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
    public ServerExchangeManager(final PluginContainer container, final Scheduler scheduler, final Logger logger, @ChannelId(NetworkConfig.CHANNEL)
        final ChannelBinding.IndexedMessageChannel network, final DatabaseManager databaseManager,
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

            if (!axs.isLoaded()) {
                this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, id, () -> {
                    this.loadListItems(axs);

                    this.loadForSaleItems(axs);

                    axs.setLoaded(true);

                    // TODO This will hit everyone, not just who opened the Exchange in this timeframe. Figure out a way to pass initiators
                    this.scheduler
                        .createTaskBuilder()
                        .execute(() ->
                            Sponge.getServer().getOnlinePlayers().forEach(p -> {
                                axs.getListItemsFor(p.getUniqueId()).ifPresent(items -> this.network.sendTo(p, new ClientboundListItemsResponsePacket(axs.getId(), items)));
                                this.network.sendTo(p, new ClientboundForSaleFilterRequestPacket(axs.getId()));
                            })
                        )
                        .submit(this.container);
                });
            } else {
                axs.getListItemsFor(player.getUniqueId()).ifPresent(items ->
                    this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), items)));
                this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
            }
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
        checkNotNull(axs);

        this.logger.info("Querying items for Exchange [{}], please wait...");

        final List<ListItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchListItemsAndDataFor(axs.getId())
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> {
                for (final Record record : result) {
                    final ResourceLocation location = SerializationUtil.fromString(record.getValue(AxsListItem.AXS_LIST_ITEM.ITEM_TYPE));

                    if (location == null) {
                        // TODO This is a malformed resource location
                    } else {

                        final Item item = ForgeRegistries.ITEMS.getValue(location);

                        if (item == null) {
                            // TODO They've given us an item that isn't loaded (likely a mod that vanished)
                        } else {

                            final Integer recNo = record.getValue(AxsListItem.AXS_LIST_ITEM.REC_NO);
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
                                    // TODO Malformed compound from database
                                    continue;
                                }
                            }

                            final BasicListItem basicListItem =
                                new BasicListItem(recNo, created.toInstant(), seller, item, quantity, metadata, index, compound);

                            // TODO This may not be thread-safe..
                            basicListItem.refreshSellerName();

                            items.add(basicListItem);
                        }
                    }
                }
            });

            axs.clearListItems();
            axs.clearForSaleItems();

            final Map<UUID, List<ListItem>> itemsByOwner = items.stream().collect(Collectors.groupingBy(ListItem::getSeller));
            itemsByOwner.forEach((key, value) -> value.sort(Comparator.comparingInt(ListItem::getIndex)));

            axs.putListItems(itemsByOwner.isEmpty() ? null : itemsByOwner);

            this.logger.info("Loaded {} list item(s) for Exchange [{}].", axs.getId(), items.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleModifyListItems(final Player player, final String id, @Nullable final List<InventoryAction> actions) {
        checkNotNull(player);
        checkNotNull(id);

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null) {
            // TODO Exchange no longer exists, re-sync this player
            return;
        }

        final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;

        if (actions == null) {
            // TODO Might do a resync in this case or enforce at least 1 action. I'll ponder it.
            return;
        }

        List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);

        // Inventory -> Listing
        final List<InventoryAction> toListingActions =
            actions.stream().filter(a -> a.getDirection() == InventoryAction.Direction.TO_LISTING).collect(Collectors.toList());

        // TODO Listing limits (listing amounts)
        for (int i = 0; i < toListingActions.size(); i++) {
            final VanillaStack stack = toListingActions.get(i).getStack();

            int amountLeft = stack.getQuantity();

            final Iterator<ItemStack> iter = serverPlayer.inventory.mainInventory.iterator();
            while (iter.hasNext()) {
                final ItemStack next = iter.next();

                if (ItemStack.areItemsEqual(stack.asRealStack(), next)) {
                    final int toRemove = Math.min(next.getCount(), amountLeft);
                    next.setCount(next.getCount() - toRemove);
                    amountLeft -= toRemove;

                    if (next.getCount() <= 0) {
                        iter.remove();
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
                if (ItemStack.areItemsEqual(stack.asRealStack(), listItem.asRealStack())) {
                    found = listItem;
                    break;
                }
            }

            if (found != null) {
                found.setQuantity(listingQuantity);
            } else {
                found = new BasicListItem(BasicListItem.NEW_ROW, Instant.now(), player.getUniqueId(), stack.getItem(), listingQuantity,
                    stack.getMetadata(), i, stack.getCompound());
                listItems.add(found);
            }
        }

        // Listing -> Inventory
        final List<InventoryAction> toInventoryActions =
            actions.stream().filter(a -> a.getDirection() == InventoryAction.Direction.TO_INVENTORY).collect(Collectors.toList());

        if ((listItems == null || listItems.isEmpty()) && !toInventoryActions.isEmpty()) {
            // TODO They want to move items back to their Inventory but we know of no listings, re-sync this player
            return;
        }

        // TODO Not using this yet but I can at least tell the client what we couldn't move back
        final List<VanillaStack> inventoryRejections = new ArrayList<>();

        for (final InventoryAction action : toInventoryActions) {
            final VanillaStack stack = action.getStack();

            ListItem found = null;

            for (final ListItem listItem : listItems) {
                if (ItemStack.areItemsEqual(stack.asRealStack(), listItem.asRealStack())) {
                    found = listItem;
                    break;
                }
            }

            if (found == null || found.getQuantity() < stack.getQuantity()) {
                // TODO They sent us up an unknown listing or asked to remove too much, we're bailing
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
            found.setQuantity(amountRejected);

            if (amountRejected > 0) {
                final VanillaStack rejectedStack = stack.copy();
                rejectedStack.setQuantity(amountAdded);
                inventoryRejections.add(rejectedStack);
            }
        }

        // TODO Tell the Player we cannot pull all their listings back into their inventory
    }

    /**
     * ForSaleItem
     */

    public void loadForSaleItems(final Exchange axs) {
        checkNotNull(axs);

        this.logger.info("Querying for sale items for Exchange [{}], please wait...");

        final List<ForSaleItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchForSaleItemsFor(axs.getId())
                .build(context)
                .keepStatement(false)
                .fetchMany();

            // We're async here, we have to get a copy of the map else iteration will be synchronized for the entirety of the below..
            final Map<UUID, List<ListItem>> listItems = new HashMap<>(axs.getListItems());

            results.forEach(result -> result.forEach(record -> {
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
                    // TODO If we're here, this is a dead for sale item as the list item doesn't exist in the exchange
                } else {
                    final Timestamp created = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.CREATED);
                    final BigDecimal price = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.PRICE);

                    items.add(new BasicForSaleItem((BasicListItem) found, created.toInstant(), price));
                }
            }));

            axs.clearForSaleItems();

            axs.putForSaleItems(items.isEmpty() ? null : items.stream().collect(Collectors.groupingBy(forSaleItem -> forSaleItem
                .getListItem().getSeller())));

            this.logger.info("Loaded {} for sale item(s) for Exchange [{}].", axs.getId(), items.size());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleForSaleFilter(final Player player, final String filter) {
        checkNotNull(player);
        checkNotNull(filter);

        // TODO
    }

    public void handleListForSaleItem(final Player player, final String id, final int listItemRecNo, final BigDecimal price) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        // TODO
    }

    public void handleDelistForSaleItem(final Player player, final String id, final int listItemRecNo) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);

        // TODO
    }

    public void handleAdjustPriceForSaleItem(final Player player, final String id, final int listItemRecNo, final BigDecimal price) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        // TODO
    }

    /**
     * Transaction
     */

    public void handleTransaction(final Player player, final String id, final int listItemRecNo, final int quantity) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkState(quantity >= 0);

        // TODO
    }
}
