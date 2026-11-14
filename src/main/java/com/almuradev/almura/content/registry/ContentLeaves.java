/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.BiomeRange;
import com.almuradev.almura.content.def.ContentDefs.DecayDef;
import com.almuradev.almura.content.def.ContentDefs.DropDef;
import com.almuradev.almura.content.def.ContentDefs.LeafDef;
import com.almuradev.almura.content.def.ContentDefs.Range;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Leaves from the content, as 3.5's {@code LeafBlockImpl} kept them:
 * <ul>
 *     <li>they decay away from a log as vanilla's do, dropping their {@code decay} drops at the biome's chance (not what they drop when
 *     broken, which is their loot table);</li>
 *     <li>on each random tick, at their {@code spread} chance for the biome, they grow their fruit leaf into the first air beside them
 *     (below, above, then north, south, west and east).</li>
 * </ul>
 */
public final class ContentLeaves extends LeavesBlock {

    private final LeafDef def;
    private @Nullable Block spread;
    private boolean spreadResolved;

    ContentLeaves(final LeafDef def, final Properties properties) {
        super(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
        this.def = def;
    }

    public LeafDef def() {
        return this.def;
    }

    /**
     * Leaves that spread fruit tick always, as every leaf ticked on 1.12; the rest only to decay.
     */
    @Override
    protected boolean isRandomlyTicking(final BlockState state) {
        return this.def.spreadBlock() != null || super.isRandomlyTicking(state);
    }

    @Override
    protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        if (this.decaying(state)) {
            this.dropDecay(level, pos, random);
            level.removeBlock(pos, false);
            return;
        }
        this.spread(level, pos, state, random);
    }

    private void dropDecay(final ServerLevel level, final BlockPos pos, final RandomSource random) {
        final String biome = ContentTrees.biome(level, pos);
        for (final DecayDef decay : this.def.decay()) {
            final Range chance = BiomeRange.pick(decay.chance(), biome);
            if (chance != null && !(random.nextDouble() <= chance.random(random.nextDouble()) / 100)) {
                continue;
            }
            for (final DropDef drop : decay.drops()) {
                final Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(drop.item())).orElse(null);
                if (item == null) {
                    continue;
                }
                int amount = drop.amountMin() + random.nextInt(Math.max(1, drop.amountMax() - drop.amountMin() + 1));
                if (drop.bonusAmount() > 0 && random.nextInt(100) < drop.bonusChance()) {
                    amount += drop.bonusAmount();
                }
                if (amount > 0) {
                    Block.popResource(level, pos, new ItemStack(item, amount));
                }
            }
        }
    }

    private void spread(final ServerLevel level, final BlockPos pos, final BlockState state, final RandomSource random) {
        if (this.def.spreadBlock() == null) {
            return;
        }
        final Range chance = BiomeRange.pick(this.def.spreadChance(), ContentTrees.biome(level, pos));
        if (chance == null || !(random.nextDouble() <= chance.random(random.nextDouble()) / 100)) {
            return;
        }
        if (!this.spreadResolved) {
            this.spread = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(this.def.spreadBlock())).orElse(null);
            this.spreadResolved = true;
        }
        for (final Direction direction : Direction.values()) {
            final BlockPos offset = pos.relative(direction);
            // Leaves on a chunk edge don't load the next chunk to look for air
            if (!level.isLoaded(offset)) {
                continue;
            }
            if (level.getBlockState(offset).isAir()) {
                if (this.spread != null) {
                    BlockState fruit = this.spread.defaultBlockState();
                    if (fruit.getBlock() instanceof LeavesBlock) {
                        // A step further from the log than this leaf, worked out again on its first tick
                        fruit = fruit.setValue(LeavesBlock.DISTANCE, Math.min(LeavesBlock.DECAY_DISTANCE, state.getValue(DISTANCE) + 1));
                        level.scheduleTick(offset, fruit.getBlock(), 1);
                    }
                    level.setBlock(offset, fruit, Block.UPDATE_ALL);
                    level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 0);
                }
                break;
            }
        }
    }
}
