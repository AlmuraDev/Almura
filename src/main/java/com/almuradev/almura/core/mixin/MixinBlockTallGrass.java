/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin;

import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.node.GrassNode;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;

@Mixin(value = BlockTallGrass.class)
public abstract class MixinBlockTallGrass extends BlockBush {

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        final ArrayList<ItemStack> ret = Lists.newArrayList();
        final ItemStack seed = ForgeHooks.getGrassSeed(world);
        if (seed != null) {
            if (seed.getItem().getClass() == PackSeeds.class) {
                final GrassNode grassNode = ((INodeContainer) seed.getItem()).getNode(GrassNode.class);
                final double chance = grassNode.getChanceProperty().getValueWithinRange();
                if (RangeProperty.RANDOM.nextDouble() <= (chance / 100)) {
                    seed.stackSize = grassNode.getValue().getAmountProperty().getValueWithinRange();
                } else {
                    return ret;
                }
            } else {
                //Preserve Vanilla random logic
                if (world.rand.nextInt(8) != 0) {
                    return ret;
                }
            }

            ret.add(seed);
        }
        return ret;
    }
}
