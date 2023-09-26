/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.exchange.*;
import com.almuradev.almura.feature.exchange.basic.listing.BasicForSaleItem;
import com.almuradev.almura.feature.exchange.basic.listing.BasicListItem;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeManagementScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeOfferScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen;
import com.almuradev.almura.feature.exchange.listing.ForSaleItem;
import com.almuradev.almura.feature.exchange.listing.ListItem;
import com.almuradev.almura.feature.exchange.network.*;
import com.almuradev.almura.shared.feature.FeatureConstants;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Singleton
@SideOnly(Side.CLIENT)
public final class ClientExchangeManager implements Witness {

    @Nullable private static final String defaultFilter = null;
    private static final String defaultSort = ExchangeModule.ID + "_price" + FeatureConstants.EQUALITY + "asc" + FeatureConstants.DELIMITER;
    private static final int defaultSkip = 0;
    private static final int defaultLimit = 10;

    private final ChannelBinding.IndexedMessageChannel network;
    private final List<Exchange> exchanges = new ArrayList<>();

    @Nullable private String currentFilter = defaultFilter;
    @Nullable private String currentSort = defaultSort;
    private int currentSkip = defaultSkip;
    private int currentLimit = defaultLimit;

    @Inject
    public ClientExchangeManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {

        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.exchanges.clear();
    }

    @Nullable
    private Exchange getExchange(final String id) {
        checkNotNull(id);

        return this.exchanges.stream().filter(axs -> axs.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Exchange> getExchanges() {
        return this.exchanges;
    }

    private void putExchanges(@Nullable final Set<Exchange> exchanges) {
        this.exchanges.clear();

        if (exchanges != null) {
            this.exchanges.addAll(exchanges);
        }
    }

    public void requestExchangeManageGui() {
        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " exchange manage");
    }

    public void requestExchangeSpecificGui(final String id) {
        checkNotNull(id);

        Minecraft.getMinecraft().player.sendChatMessage("/" + Almura.ID + " exchange open " + id);
    }

    public void requestExchangeSpecificOfferGui(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundExchangeSpecificOfferRequestPacket(id));
    }

