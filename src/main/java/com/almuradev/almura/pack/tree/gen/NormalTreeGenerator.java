/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2016 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.pack.tree.gen;

import com.almuradev.almura.pack.mapper.GameObject;
import com.almuradev.almura.pack.tree.FruitPrefab;
import com.almuradev.almura.pack.tree.TreePrefab;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class NormalTreeGenerator extends WorldGenerator {

    private final TreePrefab treePrefab;

    public NormalTreeGenerator(TreePrefab treePrefab) {
        super(true);
        this.treePrefab = treePrefab;
    }

    public boolean generate(World world, Random random, int x, int y, int z) {
        int l = this.treePrefab.getHeightVariance().getValueWithinRange() + this.treePrefab.getMinTreeHeight();
        boolean flag = true;

        if (y >= 1 && y + l + 1 <= 256) {
            byte b0;
            int k1;
            Block block;

            for (int i1 = y; i1 <= y + 1 + l; ++i1) {
                b0 = 1;

                if (i1 == y) {
                    b0 = 0;
                }

                if (i1 >= y + 1 + l - 2) {
                    b0 = 2;
                }

                for (int j1 = x - b0; j1 <= x + b0 && flag; ++j1) {
                    for (k1 = z - b0; k1 <= z + b0 && flag; ++k1) {
                        if (i1 >= 0 && i1 < 256) {
                            block = world.getBlock(j1, i1, k1);

                            if (!this.isReplaceable(world, block, j1, i1, k1)) {
                                flag = false;
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
            }

            if (!flag) {
                return false;
            } else {
                Block block2 = world.getBlock(x, y - 1, z);

                boolean isSoil = block2.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (BlockSapling) Blocks.sapling);
                if (isSoil && y < 256 - l - 1) {
                    block2.onPlantGrow(world, x, y - 1, z, x, y, z);
                    b0 = 3;
                    byte b1 = 0;
                    int l1;
                    int i2;
                    int j2;
                    int i3;

                    for (k1 = y - b0 + l; k1 <= y + l; ++k1) {
                        i3 = k1 - (y + l);
                        l1 = b1 + 1 - i3 / 2;

                        for (i2 = x - l1; i2 <= x + l1; ++i2) {
                            j2 = i2 - x;

                            for (int k2 = z - l1; k2 <= z + l1; ++k2) {
                                int l2 = k2 - z;

                                if (Math.abs(j2) != l1 || Math.abs(l2) != l1 || random.nextInt(2) != 0 && i3 != 0) {
                                    Block block1 = world.getBlock(i2, k1, k2);

                                    if (block1.isAir(world, i2, k1, k2) || block1 == this.treePrefab.getLeaves().minecraftObject) {
                                        GameObject toPlaceObject = this.treePrefab.getLeaves();

                                        if (this.treePrefab.isGenerationEnabled() && !this.treePrefab.getFruitPrefabs().isEmpty()) {
                                            final FruitPrefab prefab = this.treePrefab.getFruitPrefabs().iterator().next();
                                            final double chance = prefab.getFruitChance().getValueWithinRange();
                                            if (random.nextDouble() <= (chance / 100)) {
                                                toPlaceObject = prefab.getFruit();
                                            }
                                        }

                                        this.setBlockAndNotifyAdequately(world, i2, k1, k2, toPlaceObject.minecraftObject instanceof ItemBlock ?
                                                ((ItemBlock) toPlaceObject.minecraftObject).blockInstance : (Block) toPlaceObject.minecraftObject, toPlaceObject
                                                .data);

                                        Optional<GameObject> optToPlaceHangingObj = Optional.absent();

                                        if (this.treePrefab.isGenerationEnabled() && !this.treePrefab.getHangingFruitPrefabs().isEmpty()) {
                                            final FruitPrefab prefab = this.treePrefab.getHangingFruitPrefabs().iterator().next();
                                            final double chance = prefab.getFruitChance().getValueWithinRange();
                                            if (random.nextDouble() <= (chance / 100)) {
                                                optToPlaceHangingObj = Optional.of(prefab.getFruit());
                                            }
                                        }

                                        if (optToPlaceHangingObj.isPresent()) {
                                            final Block block3 = world.getBlock(i2, k1 - 1, k2);
                                            if (block3 == Blocks.air) {
                                                this.setBlockAndNotifyAdequately(world, i2, k1 - 1, k2, optToPlaceHangingObj.get().minecraftObject
                                                        instanceof ItemBlock ?
                                                        ((ItemBlock) optToPlaceHangingObj.get().minecraftObject).blockInstance : (Block)
                                                        optToPlaceHangingObj.get().minecraftObject, optToPlaceHangingObj.get().data);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    for (k1 = 0; k1 < l; ++k1) {
                        block = world.getBlock(x, y + k1, z);
                        final int blockMetadata = world.getBlockMetadata(x, y + k1, z);

                        if (block.isAir(world, x, y + k1, z) || block == this.treePrefab.getLeaves().minecraftObject || isFruitOrHangingFruit
                                (block, blockMetadata)) {
                            this.setBlockAndNotifyAdequately(world, x, y + k1, z, (Block) this.treePrefab.getWood().minecraftObject, this.treePrefab.getWood()
                                    .data);
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private boolean isReplaceable(World world, Block block, int x, int y, int z) {
        return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z) || block.isWood(world, x, y, z) || block.getMaterial() == Material.air
                || block.getMaterial() == Material.leaves || block == Blocks.grass || block == Blocks.dirt || block == Blocks.log
                || block == Blocks.log2 || block instanceof BlockSapling || block == Blocks.vine;
    }

    private boolean isFruitOrHangingFruit(Block block, int blockMetadata) {
        for (FruitPrefab fruitPrefab : this.treePrefab.getFruitPrefabs()) {
            Block toCheck;

            if (fruitPrefab.getFruit().minecraftObject instanceof ItemBlock) {
                toCheck = ((ItemBlock) fruitPrefab.getFruit().minecraftObject).blockInstance;
            } else {
                toCheck = (Block) fruitPrefab.getFruit().minecraftObject;
            }
            if (block == toCheck && blockMetadata == fruitPrefab.getFruit().data) {
                return true;
            }
        }

        for (FruitPrefab fruitPrefab : this.treePrefab.getHangingFruitPrefabs()) {
            Block toCheck;

            if (fruitPrefab.getFruit().minecraftObject instanceof ItemBlock) {
                toCheck = ((ItemBlock) fruitPrefab.getFruit().minecraftObject).blockInstance;
            } else {
                toCheck = (Block) fruitPrefab.getFruit().minecraftObject;
            }
            
            if (block == toCheck && blockMetadata == fruitPrefab.getFruit().data) {
                return true;
            }
        }

        return false;
    }
}
