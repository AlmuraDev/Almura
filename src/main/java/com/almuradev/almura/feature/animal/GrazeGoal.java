/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.EventHooks;

import java.util.EnumSet;

/**
 * Grazing, as 3.5's {@code MixinEntityAIEatGrass}: cows eat grass as sheep do (one try in 50 a calf, 150 a cow; vanilla cows never
 * graze), and cows and sheep graze alfalfa they stand in, taking a stage off it. A calf that grazes grows a minute; a sheep that grazes
 * alfalfa grows its wool back, as when it eats grass. Sheep keep vanilla's grass eating; this adds only alfalfa for them.
 */
final class GrazeGoal extends Goal {

    private static final Identifier ALFALFA = Identifier.fromNamespaceAndPath("almura", "crop/alfalfa");
    private static final int EAT_TICKS = 40, BITE_TICK = 4;

    private final Animal animal;
    private final boolean cow;
    private int timer;

    GrazeGoal(final Animal animal) {
        this.animal = animal;
        this.cow = animal instanceof AbstractCow;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        final int odds = this.animal.isBaby() ? 50 : this.cow ? 150 : 1000;
        if (this.animal.getRandom().nextInt(this.adjustedTickDelay(odds)) != 0) {
            return false;
        }
        final BlockPos pos = this.feet();
        if (this.alfalfaAge(pos) > 1) {
            return true;
        }
        // Grass: a cow's only (vanilla's goal feeds sheep)
        return this.cow && (this.animal.level().getBlockState(pos).is(BlockTags.EDIBLE_FOR_SHEEP)
            || this.animal.level().getBlockState(pos.below()).is(Blocks.GRASS_BLOCK));
    }

    @Override
    public void start() {
        this.timer = this.adjustedTickDelay(EAT_TICKS);
        // The head goes down on clients that animate it (sheep)
        this.animal.level().broadcastEntityEvent(this.animal, (byte) 10);
        this.animal.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.timer = 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.timer > 0;
    }

    @Override
    public void tick() {
        this.timer = Math.max(0, this.timer - 1);
        if (this.timer != this.adjustedTickDelay(BITE_TICK) || !(this.animal.level() instanceof final ServerLevel level)) {
            return;
        }
        final BlockPos pos = this.feet();
        final int age = this.alfalfaAge(pos);
        if (age > 1) {
            final BlockState crop = level.getBlockState(pos);
            level.setBlock(pos, withAge(crop, age - 1), Block.UPDATE_CLIENTS);
            this.ate();
            return;
        }
        if (!this.cow) {
            return;
        }
        if (level.getBlockState(pos).is(BlockTags.EDIBLE_FOR_SHEEP)) {
            if (EventHooks.canEntityGrief(level, this.animal)) {
                level.destroyBlock(pos, false);
            }
            this.ate();
        } else if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
            if (EventHooks.canEntityGrief(level, this.animal)) {
                level.levelEvent(2001, pos.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            this.ate();
        }
    }

    private void ate() {
        if (this.cow) {
            // 3.5's cow's eatGrassBonus: a calf grows a minute
            this.animal.gameEvent(GameEvent.EAT);
            if (this.animal.isBaby()) {
                this.animal.ageUp(60);
            }
        } else {
            // A sheep's own: its wool back, and a lamb grows
            this.animal.ate();
        }
    }

    /**
     * Where the animal eats: the block it stands in, or the one over the farmland it stands on (just under the block line).
     */
    private BlockPos feet() {
        final BlockPos pos = this.animal.blockPosition();
        return this.animal.level().getBlockState(pos).getBlock() instanceof FarmlandBlock ? pos.above() : pos;
    }

    /**
     * @return the alfalfa crop's age there, or -1 for no alfalfa
     */
    private int alfalfaAge(final BlockPos pos) {
        final BlockState state = this.animal.level().getBlockState(pos);
        if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(ALFALFA)) {
            return -1;
        }
        final Property<?> age = state.getBlock().getStateDefinition().getProperty("age");
        return age instanceof final IntegerProperty integer ? state.getValue(integer) : -1;
    }

    private static BlockState withAge(final BlockState state, final int age) {
        final Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        return property instanceof final IntegerProperty integer ? state.setValue(integer, age) : state;
    }
}
