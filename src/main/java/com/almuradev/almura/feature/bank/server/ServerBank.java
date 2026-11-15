/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank.server;

import com.almuradev.almura.Almura;
import com.almuradev.almura.core.database.AlmuraDatabase;
import com.almuradev.almura.feature.bank.BankAction;
import com.almuradev.almura.feature.bank.BankCoins;
import com.almuradev.almura.feature.bank.BankMoney;
import com.almuradev.almura.feature.bank.BankTransactionType;
import com.almuradev.almura.feature.bank.network.BankMessages.AccountView;
import com.almuradev.almura.feature.bank.network.BankMessages.ClientboundHistory;
import com.almuradev.almura.feature.bank.network.BankMessages.ClientboundState;
import com.almuradev.almura.feature.bank.network.BankMessages.HistoryEntry;
import com.almuradev.almura.feature.bank.network.BankMessages.Member;
import com.almuradev.almura.feature.bank.network.BankMessages.ServerboundAction;
import com.almuradev.almura.feature.bank.network.BankMessages;
import com.almuradev.almura.feature.economy.Economy;
import com.almuradev.almura.feature.economy.EconomyAccounts;
import com.almuradev.almura.feature.economy.EconomyAccounts.Account;
import com.almuradev.almura.feature.notification.server.ServerNotifications;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * The bank on the server, as 3.5's {@code ServerBankManager}: accounts a player owns (up to a limit) and shares with others, each the
 * economy's virtual account {@code almura_bank_<random>} holding its balance, with money moved between it and the player's wallet, coins
 * cashed in, and a history of what was done. Accounts, members and history are in Almura's database ({@link AlmuraDatabase}); the accounts
 * and members are kept in memory, read again after each change.
 * <p>
 * Needs the server's economy (SpongeNeo with an economy plugin such as EconomyLite); without one the bank says it is unavailable.
 * <p>
 * Permissions, as 3.5's (a permission plugin such as LuckPerms grants them; without one only operators have them):
 * <ul>
 *     <li>{@code almura.bank.use}: use the bank ({@code /bank}, the Features menu's Banking, the Coin Exchange);</li>
 *     <li>{@code almura.bank.admin}: search any player's bank history;</li>
 *     <li>{@code almura.bank.account.limit}: how many accounts a player may own (3 unless the permission plugin gives a number; 3.5's
 *     option of the same name).</li>
 * </ul>
 */
public final class ServerBank {

    private static final int DEFAULT_ACCOUNT_LIMIT = 3;
    private static final int HISTORY_LIMIT = 50, ADMIN_HISTORY_LIMIT = 200;
    // The economy plugin keeps balances as DECIMAL(11, 2)
    private static final BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");
    private static final String TITLE = "Bank";

    public static final PermissionNode<Boolean> USE = node("bank.use");
    public static final PermissionNode<Boolean> ADMIN = node("bank.admin");
    public static final PermissionNode<Integer> ACCOUNT_LIMIT = new PermissionNode<>(Almura.ID, "bank.account.limit", PermissionTypes.INTEGER,
        (player, uuid, context) -> DEFAULT_ACCOUNT_LIMIT);

    private static final Map<Integer, BankAccount> accounts = new LinkedHashMap<>();
    private static boolean ready;

    /**
     * An account: its owner, name, the economy account holding its balance, and its members with whether each may withdraw (everyone on
     * it may deposit).
     */
    private record BankAccount(int recNo, UUID owner, String name, String economyId, Map<UUID, Boolean> members) {

        boolean isOwner(final UUID player) {
            return this.owner.equals(player);
        }

        boolean isMember(final UUID player) {
            return this.members.containsKey(player);
        }

        boolean canAccess(final UUID player) {
            return this.isOwner(player) || this.isMember(player);
        }

        boolean canWithdraw(final UUID player) {
            return this.isOwner(player) || this.members.getOrDefault(player, false);
        }

        Set<UUID> everyone() {
            final Set<UUID> everyone = new HashSet<>(this.members.keySet());
            everyone.add(this.owner);
            return everyone;
        }

        Account economyAccount() {
            return Account.named(this.economyId);
        }
    }

    private static PermissionNode<Boolean> node(final String path) {
        return new PermissionNode<>(Almura.ID, path, PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(USE, ADMIN, ACCOUNT_LIMIT);
    }

    @SubscribeEvent
    public void onServerStarted(final ServerStartedEvent event) {
        // Only where there can be an economy: a server without Sponge has no bank, and no database to open for it
        if (!Economy.available()) {
            return;
        }
        AlmuraDatabase.install("bank").whenComplete((result, error) -> {
            if (error != null) {
                Almura.LOGGER.error("Couldn't make the bank's tables; the bank is unavailable", error.getCause() == null ? error : error.getCause());
                return;
            }
            reload(() -> {
                ready = true;
                Almura.LOGGER.info("Loaded {} bank account(s)", accounts.size());
            });
        });
    }

    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent event) {
        ready = false;
        accounts.clear();
        AlmuraDatabase.close();
    }

    /**
     * {@code /bank}: opens the bank, as 3.5's.
     */
    @SubscribeEvent
    public void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bank").executes(context -> {
            final ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("Only players can use the bank."));
                return 0;
            }
            open(player);
            return 1;
        }));
    }

    // ---- Opening and actions

    /**
     * Opens the bank window ({@code /bank}, the Features menu, the Coin Exchange).
     */
    public static void open(final ServerPlayer player) {
        if (unavailable(player)) {
            return;
        }
        sendState(player, true);
    }

    public static void handle(final ServerPlayer player, final ServerboundAction message) {
        if (message.action() == BankAction.OPEN) {
            open(player);
            return;
        }
        if (unavailable(player)) {
            return;
        }
        final EconomyAccounts economy = Economy.accounts();
        if (economy == null) {
            return;
        }
        switch (message.action()) {
            case REFRESH -> sendState(player, false);
            case CREATE -> create(player, message.text());
            case RENAME -> rename(player, message.account(), message.text());
            case DELETE -> delete(player, economy, message.account());
            case DEPOSIT -> transfer(player, economy, message.account(), BankMoney.parse(message.amount()), true);
            case WITHDRAW -> transfer(player, economy, message.account(), BankMoney.parse(message.amount()), false);
            case DEPOSIT_COINS -> depositCoins(player, economy, message.account());
            case ADD_MEMBER -> addMember(player, message.account(), message.text(), message.flag());
            case REMOVE_MEMBER -> removeMember(player, message.account(), message.member());
            case SET_MEMBER_WITHDRAW -> setMemberWithdraw(player, message.account(), message.member(), message.flag());
            case HISTORY -> sendAccountHistory(player, message.account());
            case ADMIN_HISTORY -> sendPlayerHistory(player, message.text());
            default -> { }
        }
    }

    /**
     * Whether the bank can't be used by the player (no permission, no economy, the database not read), telling them why.
     */
    private static boolean unavailable(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, USE)) {
            error(player, "You do not have permission to use the bank!");
            return true;
        }
        if (Economy.accounts() == null) {
            error(player, "The bank is unavailable: there is no economy on this server.");
            return true;
        }
        if (!ready) {
            error(player, "The bank is unavailable right now, try again later.");
            return true;
        }
        return false;
    }

    // ---- Accounts

    private static void create(final ServerPlayer player, final String rawName) {
        final String name = validateName(player, rawName, null);
        if (name == null) {
            return;
        }
        final int limit = accountLimit(player);
        if (accounts.values().stream().filter(account -> account.isOwner(player.getUUID())).count() >= limit) {
            error(player, "You have reached your limit of " + limit + " bank account(s).");
            return;
        }
        final UUID owner = player.getUUID();
        final String economyId = "almura_bank_" + Long.toHexString(ThreadLocalRandom.current().nextLong());
        database("open a bank account", connection -> {
            try (PreparedStatement insert = connection.prepareStatement("insert into bank_account (created, owner, name, economy_id) values (?, ?, ?, ?)")) {
                insert.setTimestamp(1, Timestamp.from(Instant.now()));
                insert.setObject(2, owner);
                insert.setString(3, name);
                insert.setString(4, economyId);
                insert.executeUpdate();
            }
        }, () -> {
            accounts.values().stream().filter(account -> account.economyId().equals(economyId)).findFirst()
                .ifPresent(account -> log(account, player, BankTransactionType.CREATE, BigDecimal.ZERO));
            sendState(player, false);
        });
    }

    private static void rename(final ServerPlayer player, final int recNo, final String rawName) {
        final BankAccount account = ownedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final String name = validateName(player, rawName, account);
        if (name == null) {
            return;
        }
        database("rename a bank account", connection -> {
            try (PreparedStatement update = connection.prepareStatement("update bank_account set name = ? where rec_no = ?")) {
                update.setString(1, name);
                update.setInt(2, recNo);
                update.executeUpdate();
            }
        }, () -> {
            final BankAccount renamed = accounts.get(recNo);
            if (renamed != null) {
                log(renamed, player, BankTransactionType.RENAME, BigDecimal.ZERO);
                refreshViewers(renamed, player);
            }
        });
    }

    private static void delete(final ServerPlayer player, final EconomyAccounts economy, final int recNo) {
        final BankAccount account = ownedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final BigDecimal balance = economy.balance(account.economyAccount());
        if (balance == null || economy.balance(Account.player(player.getUUID())) == null) {
            error(player, "The bank could not reach your accounts, try again later.");
            return;
        }
        // Pay what is left back to the owner first; the account is only closed if that worked
        if (balance.signum() > 0) {
            if (!economy.transfer(account.economyAccount(), Account.player(player.getUUID()), balance)) {
                error(player, "The account's balance could not be paid into your wallet, so it was not deleted.");
                return;
            }
            log(account, player, BankTransactionType.CLOSING_PAYOUT, balance);
        }
        final Set<UUID> viewers = account.everyone();
        log(account, player, BankTransactionType.DELETE, BigDecimal.ZERO);
        database("delete a bank account", connection -> {
            // Its members go with it (on delete cascade); its history is kept
            try (PreparedStatement members = connection.prepareStatement("delete from bank_account_member where account = ?");
                 PreparedStatement delete = connection.prepareStatement("delete from bank_account where rec_no = ?")) {
                members.setInt(1, recNo);
                members.executeUpdate();
                delete.setInt(1, recNo);
                delete.executeUpdate();
            }
        }, () -> {
            sendStateTo(viewers);
            if (balance.signum() > 0) {
                popup(player, "Closed " + account.name() + ": §6" + BankMoney.format(balance) + "§r paid into your wallet.");
            }
        });
    }

    // ---- Money

    private static void transfer(final ServerPlayer player, final EconomyAccounts economy, final int recNo, final BigDecimal rawAmount,
        final boolean deposit) {
        final BankAccount account = accessibleAccount(player, recNo);
        if (account == null) {
            return;
        }
        if (!deposit && !account.canWithdraw(player.getUUID())) {
            error(player, "You may not withdraw from " + account.name() + ".");
            return;
        }
        final BigDecimal amount = validateAmount(player, rawAmount);
        if (amount == null) {
            return;
        }
        final Account wallet = Account.player(player.getUUID());
        final Account from = deposit ? wallet : account.economyAccount();
        final Account to = deposit ? account.economyAccount() : wallet;
        final BigDecimal fromBalance = economy.balance(from);
        final BigDecimal toBalance = economy.balance(to);
        if (fromBalance == null || toBalance == null) {
            error(player, "The bank could not reach your accounts, try again later.");
            return;
        }
        if (fromBalance.compareTo(amount) < 0) {
            error(player, deposit ? "You don't have that much in your wallet." : "The account doesn't have that much.");
            return;
        }
        if (toBalance.add(amount).compareTo(MAX_BALANCE) > 0) {
            error(player, "That would take " + (deposit ? "the account" : "your wallet") + " over the most it can hold ("
                + BankMoney.format(MAX_BALANCE) + ").");
            return;
        }
        if (!economy.transfer(from, to, amount)) {
            error(player, "The bank could not complete that, nothing was moved.");
            return;
        }
        log(account, player, deposit ? BankTransactionType.DEPOSIT : BankTransactionType.WITHDRAW, amount);
        refreshViewers(account, player);
    }

    /**
     * Deposits every coin in the player's inventory into an account they may use, or (account -1) their wallet. The coins are only taken
     * once the money is in.
     */
    private static void depositCoins(final ServerPlayer player, final EconomyAccounts economy, final int recNo) {
        final BankAccount account = recNo < 0 ? null : accessibleAccount(player, recNo);
        if (recNo >= 0 && account == null) {
            return;
        }
        final Account target = account == null ? Account.player(player.getUUID()) : account.economyAccount();
        final BigDecimal targetBalance = economy.balance(target);
        if (targetBalance == null) {
            error(player, "The bank could not reach your accounts, try again later.");
            return;
        }
        final Inventory inventory = player.getInventory();
        final List<Integer> coinSlots = new ArrayList<>();
        long total = 0;
        int coins = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final ItemStack stack = inventory.getItem(slot);
            final long value = coinValue(stack);
            if (value > 0) {
                coinSlots.add(slot);
                total += value * stack.getCount();
                coins += stack.getCount();
            }
        }
        if (total == 0) {
            error(player, "You have no coins to deposit.");
            return;
        }
        final BigDecimal amount = BigDecimal.valueOf(total);
        if (targetBalance.add(amount).compareTo(MAX_BALANCE) > 0) {
            error(player, "That would take " + (account == null ? "your wallet" : "the account") + " over the most it can hold ("
                + BankMoney.format(MAX_BALANCE) + ").");
            return;
        }
        if (!economy.deposit(target, amount)) {
            error(player, "The bank could not take your coins, you still have them.");
            return;
        }
        for (final int slot : coinSlots) {
            inventory.setItem(slot, ItemStack.EMPTY);
        }
        if (account != null) {
            log(account, player, BankTransactionType.COIN_DEPOSIT, amount);
            refreshViewers(account, player);
        } else {
            sendState(player, false);
        }
        popup(player, "Deposited " + coins + " coin(s): §6" + BankMoney.format(amount) + "§r into "
            + (account == null ? "your wallet" : account.name()) + ".");
    }

    /**
     * @return what one of the stack's item is worth, 0 if it isn't a coin
     */
    public static long coinValue(final ItemStack stack) {
        return stack.isEmpty() ? 0 : BankCoins.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    // ---- Members

    private static void addMember(final ServerPlayer player, final int recNo, final String rawName, final boolean canWithdraw) {
        final BankAccount account = ownedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final NameAndId member = findPlayer(rawName.trim()).orElse(null);
        if (member == null) {
            error(player, "There is no player called " + rawName.trim() + ".");
            return;
        }
        if (account.isOwner(member.id())) {
            error(player, "You already own " + account.name() + ".");
            return;
        }
        if (account.isMember(member.id())) {
            error(player, member.name() + " is already on " + account.name() + ".");
            return;
        }
        database("add a bank account member", connection -> {
            try (PreparedStatement insert = connection.prepareStatement("insert into bank_account_member (account, member, can_withdraw) values (?, ?, ?)")) {
                insert.setInt(1, recNo);
                insert.setObject(2, member.id());
                insert.setBoolean(3, canWithdraw);
                insert.executeUpdate();
            }
        }, () -> {
            final BankAccount updated = accounts.get(recNo);
            if (updated != null) {
                log(updated, player, BankTransactionType.MEMBER_ADD, BigDecimal.ZERO);
                refreshViewers(updated, player);
            }
        });
    }

    /**
     * Removes a member: the owner removing anyone, or a member leaving.
     */
    private static void removeMember(final ServerPlayer player, final int recNo, final String rawMember) {
        final UUID memberId = parseUuid(rawMember);
        final BankAccount account = accounts.get(recNo);
        if (account == null || memberId == null || !account.isMember(memberId)
            || !account.isOwner(player.getUUID()) && !memberId.equals(player.getUUID())) {
            sendState(player, false);
            return;
        }
        final Set<UUID> viewers = account.everyone();
        database("remove a bank account member", connection -> {
            try (PreparedStatement delete = connection.prepareStatement("delete from bank_account_member where account = ? and member = ?")) {
                delete.setInt(1, recNo);
                delete.setObject(2, memberId);
                delete.executeUpdate();
            }
        }, () -> {
            log(account, player, BankTransactionType.MEMBER_REMOVE, BigDecimal.ZERO);
            sendStateTo(viewers);
        });
    }

    private static void setMemberWithdraw(final ServerPlayer player, final int recNo, final String rawMember, final boolean canWithdraw) {
        final BankAccount account = ownedAccount(player, recNo);
        final UUID memberId = parseUuid(rawMember);
        if (account == null || memberId == null || !account.isMember(memberId)) {
            return;
        }
        database("change a bank account member", connection -> {
            try (PreparedStatement update = connection.prepareStatement("update bank_account_member set can_withdraw = ? where account = ? and member = ?")) {
                update.setBoolean(1, canWithdraw);
                update.setInt(2, recNo);
                update.setObject(3, memberId);
                update.executeUpdate();
            }
        }, () -> {
            final BankAccount updated = accounts.get(recNo);
            if (updated != null) {
                log(updated, player, BankTransactionType.MEMBER_CHANGE, BigDecimal.ZERO);
                refreshViewers(updated, player);
            }
        });
    }

    // ---- History

    private record HistoryRow(Instant created, String accountName, UUID actor, String type, BigDecimal amount) {
    }

    private static void sendAccountHistory(final ServerPlayer player, final int recNo) {
        final BankAccount account = accessibleAccount(player, recNo);
        if (account == null) {
            return;
        }
        fetchHistory("select * from bank_transaction where account = ? order by rec_no desc limit " + HISTORY_LIMIT, statement ->
            statement.setInt(1, recNo), entries -> PacketDistributor.sendToPlayer(player, new ClientboundHistory(recNo, account.name(), entries)));
    }

    private static void sendPlayerHistory(final ServerPlayer player, final String rawName) {
        if (!PermissionAPI.getPermission(player, ADMIN)) {
            error(player, "You do not have permission to search bank history!");
            return;
        }
        final NameAndId target = findPlayer(rawName.trim()).orElse(null);
        if (target == null) {
            error(player, "There is no player called " + rawName.trim() + ".");
            return;
        }
        // What they did, and what was done on accounts they own
        fetchHistory("select * from bank_transaction where actor = ? or owner = ? order by rec_no desc limit " + ADMIN_HISTORY_LIMIT, statement -> {
            statement.setObject(1, target.id());
            statement.setObject(2, target.id());
        }, entries -> PacketDistributor.sendToPlayer(player, new ClientboundHistory(-1, target.name(), entries)));
    }

    @FunctionalInterface
    private interface Parameters {

        void set(PreparedStatement statement) throws SQLException;
    }

    private static void fetchHistory(final String sql, final Parameters parameters, final Consumer<List<HistoryEntry>> then) {
        AlmuraDatabase.run("read bank history", connection -> {
            final List<HistoryRow> rows = new ArrayList<>();
            try (PreparedStatement select = connection.prepareStatement(sql)) {
                parameters.set(select);
                try (ResultSet result = select.executeQuery()) {
                    while (result.next()) {
                        rows.add(new HistoryRow(result.getTimestamp("created").toInstant(), result.getString("account_name"),
                            result.getObject("actor", UUID.class), result.getString("type"), result.getBigDecimal("amount")));
                    }
                }
            }
            return rows;
        }, rows -> then.accept(rows.stream()
            // Names are looked up on the server's thread
            .map(row -> new HistoryEntry(row.created().toEpochMilli(), row.accountName(), name(row.actor()), parseType(row.type()),
                row.amount().toPlainString()))
            .toList()));
    }

    // ---- State

    private static void sendState(final ServerPlayer player, final boolean open) {
        final EconomyAccounts economy = Economy.accounts();
        if (economy == null) {
            return;
        }
        final UUID playerId = player.getUUID();
        final BigDecimal wallet = Optional.ofNullable(economy.balance(Account.player(playerId))).orElse(BigDecimal.ZERO);
        final List<AccountView> views = accounts.values().stream()
            .filter(account -> account.canAccess(playerId))
            .sorted(Comparator.<BankAccount>comparingInt(account -> account.isOwner(playerId) ? 0 : 1)
                .thenComparing(BankAccount::name, String.CASE_INSENSITIVE_ORDER))
            .map(account -> new AccountView(account.recNo(), account.name(), name(account.owner()), account.isOwner(playerId),
                account.canWithdraw(playerId),
                Optional.ofNullable(economy.balance(account.economyAccount())).orElse(BigDecimal.ZERO).toPlainString(),
                account.members().entrySet().stream().map(entry -> new Member(entry.getKey(), name(entry.getKey()), entry.getValue())).toList()))
            .toList();
        PacketDistributor.sendToPlayer(player, new ClientboundState(open, PermissionAPI.getPermission(player, ADMIN), accountLimit(player),
            wallet.toPlainString(), views));
    }

    /**
     * Sends the new state to everyone on the account who is online, and to the player who changed it.
     */
    private static void refreshViewers(final BankAccount account, final ServerPlayer player) {
        final Set<UUID> viewers = account.everyone();
        viewers.add(player.getUUID());
        sendStateTo(viewers);
    }

    private static void sendStateTo(final Set<UUID> players) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (final UUID uniqueId : players) {
            final ServerPlayer online = server.getPlayerList().getPlayer(uniqueId);
            if (online != null) {
                sendState(online, false);
            }
        }
    }

    // ---- Helpers

    @FunctionalInterface
    private interface Change {

        void run(Connection connection) throws SQLException;
    }

    /**
     * Runs a change on the database's thread, reads the accounts again, then the follow up on the server's thread.
     */
    private static void database(final String what, final Change change, final Runnable then) {
        AlmuraDatabase.run(what, connection -> {
            change.run(connection);
            return readAccounts(connection);
        }, loaded -> {
            accounts.clear();
            accounts.putAll(loaded);
            then.run();
        });
    }

    private static void reload(final Runnable then) {
        AlmuraDatabase.run("read the bank accounts", ServerBank::readAccounts, loaded -> {
            accounts.clear();
            accounts.putAll(loaded);
            then.run();
        });
    }

    private static Map<Integer, BankAccount> readAccounts(final Connection connection) throws SQLException {
        final Map<Integer, Map<UUID, Boolean>> members = new HashMap<>();
        try (PreparedStatement select = connection.prepareStatement("select account, member, can_withdraw from bank_account_member order by rec_no");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                members.computeIfAbsent(result.getInt("account"), key -> new LinkedHashMap<>())
                    .put(result.getObject("member", UUID.class), result.getBoolean("can_withdraw"));
            }
        }
        final Map<Integer, BankAccount> loaded = new LinkedHashMap<>();
        try (PreparedStatement select = connection.prepareStatement("select rec_no, owner, name, economy_id from bank_account order by rec_no");
             ResultSet result = select.executeQuery()) {
            while (result.next()) {
                final int recNo = result.getInt("rec_no");
                loaded.put(recNo, new BankAccount(recNo, result.getObject("owner", UUID.class), result.getString("name"),
                    result.getString("economy_id"), Collections.unmodifiableMap(members.getOrDefault(recNo, new LinkedHashMap<>()))));
            }
        }
        return loaded;
    }

    private static void log(final BankAccount account, final ServerPlayer actor, final BankTransactionType type, final BigDecimal amount) {
        final UUID actorId = actor.getUUID();
        AlmuraDatabase.change("record bank transaction " + type + " of " + amount + " on account " + account.recNo() + " by " + actorId,
            connection -> {
                try (PreparedStatement insert = connection.prepareStatement("insert into bank_transaction (created, account, account_name, owner, "
                    + "actor, type, amount) values (?, ?, ?, ?, ?, ?, ?)")) {
                    insert.setTimestamp(1, Timestamp.from(Instant.now()));
                    insert.setInt(2, account.recNo());
                    insert.setString(3, account.name());
                    insert.setObject(4, account.owner());
                    insert.setObject(5, actorId);
                    insert.setString(6, type.name());
                    insert.setBigDecimal(7, amount);
                    insert.executeUpdate();
                }
                return null;
            });
    }

    private static @Nullable BankAccount ownedAccount(final ServerPlayer player, final int recNo) {
        final BankAccount account = accounts.get(recNo);
        if (account == null || !account.isOwner(player.getUUID())) {
            error(player, "Only the account's owner may do that.");
            sendState(player, false);
            return null;
        }
        return account;
    }

    private static @Nullable BankAccount accessibleAccount(final ServerPlayer player, final int recNo) {
        final BankAccount account = accounts.get(recNo);
        if (account == null || !account.canAccess(player.getUUID())) {
            // Deleted, or the player was removed from it: show them what they have now
            sendState(player, false);
            return null;
        }
        return account;
    }

    private static @Nullable String validateName(final ServerPlayer player, final String rawName, final @Nullable BankAccount renaming) {
        final String name = rawName.replaceAll("§.", "").trim();
        if (name.isEmpty() || name.length() > BankMessages.MAX_NAME) {
            error(player, "Account names are 1 to " + BankMessages.MAX_NAME + " characters.");
            return null;
        }
        final boolean taken = accounts.values().stream()
            .anyMatch(account -> account != renaming && account.isOwner(player.getUUID()) && account.name().equalsIgnoreCase(name));
        if (taken) {
            error(player, "You already have an account called " + name + ".");
            return null;
        }
        return name;
    }

    private static @Nullable BigDecimal validateAmount(final ServerPlayer player, final BigDecimal rawAmount) {
        final BigDecimal amount = rawAmount.setScale(2, RoundingMode.DOWN);
        if (amount.signum() <= 0 || amount.compareTo(MAX_BALANCE) > 0) {
            error(player, "Enter an amount between 0.01 and " + BankMoney.format(MAX_BALANCE) + ".");
            return null;
        }
        return amount;
    }

    /**
     * @return how many accounts the player may own: {@code almura.bank.account.limit}, 3 unless the permission plugin gives a number
     */
    private static int accountLimit(final ServerPlayer player) {
        return Math.max(0, PermissionAPI.getPermission(player, ACCOUNT_LIMIT));
    }

    /**
     * @return the player called that (online, or known to the server), as typed or not
     */
    private static Optional<NameAndId> findPlayer(final String name) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || name.isEmpty()) {
            return Optional.empty();
        }
        final ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) {
            return Optional.of(new NameAndId(online.getUUID(), online.getGameProfile().name()));
        }
        return server.services().nameToIdCache().get(name);
    }

    private static String name(final UUID uniqueId) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return uniqueId.toString().substring(0, 8);
        }
        final ServerPlayer online = server.getPlayerList().getPlayer(uniqueId);
        if (online != null) {
            return online.getGameProfile().name();
        }
        return server.services().nameToIdCache().get(uniqueId).map(NameAndId::name).orElse(uniqueId.toString().substring(0, 8));
    }

    private static @Nullable UUID parseUuid(final String raw) {
        try {
            return UUID.fromString(raw);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private static BankTransactionType parseType(final String raw) {
        try {
            return BankTransactionType.valueOf(raw);
        } catch (final IllegalArgumentException e) {
            return BankTransactionType.DEPOSIT;
        }
    }

    private static void popup(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, TITLE, message, 5);
    }

    private static void error(final ServerPlayer player, final String message) {
        ServerNotifications.sendPopup(player, "§c" + TITLE, message, 5);
    }
}
