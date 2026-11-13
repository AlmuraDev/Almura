/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.client;

import com.almuradev.almura.feature.bank.BankAccountView;
import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.client.gui.BankScreen;
import com.almuradev.almura.feature.bank.network.ClientboundBankHistoryPacket;
import com.almuradev.almura.feature.bank.network.ClientboundBankStatePacket;
import com.almuradev.almura.feature.bank.network.ServerboundBankActionPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The client's view of the bank: the latest accounts and balances the server sent, and requests to it.
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientBankManager implements Witness {

    private final ChannelBinding.IndexedMessageChannel network;

    private boolean admin;
    private int limit;
    private BigDecimal wallet = BigDecimal.ZERO;
    private List<BankAccountView> accounts = new ArrayList<>();

    @Inject
    public ClientBankManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public int getLimit() {
        return this.limit;
    }

    public BigDecimal getWallet() {
        return this.wallet;
    }

    public List<BankAccountView> getAccounts() {
        return Collections.unmodifiableList(this.accounts);
    }

    public Optional<BankAccountView> getAccount(final int recNo) {
        return this.accounts.stream().filter(account -> account.getRecNo() == recNo).findFirst();
    }

    public long getOwnedCount() {
        return this.accounts.stream().filter(BankAccountView::isMine).count();
    }

    public void handleState(final ClientboundBankStatePacket message) {
        this.admin = message.admin;
        this.limit = message.limit;
        this.wallet = message.wallet;
        this.accounts = new ArrayList<>(message.accounts);

        if (message.open) {
            new BankScreen().display();
            return;
        }
        this.getOpenScreen().ifPresent(BankScreen::refresh);
        final GuiScreen current = Minecraft.getMinecraft().currentScreen;
        if (current instanceof BankScreen.Child) {
            ((BankScreen.Child) current).onBankRefresh();
        }
    }

    public void handleHistory(final ClientboundBankHistoryPacket message) {
        this.getOpenScreen().ifPresent(screen -> screen.showHistory(message.account, message.title, message.entries));
    }

    /**
     * @return The bank window, if it is open (also behind one of its dialogs)
     */
    private Optional<BankScreen> getOpenScreen() {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof BasicMessageBox.MessageBoxDialogScreen) {
            screen = ((BasicMessageBox.MessageBoxDialogScreen) screen).getParent();
        }
        if (screen instanceof BankScreen.Child) {
            return Optional.of(((BankScreen.Child) screen).getBankScreen());
        }
        return screen instanceof BankScreen ? Optional.of((BankScreen) screen) : Optional.empty();
    }

    public void request(final BankAction action, final int account) {
        this.request(action, account, "", BigDecimal.ZERO, "", false);
    }

    public void request(final BankAction action, final int account, final String text) {
        this.request(action, account, text, BigDecimal.ZERO, "", false);
    }

    public void request(final BankAction action, final int account, final BigDecimal amount) {
        this.request(action, account, "", amount, "", false);
    }

    public void request(final BankAction action, final int account, final String text, final BigDecimal amount, final String member,
        final boolean flag) {
        this.network.sendToServer(new ServerboundBankActionPacket(action, account, text, amount, member, flag));
    }
}
