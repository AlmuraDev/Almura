/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.client.gui;

import com.almuradev.almura.feature.bank.BankAccountView;
import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.BankHistoryEntry;
import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.feature.bank.client.ClientBankManager;
import com.almuradev.almura.feature.bank.BankMoney;
import com.google.inject.Inject;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.BasicScreen;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.BasicContainer;
import net.malisis.core.client.gui.component.container.BasicForm;
import net.malisis.core.client.gui.component.container.BasicList;
import net.malisis.core.client.gui.component.container.dialog.BasicMessageBox;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxButtons;
import net.malisis.core.client.gui.component.container.dialog.MessageBoxResult;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.BasicTextBox;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.button.builder.UIButtonBuilder;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.util.FontColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * The bank window: your accounts on the left; your wallet, the selected account (deposit, withdraw, coins, members) and its history on the
 * right. Admins can also look up any player's history by name.
 */
@SideOnly(Side.CLIENT)
public final class BankScreen extends BasicScreen {

    /**
     * A dialog opened over the bank window, which refreshes with it.
     */
    public interface Child {

        BankScreen getBankScreen();

        /**
         * New accounts and balances arrived (the bank window behind it has been refreshed).
         */
        default void onBankRefresh() {
        }
    }

    @Inject private static ClientBankManager manager;

