/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.content.registry;

import com.almuradev.almura.content.def.ContentDefs.SaplingDef;
import com.almuradev.almura.content.def.ContentDefs.TreeDef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A sapling from the content: it grows as vanilla's saplings do (in light, one random tick in seven, or bone meal at 45%), first a
 * stage and then into its tree ({@link ContentTrees}), as 3.5's {@code SaplingBlockImpl} did. When the tree has no room the sapling stays.
 */
public final class ContentSapling extends SaplingBlock {

    private final SaplingDef def;

    ContentSapling(final SaplingDef def, final Properties properties) {
        // The tree is the content's, not a vanilla feature: this grower is never asked
        super(new TreeGrower("almura_" + def.id().replace(':', '_').replace('/', '_'), WeightedList.of(), WeightedList.of(), WeightedList.of(),
            null), properties);
        this.def = def;
    }

    public SaplingDef def() {
        return this.def;
    }

    @Override
    public void advanceTree(final ServerLevel level, final BlockPos pos, final BlockState state, final RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), Block.UPDATE_NONE);
            return;
        }
        final TreeDef tree = ContentTrees.tree(this.def.tree());
        if (tree == null) {
            return;
        }
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
        if (!ContentTrees.grow(level, pos, tree, random)) {
            level.setBlock(pos, state, Block.UPDATE_NONE);
        }
    }

    /**
     * Bone meal always takes, as on 1.12 (the tree finds out whether it has room).
     */
    @Override
    public boolean isValidBonemealTarget(final LevelReader level, final BlockPos pos, final BlockState state, final BonemealSource source) {
        return ContentTrees.tree(this.def.tree()) != null;
    }
}
