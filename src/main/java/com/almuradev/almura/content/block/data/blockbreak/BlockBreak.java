/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.block.data.blockbreak;

import com.almuradev.almura.content.block.data.Action;
import com.almuradev.almura.content.block.data.blockbreak.drop.Drop;
import com.almuradev.almura.content.loader.CatalogDelegate;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class BlockBreak {

    private final Set<CatalogDelegate<ItemType>> with;
    private final List<Action> actions;
    private final List<Drop> drops;

    BlockBreak(final Set<CatalogDelegate<ItemType>> with, final List<Action> actions, final List<Drop> drops) {
        this.with = with;
        this.actions = actions;
        this.drops = drops;
    }

    public boolean doesItemMatch(ItemType type) {
        return this.with.stream().anyMatch(input -> input.test(type));
    }

    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(this.actions);
    }

    public Collection<Drop> getDrops() {
        return Collections.unmodifiableCollection(this.drops);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("with", this.with)
                .add("actions", this.actions)
                .add("drops", this.drops)
                .toString();
    }
}
