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
import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.feature.bank.network.BankMessages.AccountView;
import com.almuradev.almura.feature.bank.network.BankMessages.HistoryEntry;
import com.almuradev.almura.feature.bank.network.BankMessages.Member;
import com.almuradev.almura.feature.bank.network.BankMessages.ServerboundAction;
import com.almuradev.almura.feature.bank.network.BankMessages;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.component.Component;
import org.inspirenxe.strata.api.control.attribute.item.Selection;
import org.inspirenxe.strata.api.control.attribute.text.TextInput;
import org.inspirenxe.strata.api.control.button.Button;
import org.inspirenxe.strata.api.control.button.CheckBox;
import org.inspirenxe.strata.api.control.button.CheckState;
import org.inspirenxe.strata.api.control.collection.ListView;
import org.inspirenxe.strata.api.control.display.Label;
import org.inspirenxe.strata.api.control.field.TextBox;
import org.inspirenxe.strata.api.control.window.MessageBox;
import org.inspirenxe.strata.api.control.window.Window;
import org.inspirenxe.strata.api.layout.Anchor;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.StackPanel;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.overlay.Overlay;
import org.inspirenxe.strata.api.screen.Screen;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.api.theme.Border;
import org.inspirenxe.strata.api.theme.Paint;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * The bank window, as 3.5's {@code BankScreen}: the player's accounts at the left (with + - ? to open, delete and rename one), their wallet
 * and the selected account at the right with its deposit, withdraw, coins and members, and its history under them (for admins, any player's
 * history by name).
 */
final class BankScreen implements Screen {

