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
import com.almuradev.almura.feature.exchange.basic.BasicExchange;
import com.almuradev.almura.feature.exchange.database.ExchangeQueries;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeGuiResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundExchangeRegistryPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleFilterRequestPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundForSaleItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsResponsePacket;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsSaleStatusPacket;
import com.almuradev.almura.feature.exchange.network.ClientboundTransactionCompletePacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.database.DatabaseQueue;
import com.almuradev.almura.shared.feature.IngameFeature;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.feature.filter.FilterRegistry;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.feature.exchange.basic.listing.BasicForSaleItem;
import com.almuradev.almura.feature.exchange.basic.listing.BasicListItem;
import com.almuradev.almura.shared.item.VanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.core.event.Witness;
import com.almuradev.generated.axs.tables.Axs;
import com.almuradev.generated.axs.tables.AxsForSaleItem;
import com.almuradev.generated.axs.tables.AxsListItem;
import com.almuradev.generated.axs.tables.AxsListItemData;
import com.almuradev.generated.axs.tables.records.AxsForSaleItemRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemDataRecord;
import com.almuradev.generated.axs.tables.records.AxsListItemRecord;
import com.google.common.collect.Lists;
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
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Results;
import org.slf4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.sound.SoundTypes;
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
import java.util.stream.Stream;

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
    private final ServiceManager serviceManager;
    private final ServerNotificationManager notificationManager;

    private final Map<String, Exchange> exchanges = new HashMap<>();
    private final List<UUID> playerSpecificInitiatorIds = new ArrayList<>();

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
        this.logger.info("Querying database for exchanges, please wait...");

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

    private void loadExchanges() {

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
                    final BasicExchange exchange = new BasicExchange("almura.exchange.global", Instant.now(), FeatureConstants.UNKNOWN_OWNER, "Global "
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

                this.exchanges.values().forEach(IngameFeature::syncCreatorNameToUniqueId);

                Sponge.getServer().getOnlinePlayers().forEach(this::syncExchangeRegistryTo);
            })
            .submit(this.container);
    }

    void openExchangeManage(final Player player) {
        checkNotNull(player);

        if (!player.hasPermission(Almura.ID + ".exchange.admin")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to manage exchanges!"), 5);
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.MANAGE));
    }

    void openExchangeSpecific(final Player player, final Exchange axs) {
        checkNotNull(player);
        checkNotNull(axs);

        if (!player.hasPermission(axs.getPermission())) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Store"), Text.of("You do not have permission "
              + "to open this exchange!"), 5);
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.SPECIFIC, axs.getId(), this.getListingsLimit(player)));

        if (!axs.isLoaded()) {
            this.playerSpecificInitiatorIds.add(player.getUniqueId());

            this.databaseManager.getQueue().queue(DatabaseQueue.ActionType.FETCH_IGNORE_DUPLICATES, axs.getId(), () -> {
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
                                        this.network.sendTo(p, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, null));
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
                    this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, null));
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
            this.logger.error("Player '{}' attempted to open an offer screen for exchange '{}' but the server has no knowledge of it. Syncing "
                    + "exchange registry...", player.getName(), id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        this.network.sendTo(player, new ClientboundExchangeGuiResponsePacket(ExchangeGuiType.SPECIFIC_OFFER, id));
    }

    public void handleExchangeAdd(final Player player, final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(player);
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        if (!player.hasPermission(Almura.ID + ".exchange.add")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of("You do not have permission "
                + "to add exchanges!"), 5);
            return;
        }

        if (this.getExchange(id).isPresent()) {
            this.logger.error("Player '{}' attempted to add exchange '{}' but it already exists. Syncing exchange registry...", player.getName(),
                id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final UUID creator = player.getUniqueId();
        
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Instant created = Instant.now();

                    final int result = ExchangeQueries
                        .createInsertExchange(created, creator, id, name, permission, isHidden)
                        .build(context)
                        .keepStatement(false)
                        .execute();

                    if (result == 0) {
                        this.logger.error("Player '{}' submitted a new exchange '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.scheduler
                      .createTaskBuilder()
                      .execute(() -> {
                          final BasicExchange basicExchange = new BasicExchange(id, created, creator, name, permission, isHidden);
                          basicExchange.syncCreatorNameToUniqueId();

                          this.exchanges.put(id, basicExchange);

                          Sponge.getServer().getOnlinePlayers().forEach(this::syncExchangeRegistryTo);
                      })
                      .submit(this.container);
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
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of("You do not have permission "
                + "to modify exchanges!"), 5);
            return;
        }

        final Exchange axs = this.getExchange(id).orElse(null);

        if (axs == null) {
            this.logger.error("Player '{}' attempted to modify exchange '{}' but the server has no knowledge of it. Syncing exchange registry...",
                player.getName(), id);
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
                        this.logger.error("Player '{}' submitted a modified exchange '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.loadExchanges();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    public void handleExchangeDelete(final Player player, final String id) {
        checkNotNull(player);
        checkNotNull(id);

        if (!player.hasPermission(Almura.ID + ".exchange.delete")) {
            this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Exchange"), Text.of("You do not have permission "
                + "to delete exchanges!"), 5);
            return;
        }

        if (!this.getExchange(id).isPresent()) {
            this.logger.error("Player '{}' attempted to delete exchange '{}' but the server has no knowledge of it. Syncing exchange registry...",
                player.getName(), id);
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
                        this.logger.error("Player '{}' submitted a deleted exchange '{}' to the database but it failed. Discarding changes...",
                            player.getName(), id);
                        return;
                    }

                    this.loadExchanges();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    /**
     * ListItem
     */

    private void loadListItems(final Exchange axs) {
        checkNotNull(axs);

        this.logger.info("Querying list items for exchange '{}' ({}), please wait...", axs.getName(), axs.getId());

        final List<ListItem> items = new ArrayList<>();

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchListItemsAndDataFor(axs.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> items.addAll(this.parseListItemsFrom(result)));

            axs.clearListItems();
            axs.clearForSaleItems();

            final Map<UUID, List<ListItem>> itemsByOwner = items
                .stream()
                .collect(Collectors.groupingBy(ListItem::getSeller));

            itemsByOwner.forEach((key, value) -> value.sort(Comparator.comparingInt(ListItem::getIndex)));

            axs.putListItems(itemsByOwner.isEmpty() ? null : itemsByOwner);

            this.logger.info("Loaded [{}] list item(s) for exchange '{}' ({}).", items.size(), axs.getName(), axs.getId());

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
            this.logger.error("Player '{}' attempted to list items for exchange '{}' but the server has no knowledge of it. Syncing exchange "
                    + "registry...", player.getName(), id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final UUID seller = player.getUniqueId();
        final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        final IItemHandler simulatedInventory = serverPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

        // Inventory -> Listing
        final List<InventoryAction> toListingActions = actions
            .stream()
            .filter(a -> a.getDirection() == InventoryAction.Direction.TO_LISTING)
            .collect(Collectors.toList());

        final List<VanillaStack> toListingStacks = new ArrayList<>();
        final List<VanillaStack> unknownInventoryStacks = new ArrayList<>();
        final List<VanillaStack> leftoverToListingStacks = new ArrayList<>();

        for (InventoryAction toListingAction : toListingActions) {
            final VanillaStack stack = toListingAction.getStack();

            int amountLeft = stack.getQuantity();

            boolean matched = false;

            for (int j = 0; j < simulatedInventory.getSlots(); j++) {
                final ItemStack slotStack = simulatedInventory.getStackInSlot(j);

                if (ItemHandlerHelper.canItemStacksStack(slotStack, stack.asRealStack())) {
                    amountLeft -= simulatedInventory.extractItem(j, amountLeft, false).getCount();
                    matched = true;
                }

                if (amountLeft <= 0) {
                    break;
                }
            }

            if (!matched) {
                unknownInventoryStacks.add(stack);
            } else {
                if (amountLeft > 0) {
                    final VanillaStack copyStack = stack.copy();
                    copyStack.setQuantity(amountLeft);
                    leftoverToListingStacks.add(copyStack);
                }

                final VanillaStack copyStack = stack.copy();
                copyStack.setQuantity(stack.getQuantity() - amountLeft);

                if (copyStack.getQuantity() != 0) {
                    toListingStacks.add(copyStack);
                }
            }
        }

        // Listing -> Inventory
        final List<InventoryAction> toInventoryActions = actions
            .stream()
            .filter(a -> a.getDirection() == InventoryAction.Direction.TO_INVENTORY)
            .collect(Collectors.toList());

        final List<VanillaStack> listingNotFoundStacks = new ArrayList<>();
        final List<ListItem> desyncToInventoryStacks = new ArrayList<>();
        final List<ListItem> toInventoryStacks = new ArrayList<>();
        final List<ListItem> partialToInventoryStacks = new ArrayList<>();

        final List<ListItem> currentListItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);

        if (!toInventoryActions.isEmpty()) {

            if (currentListItems == null || currentListItems.isEmpty()) {
                this.logger.error("Player '{}' attempted to move listings back to the inventory for exchange '{}' but the server knows of no "
                    + "listings for them. This could be a de-sync or an exploit. Printing stacks...", player.getName(), axs.getId());
                this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), null));
                this.printStacksToConsole(toInventoryActions.stream().map(InventoryAction::getStack).collect(Collectors.toList()));
            } else {
                for (final InventoryAction action : toInventoryActions) {
                    final VanillaStack stack = action.getStack();

                    ListItem found = null;

                    for (final ListItem listItem : currentListItems) {
                        if (ItemHandlerHelper.canItemStacksStack(stack.asRealStack(), listItem.asRealStack())) {
                            found = listItem;
                            break;
                        }
                    }

                    // Unknown listing
                    if (found == null) {
                        listingNotFoundStacks.add(stack);
                        continue;
                    }

                    ListItem toRemove = found.copy();

                    // Listing quantity mismatch (tracking this to let the user know)
                    if (found.getQuantity() < stack.getQuantity()) {
                        desyncToInventoryStacks.add(found);
                        toRemove.setQuantity(found.getQuantity());
                    } else {
                        toRemove.setQuantity(stack.getQuantity());
                    }

                    final ItemStack resultStack = ItemHandlerHelper.insertItemStacked(simulatedInventory, toRemove.asRealStack(), true);

                    // Simulated a partial stack insertion
                    if (!resultStack.isEmpty()) {
                        final ListItem copyStack = toRemove.copy();
                        copyStack.setQuantity(resultStack.getCount());
                        partialToInventoryStacks.add(copyStack);
                    }

                    final ListItem copyStack = toRemove.copy();
                    copyStack.setQuantity(toRemove.getQuantity() - resultStack.getCount());

                    toInventoryStacks.add(copyStack);
                }
            }
        }

        // This may seem quite weird but we need to clear out the listing items reference for this player across the board to await what the results
        // are from the database to ensure we're 1:1 in sync. Otherwise, the Exchange would keep selling..
        axs.putListItemsFor(seller, null);
        axs.putForSaleItemsFor(seller, null);

        Sponge.getServer().getOnlinePlayers()
            .stream()
            .filter(p -> p.getUniqueId() != seller)
            .forEach(p -> this.network.sendTo(p, new ClientboundForSaleFilterRequestPacket(axs.getId())));

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {

                    final Iterator<VanillaStack> listingIter = toListingStacks.iterator();

                    int index = 0;

                    // New listing
                    while (listingIter.hasNext()) {
                        final VanillaStack stack = listingIter.next();
                        final ItemStack realStack = stack.asRealStack();

                        ListItem found = null;

                        if (currentListItems != null) {
                            found = currentListItems
                                    .stream()
                                    .filter(item -> ItemHandlerHelper.canItemStacksStack(realStack, item.asRealStack()))
                                    .findAny()
                                    .orElse(null);
                        }

                        if (found == null) {
                            final AxsListItemRecord itemRecord = ExchangeQueries
                                .createInsertListItem(axs.getId(), Instant.now(), seller, realStack.getItem(), stack.getQuantity(),
                                    realStack.getMetadata(), index)
                                .build(context)
                                .fetchOne();

                            if (itemRecord == null) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                                        Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' submitted a new list item for exchange '{}' to the database but it failed. "
                                        + "Discarding changes and printing stack...", player.getName(), id);
                                this.printStacksToConsole(Lists.newArrayList(stack));
                                continue;
                            }

                            final NBTTagCompound compound = stack.getCompound();
                            if (compound == null) {
                                continue;
                            }

                            final AxsListItemDataRecord dataRecord = ExchangeQueries
                                .createInsertItemData(itemRecord.getRecNo(), compound)
                                .build(context)
                                .fetchOne();

                            if (dataRecord == null) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                                        Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' submitted data for item record '{}' for exchange '{}' but it failed. "
                                        + "Discarding changes...", player.getName(), itemRecord.getRecNo(), id);

                                ExchangeQueries
                                    .createDeleteListItem(itemRecord.getRecNo())
                                    .build(context)
                                    .execute();
                            }

                        } else {
                            final int result = ExchangeQueries
                                .createUpdateListItem(found.getRecord(), stack.getQuantity() + found.getQuantity(), index)
                                .build(context)
                                .execute();

                            if (result == 0) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                                        Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' attempted to add quantity to list item '{}' for exchange '{}' but it failed. "
                                        + "Discarding changes...", player.getName(), found.getRecord(), id);

                                listingIter.remove();
                            }
                        }

                        index++;
                    }

                    // Update/Delete listings

                    for (final ListItem next : toInventoryStacks) {
                        ListItem existingStack = null;

                        for (final ListItem stack : currentListItems) {
                            if (next.getRecord() == stack.getRecord()) {
                                existingStack = stack;
                                break;
                            }
                        }

                        final int diff = existingStack.getQuantity() - next.getQuantity();

                        if (diff == 0) {
                            final int result = ExchangeQueries
                                .createUpdateListItemIsHidden(next.getRecord(), true)
                                .build(context)
                                .execute();

                            if (result == 0) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                                        Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' attempted to remove list item '{}' for exchange '{}' but it failed. Discarding "
                                        + "changes...", player.getName(), next.getRecord(), id);
                            }

                        // Update partial listings
                        } else {
                            final int result = ExchangeQueries
                                .createUpdateListItem(next.getRecord(), diff, next.getIndex())
                                .build(context)
                                .execute();

                            if (result == 0) {
                                this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                                        Text.of("Critical error encountered, check the server console for more details!"));
                                this.logger.error("Player '{}' removed quantity from list item '{}' for exchange '{}' to the database but it "
                                        + "failed. Discarding changes...",
                                        player.getName(), next.getRecord(), id);
                            }
                        }
                    }

                    final Results listItemResults = ExchangeQueries
                        .createFetchListItemsAndDataFor(seller, false)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();

                    final Results forSaleItemResults = ExchangeQueries
                        .createFetchForSaleItemsFor(seller, false)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {

                            final Player sellerPlayer = Sponge.getServer().getPlayer(seller).orElse(null);
                            if (sellerPlayer != null) {

                                final IItemHandler inventory =
                                  ((EntityPlayerMP) sellerPlayer).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                                    , EnumFacing.UP);

                                // Add stacks from listings
                                for (final ListItem stack : toInventoryStacks) {
                                    final ItemStack resultStack = ItemHandlerHelper.insertItemStacked(inventory, stack.asRealStack(), false);

                                    if (!resultStack.isEmpty()) {
                                        // TODO Their inventory changed since simulation. Best case scenario we toss it on the ground
                                    }
                                }
                            } else {
                                // TODO They went offline on us. It is a very rare off-case. Half tempted to print what they should have got and let
                                // TODO an admin deal with it
                            }

                            final List<ListItem> listItems = new ArrayList<>();
                            listItemResults.forEach(result -> listItems.addAll(this.parseListItemsFrom(result)));

                            final List<ForSaleItem> forSaleItems = new ArrayList<>();
                            forSaleItemResults.forEach(result -> forSaleItems.addAll(this.parseForSaleItemsFrom(listItems, result)));

                            // TODO Build a notification that says...
                            // TODO  - Stacks requested to go to a listing but inventory can't fulfill it
                            // TODO  - Stacks requested to go to the inventory but listings aren't found
                            // TODO  - Stacks requested to go to the inventory but the listing couldn't fulfill it so we took what we could

                            axs.putListItemsFor(seller, listItems);
                            axs.putForSaleItemsFor(seller, forSaleItems);

                            if (sellerPlayer != null) {
                                this.network.sendTo(sellerPlayer, new ClientboundListItemsResponsePacket(axs.getId(), listItems));
                                this.network.sendTo(sellerPlayer, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, null));
                            }

                            Sponge.getServer().getOnlinePlayers().forEach(p -> this.network.sendTo(p, new ClientboundForSaleFilterRequestPacket(
                                axs.getId())));

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

    private void loadForSaleItems(final Exchange axs) {
        checkNotNull(axs);

        this.logger.info("Querying for sale items for exchange '{}' ({}), please wait...", axs.getName(), axs.getId());

        final List<ForSaleItem> forSaleItems = new ArrayList<>();

        final List<ListItem> listItems = axs.getListItems().entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Results results = ExchangeQueries
                .createFetchForSaleItemsFor(axs.getId(), false)
                .build(context)
                .keepStatement(false)
                .fetchMany();

            results.forEach(result -> forSaleItems.addAll(this.parseForSaleItemsFrom(listItems, result)));

            axs.clearForSaleItems();

            axs.putForSaleItems(forSaleItems.isEmpty() ? null : forSaleItems
                .stream()
                .collect(Collectors.groupingBy(forSaleItem -> forSaleItem.getListItem().getSeller())));

            this.logger.info("Loaded [{}] for sale item(s) for exchange '{}' ({}).", forSaleItems.size(), axs.getName(), axs.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleForSaleFilter(final Player player, final String id, @Nullable final String filter, @Nullable final String sorter,
        final int skip, final int limit) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(skip >= 0);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to filter for sale items for exchange '{}' but the server has no knowledge of it. Syncing "
                + "exchange registry...", player.getName(), id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        Stream<ForSaleItem> stream = axs.getForSaleItems().entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .flatMap(List::stream);

        if (filter != null) {
            final List<FilterRegistry.FilterElement<ListItem>> elements = FilterRegistry.instance.getFilterElements(filter);
            stream = stream
                .filter(forSaleItem -> elements
                    .stream()
                    .allMatch(element -> element.getFilter().test(forSaleItem.getListItem(), element.getValue())));
        }

        if (sorter != null) {
            final List<FilterRegistry.SorterElement<ListItem>> elements = FilterRegistry.instance.getSortingElements(sorter);
            final Comparator<ListItem> comparator = FilterRegistry.instance.buildSortingComparator(elements).orElse(null);
            if (comparator != null) {
                stream = stream.map(ForSaleItem::getListItem).sorted(comparator).map(k -> k.getForSaleItem().orElse(null));
            }
        }

        final List<ForSaleItem> result = stream.collect(Collectors.toList());

        final Stream<ForSaleItem> adjustedStream = result.stream().skip(skip);

        final List<ForSaleItem> limitedResult;
        if (limit > -1) {
            limitedResult = adjustedStream.limit(limit).collect(Collectors.toList());
        } else {
            limitedResult = adjustedStream.collect(Collectors.toList());
        }

        this.network.sendTo(player, new ClientboundForSaleItemsResponsePacket(axs.getId(), limitedResult, result.size()));
    }

    public void handleListForSaleItem(final Player player, final String id, final int listItemRecNo, final BigDecimal price) {
        checkNotNull(player);
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkNotNull(price);
        checkState(price.doubleValue() >= 0);

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to mark list item '{}' for sale for exchange '{}' but the server has no knowledge of that "
              + "exchange. Syncing exchange registry...", player.getName(), listItemRecNo, id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null || listItems.isEmpty()) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                    + "server console for more details!"));
            this.logger.error("Player '{}' attempted to mark list item '{}' for sale for exchange '{}' but the server has no record of any list "
              + "items for that player. Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(id, listItems));
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                    + "server console for more details!"));
            this.logger.error("Player '{}' attempted to mark list item '{}' for sale for exchange '{}' but the server has no record of the listing. "
              + "Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(id, listItems));
            return;
        }

        final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
        if (found.getForSaleItem().isPresent()) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                    + "server console for more details!"));
            this.logger.error("Player '{}' attempted to mark list item '{}' for sale for exchange '{}' but the "
              + "server already has a listing for that item. Syncing list items sale status...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(id, forSaleItems, null));
            return;
        }

        if (forSaleItems != null && forSaleItems.size() + 1 > this.getListingsLimit(player)) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("You have reached your listing limit."));
            return;
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Instant created = Instant.now();

                    final AxsForSaleItemRecord record = ExchangeQueries
                        .createInsertForSaleItem(created, found.getRecord(), price)
                        .build(context)
                        .fetchOne();

                    if (record == null) {
                        this.logger.error("Player '{}' attempted to mark list item '{}' for sale for exchange '{}' to the database but it failed. "
                          + "Discarding changes...", player.getName(), listItemRecNo, id);
                        return;
                    }

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final BasicForSaleItem basicForSaleItem = new BasicForSaleItem((BasicListItem) found, record.getRecNo(),
                                created, record.getPrice());

                            List<ForSaleItem> forSaleItemsRef = forSaleItems;
                            if (forSaleItemsRef == null) {
                                forSaleItemsRef = new ArrayList<>();
                                axs.putForSaleItemsFor(player.getUniqueId(), forSaleItemsRef);
                            }

                            forSaleItemsRef.add(basicForSaleItem);

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, null));

                            this.network.sendToAll(new ClientboundForSaleFilterRequestPacket(axs.getId()));
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
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' but the server has no knowledge of it. Syncing "
                + "exchange registry...", player.getName(), listItemRecNo, id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null || listItems.isEmpty()) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' but the server has no record of any list items for "
              + "that player. Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(id, listItems));
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' but the server has no record of the listing. "
              + "Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(id, listItems));
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        final List<ForSaleItem> forSaleItems = axs.getForSaleItemsFor(player.getUniqueId()).orElse(null);
        if (forSaleItem == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' but the server doesn't have a listing for "
              + "that item. Syncing list items sale status...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(id, forSaleItems, null));
            return;
        }

        if (forSaleItems == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' but the server has no record of any listings for "
              + "that player. Syncing list items sale status...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(id, null, null));
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
                        this.logger.error("Player '{}' attempted to de-list list item '{}' for exchange '{}' to the database but it failed. "
                            + "Discarding changes...", player.getName(), listItemRecNo, id);
                        return;
                    }

                    ExchangeQueries
                        .createUpdateListItemLastKnownPrice(listItemRecNo, forSaleItem.getPrice())
                        .build(context)
                        .execute();

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            found.setLastKnownPrice(forSaleItem.getPrice());

                            forSaleItems.remove(forSaleItem);

                            found.setForSaleItem(null);

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, Lists.newArrayList(found)));

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
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to adjust a price for a list item for exchange '{}' but the server has no knowledge of it. "
                + "Syncing exchange registry...", player.getName(), id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(player.getUniqueId()).orElse(null);
        if (listItems == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to adjust the price for list item '{}' for exchange '{}' but the server has no knowledge "
              + "of any list items for them. Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), null));
            return;
        }

        final ListItem found = listItems.stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);

        if (found == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to adjust the price for list item '{}' for exchange '{}' but the server has no knowledge "
              + "of it. Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), listItems));
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        if (forSaleItem == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
              + "server console for more details!"));
            this.logger.error("Player '{}' attempted to adjust the price for list item '{}' for exchange '{}' but the server has no knowledge "
              + "of it being for sale. Syncing list items...", player.getName(), listItemRecNo, id);
            this.network.sendTo(player, new ClientboundListItemsResponsePacket(axs.getId(), listItems));
            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(),
              axs.getForSaleItemsFor(player.getUniqueId()).orElse(null), null));
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
                        this.logger.error("Player '{}' attempted to adjust the price for list item '{}' for exchange '{}' to the database but it "
                          + "failed. Discarding changes...", player.getName(), listItemRecNo, id);
                        return;
                    }

                    ExchangeQueries
                      .createUpdateListItemLastKnownPrice(found.getRecord(), forSaleItem.getPrice())
                      .build(context)
                      .execute();

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            found.setLastKnownPrice(forSaleItem.getPrice());

                            ((BasicForSaleItem) forSaleItem).setPrice(price);

                            this.network.sendTo(player, new ClientboundListItemsSaleStatusPacket(axs.getId(),
                                axs.getForSaleItemsFor(player.getUniqueId()).orElse(null), Lists.newArrayList(found)));
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

        final Exchange axs = this.getExchange(id).orElse(null);
        if (axs == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' but the server has no knowledge of it. Syncing exchange "
                + "registry...", player.getName(), id);
            this.syncExchangeRegistryTo(player);
            return;
        }

        final EconomyService economyService = this.serviceManager.provide(EconomyService.class).orElse(null);
        if (economyService == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' but the economy service no longer exists. This is a "
                + "critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        final UniqueAccount buyerAccount = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (buyerAccount == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' but the economy service returned no account for them. "
                + "This is a critical error that should be reported to your economy plugin ASAP.", player.getName(), id);
            return;
        }

        ListItem found = null;

        for (final Map.Entry<UUID, List<ListItem>> kv : axs.getListItems().entrySet()) {
            final ListItem listItem = kv.getValue().stream().filter(item -> item.getRecord() == listItemRecNo).findAny().orElse(null);
            if (listItem != null) {
                found = listItem;
                break;
            }
        }

        if (found == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("This item is no longer for sale!"));
            this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
            return;
        }

        final UUID seller = found.getSeller();
        final UUID buyer = player.getUniqueId();

        if (buyer.equals(seller)) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("You cannot purchase your own items."));
            return;
        }

        final UniqueAccount sellerAccount = economyService.getOrCreateAccount(seller).orElse(null);
        if (sellerAccount == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("Critical error encountered, check the "
                + "server console for more details!"));
            this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' but the economy service returned no account for seller"
                + " '{}'. This is a critical error that should be reported to your economy plugin ASAP.", player.getName(), id, seller);
            return;
        }

        final ForSaleItem forSaleItem = found.getForSaleItem().orElse(null);

        if (forSaleItem == null) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("This item is no longer for sale!"));
            this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
            return;
        }

        if (found.getQuantity() < quantity) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("There is not enough quantity left to "
                + "purchase this item!"));
            this.network.sendTo(player, new ClientboundForSaleFilterRequestPacket(axs.getId()));
            return;
        }

        final BigDecimal balance = buyerAccount.getBalance(economyService.getDefaultCurrency());
        final BigDecimal price = forSaleItem.getPrice();
        final double total = price.doubleValue() * quantity;

        if (total > balance.doubleValue()) {
            final String formattedTotal = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total);
            final String formattedBalance = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(balance.doubleValue());
            final String formattedDifference = FeatureConstants.CURRENCY_DECIMAL_FORMAT.format(total - balance.doubleValue());
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"),
                    Text.of("You attempted to purchase items totaling to ", TextColors.RED, formattedTotal, TextColors.RESET, " while you only have ",
                            TextColors.GREEN, formattedBalance, TextColors.RESET, ".", Text.NEW_LINE, Text.NEW_LINE, "You need ",
                        TextColors.LIGHT_PURPLE, formattedDifference, TextColors.RESET, " more!"));
            return;
        }

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        final IItemHandler inventory = serverPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);

        final ListItem copyStack = found.copy();
        copyStack.setQuantity(quantity);

        final ItemStack simulatedResultStack = ItemHandlerHelper.insertItemStacked(inventory, copyStack.asRealStack(), true);
        if (!simulatedResultStack.isEmpty()) {
            this.notificationManager.sendWindowMessage(player, Text.of("Exchange"), Text.of("You lack sufficient inventory space to "
              + "purchase these item(s)!"));
            return;
        }

        final int originalQuantity = found.getQuantity();
        final int quantityRemaining = originalQuantity - (quantity - simulatedResultStack.getCount());
        final int forSaleItemRecord = forSaleItem.getRecord();

        // Charge the buyer
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(axs);

            buyerAccount.transfer(sellerAccount, economyService.getDefaultCurrency(), price, frame.getCurrentCause());
        }

        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {

                    // Update listed quantity
                    int result = ExchangeQueries
                        .createUpdateListItemQuantity(listItemRecNo, quantityRemaining, true)
                        .build(context)
                        .execute();
                    if (result == 0) {
                        this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' to the database but it failed. Discarding "
                          + "changes...", player.getName(), id);
                        return;
                    }

                    if (quantityRemaining == 0) {
                        result = ExchangeQueries
                            .createUpdateForSaleItemIsHidden(forSaleItemRecord, true)
                            .build(context)
                            .execute();

                        if (result == 0) {
                            this.logger.error("Player '{}' attempted to make a transaction in-which was the entire listing for exchange '{}' to the "
                              + "database but it failed. Discarding changes...", player.getName(), id);

                            ExchangeQueries
                              .createUpdateListItemQuantity(listItemRecNo, originalQuantity, true)
                              .build(context)
                              .execute();
                            return;
                        }
                    }

                    // Issue a transaction
                    result = ExchangeQueries
                        .createInsertTransaction(Instant.now(), forSaleItemRecord, buyer, price, quantity)
                        .build(context)
                        .execute();

                    if (result == 0) {

                        this.logger.error("Player '{}' attempted to make a transaction for exchange '{}' to the database but it failed. Discarding "
                          + "changes...", player.getName(), id);

                        ExchangeQueries
                          .createUpdateListItemQuantity(listItemRecNo, originalQuantity, false)
                          .build(context)
                          .execute();

                        ExchangeQueries
                          .createUpdateForSaleItemIsHidden(forSaleItemRecord, false)
                          .build(context)
                          .execute();

                        return;
                    }

                    final Results listItemResults = ExchangeQueries
                        .createFetchListItemsAndDataFor(seller, false)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();

                    final Results forSaleItemResults = ExchangeQueries
                        .createFetchForSaleItemsFor(seller, false)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final List<ListItem> listItems = new ArrayList<>();
                            listItemResults.forEach(r -> listItems.addAll(this.parseListItemsFrom(r)));

                            final List<ForSaleItem> forSaleItems = new ArrayList<>();
                            forSaleItemResults.forEach(r -> forSaleItems.addAll(this.parseForSaleItemsFrom(listItems, r)));

                            axs.putListItemsFor(seller, listItems);
                            axs.putForSaleItemsFor(seller, forSaleItems);

                            final ItemStack resultStack = ItemHandlerHelper.insertItemStacked(inventory, copyStack.asRealStack(), false);
                            if (!resultStack.isEmpty()) {
                                // TODO Inventory changed awaiting DB and now we're full...could drop it on the ground? It is an off-case
                            }

                            // If the seller is online, send them a list item update
                            final Player sellerPlayer = Sponge.getServer().getPlayer(seller).orElse(null);
                            if (sellerPlayer != null) {
                                this.network.sendTo(sellerPlayer, new ClientboundListItemsResponsePacket(axs.getId(), listItems));
                                this.network.sendTo(sellerPlayer, new ClientboundListItemsSaleStatusPacket(axs.getId(), forSaleItems, null));
                            }

                            this.network.sendToAll(new ClientboundForSaleFilterRequestPacket(axs.getId()));

                            Sponge.getServer().getPlayer(buyer)
                                .ifPresent(buyerPlayer -> this.network.sendTo(buyerPlayer, new ClientboundTransactionCompletePacket()));
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
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

    private List<ListItem> parseListItemsFrom(final Result<Record> result) {
        final List<ListItem> items = new ArrayList<>();

        for (final Record record : result) {
            final Integer recNo = record.getValue(AxsListItem.AXS_LIST_ITEM.REC_NO);

            final String domain = record.getValue(AxsListItem.AXS_LIST_ITEM.DOMAIN);
            final String path = record.getValue(AxsListItem.AXS_LIST_ITEM.PATH);

            final ResourceLocation location = new ResourceLocation(domain, path);

            final Item item = ForgeRegistries.ITEMS.getValue(location);

            if (item == null) {
                this.logger.error("Unknown item for domain '{}' and path '{}' found at record number '{}'. Skipping... (Did you remove a mod?)",
                    domain, path, recNo);
                continue;
            }

            final Timestamp created = record.getValue(AxsListItem.AXS_LIST_ITEM.CREATED);
            final UUID seller = SerializationUtil.uniqueIdFromBytes(record.getValue(AxsListItem.AXS_LIST_ITEM.SELLER));
            final Integer quantity = record.getValue(AxsListItem.AXS_LIST_ITEM.QUANTITY);
            final Integer metadata = record.getValue(AxsListItem.AXS_LIST_ITEM.METADATA);
            final Integer index = record.getValue(AxsListItem.AXS_LIST_ITEM.INDEX);
            final BigDecimal lastKnownPrice = record.getValue(AxsListItem.AXS_LIST_ITEM.LAST_KNOWN_PRICE);
            final byte[] compoundData = record.getValue(AxsListItemData.AXS_LIST_ITEM_DATA.DATA);

            NBTTagCompound compound = null;
            if (compoundData != null) {
                try {
                    compound = SerializationUtil.compoundFromBytes(compoundData);
                } catch (IOException e) {
                    this.logger.error("Malformed item data found at record number '{}'. Skipping...", recNo);
                    continue;
                }
            }

            final BasicListItem basicListItem = new BasicListItem(recNo, created.toInstant(), seller, item, quantity, metadata, index,
              lastKnownPrice, compound);

            basicListItem.syncSellerNameToUniqueId();

            items.add(basicListItem);
        }

        return items;
    }

    private List<ForSaleItem> parseForSaleItemsFrom(final List<ListItem> listItems, final Result<Record> result) {
        final List<ForSaleItem> forSaleItems = new ArrayList<>();

        result.forEach(record -> {
            final Integer recNo = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.REC_NO);
            final Integer itemRecNo = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.LIST_ITEM);

            final ListItem found = listItems
                .stream()
                .filter(item -> item.getRecord() == itemRecNo)
                .findAny()
                .orElse(null);

            if (found == null) {
                this.logger.error("A for sale listing at record number '{}' is being loaded but the listing no longer exists. Somehow an entity has"
                    + " tampered with the structure of the database. Report to an AlmuraDev developer ASAP.", recNo);
            } else {
                final Timestamp created = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.CREATED);
                final BigDecimal price = record.getValue(AxsForSaleItem.AXS_FOR_SALE_ITEM.PRICE);

                final BasicForSaleItem basicForSaleItem = new BasicForSaleItem((BasicListItem) found, recNo, created.toInstant(), price);

                forSaleItems.add(basicForSaleItem);
            }
        });

        return forSaleItems;
    }

    private int getListingsLimit(final Player player) {
        // TODO Do this better when released standalone
        int slots = 0;

        if (player.hasPermission("almura.exchange.admin")) {
            return 1000;
        }

        if (player.hasPermission("almura.exchange.slots.500")) {
            return 500;
        }

        if (player.hasPermission("almura.exchange.slots.100")) {
            return 100;
        }

        if (player.hasPermission("almura.exchange.slots.50")) {
            return 50;
        }

        if (player.hasPermission("almura.exchange.slots.25")) {
            return 25;
        }

        if (player.hasPermission("almura.exchange.slots.10")) {
            return 10;
        }

        return slots;
    }

    private void printStacksToConsole(final List<VanillaStack> stacks) {
        // TODO
    }
}
