/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.client;

import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.BankMoney;
import com.almuradev.almura.feature.bank.network.BankMessages.AccountView;
import com.almuradev.almura.feature.bank.network.BankMessages.ClientboundHistory;
import com.almuradev.almura.feature.bank.network.BankMessages.ClientboundState;
import com.almuradev.almura.feature.bank.network.BankMessages.ServerboundAction;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * The bank on the client, as 3.5's {@code ClientBankManager}: the player's accounts and wallet as the server last sent them, the bank window
 * opened or refreshed with them, and the player's requests sent to the server.
 */
public final class ClientBank {

    private static boolean admin;
    private static int limit;
    private static BigDecimal wallet = BigDecimal.ZERO;
    private static List<AccountView> accounts = List.of();
    private static @Nullable BankScreen open;

    private ClientBank() {
    }

    static boolean admin() {
        return admin;
    }

    static int limit() {
        return limit;
    }

    static BigDecimal wallet() {
        return wallet;
    }

    static List<AccountView> accounts() {
        return accounts;
    }

    static Optional<AccountView> account(final int recNo) {
        return accounts.stream().filter(account -> account.recNo() == recNo).findFirst();
    }

    static long ownedCount() {
        return accounts.stream().filter(AccountView::mine).count();
    }

    /**
     * The player's accounts: opens the bank window on them, or refreshes the open one.
     */
    public static void onState(final ClientboundState state) {
        admin = state.admin();
        limit = state.limit();
        wallet = BankMoney.parse(state.wallet());
        accounts = List.copyOf(state.accounts());
        final BankScreen screen = shownScreen();
        if (screen != null) {
            screen.refresh();
            return;
        }
        if (state.open()) {
            open = new BankScreen();
            InGameScreens.open(open, Minecraft.getInstance().gui.screen());
        }
    }

    /**
     * A history: shown in the open bank window.
     */
    public static void onHistory(final ClientboundHistory history) {
        final BankScreen screen = shownScreen();
        if (screen != null) {
            screen.showHistory(history.account(), history.title(), history.entries());
        }
    }

    /**
     * Asks the server to open the bank (the Features menu's Banking button).
     */
    public static void requestOpen() {
        request(ServerboundAction.of(BankAction.OPEN, -1));
    }

    static void request(final BankAction action, final int account) {
        request(ServerboundAction.of(action, account));
    }

    static void request(final ServerboundAction action) {
        ClientPacketDistributor.sendToServer(action);
    }

    /**
     * The bank window, while it's the screen shown.
     */
    private static @Nullable BankScreen shownScreen() {
        if (open != null && !InGameScreens.isShowing(open)) {
            open = null;
        }
        return open;
    }
}
