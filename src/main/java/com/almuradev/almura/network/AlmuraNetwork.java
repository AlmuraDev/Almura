/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.network;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages;
import com.almuradev.almura.feature.accessory.server.ServerAccessories;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import com.almuradev.almura.feature.bank.network.BankMessages;
import com.almuradev.almura.feature.bank.server.ServerBank;
import com.almuradev.almura.feature.death.network.DeathMessages;
import com.almuradev.almura.feature.death.server.ServerDeath;
import com.almuradev.almura.feature.exchange.network.ExchangeMessages;
import com.almuradev.almura.feature.exchange.server.ServerExchange;
import com.almuradev.almura.feature.guide.network.GuideMessages;
import com.almuradev.almura.feature.guide.server.ServerGuide;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook;
import com.almuradev.almura.feature.hud.network.ClientboundBiomeNames;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerMoney;
import com.almuradev.almura.feature.hud.network.ClientboundPlayerWorlds;
import com.almuradev.almura.feature.hud.network.ClientboundServerTps;
import com.almuradev.almura.feature.hud.network.ClientboundWorldName;
import com.almuradev.almura.feature.membership.network.MembershipMessages;
import com.almuradev.almura.feature.membership.server.ServerMemberships;
import com.almuradev.almura.feature.menu.ingame.network.FeaturesMessages;
import com.almuradev.almura.feature.menu.ingame.server.ServerFeatures;
import com.almuradev.almura.feature.nick.network.NickMessages;
import com.almuradev.almura.feature.nick.server.ServerNicks;
import com.almuradev.almura.feature.notification.network.ClientboundPopupNotification;
import com.almuradev.almura.feature.skills.network.ClientboundSkills;
import com.almuradev.almura.feature.store.network.StoreMessages;
import com.almuradev.almura.feature.store.server.ServerStore;
import com.almuradev.almura.feature.title.network.TitleMessages;
import com.almuradev.almura.feature.title.server.ServerTitles;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Almura's messages between the server and the client, as NeoForge payloads under the {@code almura} namespace.
 *
 * <p>Registered on both sides. Messages to the client are handled by the client alone (see
 * {@code com.almuradev.almura.network.client.ClientPayloads}), so none of their handling is loaded on a dedicated server. The client and the
 * server must both have Almura at the same {@link #VERSION}, as 1.12 required its client; a player without it can't join.</p>
 */
public final class AlmuraNetwork {

    /** Raised whenever a message changes in a way older clients or servers can't read. */
    public static final String VERSION = "1";

    private AlmuraNetwork() {
    }

    public static void register(final IEventBus modBus) {
        modBus.addListener(AlmuraNetwork::onRegisterPayloads);
    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(final String path) {
        return new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Almura.ID, path));
    }

    private static void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Almura.ID).versioned(VERSION);
        // To the client
        registrar.playToClient(DeathMessages.ClientboundDied.TYPE, DeathMessages.ClientboundDied.CODEC);
        registrar.playToClient(ClientboundPopupNotification.TYPE, ClientboundPopupNotification.CODEC);
        registrar.playToClient(ClientboundWorldName.TYPE, ClientboundWorldName.CODEC);
        registrar.playToClient(ClientboundServerTps.TYPE, ClientboundServerTps.CODEC);
        registrar.playToClient(ClientboundPlayerWorlds.TYPE, ClientboundPlayerWorlds.CODEC);
        registrar.playToClient(ClientboundPlayerMoney.TYPE, ClientboundPlayerMoney.CODEC);
        registrar.playToClient(ClientboundBiomeNames.TYPE, ClientboundBiomeNames.CODEC);
        registrar.playToClient(ClientboundAlmanacField.TYPE, ClientboundAlmanacField.CODEC);
        registrar.playToClient(ClientboundAlmanacCrops.TYPE, ClientboundAlmanacCrops.CODEC);
        registrar.playToClient(GuideMessages.ClientboundListing.TYPE, GuideMessages.ClientboundListing.CODEC);
        registrar.playToClient(GuideMessages.ClientboundPage.TYPE, GuideMessages.ClientboundPage.CODEC);
        registrar.playToClient(GuideMessages.ClientboundChangeResult.TYPE, GuideMessages.ClientboundChangeResult.CODEC);
        registrar.playToClient(BankMessages.ClientboundState.TYPE, BankMessages.ClientboundState.CODEC);
        registrar.playToClient(BankMessages.ClientboundHistory.TYPE, BankMessages.ClientboundHistory.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundRegistry.TYPE, ExchangeMessages.ClientboundRegistry.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundOpen.TYPE, ExchangeMessages.ClientboundOpen.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundListItems.TYPE, ExchangeMessages.ClientboundListItems.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundForSale.TYPE, ExchangeMessages.ClientboundForSale.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundRefresh.TYPE, ExchangeMessages.ClientboundRefresh.CODEC);
        registrar.playToClient(ExchangeMessages.ClientboundTransactionComplete.TYPE, ExchangeMessages.ClientboundTransactionComplete.CODEC);
        registrar.playToClient(StoreMessages.ClientboundRegistry.TYPE, StoreMessages.ClientboundRegistry.CODEC);
        registrar.playToClient(StoreMessages.ClientboundOpen.TYPE, StoreMessages.ClientboundOpen.CODEC);
        registrar.playToClient(StoreMessages.ClientboundItems.TYPE, StoreMessages.ClientboundItems.CODEC);
        registrar.playToClient(FeaturesMessages.ClientboundOpen.TYPE, FeaturesMessages.ClientboundOpen.CODEC);
        registrar.playToClient(ClientboundSkills.TYPE, ClientboundSkills.CODEC);
        registrar.playToClient(ClientboundHandbook.TYPE, ClientboundHandbook.CODEC);
        registrar.playToClient(TitleMessages.ClientboundRegistry.TYPE, TitleMessages.ClientboundRegistry.CODEC);
        registrar.playToClient(TitleMessages.ClientboundAvailable.TYPE, TitleMessages.ClientboundAvailable.CODEC);
        registrar.playToClient(TitleMessages.ClientboundSelected.TYPE, TitleMessages.ClientboundSelected.CODEC);
        registrar.playToClient(TitleMessages.ClientboundOpen.TYPE, TitleMessages.ClientboundOpen.CODEC);
        registrar.playToClient(NickMessages.ClientboundNames.TYPE, NickMessages.ClientboundNames.CODEC);
        registrar.playToClient(NickMessages.ClientboundOpen.TYPE, NickMessages.ClientboundOpen.CODEC);
        registrar.playToClient(MembershipMessages.ClientboundOpen.TYPE, MembershipMessages.ClientboundOpen.CODEC);
        registrar.playToClient(MembershipMessages.ClientboundApplied.TYPE, MembershipMessages.ClientboundApplied.CODEC);
        registrar.playToClient(AccessoryMessages.ClientboundOpen.TYPE, AccessoryMessages.ClientboundOpen.CODEC);
        registrar.playToClient(AccessoryMessages.ClientboundWorn.TYPE, AccessoryMessages.ClientboundWorn.CODEC);
        // To the server
        registrar.playToServer(DeathMessages.ServerboundRevive.TYPE, DeathMessages.ServerboundRevive.CODEC, (payload, context) ->
            ServerDeath.revive((ServerPlayer) context.player()));
        registrar.playToServer(GuideMessages.ServerboundOpen.TYPE, GuideMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerGuide.open((ServerPlayer) context.player()));
        registrar.playToServer(GuideMessages.ServerboundPage.TYPE, GuideMessages.ServerboundPage.CODEC, (payload, context) ->
            ServerGuide.openPage((ServerPlayer) context.player(), payload.id()));
        registrar.playToServer(GuideMessages.ServerboundChange.TYPE, GuideMessages.ServerboundChange.CODEC, (payload, context) ->
            ServerGuide.change((ServerPlayer) context.player(), payload));
        registrar.playToServer(GuideMessages.ServerboundMove.TYPE, GuideMessages.ServerboundMove.CODEC, (payload, context) ->
            ServerGuide.move((ServerPlayer) context.player(), payload.id(), payload.up()));
        registrar.playToServer(BankMessages.ServerboundAction.TYPE, BankMessages.ServerboundAction.CODEC, (payload, context) ->
            ServerBank.handle((ServerPlayer) context.player(), payload));
        registrar.playToServer(ExchangeMessages.ServerboundOpen.TYPE, ExchangeMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerExchange.open((ServerPlayer) context.player(), payload.id()));
        registrar.playToServer(ExchangeMessages.ServerboundOfferRequest.TYPE, ExchangeMessages.ServerboundOfferRequest.CODEC, (payload, context) ->
            ServerExchange.openOffer((ServerPlayer) context.player(), payload.id()));
        registrar.playToServer(ExchangeMessages.ServerboundModify.TYPE, ExchangeMessages.ServerboundModify.CODEC, (payload, context) ->
            ServerExchange.modify((ServerPlayer) context.player(), payload));
        registrar.playToServer(ExchangeMessages.ServerboundQuery.TYPE, ExchangeMessages.ServerboundQuery.CODEC, (payload, context) ->
            ServerExchange.query((ServerPlayer) context.player(), payload));
        registrar.playToServer(ExchangeMessages.ServerboundMoveItems.TYPE, ExchangeMessages.ServerboundMoveItems.CODEC, (payload, context) ->
            ServerExchange.moveItems((ServerPlayer) context.player(), payload));
        registrar.playToServer(ExchangeMessages.ServerboundListStatus.TYPE, ExchangeMessages.ServerboundListStatus.CODEC, (payload, context) ->
            ServerExchange.listStatus((ServerPlayer) context.player(), payload));
        registrar.playToServer(ExchangeMessages.ServerboundBuy.TYPE, ExchangeMessages.ServerboundBuy.CODEC, (payload, context) ->
            ServerExchange.buy((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundOpen.TYPE, StoreMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerStore.handleOpen((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundModifyStore.TYPE, StoreMessages.ServerboundModifyStore.CODEC, (payload, context) ->
            ServerStore.handleModifyStore((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundListItem.TYPE, StoreMessages.ServerboundListItem.CODEC, (payload, context) ->
            ServerStore.handleListItem((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundModifyItems.TYPE, StoreMessages.ServerboundModifyItems.CODEC, (payload, context) ->
            ServerStore.handleModifyItems((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundDelistItems.TYPE, StoreMessages.ServerboundDelistItems.CODEC, (payload, context) ->
            ServerStore.handleDelistItems((ServerPlayer) context.player(), payload));
        registrar.playToServer(StoreMessages.ServerboundTransaction.TYPE, StoreMessages.ServerboundTransaction.CODEC, (payload, context) ->
            ServerStore.handleTransaction((ServerPlayer) context.player(), payload));
        registrar.playToServer(FeaturesMessages.ServerboundOpen.TYPE, FeaturesMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerFeatures.open((ServerPlayer) context.player()));
        registrar.playToServer(TitleMessages.ServerboundOpen.TYPE, TitleMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerTitles.open((ServerPlayer) context.player()));
        registrar.playToServer(TitleMessages.ServerboundSelect.TYPE, TitleMessages.ServerboundSelect.CODEC, (payload, context) ->
            ServerTitles.select((ServerPlayer) context.player(), payload.id()));
        registrar.playToServer(TitleMessages.ServerboundModify.TYPE, TitleMessages.ServerboundModify.CODEC, (payload, context) ->
            ServerTitles.modify((ServerPlayer) context.player(), payload));
        registrar.playToServer(NickMessages.ServerboundOpen.TYPE, NickMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerNicks.open((ServerPlayer) context.player()));
        registrar.playToServer(NickMessages.ServerboundChange.TYPE, NickMessages.ServerboundChange.CODEC, (payload, context) ->
            ServerNicks.change((ServerPlayer) context.player(), payload.nickname()));
        registrar.playToServer(MembershipMessages.ServerboundOpen.TYPE, MembershipMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerMemberships.open((ServerPlayer) context.player()));
        registrar.playToServer(MembershipMessages.ServerboundUpgrade.TYPE, MembershipMessages.ServerboundUpgrade.CODEC, (payload, context) ->
            ServerMemberships.upgrade((ServerPlayer) context.player(), payload.level(), payload.bySkills()));
        registrar.playToServer(AccessoryMessages.ServerboundOpen.TYPE, AccessoryMessages.ServerboundOpen.CODEC, (payload, context) ->
            ServerAccessories.open((ServerPlayer) context.player()));
        registrar.playToServer(AccessoryMessages.ServerboundSelect.TYPE, AccessoryMessages.ServerboundSelect.CODEC, (payload, context) ->
            ServerAccessories.select((ServerPlayer) context.player(), payload.accessory(), payload.variant()));
    }
}
