/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.relight;

import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.string;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

/**
 * /almura relight [radius]: recalculates the light around light sources whose light never spread (chunks placed or generated
 * without light updates), in the loaded chunks around the player, and resends those chunks so players see it. Minecraft's own
 * chunk relight leaves such sources alone when they already store their full light, and never sends light-only changes to
 * players who have the chunk.
 */
public final class RelightCommand {

    private static final int DEFAULT_RADIUS = 6;
    private static final int MAX_RADIUS = 10;

    @Inject private static PluginContainer container;
    @Inject private static Logger logger;

    private RelightCommand() {
    }

    public static CommandSpec generateRelightCommand() {
        return CommandSpec.builder()
                .permission("almura.command.relight")
                .description(Text.of("Recalculates the light of light sources around you and resends the chunks"))
                .child(generateWorldCommand(), "world")
                .child(generateStopCommand(), "stop")
                .arguments(optional(integer(Text.of("radius"))))
                .executor((source, args) -> {
                    if (!(source instanceof Player)) {
                        throw new CommandException(Text.of("Only a player can relight (around themselves)."));
                    }
                    final int radius = MathHelper.clamp(args.<Integer>getOne("radius").orElse(DEFAULT_RADIUS), 0, MAX_RADIUS);
                    final Entity player = (Entity) source;
                    final WorldServer world = (WorldServer) player.world;
                    final int centerX = MathHelper.floor(player.posX) >> 4;
                    final int centerZ = MathHelper.floor(player.posZ) >> 4;

                    final List<Chunk> chunks = new ArrayList<>();
                    int relit = 0;
                    for (int x = centerX - radius; x <= centerX + radius; x++) {
                        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                            final Chunk chunk = world.getChunkProvider().getLoadedChunk(x, z);
                            if (chunk != null) {
                                relit += relight(world, chunk);
                                chunks.add(chunk);
                            }
                        }
                    }
                    for (final Chunk chunk : chunks) {
                        final PlayerChunkMapEntry entry = world.getPlayerChunkMap().getEntry(chunk.x, chunk.z);
                        if (entry != null && entry.isSentToPlayers()) {
                            entry.sendPacket(new SPacketChunkData(chunk, 65535));
                        }
                    }

                    source.sendMessage(Text.of(TextColors.GREEN, "Relit ", relit, " light sources in ", chunks.size(), " chunks (radius ", radius, ")."));
                    return CommandResult.success();
                })
                .build();
    }

    /** /almura relight world [name]: relights every saved chunk of a world (the player's own without a name), see {@link WorldRelighter}. */
    private static CommandSpec generateWorldCommand() {
        return CommandSpec.builder()
                .permission("almura.command.relight")
                .description(Text.of("Relights every saved chunk of a world, a little each tick"))
                .arguments(optional(string(Text.of("world"))))
                .executor((source, args) -> {
                    if (WorldRelighter.isRunning()) {
                        throw new CommandException(Text.of("A relight is already running; /almura relight stop ends it."));
                    }
                    final World world;
                    final Optional<String> name = args.getOne("world");
                    if (name.isPresent()) {
                        world = Sponge.getServer().getWorld(name.get())
                                .orElseThrow(() -> new CommandException(Text.of("No loaded world named ", name.get(), ".")));
                    } else if (source instanceof Player) {
                        world = ((Player) source).getWorld();
                    } else {
                        throw new CommandException(Text.of("Name the world to relight."));
                    }
                    final int chunks;
                    try {
                        chunks = WorldRelighter.start((WorldServer) world, source, logger, container);
                    } catch (final IOException e) {
                        throw new CommandException(Text.of("Couldn't read the region files of ", world.getName(), ": ", e.getMessage()), e);
                    }
                    source.sendMessage(Text.of(TextColors.GREEN, "Relighting ", chunks, " saved chunks of ", world.getName(),
                            ". Progress is reported every 10%; /almura relight stop ends it."));
                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec generateStopCommand() {
        return CommandSpec.builder()
                .permission("almura.command.relight")
                .description(Text.of("Stops a running world relight"))
                .executor((source, args) -> {
                    if (!WorldRelighter.stop()) {
                        throw new CommandException(Text.of("No relight is running."));
                    }
                    return CommandResult.success();
                })
                .build();
    }

    /**
     * Relights the light sources of a chunk that leave a transparent neighbour darker than their light allows, returning how many.
     * The light is spread here, on the server thread, while the chunks are loaded: World.checkLightFor is no use, as Sponge's async
     * lighting only queues it, and drops it once the chunk is queued to unload (which the world relight does straight after).
     */
    static int relight(final WorldServer world, final Chunk chunk) {
        int relit = 0;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (final ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
            if (storage == Chunk.NULL_BLOCK_STORAGE || storage.isEmpty()) {
                continue;
            }
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        final IBlockState state = storage.get(x, y, z);
                        pos.setPos((chunk.x << 4) + x, storage.getYLocation() + y, (chunk.z << 4) + z);
                        // the world aware light value, which modded light sources (such as flickering lights) answer
                        final int light = state.getLightValue(world, pos);
                        if (light > 1 && leavesNeighborDark(world, pos, neighbor, light)) {
                            spread(world, pos.toImmutable(), light);
                            relit++;
                        }
                    }
                }
            }
        }
        return relit;
    }

    /**
     * Spreads a light source's block light the way Minecraft raises light: each neighbour gets the light less its opacity (at least
     * 1) where that is brighter than what it has, outward until the light runs out. Light is only ever raised, never lowered.
     */
    private static void spread(final WorldServer world, final BlockPos source, final int light) {
        if (world.getLightFor(EnumSkyBlock.BLOCK, source) < light) {
            world.setLightFor(EnumSkyBlock.BLOCK, source, light);
        }
        final Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(source);
        while (!queue.isEmpty()) {
            final BlockPos pos = queue.poll();
            final int level = world.getLightFor(EnumSkyBlock.BLOCK, pos);
            if (level <= 1) {
                continue;
            }
            for (final EnumFacing facing : EnumFacing.values()) {
                final BlockPos next = pos.offset(facing);
                if (next.getY() < 0 || next.getY() > 255 || !world.isBlockLoaded(next)) {
                    continue;
                }
                final int nextLevel = level - Math.max(1, world.getBlockState(next).getLightOpacity(world, next));
                if (nextLevel > world.getLightFor(EnumSkyBlock.BLOCK, next)) {
                    world.setLightFor(EnumSkyBlock.BLOCK, next, nextLevel);
                    queue.add(next);
                }
            }
        }
    }

    private static boolean leavesNeighborDark(final WorldServer world, final BlockPos pos, final BlockPos.MutableBlockPos neighbor, final int light) {
        for (final EnumFacing facing : EnumFacing.values()) {
            neighbor.setPos(pos.getX() + facing.getXOffset(), pos.getY() + facing.getYOffset(), pos.getZ() + facing.getZOffset());
            if (neighbor.getY() < 0 || neighbor.getY() > 255) {
                continue;
            }
            final int opacity = Math.max(1, world.getBlockState(neighbor).getLightOpacity(world, neighbor));
            if (opacity < 15 && world.getLightFor(EnumSkyBlock.BLOCK, neighbor) < light - opacity) {
                return true;
            }
        }
        return false;
    }
}
