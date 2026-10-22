/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.bank.database.BankQueries;
import com.almuradev.almura.feature.bank.network.ClientboundBankHistoryPacket;
import com.almuradev.almura.feature.bank.network.ClientboundBankStatePacket;
import com.almuradev.almura.feature.bank.network.ServerboundBankActionPacket;
import com.almuradev.almura.feature.notification.ServerNotificationManager;
import com.almuradev.almura.shared.database.DatabaseManager;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.almura.shared.util.SerializationUtil;
import com.almuradev.core.event.Witness;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.slf4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Bank accounts: virtual accounts of the economy plugin holding the money, with their owner, name and members kept in the bank tables.
 * Deposits and withdrawals are single economy transfers between a player's wallet and an account, so money is never held anywhere else.
 *
 * <p>Permissions: almura.bank.use to use the bank, the almura.bank.account.limit option for how many accounts a player may own (default
 * {@link #DEFAULT_ACCOUNT_LIMIT}), almura.bank.admin to search any player's history. Owners rename, delete and manage members; members
 * deposit, and withdraw if allowed.</p>
 */
@Singleton
public final class ServerBankManager extends Witness.Impl implements Witness.Lifecycle {

    public static final String PERMISSION_USE = Almura.ID + ".bank.use";
    public static final String PERMISSION_ADMIN = Almura.ID + ".bank.admin";
    public static final String OPTION_ACCOUNT_LIMIT = Almura.ID + ".bank.account.limit";
    private static final int DEFAULT_ACCOUNT_LIMIT = 3;
    private static final int MAX_NAME_LENGTH = 32;
    private static final int HISTORY_LIMIT = 50, ADMIN_HISTORY_LIMIT = 200;
    // The economy plugin keeps balances as DECIMAL(11, 2)
    private static final BigDecimal MAX_BALANCE = new BigDecimal("999999999.99");
    private static final Text TITLE = Text.of("Bank");

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final Logger logger;
    private final ChannelBinding.IndexedMessageChannel network;
    private final DatabaseManager databaseManager;
    private final ServerNotificationManager notificationManager;

    private final Map<Integer, BankAccount> accounts = new LinkedHashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    @Inject
    public ServerBankManager(final PluginContainer container, final Scheduler scheduler, final Logger logger,
        @ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network, final DatabaseManager databaseManager,
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
        this.reloadAccounts(() -> this.logger.info("Loaded [{}] bank account(s).", this.accounts.size()));
    }

    /**
     * Opens the bank window (the /bank command and the Coin Exchange block).
     */
    public void openBank(final Player player) {
        if (!player.hasPermission(PERMISSION_USE)) {
            this.error(player, "You do not have permission to use the bank!");
            return;
        }
        if (!this.economy().isPresent()) {
            this.error(player, "The bank is unavailable: there is no economy on this server.");
            return;
        }
        this.sendState(player, true);
    }

    public void handleAction(final Player player, final ServerboundBankActionPacket message) {
        if (!player.hasPermission(PERMISSION_USE)) {
            this.error(player, "You do not have permission to use the bank!");
            return;
        }
        final EconomyService economy = this.economy().orElse(null);
        if (economy == null) {
            this.error(player, "The bank is unavailable: there is no economy on this server.");
            return;
        }

        switch (message.action) {
            case OPEN:
                this.openBank(player);
                break;
            case REFRESH:
                this.sendState(player, false);
                break;
            case CREATE:
                this.create(player, message.text);
                break;
            case RENAME:
                this.rename(player, message.account, message.text);
                break;
            case DELETE:
                this.delete(player, economy, message.account);
                break;
            case DEPOSIT:
                this.transfer(player, economy, message.account, message.amount, true);
                break;
            case WITHDRAW:
                this.transfer(player, economy, message.account, message.amount, false);
                break;
            case DEPOSIT_COINS:
                this.depositCoins(player, economy, message.account);
                break;
            case ADD_MEMBER:
                this.addMember(player, message.account, message.text, message.flag);
                break;
            case REMOVE_MEMBER:
                this.removeMember(player, message.account, message.member);
                break;
            case SET_MEMBER_WITHDRAW:
                this.setMemberWithdraw(player, message.account, message.member, message.flag);
                break;
            case HISTORY:
                this.sendAccountHistory(player, message.account);
                break;
            case ADMIN_HISTORY:
                this.sendPlayerHistory(player, message.text);
                break;
        }
    }

    /**
     * Accounts
     */

    private void create(final Player player, final String rawName) {
        final String name = this.validateName(player, rawName, null);
        if (name == null) {
            return;
        }
        final int limit = this.getAccountLimit(player);
        if (this.accounts.values().stream().filter(account -> account.isOwner(player.getUniqueId())).count() >= limit) {
            this.error(player, "You have reached your limit of " + limit + " bank account(s).");
            return;
        }

        final String economyId = "almura_bank_" + Long.toHexString(ThreadLocalRandom.current().nextLong());
        this.database(context -> BankQueries.insertAccount(context, player.getUniqueId(), name, economyId), () -> this.reloadAccounts(() -> {
            this.accounts.values().stream().filter(account -> account.getEconomyId().equals(economyId)).findFirst()
                .ifPresent(account -> this.log(account, player, BankTransactionType.CREATE, BigDecimal.ZERO));
            this.sendState(player, false);
        }));
    }

    private void rename(final Player player, final int recNo, final String rawName) {
        final BankAccount account = this.getOwnedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final String name = this.validateName(player, rawName, account);
        if (name == null) {
            return;
        }
        this.database(context -> BankQueries.renameAccount(context, recNo, name), () -> this.reloadAccounts(() -> {
            final BankAccount renamed = this.accounts.get(recNo);
            if (renamed != null) {
                this.log(renamed, player, BankTransactionType.RENAME, BigDecimal.ZERO);
                this.refreshViewers(renamed, player);
            }
        }));
    }

    private void delete(final Player player, final EconomyService economy, final int recNo) {
        final BankAccount account = this.getOwnedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final Account bankAccount = economy.getOrCreateAccount(account.getEconomyId()).orElse(null);
        final Account wallet = economy.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (bankAccount == null || wallet == null) {
            this.error(player, "The bank could not reach your accounts, try again later.");
            return;
        }

        // Pay what is left back to the owner first; the account is only closed if that worked
        final Currency currency = economy.getDefaultCurrency();
        final BigDecimal balance = bankAccount.getBalance(currency);
        if (balance.signum() > 0) {
            if (!this.transferBetween(player, bankAccount, wallet, currency, balance)) {
                this.error(player, "The account's balance could not be paid into your wallet, so it was not deleted.");
                return;
            }
            this.log(account, player, BankTransactionType.CLOSING_PAYOUT, balance);
        }

        final Set<UUID> viewers = new HashSet<>(account.getMembers().keySet());
        viewers.add(account.getOwner());
        this.log(account, player, BankTransactionType.DELETE, BigDecimal.ZERO);
        this.database(context -> BankQueries.deleteAccount(context, recNo), () -> this.reloadAccounts(() -> {
            this.sendStateTo(viewers);
            if (balance.signum() > 0) {
                this.notificationManager.sendPopupNotification(player, TITLE, Text.of("Closed ", account.getName(), ": ",
                    TextColors.GOLD, this.format(balance), TextColors.RESET, " paid into your wallet."), 5);
            }
        }));
    }

    /**
     * Money
     */

    private void transfer(final Player player, final EconomyService economy, final int recNo, final BigDecimal rawAmount, final boolean deposit) {
        final BankAccount account = this.getAccessibleAccount(player, recNo);
        if (account == null) {
            return;
        }
        if (!deposit && !account.canWithdraw(player.getUniqueId())) {
            this.error(player, "You may not withdraw from " + account.getName() + ".");
            return;
        }
        final BigDecimal amount = this.validateAmount(player, rawAmount);
        if (amount == null) {
            return;
        }

        final Account bankAccount = economy.getOrCreateAccount(account.getEconomyId()).orElse(null);
        final Account wallet = economy.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (bankAccount == null || wallet == null) {
            this.error(player, "The bank could not reach your accounts, try again later.");
            return;
        }

        final Currency currency = economy.getDefaultCurrency();
        final Account from = deposit ? wallet : bankAccount;
        final Account to = deposit ? bankAccount : wallet;
        if (from.getBalance(currency).compareTo(amount) < 0) {
            this.error(player, deposit ? "You don't have that much in your wallet." : "The account doesn't have that much.");
            return;
        }
        if (to.getBalance(currency).add(amount).compareTo(MAX_BALANCE) > 0) {
            this.error(player, "That would take " + (deposit ? "the account" : "your wallet") + " over the most it can hold ("
                + this.format(MAX_BALANCE) + ").");
            return;
        }
        if (!this.transferBetween(player, from, to, currency, amount)) {
            this.error(player, "The bank could not complete that, nothing was moved.");
            return;
        }

        this.log(account, player, deposit ? BankTransactionType.DEPOSIT : BankTransactionType.WITHDRAW, amount);
        this.refreshViewers(account, player);
    }

    /**
     * Deposits every coin in the player's inventory into an account they may use, or (account -1) their wallet. The coins are only taken once
     * the money is in.
     */
    private void depositCoins(final Player player, final EconomyService economy, final int recNo) {
        final BankAccount account = recNo < 0 ? null : this.getAccessibleAccount(player, recNo);
        if (recNo >= 0 && account == null) {
            return;
        }
        final Account target = account == null
            ? economy.getOrCreateAccount(player.getUniqueId()).orElse(null)
            : economy.getOrCreateAccount(account.getEconomyId()).orElse(null);
        if (target == null) {
            this.error(player, "The bank could not reach your accounts, try again later.");
            return;
        }

        final EntityPlayerMP mcPlayer = (EntityPlayerMP) player;
        final List<Integer> coinSlots = new ArrayList<>();
        long total = 0;
        int coins = 0;
        for (int slot = 0; slot < mcPlayer.inventory.getSizeInventory(); slot++) {
            final ItemStack stack = mcPlayer.inventory.getStackInSlot(slot);
            if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
                continue;
            }
            final long value = BankCoins.valueOf(stack.getItem().getRegistryName().toString());
            if (value > 0) {
                coinSlots.add(slot);
                total += value * stack.getCount();
                coins += stack.getCount();
            }
        }
        if (total == 0) {
            this.error(player, "You have no coins to deposit.");
            return;
        }

        final Currency currency = economy.getDefaultCurrency();
        final BigDecimal amount = BigDecimal.valueOf(total);
        if (target.getBalance(currency).add(amount).compareTo(MAX_BALANCE) > 0) {
            this.error(player, "That would take " + (account == null ? "your wallet" : "the account") + " over the most it can hold ("
                + this.format(MAX_BALANCE) + ").");
            return;
        }
        final boolean deposited;
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            deposited = target.deposit(currency, amount, frame.getCurrentCause()).getResult() == ResultType.SUCCESS;
        }
        if (!deposited) {
            this.error(player, "The bank could not take your coins, you still have them.");
            return;
        }

        for (final int slot : coinSlots) {
            mcPlayer.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        }
        mcPlayer.inventoryContainer.detectAndSendChanges();

        if (account != null) {
            this.log(account, player, BankTransactionType.COIN_DEPOSIT, amount);
            this.refreshViewers(account, player);
        } else {
            this.sendState(player, false);
        }
        this.notificationManager.sendPopupNotification(player, TITLE, Text.of("Deposited ", coins, " coin(s): ", TextColors.GOLD,
            this.format(amount), TextColors.RESET, " into ", account == null ? "your wallet" : account.getName(), "."), 5);
    }

    private boolean transferBetween(final Player player, final Account from, final Account to, final Currency currency, final BigDecimal amount) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            return from.transfer(to, currency, amount, frame.getCurrentCause()).getResult() == ResultType.SUCCESS;
        }
    }

    /**
     * Members
     */

    private void addMember(final Player player, final int recNo, final String memberName, final boolean canWithdraw) {
        final BankAccount account = this.getOwnedAccount(player, recNo);
        if (account == null) {
            return;
        }
        final User member = this.findUser(memberName.trim()).orElse(null);
        if (member == null) {
            this.error(player, "There is no player called " + memberName.trim() + ".");
            return;
        }
        if (account.isOwner(member.getUniqueId())) {
            this.error(player, "You already own " + account.getName() + ".");
            return;
        }
        if (account.isMember(member.getUniqueId())) {
            this.error(player, member.getName() + " is already on " + account.getName() + ".");
            return;
        }

        final UUID memberId = member.getUniqueId();
        this.database(context -> BankQueries.insertMember(context, recNo, memberId, canWithdraw), () -> this.reloadAccounts(() -> {
            final BankAccount updated = this.accounts.get(recNo);
            if (updated != null) {
                this.log(updated, player, BankTransactionType.MEMBER_ADD, BigDecimal.ZERO);
                this.refreshViewers(updated, player);
            }
        }));
    }

    /**
     * Removes a member: the owner removing anyone, or a member leaving.
     */
    private void removeMember(final Player player, final int recNo, final String rawMember) {
        final UUID memberId = this.parseUuid(rawMember);
        final BankAccount account = this.accounts.get(recNo);
        if (account == null || memberId == null || !account.isMember(memberId)
            || !account.isOwner(player.getUniqueId()) && !memberId.equals(player.getUniqueId())) {
            this.sendState(player, false);
            return;
        }

        this.database(context -> BankQueries.deleteMember(context, recNo, memberId), () -> this.reloadAccounts(() -> {
            this.log(account, player, BankTransactionType.MEMBER_REMOVE, BigDecimal.ZERO);
            final Set<UUID> viewers = new HashSet<>(account.getMembers().keySet());
            viewers.add(account.getOwner());
            this.sendStateTo(viewers);
        }));
    }

    private void setMemberWithdraw(final Player player, final int recNo, final String rawMember, final boolean canWithdraw) {
        final BankAccount account = this.getOwnedAccount(player, recNo);
        final UUID memberId = this.parseUuid(rawMember);
        if (account == null || memberId == null || !account.isMember(memberId)) {
            return;
        }
        this.database(context -> BankQueries.updateMember(context, recNo, memberId, canWithdraw), () -> this.reloadAccounts(() -> {
            final BankAccount updated = this.accounts.get(recNo);
            if (updated != null) {
                this.log(updated, player, BankTransactionType.MEMBER_CHANGE, BigDecimal.ZERO);
                this.refreshViewers(updated, player);
            }
        }));
    }

    /**
     * History
     */

    private void sendAccountHistory(final Player player, final int recNo) {
        final BankAccount account = this.getAccessibleAccount(player, recNo);
        if (account == null) {
            return;
        }
        this.fetchHistory(context -> BankQueries.fetchAccountHistory(context, recNo, HISTORY_LIMIT),
            entries -> this.network.sendTo(player, new ClientboundBankHistoryPacket(recNo, account.getName(), entries)));
    }

    private void sendPlayerHistory(final Player player, final String playerName) {
        if (!player.hasPermission(PERMISSION_ADMIN)) {
            this.error(player, "You do not have permission to search bank history!");
            return;
        }
        final User target = this.findUser(playerName.trim()).orElse(null);
        if (target == null) {
            this.error(player, "There is no player called " + playerName.trim() + ".");
            return;
        }
        final UUID targetId = target.getUniqueId();
        this.fetchHistory(context -> BankQueries.fetchPlayerHistory(context, targetId, ADMIN_HISTORY_LIMIT),
            entries -> this.network.sendTo(player, new ClientboundBankHistoryPacket(-1, target.getName(), entries)));
    }

    private void fetchHistory(final Function<DSLContext, List<Record>> query, final Consumer<List<BankHistoryEntry>> then) {
        this.scheduler.createTaskBuilder().async().execute(() -> {
            try (final DSLContext context = this.databaseManager.createContext(true)) {
                final List<Record> records = query.apply(context);
                // Names are looked up on the main thread
                this.scheduler.createTaskBuilder().execute(() -> then.accept(records.stream()
                    .map(record -> new BankHistoryEntry(
                        record.get(BankQueries.TRANSACTION_CREATED).toInstant(),
                        record.get(BankQueries.TRANSACTION_ACCOUNT_NAME),
                        this.getName(SerializationUtil.uniqueIdFromBytes(record.get(BankQueries.TRANSACTION_ACTOR))),
                        this.parseType(record.get(BankQueries.TRANSACTION_TYPE)),
                        record.get(BankQueries.TRANSACTION_AMOUNT)))
                    .collect(Collectors.toList()))).submit(this.container);
            } catch (final Exception e) {
                this.logger.error("Could not read bank history.", e);
            }
        }).submit(this.container);
    }

    /**
     * State
     */

    private void sendState(final Player player, final boolean open) {
        final EconomyService economy = this.economy().orElse(null);
        if (economy == null) {
            return;
        }
        final Currency currency = economy.getDefaultCurrency();
        final UUID playerId = player.getUniqueId();
        final BigDecimal wallet = economy.getOrCreateAccount(playerId).map(account -> account.getBalance(currency)).orElse(BigDecimal.ZERO);

        final List<BankAccountView> views = this.accounts.values()
            .stream()
            .filter(account -> account.canAccess(playerId))
            .sorted(Comparator.<BankAccount>comparingInt(account -> account.isOwner(playerId) ? 0 : 1)
                .thenComparing(BankAccount::getName, String.CASE_INSENSITIVE_ORDER))
            .map(account -> new BankAccountView(account.getRecNo(), account.getName(), this.getName(account.getOwner()),
                account.isOwner(playerId), account.canWithdraw(playerId),
                economy.getOrCreateAccount(account.getEconomyId()).map(bankAccount -> bankAccount.getBalance(currency)).orElse(BigDecimal.ZERO),
                account.getMembers().entrySet().stream()
                    .map(entry -> new BankAccountView.Member(entry.getKey(), this.getName(entry.getKey()), entry.getValue()))
                    .collect(Collectors.toList())))
            .collect(Collectors.toList());

        this.network.sendTo(player, new ClientboundBankStatePacket(open, player.hasPermission(PERMISSION_ADMIN), this.getAccountLimit(player),
            wallet, views));
    }

    /**
     * Sends the new state to everyone on the account who is online, and to the player who changed it.
     */
    private void refreshViewers(final BankAccount account, final Player player) {
        final Set<UUID> viewers = new HashSet<>(account.getMembers().keySet());
        viewers.add(account.getOwner());
        viewers.add(player.getUniqueId());
        this.sendStateTo(viewers);
    }

    private void sendStateTo(final Set<UUID> players) {
        players.forEach(uniqueId -> Sponge.getServer().getPlayer(uniqueId).ifPresent(online -> this.sendState(online, false)));
    }

    /**
     * Helpers
     */

    private void reloadAccounts(final Runnable then) {
        this.scheduler.createTaskBuilder().async().execute(() -> {
            try (final DSLContext context = this.databaseManager.createContext(true)) {
                final Map<Integer, Map<UUID, Boolean>> members = new HashMap<>();
                for (final Record record : BankQueries.fetchMembers(context)) {
                    members.computeIfAbsent(record.get(BankQueries.MEMBER_ACCOUNT), key -> new LinkedHashMap<>())
                        .put(SerializationUtil.uniqueIdFromBytes(record.get(BankQueries.MEMBER_MEMBER)),
                            Boolean.TRUE.equals(record.get(BankQueries.MEMBER_CAN_WITHDRAW)));
                }
                final Map<Integer, BankAccount> loaded = new LinkedHashMap<>();
                for (final Record record : BankQueries.fetchAccounts(context)) {
                    final int recNo = record.get(BankQueries.ACCOUNT_REC_NO);
                    loaded.put(recNo, new BankAccount(recNo, record.get(BankQueries.ACCOUNT_CREATED).toInstant(),
                        SerializationUtil.uniqueIdFromBytes(record.get(BankQueries.ACCOUNT_OWNER)), record.get(BankQueries.ACCOUNT_NAME),
                        record.get(BankQueries.ACCOUNT_ECONOMY_ID), members.getOrDefault(recNo, new LinkedHashMap<>())));
                }
                this.scheduler.createTaskBuilder().execute(() -> {
                    this.accounts.clear();
                    this.accounts.putAll(loaded);
                    then.run();
                }).submit(this.container);
            } catch (final Exception e) {
                this.logger.error("Could not load bank accounts.", e);
            }
        }).submit(this.container);
    }

    /**
     * Runs a database change off the main thread, then the follow up back on it.
     */
    private void database(final Consumer<DSLContext> change, final Runnable then) {
        this.scheduler.createTaskBuilder().async().execute(() -> {
            try (final DSLContext context = this.databaseManager.createContext(true)) {
                change.accept(context);
                this.scheduler.createTaskBuilder().execute(then).submit(this.container);
            } catch (final Exception e) {
                this.logger.error("Could not update the bank tables.", e);
            }
        }).submit(this.container);
    }

    private void log(final BankAccount account, final Player actor, final BankTransactionType type, final BigDecimal amount) {
        final int recNo = account.getRecNo();
        final String name = account.getName();
        final UUID owner = account.getOwner();
        final UUID actorId = actor.getUniqueId();
        this.scheduler.createTaskBuilder().async().execute(() -> {
            try (final DSLContext context = this.databaseManager.createContext(true)) {
                BankQueries.insertTransaction(context, recNo, name, owner, actorId, type, amount);
            } catch (final Exception e) {
                this.logger.error("Could not record bank transaction {} of {} on account {} by {}.", type, amount, recNo, actorId, e);
            }
        }).submit(this.container);
    }

    @Nullable
    private BankAccount getOwnedAccount(final Player player, final int recNo) {
        final BankAccount account = this.accounts.get(recNo);
        if (account == null || !account.isOwner(player.getUniqueId())) {
            this.error(player, "Only the account's owner may do that.");
            this.sendState(player, false);
            return null;
        }
        return account;
    }

    @Nullable
    private BankAccount getAccessibleAccount(final Player player, final int recNo) {
        final BankAccount account = this.accounts.get(recNo);
        if (account == null || !account.canAccess(player.getUniqueId())) {
            // Deleted, or the player was removed from it: show them what they have now
            this.sendState(player, false);
            return null;
        }
        return account;
    }

    @Nullable
    private String validateName(final Player player, final String rawName, @Nullable final BankAccount renaming) {
        final String name = rawName.replaceAll("§.", "").trim();
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH) {
            this.error(player, "Account names are 1 to " + MAX_NAME_LENGTH + " characters.");
            return null;
        }
        final boolean taken = this.accounts.values().stream()
            .anyMatch(account -> account != renaming && account.isOwner(player.getUniqueId()) && account.getName().equalsIgnoreCase(name));
        if (taken) {
            this.error(player, "You already have an account called " + name + ".");
            return null;
        }
        return name;
    }

    @Nullable
    private BigDecimal validateAmount(final Player player, final BigDecimal rawAmount) {
        final BigDecimal amount = rawAmount.setScale(2, RoundingMode.DOWN);
        if (amount.signum() <= 0 || amount.compareTo(MAX_BALANCE) > 0) {
            this.error(player, "Enter an amount between 0.01 and " + this.format(MAX_BALANCE) + ".");
            return null;
        }
        return amount;
    }

    /**
     * @return How many accounts the player may own: the almura.bank.account.limit option, or {@link #DEFAULT_ACCOUNT_LIMIT}
     */
    public int getAccountLimit(final Player player) {
        final String raw = player.getOption(OPTION_ACCOUNT_LIMIT).orElse(null);
        if (raw != null) {
            try {
                return Math.max(0, Integer.parseInt(raw.trim()));
            } catch (final NumberFormatException e) {
                this.logger.warn("Option '{}' of '{}' is '{}', not a number. Using {}.", OPTION_ACCOUNT_LIMIT, player.getName(), raw,
                    DEFAULT_ACCOUNT_LIMIT);
            }
        }
        return DEFAULT_ACCOUNT_LIMIT;
    }

    private Optional<EconomyService> economy() {
        return Sponge.getServiceManager().provide(EconomyService.class);
    }

    private Optional<User> findUser(final String name) {
        if (name.isEmpty()) {
            return Optional.empty();
        }
        return Sponge.getServiceManager().provide(UserStorageService.class).flatMap(service -> service.get(name));
    }

    private String getName(final UUID uniqueId) {
        return this.names.computeIfAbsent(uniqueId, key -> Sponge.getServiceManager().provide(UserStorageService.class)
            .flatMap(service -> service.get(key))
            .map(User::getName)
            .orElse(key.toString().substring(0, 8)));
    }

    @Nullable
    private UUID parseUuid(final String raw) {
        try {
            return UUID.fromString(raw);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private BankTransactionType parseType(final String raw) {
        try {
            return BankTransactionType.valueOf(raw);
        } catch (final IllegalArgumentException e) {
            return BankTransactionType.DEPOSIT;
        }
    }

    private String format(final BigDecimal amount) {
        return BankMoney.format(amount);
    }

    private void error(final Player player, final String message) {
        this.notificationManager.sendPopupNotification(player, Text.of(TextColors.RED, "Bank"), Text.of(message), 5);
    }
}
