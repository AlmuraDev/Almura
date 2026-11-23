/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 3.5's stores and exchanges, from its H2 1.4 database ({@code config/almura/h2.mv.db}, schema {@code dev}), into 4.0's
 * ({@code config/almura/database/almura.mv.db}).
 * <p>
 * The tables are 3.5's, renamed: a store's items were in two tables, {@code store_selling_item} (what the store sells: 4.0's BUY side) and
 * {@code store_buying_item} (what it buys: SELL), each with its transactions; an exchange's in {@code axs_list_item},
 * {@code axs_for_sale_item} and {@code axs_transaction}. An item was its 1.12 id ({@code domain}, {@code path}), its {@code metadata} and,
 * when it had NBT, a row in the matching {@code *_data} table (uncompressed NBT); 4.0 keeps an item as the JSON of the game's item codec. Each
 * goes through the world converter's item upgrade (the game's data fixer, Almura's moved items by their name, SGCraft's and Iron Backpacks'
 * items). An item 4.0 doesn't have is left out with what depends on it, and counted.
 * <p>
 * 3.5's database is read from a copy, with H2 1.4 in a class loader of its own (4.0 runs H2 2, which can't open it). A store or exchange
 * already in 4.0 under the same id is replaced; others are kept. New rows get 4.0's own numbers.
 */
final class LegacyMarketConversion {

    private final LegacyWorldConverter converter;
    final Map<String, Integer> lostItems = new TreeMap<>();
    final Map<String, String> movedItems = new TreeMap<>();
    final Map<String, Integer> counts = new TreeMap<>();
    @Nullable String sample;

    LegacyMarketConversion(final LegacyWorldConverter converter) {
        this.converter = converter;
    }

    /**
     * Opens 3.5's database (a copy of it), with H2 1.4 from its jar.
     */
    static Connection openLegacy(final Path h2Jar, final Path databaseCopy) throws IOException, SQLException {
        final URLClassLoader loader = new URLClassLoader(new URL[] {h2Jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader());
        final Driver driver;
        try {
            driver = (Driver) Class.forName("org.h2.Driver", true, loader).getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IOException("Couldn't load H2 1.4 from " + h2Jar, e);
        }
        final String file = databaseCopy.toString().replace('\\', '/');
        final Properties properties = new Properties();
        // 3.5 opened it through Sponge's SQL service, with no user
        properties.setProperty("user", "");
        properties.setProperty("password", "");
        final Connection connection = driver.connect("jdbc:h2:file:" + file.substring(0, file.length() - ".mv.db".length())
            + ";DATABASE_TO_UPPER=FALSE;SCHEMA=dev", properties);
        if (connection == null) {
            throw new SQLException("H2 1.4 didn't take " + databaseCopy);
        }
        return connection;
    }

    /**
     * Opens 4.0's database as Almura does (H2 2, user sa), making its store and exchange tables if they aren't there yet.
     */
    static Connection openCurrent(final Path databaseFile) throws SQLException, IOException {
        final String file = databaseFile.toString().replace('\\', '/');
        final Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");
        final Connection connection = new org.h2.Driver().connect("jdbc:h2:file:" + file.substring(0, file.length() - ".mv.db".length()), properties);
        for (final String schema : new String[] {"/almura/database/store.sql", "/almura/database/exchange.sql"}) {
            try (InputStream in = LegacyMarketConversion.class.getResourceAsStream(schema); Statement statement = connection.createStatement()) {
                if (in == null) {
                    throw new IOException("No " + schema + " on the classpath");
                }
                // Comments out first: they may hold a ";"
                final String script = new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)--.*$", "");
                for (final String sql : script.split(";")) {
                    final String stripped = sql.trim();
                    if (!stripped.isEmpty()) {
                        statement.execute(stripped);
                    }
                }
            }
        }
        return connection;
    }

    void convert(final Connection legacy, final Connection current) throws SQLException, IOException {
        current.setAutoCommit(false);
        try {
            this.stores(legacy, current);
            this.exchanges(legacy, current);
            current.commit();
        } catch (final SQLException | IOException | RuntimeException e) {
            current.rollback();
            throw e;
        }
    }

    // ---- Stores