    private static final int FORM_WIDTH = 430, FORM_HEIGHT = 270;
    private static final int LIST_WIDTH = 150, GAP = 6;
    // Wait a moment after a change before asking for the history again, so the new entry has been saved
    private static final int HISTORY_DELAY_TICKS = 10;
    private static final FontOptions SMALL_GRAY = FontOptions.builder().from(FontColors.GRAY_FO).scale(0.8f).build();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault());

    private BasicList<BankAccountView> accountList;
    private BasicList<BankHistoryEntry> historyList;
    private UILabel walletLabel, accountTitleLabel, accountInfoLabel, balanceLabel, historyTitleLabel, emptyLabel;
    private BasicTextBox amountTextBox, playerSearchTextBox;
    private UIButton buttonNew, buttonRename, buttonDelete, buttonDeposit, buttonWithdraw, buttonAccountCoins, buttonMembers;
    // The history shown: an account's (account), or a player's for admins (account -1)
    private int historyAccount = -1;
    private boolean adminHistory;
    private int historyRequestTicks = -1;

    @Override
    public void construct() {
        this.guiscreenBackground = false;
        Keyboard.enableRepeatEvents(true);

        final BasicForm form = new BasicForm(FORM_WIDTH, FORM_HEIGHT, I18n.format("almura.feature.bank.title"));
        form.setBackgroundAlpha(255);
        final int paddedWidth = BasicScreen.getPaddedWidth(form);
        final int paddedHeight = BasicScreen.getPaddedHeight(form);
        final int x = LIST_WIDTH + GAP;
        final int rightWidth = paddedWidth - x;

        // Accounts
        this.accountList = new BasicList<>(LIST_WIDTH, paddedHeight - 17);
        this.accountList.setBorder(FontColors.WHITE, 1, 185);
        this.accountList.setPadding(2, 2);
        this.accountList.setItemComponentSpacing(1);
        this.accountList.setItemComponentFactory(AccountComponent::new);
        this.accountList.setSelectConsumer(account -> this.onAccountSelected());

        this.buttonNew = new UIButtonBuilder()
            .text(TextFormatting.GREEN + "+")
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(0, 0)
            .onClick(() -> new BankNameScreen(this, I18n.format("almura.feature.bank.title.new_account"), "",
                name -> manager.request(BankAction.CREATE, -1, name)).display())
            .build("button.new");

        // + - ? like the Guide: open, delete, rename
        this.buttonDelete = new UIButtonBuilder()
            .text(TextFormatting.RED + "-")
            .tooltip(I18n.format("almura.feature.bank.button.delete"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(BasicScreen.getPaddedX(this.buttonNew, 2), 0)
            .onClick(this::delete)
            .build("button.delete");

        this.buttonRename = new UIButtonBuilder()
            .text(TextFormatting.YELLOW + "?")
            .tooltip(I18n.format("almura.feature.bank.button.rename"))
            .anchor(Anchor.BOTTOM | Anchor.LEFT)
            .position(BasicScreen.getPaddedX(this.buttonDelete, 2), 0)
            .onClick(this::rename)
            .build("button.rename");

        // Wallet
        this.walletLabel = this.label("", x, 4);
        final UIButton buttonWalletCoins = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.deposit_coins"))
            .tooltip(I18n.format("almura.feature.bank.tooltip.coins_to_wallet"))
            .width(80)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .position(0, 0)
            .onClick(() -> manager.request(BankAction.DEPOSIT_COINS, -1))
            .build("button.wallet_coins");

        // Selected account
        final BasicContainer<?> walletLine = this.line(x, 19, rightWidth);
        this.accountTitleLabel = this.label("", x, 24);
        this.accountInfoLabel = this.label("", x, 36);
        this.accountInfoLabel.setFontOptions(SMALL_GRAY);
        this.balanceLabel = this.label("", x, 47);

        final UILabel amountLabel = this.label(I18n.format("almura.feature.bank.label.amount"), x, 65);
        this.amountTextBox = new BasicTextBox("");
        this.amountTextBox.setSize(80, 0);
        this.amountTextBox.setPosition(BasicScreen.getPaddedX(amountLabel, 4), 63);
        this.amountTextBox.setAcceptsReturn(false);
        this.amountTextBox.setAcceptsTab(false);
        this.amountTextBox.setFilter(BankScreen::filterAmount);
        this.amountTextBox.setOnEnter(tb -> this.transfer(BankAction.DEPOSIT));

        this.buttonWithdraw = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.withdraw"))
            .width(58)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .position(0, 62)
            .onClick(() -> this.transfer(BankAction.WITHDRAW))
            .build("button.withdraw");

        this.buttonDeposit = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.deposit"))
            .width(58)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .position(-62, 62)
            .onClick(() -> this.transfer(BankAction.DEPOSIT))
            .build("button.deposit");

        this.buttonAccountCoins = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.deposit_coins"))
            .tooltip(I18n.format("almura.feature.bank.tooltip.coins_to_account"))
            .width(80)
            .position(x, 82)
            .onClick(this::depositCoins)
            .build("button.account_coins");

        this.buttonMembers = new UIButtonBuilder()
            .text(I18n.format("almura.feature.bank.button.members"))
            .width(80)
            .anchor(Anchor.TOP | Anchor.RIGHT)
            .position(0, 82)
            .onClick(this::members)
            .build("button.members");

        // History
        final BasicContainer<?> historyLine = this.line(x, 101, rightWidth);
        this.historyTitleLabel = this.label("", x, 107);

        form.add(this.accountList, this.buttonNew, this.buttonRename, this.buttonDelete, this.walletLabel, buttonWalletCoins, walletLine,
            this.accountTitleLabel, this.accountInfoLabel, this.balanceLabel, amountLabel, this.amountTextBox, this.buttonDeposit,
            this.buttonWithdraw, this.buttonAccountCoins, this.buttonMembers, historyLine, this.historyTitleLabel);

        int historyTop = 119;
        if (manager.isAdmin()) {
            // Admins: any player's history by name
            final UIButton buttonSearch = new UIButtonBuilder()
                .text(I18n.format("almura.feature.bank.button.search"))
                .width(40)
                .anchor(Anchor.TOP | Anchor.RIGHT)
                .position(0, 103)
                .onClick(this::searchPlayer)
                .build("button.search");
            this.playerSearchTextBox = new BasicTextBox("");
            this.playerSearchTextBox.setSize(90, 0);
            this.playerSearchTextBox.setPosition(-44, 104, Anchor.TOP | Anchor.RIGHT);
            this.playerSearchTextBox.setAcceptsReturn(false);
            this.playerSearchTextBox.setAcceptsTab(false);
            this.playerSearchTextBox.setTooltip(I18n.format("almura.feature.bank.tooltip.player_search"));
            this.playerSearchTextBox.setOnEnter(tb -> this.searchPlayer());
            form.add(this.playerSearchTextBox, buttonSearch);
            historyTop = 121;
        }

        this.historyList = new BasicList<>(rightWidth, paddedHeight - historyTop);
        this.historyList.setBorder(FontColors.WHITE, 1, 185);
        this.historyList.setPadding(2, 2);
        this.historyList.setItemComponentSpacing(1);
        this.historyList.setItemComponentFactory(HistoryComponent::new);
        this.historyList.setReadOnly(true);
        this.historyList.setPosition(x, historyTop);

        // Over the empty account list (the list replaces its own contents when it refreshes)
        this.emptyLabel = new UILabel(I18n.format("almura.feature.bank.text.no_accounts"), true);
        this.emptyLabel.setFontOptions(SMALL_GRAY);
        // Wrapped to the list's width
        this.emptyLabel.setSize(LIST_WIDTH - 10, 24);
        this.emptyLabel.setPosition(5, 5);

        form.add(this.historyList, this.emptyLabel);
        this.addToScreen(form);

        this.refresh();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, final float partialTicks) {
        // Drawn behind its dialogs: no hover or tooltip of its own (the tooltip of the button that opened them would stay under the mouse)
        if (Minecraft.getMinecraft().currentScreen != this) {
            this.tooltip = null;
            mouseX = -1;
            mouseY = -1;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.historyRequestTicks > 0 && --this.historyRequestTicks == 0) {
            this.requestHistory();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * New accounts and balances from the server: keeps the selected account (or picks the first) and asks for its history again.
     */
    public void refresh() {
        final BankAccountView selected = this.accountList.getSelectedItem();
        final int selectedRecNo = selected == null ? -1 : selected.getRecNo();

        this.accountList.setItems(manager.getAccounts());
        this.emptyLabel.setVisible(manager.getAccounts().isEmpty());
        final BankAccountView reselect = manager.getAccounts().stream()
            .filter(account -> account.getRecNo() == selectedRecNo)
            .findFirst()
            .orElse(manager.getAccounts().stream().findFirst().orElse(null));
        // Quietly, so the history shown (an admin's player search too) stays until the new one arrives
        final Consumer<BankAccountView> onSelect = this.accountList.getSelectConsumer();
        this.accountList.setSelectConsumer(null);
        this.accountList.setSelectedItem(reselect);
        this.accountList.setSelectConsumer(onSelect);
        if (reselect == null || reselect.getRecNo() != selectedRecNo) {
            this.onAccountSelected(); // A different account (the old one was deleted or left)
        }

        this.walletLabel.setText(TextFormatting.WHITE + I18n.format("almura.feature.bank.label.wallet") + " " + TextFormatting.GOLD
            + format(manager.getWallet()));
        this.updateDetails();

        if (!this.adminHistory) {
            this.historyRequestTicks = HISTORY_DELAY_TICKS;
        }
    }

    public void showHistory(final int account, final String title, final List<BankHistoryEntry> entries) {
        final BankAccountView selected = this.accountList.getSelectedItem();
        if (account >= 0 && (this.adminHistory || selected == null || selected.getRecNo() != account)) {
            return; // An answer for an account no longer shown
        }
        this.historyAccount = account;
        this.historyTitleLabel.setText(TextFormatting.WHITE + (account < 0
            ? I18n.format("almura.feature.bank.label.player_history", title)
            : I18n.format("almura.feature.bank.label.history", title)));
        this.historyList.setItems(entries);
    }

    private void onAccountSelected() {
        this.adminHistory = false;
        this.updateDetails();
        this.historyList.setItems(Collections.emptyList());
        this.historyRequestTicks = 1;
    }

    private void updateDetails() {
        final BankAccountView account = this.accountList.getSelectedItem();
        final boolean selected = account != null;

        if (selected) {
            this.accountTitleLabel.setText(TextFormatting.WHITE + "" + TextFormatting.BOLD + account.getName());
            this.accountInfoLabel.setText(account.isMine()
                ? account.isShared()
                    ? I18n.format("almura.feature.bank.text.shared_with", account.getMembers().size())
                    : I18n.format("almura.feature.bank.text.personal")
                : I18n.format("almura.feature.bank.text.shared_by", account.getOwnerName())
                    + (account.canWithdraw() ? "" : " " + I18n.format("almura.feature.bank.text.deposit_only")));
            this.balanceLabel.setText(TextFormatting.WHITE + I18n.format("almura.feature.bank.label.balance") + " " + TextFormatting.GOLD
                + format(account.getBalance()));
        } else {
            this.accountTitleLabel.setText(TextFormatting.GRAY + I18n.format("almura.feature.bank.text.no_account_selected"));
            this.accountInfoLabel.setText("");
            this.balanceLabel.setText("");
        }

        final long owned = manager.getOwnedCount();
        this.buttonNew.setEnabled(owned < manager.getLimit());
        this.buttonNew.setTooltip(I18n.format("almura.feature.bank.tooltip.new_account", owned, manager.getLimit()));
        this.buttonRename.setEnabled(selected && account.isMine());
        this.buttonDelete.setEnabled(selected && account.isMine());
        this.buttonDeposit.setEnabled(selected);
        this.buttonWithdraw.setEnabled(selected && account.canWithdraw());
        this.buttonAccountCoins.setEnabled(selected);
        this.buttonMembers.setEnabled(selected);
        // Members manage the account's members as its owner, or leave it
        this.buttonMembers.setText(selected && !account.isMine()
            ? I18n.format("almura.feature.bank.button.leave")
            : I18n.format("almura.feature.bank.button.members"));
        this.amountTextBox.setEditable(selected);
    }

    private void requestHistory() {
        final BankAccountView account = this.accountList.getSelectedItem();
        if (account != null && !this.adminHistory) {
            manager.request(BankAction.HISTORY, account.getRecNo());
        } else if (account == null && !this.adminHistory) {
            this.historyTitleLabel.setText("");
            this.historyList.setItems(Collections.emptyList());
        }
    }

    private void searchPlayer() {
        final String name = this.playerSearchTextBox.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        this.adminHistory = true;
        manager.request(BankAction.ADMIN_HISTORY, -1, name);
    }

    private void transfer(final BankAction action) {
        final BankAccountView account = this.accountList.getSelectedItem();
        final String text = this.amountTextBox.getText();
        if (account == null || text.isEmpty()) {
            return;
        }
        final BigDecimal amount;
        try {
            amount = new BigDecimal(text);
        } catch (final NumberFormatException e) {
            return;
        }
        if (amount.signum() <= 0) {
            return;
        }
        manager.request(action, account.getRecNo(), amount);
        this.amountTextBox.setText("");
    }

    private void rename() {
        final BankAccountView account = this.accountList.getSelectedItem();
        if (account == null || !account.isMine()) {
            return;
        }
        new BankNameScreen(this, I18n.format("almura.feature.bank.title.rename_account"), account.getName(),
            name -> manager.request(BankAction.RENAME, account.getRecNo(), name)).display();
    }

    private void delete() {
        final BankAccountView account = this.accountList.getSelectedItem();
        if (account == null || !account.isMine()) {
            return;
        }
        final String message = account.getBalance().signum() > 0
            ? I18n.format("almura.feature.bank.dialog.delete_with_balance", account.getName(), format(account.getBalance()))
            : I18n.format("almura.feature.bank.dialog.delete", account.getName());
        BasicMessageBox.showDialog(this, I18n.format("almura.feature.bank.title.delete_account"), message, MessageBoxButtons.YES_NO,
            result -> {
                if (result == MessageBoxResult.YES) {
                    manager.request(BankAction.DELETE, account.getRecNo());
                }
            });
    }

    private void members() {
        final BankAccountView account = this.accountList.getSelectedItem();
        if (account == null) {
            return;
        }
        if (account.isMine()) {
            new BankMembersScreen(this, account.getRecNo()).display();
            return;
        }
        BasicMessageBox.showDialog(this, I18n.format("almura.feature.bank.title.leave_account"),
            I18n.format("almura.feature.bank.dialog.leave", account.getName(), account.getOwnerName()), MessageBoxButtons.YES_NO,
            result -> {
                if (result == MessageBoxResult.YES) {
                    manager.request(BankAction.REMOVE_MEMBER, account.getRecNo(), "", BigDecimal.ZERO,
                        Minecraft.getMinecraft().player.getUniqueID().toString(), false);
                }
            });
    }

    private void depositCoins() {
        final BankAccountView account = this.accountList.getSelectedItem();
        if (account != null) {
            manager.request(BankAction.DEPOSIT_COINS, account.getRecNo());
        }
    }

    private UILabel label(final String text, final int x, final int y) {
        final UILabel label = new UILabel(text);
        label.setFontOptions(FontColors.WHITE_FO);
        label.setPosition(x, y);
        return label;
    }

    private BasicContainer<?> line(final int x, final int y, final int width) {
        final BasicContainer<?> line = new BasicContainer<>(width, 1);
        line.setColor(FontColors.WHITE);
        line.setBackgroundAlpha(185);
        line.setPosition(x, y);
        return line;
    }

    static String format(final BigDecimal amount) {
        return BankMoney.format(amount);
    }

    /**
     * Digits and one decimal point, at most two decimals.
     */
    static String filterAmount(final String text) {
        final StringBuilder filtered = new StringBuilder();
        boolean point = false;
        int decimals = 0;
        for (final char c : text.toCharArray()) {
            if (c == '.' && !point) {
                point = true;
                filtered.append(c);
            } else if (Character.isDigit(c) && (!point || decimals < 2) && filtered.length() < 12) {
                filtered.append(c);
                if (point) {
                    decimals++;
                }
            }
        }
        return filtered.toString();
    }

    private static final class AccountComponent extends BasicList.ItemComponent<BankAccountView> {

        AccountComponent(final MalisisGui gui, final BasicList<BankAccountView> parent, final BankAccountView item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            this.setSize(0, 24);

            final UILabel nameLabel = new UILabel(this.item.getName());
            nameLabel.setFontOptions(FontColors.WHITE_FO);
            nameLabel.setPosition(2, 0, Anchor.TOP | Anchor.LEFT);

            final String shared = this.item.isMine()
                ? this.item.isShared() ? " " + TextFormatting.GRAY + I18n.format("almura.feature.bank.text.shared") : ""
                : " " + TextFormatting.GRAY + I18n.format("almura.feature.bank.text.by", this.item.getOwnerName());
            final UILabel balanceLabel = new UILabel(TextFormatting.GOLD + format(this.item.getBalance()) + shared);
            balanceLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().scale(0.8f).build());
            balanceLabel.setPosition(2, 0, Anchor.BOTTOM | Anchor.LEFT);

            this.add(nameLabel, balanceLabel);
        }
    }

    private static final class HistoryComponent extends BasicList.ItemComponent<BankHistoryEntry> {

        HistoryComponent(final MalisisGui gui, final BasicList<BankHistoryEntry> parent, final BankHistoryEntry item) {
            super(gui, parent, item);
        }

        @Override
        protected void construct(final MalisisGui gui) {
            super.construct(gui);
            this.setSize(0, 24);

            final BankTransactionType type = this.item.getType();
            final UILabel typeLabel = new UILabel(typeColor(type) + I18n.format("almura.feature.bank.type."
                + type.name().toLowerCase(Locale.ENGLISH)));
            typeLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().scale(0.9f).build());
            typeLabel.setPosition(2, 0, Anchor.TOP | Anchor.LEFT);
            this.add(typeLabel);

            final String sign = sign(type);
            if (sign != null) {
                final UILabel amountLabel = new UILabel(typeColor(type) + sign + format(this.item.getAmount()));
                amountLabel.setFontOptions(FontColors.WHITE_FO.toBuilder().scale(0.9f).build());
                amountLabel.setPosition(-2, 0, Anchor.TOP | Anchor.RIGHT);
                this.add(amountLabel);
            }

            final UILabel detailLabel = new UILabel(DATE_FORMAT.format(this.item.getCreated()) + "  " + this.item.getActorName() + "  "
                + this.item.getAccountName());
            detailLabel.setFontOptions(SMALL_GRAY);
            detailLabel.setPosition(2, 0, Anchor.BOTTOM | Anchor.LEFT);
            this.add(detailLabel);
        }

        @Nullable
        private static String sign(final BankTransactionType type) {
            switch (type) {
                case DEPOSIT:
                case COIN_DEPOSIT:
                    return "+";
                case WITHDRAW:
                case CLOSING_PAYOUT:
                    return "-";
                default:
                    return null;
            }
        }

        private static TextFormatting typeColor(final BankTransactionType type) {
            switch (type) {
                case DEPOSIT:
                case COIN_DEPOSIT:
                    return TextFormatting.GREEN;
                case WITHDRAW:
                case CLOSING_PAYOUT:
                    return TextFormatting.RED;
                default:
                    return TextFormatting.WHITE;
            }
        }
    }
}
