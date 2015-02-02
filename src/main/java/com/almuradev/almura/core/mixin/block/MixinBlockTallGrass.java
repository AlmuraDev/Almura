/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.core.mixin.block;

import com.almuradev.almura.pack.crop.PackSeeds;
import com.almuradev.almura.pack.node.GrassNode;
import com.almuradev.almura.pack.node.property.RangeProperty;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = BlockTallGrass.class)
public abstract class MixinBlockTallGrass extends BlockBush {

    @Inject(method = "getDrops", at = @At(value = "JUMP", opcode = Opcodes.IFEQ), cancellable = true, remap = false)
    public void onGetDrops(World world, int x, int y, int z, int meta, int fortune, CallbackInfoReturnable<ArrayList<ItemStack>> ci) {
        final ItemStack seedStack = ForgeHooks.getGrassSeed(world);
        if (seedStack.getItem() instanceof PackSeeds) {
            final GrassNode grassNode = ((PackSeeds) seedStack.getItem()).getNode(GrassNode.class);
            if (grassNode != null) {
                final double chance = grassNode.getChanceProperty().getValueWithinRange();
                if (RangeProperty.RANDOM.nextDouble() <= (chance / 100)) {
                    seedStack.stackSize = grassNode.getValue().getAmountProperty().getValueWithinRange();
                    final ArrayList<ItemStack> ret = Lists.newArrayList();
                    ret.add(seedStack);
                    ci.setReturnValue(ret);
                }
            }
        }
    }
}
