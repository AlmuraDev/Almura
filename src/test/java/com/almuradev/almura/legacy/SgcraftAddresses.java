/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.legacy;

import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SGCraft's stargate addresses: 1.12's read back to the chunk and world they name, and 26.3's made for a chunk.
 * <p>
 * 1.12 (SGCraft 2.x, {@code gcewing.sg.util.SGAddressing}) worked an address out of the gate's chunk and its dimension: 36 symbols, seven
 * for the chunk (each coordinate hashed, then their base-6 digits interleaved) and two for the dimension's place in
 * {@code data/sgcraft-dimension_map.dat}, mixed with the chunk's. 26.3 divides each world into cells and names the cell, then the world
 * and its group, then the point of origin; its position symbols come from SGCraft's own {@code GridReach.encode}, called here through
 * reflection on the loaded mod, so they are exactly what the server works out.
 */
final class SgcraftAddresses {

    // ---- 1.12 (a port of gcewing.sg.util.SGAddressing, its constants unchanged)

    private static final String SYMBOLS_112 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final long MAX_COORD = 139967;
    private static final long MIN_COORD = -MAX_COORD;
    private static final long MC = 2 * MAX_COORD + 1 + 2;
    private static final long PC = 93563;
    private static final long QC = 153742;
    private static final int DIMENSION_RANGE = 1296;
    private static final long MD = DIMENSION_RANGE + 1;
    private static final long PD = 953;
    private static final long QD = 788;

    /**
     * A 1.12 address read back.
     *
     * @param chunkX the gate's chunk
     * @param chunkZ the gate's chunk
     * @param dimensionIndex its dimension's place in the dimension map, or -1 for a seven-symbol (same-world) address
     */
    record Legacy(int chunkX, int chunkZ, int dimensionIndex) {
    }

    /**
     * @return the chunk (and dimension index) a 1.12 address names, or empty for one that isn't seven or nine of 1.12's symbols
     */
    static Optional<Legacy> decode112(final String text) {
        final String address = text.replace("-", "").replace(" ", "").toUpperCase(java.util.Locale.ROOT);
        if (address.length() != 7 && address.length() != 9) {
            return Optional.empty();
        }
        for (int i = 0; i < address.length(); i++) {
            if (SYMBOLS_112.indexOf(address.charAt(i)) < 0) {
                return Optional.empty();
            }
        }
        final long c = fromSymbols(address.substring(0, 7));
        final long[] xz = uninterleave(c);
        final int chunkX = (int) (MIN_COORD + hash(xz[0], QC, MC));
        final int chunkZ = (int) (MIN_COORD + hash(xz[1], QC, MC));
        int index = -1;
        if (address.length() == 9) {
            final long dp = hash(fromSymbols(address.substring(7)), QD, MD);
            index = (int) Math.floorMod(dp - c, DIMENSION_RANGE);
        }
        return Optional.of(new Legacy(chunkX, chunkZ, index));
    }

    /**
     * @return the nine-symbol 1.12 address of a chunk in the dimension at that index of the dimension map
     */
    static String encode112(final int chunkX, final int chunkZ, final int dimensionIndex) {
        final long c = interleave(hash(chunkX - MIN_COORD, PC, MC), hash(chunkZ - MIN_COORD, PC, MC));
        final long d = hash((dimensionIndex + c) % DIMENSION_RANGE, PD, MD);
        return toSymbols(c, 7) + toSymbols(d, 2);
    }

    private static long hash(final long i, final long f, final long m) {
        return ((i + 1) * f) % m - 1;
    }

    private static long fromSymbols(final String symbols) {
        long value = 0;
        for (int i = 0; i < symbols.length(); i++) {
            value = value * 36 + SYMBOLS_112.indexOf(symbols.charAt(i));
        }
        return value;
    }