    private void stores(final Connection legacy, final Connection current) throws SQLException, IOException {
        try (Statement select = legacy.createStatement(); ResultSet stores = select.executeQuery(
            "select \"id\", \"created\", \"creator\", \"name\", \"permission\", \"is_hidden\" from \"store\" order by \"rec_no\"")) {
            while (stores.next()) {
                final String id = stores.getString("id");
                try (PreparedStatement delete = current.prepareStatement("delete from store where id = ?")) {
                    delete.setString(1, id);
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = current.prepareStatement(
                    "insert into store (id, created, creator, name, permission, is_hidden) values (?, ?, ?, ?, ?, ?)")) {
                    insert.setString(1, id);
                    insert.setTimestamp(2, stores.getTimestamp("created"));
                    insert.setObject(3, uuid(stores.getBytes("creator")));
                    insert.setString(4, stores.getString("name"));
                    insert.setString(5, stores.getString("permission"));
                    insert.setBoolean(6, stores.getBoolean("is_hidden"));
                    insert.executeUpdate();
                }
                this.count("stores");
            }
        }
        // What a store sells is what players buy, and what it buys is what they sell
        final Map<Integer, Integer> selling = this.storeItems(legacy, current, "store_selling_item", "store_selling_item_data", "selling_item", "BUY");
        final Map<Integer, Integer> buying = this.storeItems(legacy, current, "store_buying_item", "store_buying_item_data", "buying_item", "SELL");
        this.storeTransactions(legacy, current, "store_selling_transaction", "selling_item", selling, "BUY");
        this.storeTransactions(legacy, current, "store_buying_transaction", "buying_item", buying, "SELL");
    }