    // 3.5's size, smaller on a small screen
    private static final float WIDTH = 430f, HEIGHT = 270f, LIST_WIDTH = 150f, GAP = 6f, MARGIN = 10f;
    private static final float BUTTON_HEIGHT = 16f, SMALL_WIDTH = 12f, SMALL_HEIGHT = 15f, SMALL_GAP = 3f, ROW_HEIGHT = 24f;
    private static final int WHITE = 0xFFFFFFFF, GRAY = 0xFFAAAAAA, GOLD = 0xFFFFAA00, GREEN = 0xFF55FF55, RED = 0xFFFF5555, YELLOW = 0xFFFFFF55;
    private static final Paint BOX = Paint.solid(0x23FFFFFF), LINE = Paint.solid(0xB9FFFFFF);
    private static final Border BOX_BORDER = Border.of(0xB9FFFFFF, 1f);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault());

    private @Nullable ComponentFactory factory;
    private @Nullable ListView<AccountView> accountList;
    private @Nullable ListView<HistoryEntry> historyList;
    private @Nullable Label walletLabel, accountTitle, accountInfo, balanceLabel, historyTitle, emptyLabel;
    private @Nullable TextBox amount, playerSearch;
    private @Nullable Button add, rename, delete, deposit, withdraw, accountCoins, members;
    private @Nullable MembersDialog membersDialog;
    // The selected account's record number, kept across refreshes and rebuilds
    private int selected = -1;
    // Showing an admin's search for a player's history, not the selected account's
    private boolean adminHistory;
    // Selecting from code, not the player
    private boolean selecting;

    @Override
    public Text title() {
        return Text.translatable("almura.feature.bank.title");
    }

    @Override
    public Component<?> build(final ComponentFactory factory) {
        this.factory = factory;
        final float x = LIST_WIDTH + GAP;

        // Accounts, in their own box, with + - ? under them
        final ListView<AccountView> accountList = factory.<AccountView>listView(account -> this.accountRow(factory, account));
        accountList.setRowHeight(ROW_HEIGHT);
        accountList.onEvent(Selection.SELECTED, event -> {
            if (!this.selecting) {
                this.selected = accountList.selected().map(AccountView::recNo).orElse(-1);
                this.onAccountSelected();
            }
        });
        this.accountList = accountList;
        final Label emptyLabel = factory.label(Text.translatable("almura.feature.bank.text.no_accounts").color(GRAY)).setTextScale(0.8f)
            .setWrap(true);
        emptyLabel.setHitTestVisible(false);
        this.emptyLabel = emptyLabel;
        this.add = small(factory, "+", GREEN, "", () -> this.nameDialog(Text.translatable("almura.feature.bank.title.new_account"), "",
            name -> ClientBank.request(new ServerboundAction(BankAction.CREATE, -1, name, "0", "", false))));
        this.delete = small(factory, "-", RED, "almura.feature.bank.button.delete", this::confirmDelete);
        this.rename = small(factory, "?", YELLOW, "almura.feature.bank.button.rename", this::renameDialog);
        final StackPanel accountButtons = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(SMALL_GAP)
            .add(this.add).add(this.delete).add(this.rename);

        // Wallet
        this.walletLabel = factory.label("");
        final Button walletCoins = button(factory, "almura.feature.bank.button.deposit_coins", 80f,
            () -> ClientBank.request(BankAction.DEPOSIT_COINS, -1))
            .setTooltip(Text.translatable("almura.feature.bank.tooltip.coins_to_wallet"));

        // The selected account
        this.accountTitle = factory.label("");
        this.accountInfo = factory.label("").setTextScale(0.8f);
        this.balanceLabel = factory.label("");
        final Label amountLabel = factory.label(Text.translatable("almura.feature.bank.label.amount"));
        final TextBox amount = factory.textBox().setMaxLength(12);
        // Digits and one decimal point, at most two decimals
        amount.onEvent(TextInput.EDIT, event -> event.setValue(filterAmount(event.value())));
        amount.onEvent(TextBox.SUBMITTED, event -> this.transfer(BankAction.DEPOSIT));
        amount.setSize(80f, BUTTON_HEIGHT);
        this.amount = amount;
        this.deposit = button(factory, "almura.feature.bank.button.deposit", 58f, () -> this.transfer(BankAction.DEPOSIT));
        this.withdraw = button(factory, "almura.feature.bank.button.withdraw", 58f, () -> this.transfer(BankAction.WITHDRAW));
        this.accountCoins = button(factory, "almura.feature.bank.button.deposit_coins", 80f, () -> {
            if (this.selected >= 0) {
                ClientBank.request(BankAction.DEPOSIT_COINS, this.selected);
            }
        }).setTooltip(Text.translatable("almura.feature.bank.tooltip.coins_to_account"));
        this.members = button(factory, "almura.feature.bank.button.members", 80f, this::members);

        // History
        this.historyTitle = factory.label("");
        final ListView<HistoryEntry> historyList = factory.<HistoryEntry>listView(entry -> historyRow(factory, entry));
        historyList.setRowHeight(ROW_HEIGHT);
        this.historyList = historyList;

        final AnchorPanel content = factory.anchorPanel()
            .add(factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f)
                .add(accountList.setAnchor(Anchor.FILL))
                .add(emptyLabel.setAnchor(Anchor.TOP_LEFT).setMargin(3f, 3f, 3f, 0f).setWidth(LIST_WIDTH - 10f))
                .setAnchor(Anchor.FILL).setMargin(0f, 0f, 0f, SMALL_HEIGHT + 4f)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.STRETCH).setWidth(LIST_WIDTH))
            .add(accountButtons.setAnchor(Anchor.BOTTOM_LEFT))
            .add(this.walletLabel.setAnchor(Anchor.TOP_LEFT).setMargin(x, 4f, 0f, 0f))
            .add(walletCoins.setAnchor(Anchor.TOP_RIGHT))
            .add(line(factory, x, 19f))
            .add(this.accountTitle.setAnchor(Anchor.TOP_LEFT).setMargin(x, 24f, 0f, 0f))
            .add(this.accountInfo.setAnchor(Anchor.TOP_LEFT).setMargin(x, 36f, 0f, 0f))
            .add(this.balanceLabel.setAnchor(Anchor.TOP_LEFT).setMargin(x, 47f, 0f, 0f))
            .add(amountLabel.setAnchor(Anchor.TOP_LEFT).setMargin(x, 66f, 0f, 0f))
            .add(amount.setAnchor(Anchor.TOP_LEFT).setMargin(x + 44f, 62f, 0f, 0f))
            .add(this.withdraw.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 62f, 0f, 0f))
            .add(this.deposit.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 62f, 62f, 0f))
            .add(this.accountCoins.setAnchor(Anchor.TOP_LEFT).setMargin(x, 82f, 0f, 0f))
            .add(this.members.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 82f, 0f, 0f))
            .add(line(factory, x, 101f))
            .add(this.historyTitle.setAnchor(Anchor.TOP_LEFT).setMargin(x, 107f, 0f, 0f));
        float historyTop = 119f;
        if (ClientBank.admin()) {
            // Admins: any player's history by name
            final TextBox playerSearch = factory.textBox().setMaxLength(16);
            playerSearch.setTooltip(Text.translatable("almura.feature.bank.tooltip.player_search"));
            playerSearch.onEvent(TextBox.SUBMITTED, event -> this.searchPlayer());
            playerSearch.setSize(90f, BUTTON_HEIGHT);
            this.playerSearch = playerSearch;
            content.add(playerSearch.setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 103f, 44f, 0f))
                .add(button(factory, "almura.feature.bank.button.search", 40f, this::searchPlayer).setAnchor(Anchor.TOP_RIGHT)
                    .setMargin(0f, 103f, 0f, 0f));
            historyTop = 122f;
        }
        content.add(factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f)
            .add(historyList.setAnchor(Anchor.FILL))
            .setAnchor(Anchor.FILL).setMargin(x, historyTop, 0f, 0f));

        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = factory.window(this.title())
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .add(content.setAnchor(Anchor.FILL))
            .setSize(Math.min(WIDTH, minecraft.getWindow().getGuiScaledWidth() - MARGIN),
                Math.min(HEIGHT, minecraft.getWindow().getGuiScaledHeight() - MARGIN))
            .setAnchor(Anchor.CENTER);
        // Its x closes the bank, as on the other Almura windows
        InGameScreens.closesScreen(window);
        this.refresh();
        return factory.anchorPanel().add(window).setAnchor(Anchor.FILL);
    }

    // ---- From the server

    /**
     * New accounts and balances from the server: keeps the selected account (or picks the first) and asks for its history again.
     */
    void refresh() {
        if (this.accountList == null || this.walletLabel == null || this.emptyLabel == null) {
            return;
        }
        final List<AccountView> accounts = ClientBank.accounts();
        AccountView reselect = accounts.stream().filter(account -> account.recNo() == this.selected).findFirst().orElse(null);
        final boolean changed = reselect == null;
        if (reselect == null && !accounts.isEmpty()) {
            reselect = accounts.getFirst();
        }
        this.selecting = true;
        try {
            this.accountList.setItems(accounts);
            this.accountList.refresh();
            if (reselect != null) {
                this.accountList.selectItem(reselect);
            }
        } finally {
            this.selecting = false;
        }
        this.emptyLabel.setVisible(accounts.isEmpty());
        this.walletLabel.setText(Text.translatable("almura.feature.bank.label.wallet").color(WHITE).append(Text.of(" "))
            .append(Text.of(BankMoney.format(ClientBank.wallet())).color(GOLD)));
        final int previous = this.selected;
        this.selected = reselect == null ? -1 : reselect.recNo();
        if (changed || previous != this.selected) {
            // A different account (the old one was deleted or left)
            this.onAccountSelected();
        } else {
            this.updateDetails();
            if (!this.adminHistory) {
                this.requestHistory();
            }
        }
        if (this.membersDialog != null) {
            this.membersDialog.refresh();
        }
    }

    /**
     * A history from the server: an account's (shown if it is still the one selected), or a player's for an admin.
     */
    void showHistory(final int account, final String title, final List<HistoryEntry> entries) {
        if (this.historyList == null || this.historyTitle == null) {
            return;
        }
        if (account >= 0 && (this.adminHistory || this.selected != account)) {
            return; // An answer for an account no longer shown
        }
        this.historyTitle.setText((account < 0 ? Text.translatable("almura.feature.bank.label.player_history", title)
            : Text.translatable("almura.feature.bank.label.history", title)).color(WHITE));
        this.historyList.setItems(entries);
        this.historyList.refresh();
    }

    // ---- The selected account

    private @Nullable AccountView selectedAccount() {
        return ClientBank.account(this.selected).orElse(null);
    }

    private void onAccountSelected() {
        this.adminHistory = false;
        this.updateDetails();
        if (this.historyList != null) {
            this.historyList.setItems(List.of());
            this.historyList.refresh();
        }
        this.requestHistory();
    }

    private void updateDetails() {
        if (this.accountTitle == null || this.accountInfo == null || this.balanceLabel == null || this.add == null || this.rename == null
            || this.delete == null || this.deposit == null || this.withdraw == null || this.accountCoins == null || this.members == null
            || this.amount == null) {
            return;
        }
        final AccountView account = this.selectedAccount();
        final boolean selected = account != null;
        if (selected) {
            this.accountTitle.setText(Text.of(account.name()).color(WHITE).bold());
            this.accountInfo.setText((account.mine()
                ? account.shared() ? Text.translatable("almura.feature.bank.text.shared_with", account.members().size())
                    : Text.translatable("almura.feature.bank.text.personal")
                : Text.translatable("almura.feature.bank.text.shared_by", account.ownerName())
                    .append(account.canWithdraw() ? Text.of("") : Text.of(" ").append(Text.translatable("almura.feature.bank.text.deposit_only"))))
                .color(GRAY));
            this.balanceLabel.setText(Text.translatable("almura.feature.bank.label.balance").color(WHITE).append(Text.of(" "))
                .append(Text.of(BankMoney.format(BankMoney.parse(account.balance()))).color(GOLD)));
        } else {
            this.accountTitle.setText(Text.translatable("almura.feature.bank.text.no_account_selected").color(GRAY));
            this.accountInfo.setText(Text.of(""));
            this.balanceLabel.setText(Text.of(""));
        }
        final long owned = ClientBank.ownedCount();
        this.add.setEnabled(owned < ClientBank.limit());
        this.add.setTooltip(Text.translatable("almura.feature.bank.tooltip.new_account", owned, ClientBank.limit()));
        this.rename.setEnabled(selected && account.mine());
        this.delete.setEnabled(selected && account.mine());
        this.deposit.setEnabled(selected);
        this.withdraw.setEnabled(selected && account.canWithdraw());
        this.accountCoins.setEnabled(selected);
        this.members.setEnabled(selected);
        // Members manage the account's members as its owner, or leave it
        this.members.setText(Text.translatable(selected && !account.mine() ? "almura.feature.bank.button.leave"
            : "almura.feature.bank.button.members"));
        this.amount.setReadOnly(!selected);
    }

    private void requestHistory() {
        if (this.adminHistory) {
            return;
        }
        if (this.selected >= 0) {
            ClientBank.request(BankAction.HISTORY, this.selected);
        } else if (this.historyTitle != null && this.historyList != null) {
            this.historyTitle.setText(Text.of(""));
            this.historyList.setItems(List.of());
            this.historyList.refresh();
        }
    }

    private void searchPlayer() {
        if (this.playerSearch == null || this.playerSearch.text().isBlank()) {
            return;
        }
        this.adminHistory = true;
        ClientBank.request(new ServerboundAction(BankAction.ADMIN_HISTORY, -1, this.playerSearch.text().trim(), "0", "", false));
    }

    private void transfer(final BankAction action) {
        if (this.amount == null || this.selected < 0 || this.amount.text().isEmpty()) {
            return;
        }
        final BigDecimal value = BankMoney.parse(this.amount.text());
        if (value.signum() <= 0) {
            return;
        }
        ClientBank.request(new ServerboundAction(action, this.selected, "", value.toPlainString(), "", false));
        this.amount.setText("");
    }

    private void renameDialog() {
        final AccountView account = this.selectedAccount();
        if (account != null && account.mine()) {
            this.nameDialog(Text.translatable("almura.feature.bank.title.rename_account"), account.name(),
                name -> ClientBank.request(new ServerboundAction(BankAction.RENAME, account.recNo(), name, "0", "", false)));
        }
    }

    private void confirmDelete() {
        final AccountView account = this.selectedAccount();
        if (this.factory == null || account == null || !account.mine()) {
            return;
        }
        final BigDecimal balance = BankMoney.parse(account.balance());
        final Text message = balance.signum() > 0
            ? Text.translatable("almura.feature.bank.dialog.delete_with_balance", account.name(), BankMoney.format(balance))
            : Text.translatable("almura.feature.bank.dialog.delete", account.name());
        this.ask(Text.translatable("almura.feature.bank.title.delete_account"), message,
            () -> ClientBank.request(BankAction.DELETE, account.recNo()));
    }

    private void members() {
        final AccountView account = this.selectedAccount();
        if (this.factory == null || account == null) {
            return;
        }
        if (account.mine()) {
            this.membersDialog = new MembersDialog(this.factory, account.recNo());
            return;
        }
        final String self = Minecraft.getInstance().player == null ? "" : Minecraft.getInstance().player.getUUID().toString();
        this.ask(Text.translatable("almura.feature.bank.title.leave_account"),
            Text.translatable("almura.feature.bank.dialog.leave", account.name(), account.ownerName()),
            () -> ClientBank.request(new ServerboundAction(BankAction.REMOVE_MEMBER, account.recNo(), "", "0", self, false)));
    }

    // ---- Dialogs

    private void ask(final Text title, final Text message, final Runnable yes) {
        if (this.factory == null) {
            return;
        }
        this.factory.overlays().dialog(this.factory.messageBox(title, message, MessageBox.Buttons.YES_NO)
            .onEvent(MessageBox.ANSWERED, event -> {
                if (event.value().equals(MessageBox.Result.YES)) {
                    yes.run();
                }
            }));
    }

    /**
     * 3.5's name prompt: a name of up to 32 characters, then Save or Cancel.
     */
    private void nameDialog(final Text title, final String initial, final Consumer<String> save) {
        final ComponentFactory factory = this.factory;
        if (factory == null) {
            return;
        }
        final TextBox name = factory.textBox().setMaxLength(BankMessages.MAX_NAME).setText(initial);
        name.setSelectAllOnFocus(true);
        final Overlay[] overlay = new Overlay[1];
        final Runnable done = () -> {
            if (!name.text().isBlank()) {
                save.accept(name.text().trim());
                overlay[0].close();
            }
        };
        name.onEvent(TextBox.SUBMITTED, event -> done.run());
        final StackPanel column = factory.stackPanel().setSpacing(4f)
            .add(name.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP))
            .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f)
                .add(button(factory, "almura.button.cancel", 40f, () -> overlay[0].close()))
                .add(button(factory, "almura.button.save", 40f, done))
                .setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP));
        column.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
        overlay[0] = factory.overlays().dialog(dialogWindow(factory, title, 160f).add(column));
    }

    /**
     * 3.5's Members of an account: a player's name and whether they may withdraw to add them, the members with theirs, and Toggle withdraw
     * and Remove for the one selected. Refreshed with the bank window; closed if the account goes.
     */
    private final class MembersDialog {

        private final int account;
        private final ListView<Member> list;
        private final Button toggle, remove;
        private final Overlay overlay;

        MembersDialog(final ComponentFactory factory, final int account) {
            this.account = account;
            final String accountName = ClientBank.account(account).map(AccountView::name).orElse("");
            final TextBox name = factory.textBox().setMaxLength(16);
            name.setTooltip(Text.translatable("almura.feature.bank.tooltip.member_name"));
            name.setSize(100f, BUTTON_HEIGHT);
            final CheckBox canWithdraw = factory.checkBox(Text.translatable("almura.feature.bank.checkbox.can_withdraw"));
            final Runnable add = () -> {
                if (!name.text().isBlank()) {
                    ClientBank.request(new ServerboundAction(BankAction.ADD_MEMBER, account, name.text().trim(), "0", "",
                        canWithdraw.state() == CheckState.CHECKED));
                    name.setText("");
                }
            };
            name.onEvent(TextBox.SUBMITTED, event -> add.run());
            this.list = factory.<Member>listView(member -> memberRow(factory, member));
            this.list.setRowHeight(16f);
            this.list.onEvent(Selection.SELECTED, event -> this.updateButtons());
            this.toggle = button(factory, "almura.feature.bank.button.toggle_withdraw", 90f, () -> this.list.selected().ifPresent(member ->
                ClientBank.request(new ServerboundAction(BankAction.SET_MEMBER_WITHDRAW, account, "", "0", member.uniqueId().toString(),
                    !member.canWithdraw()))));
            this.remove = factory.button(Text.translatable("almura.feature.bank.button.remove").color(RED)).setSize(50f, BUTTON_HEIGHT)
                .onEvent(Button.PRESSED, event -> this.list.selected().ifPresent(member ->
                    ClientBank.request(new ServerboundAction(BankAction.REMOVE_MEMBER, account, "", "0", member.uniqueId().toString(), false))));
            final Overlay[] overlay = new Overlay[1];
            final AnchorPanel content = factory.anchorPanel()
                .add(name.setAnchor(Anchor.TOP_LEFT))
                .add(canWithdraw.setAnchor(Anchor.TOP_LEFT).setMargin(104f, 3f, 0f, 0f))
                .add(button(factory, "almura.feature.bank.button.add", 36f, add).setAnchor(Anchor.TOP_RIGHT))
                .add(factory.anchorPanel().setBackground(BOX).setBorder(BOX_BORDER).setPadding(2f)
                    .add(this.list.setAnchor(Anchor.FILL))
                    .setAnchor(Anchor.FILL).setMargin(0f, 20f, 0f, BUTTON_HEIGHT + 6f))
                .add(factory.stackPanel(Orientation.HORIZONTAL).setSpacing(2f).add(this.toggle).add(this.remove).setAnchor(Anchor.BOTTOM_LEFT))
                .add(button(factory, "almura.button.close", 50f, () -> overlay[0].close()).setAnchor(Anchor.BOTTOM_RIGHT));
            content.setHeight(176f);
            overlay[0] = factory.overlays().dialog(dialogWindow(factory, Text.translatable("almura.feature.bank.title.members", accountName), 250f)
                .add(content.setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP)));
            this.overlay = overlay[0];
            this.refresh();
        }

        void refresh() {
            final AccountView view = ClientBank.account(this.account).orElse(null);
            if (view == null || !view.mine()) {
                this.overlay.close(); // Deleted
                return;
            }
            final Member selected = this.list.selected().orElse(null);
            this.list.setItems(view.members());
            this.list.refresh();
            if (selected != null) {
                view.members().stream().filter(member -> member.uniqueId().equals(selected.uniqueId())).findFirst().ifPresent(this.list::selectItem);
            }
            this.updateButtons();
        }

        private void updateButtons() {
            final boolean selected = this.list.selected().isPresent();
            this.toggle.setEnabled(selected);
            this.remove.setEnabled(selected);
        }
    }

    private static Window dialogWindow(final ComponentFactory factory, final Text title, final float width) {
        return factory.window(title)
            .setClosable(true)
            .setMovable(true)
            .setResizable(false)
            .setCollapsible(false)
            .setMaximizable(false)
            .setWidth(width);
    }

    // ---- Rows

    /**
     * An account: its name, and its balance under it in gold, with (shared) or whose it is.
     */
    private Component<?> accountRow(final ComponentFactory factory, final AccountView account) {
        final Text balance = Text.of(BankMoney.format(BankMoney.parse(account.balance()))).color(GOLD);
        final Text whose = account.mine()
            ? account.shared() ? Text.of(" ").append(Text.translatable("almura.feature.bank.text.shared")).color(GRAY) : Text.of("")
            : Text.of(" ").append(Text.translatable("almura.feature.bank.text.by", account.ownerName())).color(GRAY);
        final StackPanel lines = factory.stackPanel().setSpacing(1f)
            .add(factory.label(Text.of(account.name()).color(WHITE)))
            .add(factory.label(balance.append(whose)).setTextScale(0.8f));
        lines.setHitTestVisible(false);
        return factory.anchorPanel().add(lines.setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f)).setAnchor(Anchor.FILL)
            .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    /**
     * A history entry: what (green for money in, red for out) and how much, with when, by whom and on which account under it.
     */
    private static Component<?> historyRow(final ComponentFactory factory, final HistoryEntry entry) {
        final BankTransactionType type = entry.kind();
        final int colour = "+".equals(type.sign()) ? GREEN : "-".equals(type.sign()) ? RED : WHITE;
        final AnchorPanel row = factory.anchorPanel()
            .add(factory.label(Text.translatable("almura.feature.bank.type." + type.name().toLowerCase(Locale.ROOT)).color(colour))
                .setTextScale(0.9f).setAnchor(Anchor.TOP_LEFT).setMargin(2f, 2f, 0f, 0f))
            .add(factory.label(Text.of(DATE.format(Instant.ofEpochMilli(entry.created())) + "  " + entry.actorName() + "  " + entry.accountName())
                .color(GRAY)).setTextScale(0.8f).setAnchor(Anchor.BOTTOM_LEFT).setMargin(2f, 0f, 0f, 2f));
        if (type.sign() != null) {
            row.add(factory.label(Text.of(type.sign() + BankMoney.format(BankMoney.parse(entry.amount()))).color(colour)).setTextScale(0.9f)
                .setAnchor(Anchor.TOP_RIGHT).setMargin(0f, 2f, 2f, 0f));
        }
        row.setHitTestVisible(false);
        // As wide as the row, so the amount sits at its right edge
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    /**
     * A member: their name, and whether they may withdraw at the right.
     */
    private static Component<?> memberRow(final ComponentFactory factory, final Member member) {
        final AnchorPanel row = factory.anchorPanel()
            .add(factory.label(Text.of(member.name()).color(WHITE)).setAnchor(Anchor.MIDDLE_LEFT).setMargin(2f, 0f, 0f, 0f))
            .add(factory.label(member.canWithdraw() ? Text.translatable("almura.feature.bank.text.can_withdraw").color(GREEN)
                : Text.translatable("almura.feature.bank.text.member_deposit_only").color(GRAY)).setTextScale(0.8f)
                .setAnchor(Anchor.MIDDLE_RIGHT).setMargin(0f, 0f, 2f, 0f));
        row.setHitTestVisible(false);
        // As wide as the row, so the amount sits at its right edge
        return row.setAnchor(Anchor.FILL).setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.STRETCH);
    }

    // ---- Helpers

    private static Button button(final ComponentFactory factory, final String key, final float width, final Runnable press) {
        return factory.button(Text.translatable(key)).setSize(width, BUTTON_HEIGHT).onEvent(Button.PRESSED, event -> press.run());
    }

    /**
     * As 3.5's + - ?: narrow, one coloured character (as the Guide's).
     */
    private static Button small(final ComponentFactory factory, final String label, final int colour, final String tooltip,
        final Runnable press) {
        final Button button = factory.button(Text.of(label).color(colour)).setTextPadding(2f, 4f, 2f, 2f);
        button.setMinSize(0f, 0f);
        button.setSize(SMALL_WIDTH, SMALL_HEIGHT);
        if (!tooltip.isEmpty()) {
            button.setTooltip(Text.translatable(tooltip));
        }
        return button.onEvent(Button.PRESSED, event -> press.run());
    }

    private static Component<?> line(final ComponentFactory factory, final float x, final float y) {
        return factory.anchorPanel().setBackground(LINE).setHeight(1f).setAnchor(Anchor.FILL).setMargin(x, y, 0f, 0f)
            .setAlignment(HorizontalAlignment.STRETCH, VerticalAlignment.TOP);
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
}