    private static String toSymbols(long value, final int count) {
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) {
            out.insert(0, SYMBOLS_112.charAt((int) (value % 36)));
            value /= 36;
        }
        return out.toString();
    }

    private static long interleave(long x, long z) {
        long p6 = 1, c = 0;
        while (x > 0 || z > 0) {
            c += p6 * (x % 6);
            x /= 6;
            p6 *= 6;
            c += p6 * (z % 6);
            z /= 6;
            p6 *= 6;
        }
        return c;
    }

    private static long[] uninterleave(long c) {
        long p6 = 1;
        final long[] xz = {0, 0};
        while (c > 0) {
            xz[0] += p6 * (c % 6);
            c /= 6;
            xz[1] += p6 * (c % 6);
            c /= 6;
            p6 *= 6;
        }
        return xz;
    }

    // ---- 26.3 (SGCraft's own code)

    /**
     * How the 4.0 server lays one world out: its cell size, its point of origin (or none), and the symbols naming it and its group (none
     * until the server has given them).
     */
    record Layout(int cellChunks, @Nullable Character origin, @Nullable Character worldSymbol, @Nullable Character groupSymbol) {
    }

    private final Method encode;
    private final Method decode;
    private final Constructor<?> partition;
    private final Constructor<?> address;
    private final Method symbolOf;
    private final Map<String, Layout> layouts;
    private final boolean grouped;

    /**
     * @param loader the class loader SGCraft's classes are in (its blocks')
     * @param layouts each 4.0 world's layout, by its id
     * @param grouped whether the server has more than one group, so a lasting address names the group too
     */
    SgcraftAddresses(final ClassLoader loader, final Map<String, Layout> layouts, final boolean grouped) throws ReflectiveOperationException {
        final Class<?> gridReach = Class.forName("com.almuradev.sgcraft.mod.address.GridReach", true, loader);
        final Class<?> addressPartition = Class.forName("com.almuradev.sgcraft.api.address.AddressPartition", true, loader);
        final Class<?> laidOut = Class.forName("com.almuradev.sgcraft.mod.address.LaidOutPartition", true, loader);
        final Class<?> symbol = Class.forName("com.almuradev.sgcraft.api.address.Symbol", true, loader);
        final Class<?> addressClass = Class.forName("com.almuradev.sgcraft.api.address.Address", true, loader);
        this.encode = gridReach.getMethod("encode", ChunkPos.class, addressPartition, int.class);
        this.decode = gridReach.getMethod("decode", List.class, addressPartition);
        this.partition = laidOut.getConstructor(int.class, Optional.class, Optional.class);
        this.address = addressClass.getConstructor(List.class);
        this.symbolOf = symbol.getMethod("of", char.class);
        this.layouts = layouts;
        this.grouped = grouped;
    }

    Optional<Layout> layout(final String world) {
        return Optional.ofNullable(this.layouts.get(world));
    }

    /**
     * The address of a chunk's cell that reaches it from every world, as a PDD and a gate's address rules keep it: its position, the world's
     * symbol, the group's if the server has more than one, then the world's point of origin (SGCraft's {@code lastingAddressOf}).
     *
     * @return the address as 26.3 writes it ({@code XXXX-XXX-X@}), or empty when the world has no symbol yet or the cell is off the grid
     */
    Optional<String> lasting(final String world, final int chunkX, final int chunkZ) {
        final Layout layout = this.layouts.get(world);
        if (layout == null || layout.worldSymbol == null || this.grouped && layout.groupSymbol == null) {
            return Optional.empty();
        }
        final List<Character> after = new ArrayList<>();
        after.add(layout.worldSymbol);
        if (this.grouped) {
            after.add(layout.groupSymbol);
        }
        return this.address(layout, chunkX, chunkZ, this.grouped ? 9 : 8, after);
    }

    /**
     * @return the address a gate in the same world dials a chunk's cell by (seven symbols, SGCraft's same-world reach)
     */
    Optional<String> local(final String world, final int chunkX, final int chunkZ) {
        final Layout layout = this.layouts.get(world);
        return layout == null ? Optional.empty() : this.address(layout, chunkX, chunkZ, 7, List.of());
    }

    private Optional<String> address(final Layout layout, final int chunkX, final int chunkZ, final int total, final List<Character> after) {
        try {
            final int positions = total - after.size() - (layout.origin == null ? 0 : 1);
            final Object origin = layout.origin == null ? Optional.empty() : this.symbolOf.invoke(null, layout.origin);
            final Object partition = this.partition.newInstance(layout.cellChunks, origin, Optional.empty());
            final Optional<?> position = (Optional<?>) this.encode.invoke(null, new ChunkPos(chunkX, chunkZ), partition, positions);
            if (position.isEmpty()) {
                return Optional.empty();
            }
            final List<Object> symbols = new ArrayList<>((List<?>) position.get());
            for (final char symbol : after) {
                symbols.add(((Optional<?>) this.symbolOf.invoke(null, symbol)).orElseThrow());
            }
            if (layout.origin != null) {
                symbols.add(((Optional<?>) origin).orElseThrow());
            }
            return Optional.of(this.address.newInstance(symbols).toString());
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("SGCraft's address code couldn't be called", e);
        }
    }

    /**
     * Checks the addresses made through SGCraft against a plain port of its encoding and against SGCraft's own decoding (each cell's
     * position read back to the cell's first chunk), and the 1.12 port against itself, for each chunk given.
     *
     * @return what was checked and anything that didn't match
     */
    List<String> verify(final String world, final List<ChunkPos> chunks, final int dimensionIndex) {
        final List<String> out = new ArrayList<>();
        final Layout layout = this.layouts.get(world);
        if (layout == null) {
            out.add("no layout for " + world);
            return out;
        }
        int matched = 0;
        try {
            final int positions = 7 - (layout.origin == null ? 0 : 1);
            final Object origin = layout.origin == null ? Optional.empty() : this.symbolOf.invoke(null, layout.origin);
            final Object partition = this.partition.newInstance(layout.cellChunks, origin, Optional.empty());
            for (final ChunkPos chunk : chunks) {
                final Optional<?> encoded = (Optional<?>) this.encode.invoke(null, chunk, partition, positions);
                if (encoded.isEmpty()) {
                    out.add(chunk + ": off the grid");
                    continue;
                }
                final String reflective = this.address.newInstance(encoded.get()).toString().replace("-", "");
                final String ported = portedPosition(chunk.x(), chunk.z(), layout.cellChunks, positions);
                final Optional<?> decoded = (Optional<?>) this.decode.invoke(null, encoded.get(), partition);
                final ChunkPos cell = new ChunkPos(Math.floorDiv(chunk.x(), layout.cellChunks) * layout.cellChunks,
                    Math.floorDiv(chunk.z(), layout.cellChunks) * layout.cellChunks);
                final Optional<Legacy> legacy = decode112(encode112(chunk.x(), chunk.z(), dimensionIndex));
                if (!reflective.equals(ported) || decoded.isEmpty() || !decoded.get().equals(cell) || legacy.isEmpty()
                    || legacy.get().chunkX() != chunk.x() || legacy.get().chunkZ() != chunk.z() || legacy.get().dimensionIndex() != dimensionIndex) {
                    out.add("MISMATCH at " + chunk + ": SGCraft " + reflective + ", port " + ported + ", SGCraft decodes " + decoded + ", 1.12 " + legacy);
                } else {
                    matched++;
                }
            }
        } catch (final ReflectiveOperationException e) {
            out.add("SGCraft's address code couldn't be called: " + e);
        }
        out.add(0, matched + " of " + chunks.size() + " cells: SGCraft's GridReach.encode = the plain port, GridReach.decode reads each back,"
            + " and the 1.12 port round-trips");
        return out;
    }

    /**
     * The position symbols of a cell by a plain port of SGCraft's {@code GridReach.encode} (no secret layout), to check the reflective
     * call against.
     */
    static String portedPosition(final int chunkX, final int chunkZ, final int cellChunks, final int positions) {
        final java.math.BigInteger base = java.math.BigInteger.valueOf(38);
        final long side = base.pow(positions).sqrt().longValueExact();
        final long column = Math.floorDiv(chunkX, cellChunks) + side / 2;
        final long row = Math.floorDiv(chunkZ, cellChunks) + side / 2;
        java.math.BigInteger code = java.math.BigInteger.valueOf(column).multiply(java.math.BigInteger.valueOf(side))
            .add(java.math.BigInteger.valueOf(row)).multiply(java.math.BigInteger.valueOf(2_654_435_761L))
            .add(java.math.BigInteger.valueOf(1_013_904_223L)).mod(base.pow(positions));
        final char[] out = new char[positions];
        final String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@+.";
        for (int i = positions - 1; i >= 0; i--) {
            final int digit = code.mod(base).intValueExact();
            out[i] = symbols.charAt(digit < 36 ? digit : digit + 1);
            code = code.divide(base);
        }
        return new String(out);
    }

    /**
     * @return the position symbols of an address as SGCraft wrote it (its first {@code count} symbols)
     */
    static String positionOf(final String address, final int count) {
        return address.replace("-", "").substring(0, count);
    }
}
