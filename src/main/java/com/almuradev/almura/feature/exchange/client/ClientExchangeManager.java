/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.exchange.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.almuradev.almura.feature.exchange.Exchange;
import com.almuradev.almura.feature.exchange.ExchangeConstants;
import com.almuradev.almura.feature.exchange.ExchangeGuiType;
import com.almuradev.almura.feature.exchange.ExchangeModifyType;
import com.almuradev.almura.feature.exchange.InventoryAction;
import com.almuradev.almura.feature.exchange.ListStatusType;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeManagementScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeOfferScreen;
import com.almuradev.almura.feature.exchange.client.gui.ExchangeScreen;
import com.almuradev.almura.feature.exchange.network.ClientboundListItemsSaleStatusPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundExchangeGuiRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundForSaleFilterResponsePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundListItemsRequestPacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyExchangePacket;
import com.almuradev.almura.feature.exchange.network.ServerboundModifyForSaleItemListStatusRequestPacket;
import com.almuradev.almura.shared.feature.store.listing.ForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.ListItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicForSaleItem;
import com.almuradev.almura.shared.feature.store.listing.basic.BasicListItem;
import com.almuradev.almura.shared.item.BasicVanillaStack;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SideOnly(Side.CLIENT)
public final class ClientExchangeManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;

    private final List<Exchange> exchanges = new ArrayList<>();

    @Inject
    public ClientExchangeManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    @SubscribeEvent
    public void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.exchanges.clear();
    }

    @Nullable
    public Exchange getExchange(final String id) {
        checkNotNull(id);

        return this.exchanges.stream().filter(axs -> axs.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public List<Exchange> getExchanges() {
        return this.exchanges;
    }

    public void putExchanges(@Nullable final Set<Exchange> exchanges) {
        this.exchanges.clear();

        if (exchanges != null) {
            this.exchanges.addAll(exchanges);
        }
    }

    public void requestExchangeManageGui() {
        this.network.sendToServer(new ServerboundExchangeGuiRequestPacket(ExchangeGuiType.MANAGE, null));
    }

    public void requestExchangeSpecificGui(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundExchangeGuiRequestPacket(ExchangeGuiType.SPECIFIC, id));
    }

    public void requestExchangeSpecificOfferGui(final String id) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundExchangeGuiRequestPacket(ExchangeGuiType.SPECIFIC_OFFER, id));
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

    public void queryForSaleItemsFor(final String id, @Nullable final String filter) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundForSaleFilterResponsePacket(id, filter));
    }

    public void updateListItems(final String id, final List<InventoryAction> actions) {
        checkNotNull(id);

        this.network.sendToServer(new ServerboundListItemsRequestPacket(id, actions));
    }

    public void purchase(final String id, final int listItemRecNo, final int quantity) {
        checkNotNull(id);
        checkState(listItemRecNo >= 0);
        checkState(quantity >= 1);

        // TODO Grinch
        // TODO Send up the transaction here
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
        checkState(limit >= ExchangeConstants.UNLIMITED);
        final Exchange axs = this.getExchange(id);

        if (axs != null) {
            new ExchangeScreen(axs, limit).display();
        }
    }

    public void handleExchangeSpecificOffer(final String id) {
        checkNotNull(id);

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            // TODO Close main screen
            return;
        }

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof ExchangeScreen)) {
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

            if (axs != ((ExchangeScreen) currentScreen).getAxs()) {
                return;
            }

            axs.putListItemsFor(Minecraft.getMinecraft().player.getUniqueID(), listItems);

            ((ExchangeScreen) currentScreen).refreshListItems();
        }
    }

    public void handleListItemsSaleStatus(final String id,
        @Nullable final List<ClientboundListItemsSaleStatusPacket.ForSaleItemCandidate> itemCandidates) {
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
        listItems.forEach(item -> ((BasicListItem) item).setForSaleItem(null));

        if (itemCandidates == null) {
            // TODO Grinch, you got nothing back
        } else {
            for (final ClientboundListItemsSaleStatusPacket.ForSaleItemCandidate itemCandidate : itemCandidates) {
                listItems.stream().filter(item -> item.getRecord() == itemCandidate.listItemRecNo).findAny()
                    .ifPresent(listItem -> ((BasicListItem) listItem)
                        .setForSaleItem(new BasicForSaleItem((BasicListItem) listItem, itemCandidate.forSaleItemRecNo,
                            itemCandidate.created, itemCandidate.quantityRemaining, itemCandidate.price)));
            }
        }
    }

    public void handleForSaleFilter(final String id) {
        checkNotNull(id);

        // TODO Grinch
        // TODO Clear out for sale listings, call updateForSaleItemsFor with the filter

        // TODO TEST CODE
        this.network.sendToServer(new ServerboundForSaleFilterResponsePacket(id, ""));
    }

    public void handleForSaleItems(final String id, @Nullable final List<ForSaleItem> forSaleItems) {
        checkNotNull(id);

        final Exchange axs = this.getExchange(id);
        if (axs == null) {
            return;
        }

        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen instanceof ExchangeScreen) {
            if (axs != ((ExchangeScreen) currentScreen).getAxs()) {
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
            ((ExchangeScreen) currentScreen).refreshForSaleItemResults();
        }
    }
}