    private Map<Integer, Integer> storeItems(final Connection legacy, final Connection current, final String table, final String dataTable,
        final String dataKey, final String side) throws SQLException, IOException {
        final Map<Integer, byte[]> data = data(legacy, dataTable, dataKey);
        final Map<Integer, Integer> numbers = new HashMap<>();
        try (Statement select = legacy.createStatement(); ResultSet items = select.executeQuery("select \"rec_no\", \"created\", \"store\", \"domain\", "
            + "\"path\", \"quantity\", \"metadata\", \"price\", \"index\", \"is_hidden\" from \"" + table + "\" order by \"rec_no\"")) {
            while (items.next()) {
                final String json = this.item(items.getString("domain"), items.getString("path"), items.getInt("metadata"),
                    data.get(items.getInt("rec_no")));
                if (json == null) {
                    this.count("store items left out");
                    continue;
                }
                try (PreparedStatement insert = current.prepareStatement("insert into store_item (created, store, side, item, quantity, price, "
                    + "sort_index, is_hidden) values (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setTimestamp(1, items.getTimestamp("created"));
                    insert.setString(2, items.getString("store"));
                    insert.setString(3, side);
                    insert.setString(4, json);
                    insert.setInt(5, items.getInt("quantity"));
                    insert.setBigDecimal(6, items.getBigDecimal("price"));
                    insert.setInt(7, items.getInt("index"));
                    insert.setBoolean(8, items.getBoolean("is_hidden"));
                    insert.executeUpdate();
                    numbers.put(items.getInt("rec_no"), generated(insert));
                }
                this.count("store items (" + side + ")");
            }
        }
        return numbers;
    }

    private void storeTransactions(final Connection legacy, final Connection current, final String table, final String itemKey,
        final Map<Integer, Integer> items, final String side) throws SQLException {
        try (Statement select = legacy.createStatement(); ResultSet rows = select.executeQuery("select \"created\", \"" + itemKey
            + "\", \"buyer\", \"quantity\", \"price\" from \"" + table + "\" order by \"rec_no\"")) {
            while (rows.next()) {
                final Integer item = items.get(rows.getInt(itemKey));
                if (item == null) {
                    this.count("store transactions left out (item left out)");
                    continue;
                }
                try (PreparedStatement insert = current.prepareStatement(
                    "insert into store_transaction (created, store_item, side, player, quantity, price) values (?, ?, ?, ?, ?, ?)")) {
                    insert.setTimestamp(1, rows.getTimestamp("created"));
                    insert.setInt(2, item);
                    insert.setString(3, side);
                    insert.setObject(4, uuid(rows.getBytes("buyer")));
                    insert.setInt(5, rows.getInt("quantity"));
                    insert.setBigDecimal(6, rows.getBigDecimal("price"));
                    insert.executeUpdate();
                }
                this.count("store transactions");
            }
        }
    }

    // ---- Exchanges

    private void exchanges(final Connection legacy, final Connection current) throws SQLException, IOException {
        try (Statement select = legacy.createStatement(); ResultSet exchanges = select.executeQuery(
            "select \"id\", \"created\", \"creator\", \"name\", \"permission\", \"is_hidden\" from \"axs\" order by \"rec_no\"")) {
            while (exchanges.next()) {
                final String id = exchanges.getString("id");
                try (PreparedStatement delete = current.prepareStatement("delete from axs where id = ?")) {
                    delete.setString(1, id);
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = current.prepareStatement(
                    "insert into axs (id, created, creator, name, permission, is_hidden) values (?, ?, ?, ?, ?, ?)")) {
                    insert.setString(1, id);
                    insert.setTimestamp(2, exchanges.getTimestamp("created"));
                    insert.setObject(3, uuid(exchanges.getBytes("creator")));
                    insert.setString(4, exchanges.getString("name"));
                    insert.setString(5, exchanges.getString("permission"));
                    insert.setBoolean(6, exchanges.getBoolean("is_hidden"));
                    insert.executeUpdate();
                }
                this.count("exchanges");
            }
        }
        final Map<Integer, byte[]> data = data(legacy, "axs_list_item_data", "list_item");
        final Map<Integer, Integer> listItems = new HashMap<>();
        try (Statement select = legacy.createStatement(); ResultSet items = select.executeQuery("select \"rec_no\", \"created\", \"axs\", \"seller\", "
            + "\"domain\", \"path\", \"quantity\", \"metadata\", \"index\", \"last_known_price\", \"is_hidden\" from \"axs_list_item\" order by \"rec_no\"")) {
            while (items.next()) {
                final String json = this.item(items.getString("domain"), items.getString("path"), items.getInt("metadata"),
                    data.get(items.getInt("rec_no")));
                if (json == null) {
                    this.count("exchange listings left out");
                    continue;
                }
                try (PreparedStatement insert = current.prepareStatement("insert into axs_list_item (created, axs, seller, item, quantity, list_index, "
                    + "last_known_price, is_hidden) values (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setTimestamp(1, items.getTimestamp("created"));
                    insert.setString(2, items.getString("axs"));
                    insert.setObject(3, uuid(items.getBytes("seller")));
                    insert.setString(4, json);
                    insert.setInt(5, items.getInt("quantity"));
                    insert.setInt(6, items.getInt("index"));
                    insert.setBigDecimal(7, items.getBigDecimal("last_known_price"));
                    insert.setBoolean(8, items.getBoolean("is_hidden"));
                    insert.executeUpdate();
                    listItems.put(items.getInt("rec_no"), generated(insert));
                }
                this.count("exchange listings");
            }
        }
        final Map<Integer, Integer> forSale = new HashMap<>();
        try (Statement select = legacy.createStatement(); ResultSet rows = select.executeQuery(
            "select \"rec_no\", \"created\", \"list_item\", \"price\", \"is_hidden\" from \"axs_for_sale_item\" order by \"rec_no\"")) {
            while (rows.next()) {
                final Integer listItem = listItems.get(rows.getInt("list_item"));
                if (listItem == null) {
                    this.count("exchange sales left out (listing left out)");
                    continue;
                }
                try (PreparedStatement insert = current.prepareStatement(
                    "insert into axs_for_sale_item (created, list_item, price, is_hidden) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setTimestamp(1, rows.getTimestamp("created"));
                    insert.setInt(2, listItem);
                    insert.setBigDecimal(3, rows.getBigDecimal("price"));
                    insert.setBoolean(4, rows.getBoolean("is_hidden"));
                    insert.executeUpdate();
                    forSale.put(rows.getInt("rec_no"), generated(insert));
                }
                this.count("exchange sales");
            }
        }
        try (Statement select = legacy.createStatement(); ResultSet rows = select.executeQuery(
            "select \"created\", \"for_sale_item\", \"buyer\", \"price\", \"quantity\" from \"axs_transaction\" order by \"rec_no\"")) {
            while (rows.next()) {
                final Integer sale = forSale.get(rows.getInt("for_sale_item"));
                if (sale == null) {
                    this.count("exchange transactions left out (sale left out)");
                    continue;
                }
                try (PreparedStatement insert = current.prepareStatement(
                    "insert into axs_transaction (created, for_sale_item, buyer, price, quantity) values (?, ?, ?, ?, ?)")) {
                    insert.setTimestamp(1, rows.getTimestamp("created"));
                    insert.setInt(2, sale);
                    insert.setObject(3, uuid(rows.getBytes("buyer")));
                    insert.setBigDecimal(4, rows.getBigDecimal("price"));
                    insert.setInt(5, rows.getInt("quantity"));
                    insert.executeUpdate();
                }
                this.count("exchange transactions");
            }
        }
    }

    // ---- Items

    /**
     * A 3.5 item (one of it) as 4.0 keeps it: the JSON of the game's item codec, or null when 4.0 has no such item.
     */
    private @Nullable String item(final String domain, final String path, final int metadata, final byte @Nullable [] data) throws IOException {
        final CompoundTag legacy = new CompoundTag();
        legacy.putString("id", this.moved(domain + ":" + path));
        legacy.putByte("Count", (byte) 1);
        legacy.putShort("Damage", (short) metadata);
        if (data != null && data.length > 0) {
            legacy.put("tag", NbtIo.read(new DataInputStream(new ByteArrayInputStream(data)), NbtAccounter.unlimitedHeap()));
        }
        final CompoundTag item = this.converter.legacyItem(legacy);
        if (item == null) {
            this.lostItems.merge(domain + ":" + path + (metadata == 0 ? "" : "@" + metadata), 1, Integer::sum);
            return null;
        }
        item.putInt("count", 1);
        final String json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, item).toString();
        if (this.sample == null && data != null) {
            this.sample = json;
        }
        return json;
    }

    /**
     * An Almura item 4.0 keeps in another folder (3.5's {@code almura:normal/building/juko_plank} is 4.0's
     * {@code almura:horizontal/building/juko_plank}): the one 4.0 item of that name; any other id as it is.
     */
    private String moved(final String id) {
        final Identifier parsed = Identifier.tryParse(id);
        if (parsed == null || !parsed.getNamespace().equals("almura") || BuiltInRegistries.ITEM.containsKey(parsed)) {
            return id;
        }
        final String last = "/" + parsed.getPath().substring(parsed.getPath().lastIndexOf('/') + 1);
        Identifier found = null;
        for (final Identifier key : BuiltInRegistries.ITEM.keySet()) {
            if (key.getNamespace().equals("almura") && ("/" + key.getPath()).endsWith(last)) {
                if (found != null) {
                    return id;
                }
                found = key;
            }
        }
        if (found == null) {
            return id;
        }
        this.movedItems.put(id, found.toString());
        return found.toString();
    }

    private static Map<Integer, byte[]> data(final Connection legacy, final String table, final String key) throws SQLException {
        final Map<Integer, byte[]> data = new HashMap<>();
        try (Statement select = legacy.createStatement(); ResultSet rows = select.executeQuery("select \"" + key + "\", \"data\" from \"" + table + "\"")) {
            while (rows.next()) {
                data.put(rows.getInt(key), rows.getBytes("data"));
            }
        }
        return data;
    }

    private static UUID uuid(final byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    private static int generated(final PreparedStatement insert) throws SQLException {
        try (ResultSet keys = insert.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No rec_no for a new row");
            }
            return keys.getInt(1);
        }
    }

    private void count(final String what) {
        this.counts.merge(what, 1, Integer::sum);
    }

    String report() {
        return "Counts: " + this.counts + "\nAlmura items found under their 4.0 folder: " + this.movedItems.size() + " " + this.movedItems
            + "\nItems 4.0 doesn't have (left out): " + this.lostItems + "\nSample item JSON: " + this.sample + "\n";
    }

    static Path backup(final Path databaseFile, final Path backupFolder) throws IOException {
        Files.createDirectories(backupFolder);
        final Path copy = backupFolder.resolve(databaseFile.getFileName());
        Files.copy(databaseFile, copy);
        return copy;
    }

}