    public void addExchange(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyExchangePacket(ExchangeModifyType.ADD, id, name, permission, isHidden));
    }

    public void modifyExchange(final String id, final String name, final String permission, final boolean isHidden) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(permission);

        this.network.sendToServer(new ServerboundModifyExchangePacket(ExchangeModifyType.MODIFY, id, name, permission, isHidden));
    }

    public void deleteExchange(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundModifyExchangePacket(id));
    }

    public void queryForSaleItemsFor(final String id, @Nullable final String filter, @Nullable final String sort, final int skip, final int limit) {
        checkNotNull(id);

        this.currentFilter = filter;
        this.currentSort = sort;
        this.currentSkip = skip;
        this.currentLimit = limit;

        this.network.sendToServer(new ServerboundForSaleFilterResponsePacket(id, filter, sort, skip, limit));
    }

    public void queryForSaleItemsCached(final String id, final int skip, final int limit) {
        this.queryForSaleItemsFor(id, this.currentFilter, this.currentSort, skip, limit);
    }

    public void updateListItems(final String id, final List<InventoryAction> actions) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundListItemsRequestPacket(id, actions));
    }

    public void purchase(final String id, final int listItemRecNo, final int quantity) {
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkState(quantity >= 1);

        this.network.sendToServer(new ServerboundTransactionRequestPacket(id, listItemRecNo, quantity));
    }

    public void modifyListStatus(ListStatusType type, String id, int recordNo, @Nullable BigDecimal price) {
        this.network.sendToServer(new ServerboundModifyForSaleItemListStatusRequestPacket(type, id, recordNo, price));
    }

    /**
     * Handlers
     */

    public void handleExchangeRegistry(final Set<Exchange> exchanges) {
        this.putExchanges(exchanges);

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (currentScreen instanceof ExchangeManagementScreen) {
            ((ExchangeManagementScreen) currentScreen).refresh();
        }
    }

    public void handleExchangeManage() {
        new ExchangeManagementScreen().display();
    }

    public void handleExchangeSpecific(final String id, final int limit) {
        checkState(limit >= FeatureConstants.UNLIMITED);
        final Exchange axs = this.getExchange(id);

        if (axs != null) {
            this.clearFilterCache();
            new ExchangeScreen(axs, limit).display();
        }
    }

    public void handleExchangeSpecificOffer(final String id) {
        checkNotNull(id);

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof ExchangeScreen)) {
            return;
        }

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            ((ExchangeScreen) currentScreen).close();
            return;
        }

        final ExchangeScreen axsScreen = (ExchangeScreen) currentScreen;

        new ExchangeOfferScreen(axsScreen, axs, axsScreen.listItemList.getItems()
                .stream()
                .filter(item -> !item.getForSaleItem().isPresent())
                .map(item -> new BasicVanillaStack(item.asRealStack()))
                .collect(Collectors.toList()), axsScreen.limit)
            .display();
    }

    public void handleListItems(final String id, @Nullable final List<ListItem> listItems) {
        checkNotNull(id);

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            return;
        }

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof ExchangeScreen) {

            if (axs != ((ExchangeScreen) currentScreen).getExchange()) {
                return;
            }

            axs.putListItemsFor(Minecraft.getMinecraft().player.getUniqueID(), listItems);

            ((ExchangeScreen) currentScreen).refreshListItems();
        }
    }

    public void handleListItemsSaleStatus(final String id, @Nullable final List<ClientboundListItemsSaleStatusPacket.ListedItemUpdate>
        itemCandidates, @Nullable final List<ClientboundListItemsSaleStatusPacket.LastKnownPriceUpdate> lastKnowPriceItemCandidates) {
        checkNotNull(id);

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            return;
        }

        final List<ListItem> listItems = axs.getListItemsFor(Minecraft.getMinecraft().player.getUniqueID()).orElse(null);
        if (listItems == null || listItems.isEmpty()) {
            return;
        }

        // Null out all for sale items, our candidates will have what we currently have
        listItems.forEach(item -> item.setForSaleItem(null));

        if (itemCandidates != null) {
            for (final ClientboundListItemsSaleStatusPacket.ListedItemUpdate itemCandidate : itemCandidates) {
                listItems
                        .stream()
                        .filter(item -> item.getRecord() == itemCandidate.listItemRecNo)
                        .findAny()
                        .ifPresent(listItem -> listItem.setForSaleItem(new BasicForSaleItem((BasicListItem) listItem, itemCandidate.forSaleItemRecNo,
                            itemCandidate.created, itemCandidate.price)));
            }
        }

        if (lastKnowPriceItemCandidates != null) {
            for (final ClientboundListItemsSaleStatusPacket.LastKnownPriceUpdate lastKnownPriceItemCandidate : lastKnowPriceItemCandidates) {
                listItems
                        .stream()
                        .filter(item -> item.getRecord() == lastKnownPriceItemCandidate.listItemRecNo)
                        .findAny()
                        .ifPresent(listItem -> listItem.setLastKnownPrice(lastKnownPriceItemCandidate.lastKnownPrice));
            }
        }
    }

    public void handleForSaleFilter(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundForSaleFilterResponsePacket(id, this.currentFilter, this.currentSort, this.currentSkip,
                this.currentLimit));
    }

    public void handleForSaleItems(final String id, @Nullable final List<ForSaleItem> forSaleItems, int preLimitCount) {
        checkNotNull(id);

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            return;
        }

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof ExchangeScreen) {
            if (axs != ((ExchangeScreen) currentScreen).getExchange()) {
                return;
            }

            if (forSaleItems == null ) {
                axs.putForSaleItems(null);
            } else {
                axs.putForSaleItems(forSaleItems
                    .stream()
                    .collect(Collectors.groupingBy(k -> k.getListItem().getSeller(), ConcurrentHashMap::new,
                        Collectors.toCollection(ArrayList::new))));
            }
            ((ExchangeScreen) currentScreen).refreshForSaleItemResults(forSaleItems, preLimitCount);
        }
    }

    private void clearFilterCache() {
        this.currentFilter = defaultFilter;
        this.currentSort = defaultSort;
        this.currentSkip = defaultSkip;
        this.currentLimit = defaultLimit;
    }

    public void handleTransactionComplete() {
        final EntityPlayerSP player = Minecraft.getMinecraft().player;
        player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
