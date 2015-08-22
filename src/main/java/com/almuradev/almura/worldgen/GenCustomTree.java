package com.almuradev.almura.worldgen;

import net.minecraft.block.material.Material;

import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.init.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class GenCustomTree extends WorldGenerator {
    private final int minTreeHeight;
    private final boolean vinesGrow;
    private final Block woodType;
    private final Block leavesType;
    private final int metaWood;
    private final int metaLeaves;

    public GenCustomTree(boolean p_i2028_1_, int minTreeHeight, int woodMeta, int leavesMeta, boolean vines, Block woodType, Block leavesType) {
        this.minTreeHeight = minTreeHeight;
        this.woodType = woodType;
        this.leavesType = leavesType;
        this.metaWood = woodMeta;
        this.metaLeaves = leavesMeta;
        this.vinesGrow = vines;
    }

    public boolean generate(World world, Random random, int x, int y, int z) {
        int l = random.nextInt(3) + this.minTreeHeight;
        boolean flag = true;

        if (y >= 1 && y + l + 1 <= 256) {
            byte b0;
            int zz;
            Block block;

            for (int yy = y; yy <= y + 1 + l; ++yy) {
                b0 = 1;

                if (yy == y) {
                    b0 = 0;
                }

                if (yy >= y + 1 + l - 2) {
                    b0 = 2;
                }

                for (int xx = x - b0; xx <= x + b0 && flag; ++xx) {
                    for (zz = z - b0; zz <= z + b0 && flag; ++zz) {
                        if (yy >= 0 && yy < 256) {
                            block = world.getBlock(xx, yy, zz);
                            if (!this.isReplaceable(world, xx, yy, zz)) {
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
                boolean isSoil = block2.canSustainPlant(world, x, y - 1, z, ForgeDirection.UP, (BlockSapling)Blocks.sapling);
                if (isSoil && y < 256 - l - 1) {
                    block2.onPlantGrow(world, x, y - 1, z, x, y, z);
                    b0 = 3;
                    byte b1 = 0;
                    int l1;
                    int i2;
                    int j2;
                    int i3;

                    for (zz = y - b0 + l; zz <= y + l; ++zz) {
                        i3 = zz - (y + l);
                        l1 = b1 + 1 - i3 / 2;

                        for (i2 = x - l1; i2 <= x + l1; ++i2) {
                            j2 = i2 - x;

                            for (int k2 = z - l1; k2 <= z + l1; ++k2) {
                                int l2 = k2 - z;

                                if (Math.abs(j2) != l1 || Math.abs(l2) != l1 || random.nextInt(2) != 0 && i3 != 0) {
                                    Block bloczz = world.getBlock(i2, zz, k2);

                                    if (bloczz.isAir(world, i2, zz, k2) || bloczz.isLeaves(world, i2, zz, k2)) {
                                        this.setBlockAndNotifyAdequately(world, i2, zz, k2, this.leavesType, this.metaLeaves);
                                    }
                                }
                            }
                        }
                    }

                    for (zz = 0; zz < l; ++zz) {
                        block = world.getBlock(x, y + zz, z);

                        if (block.isAir(world, x, y + zz, z) || block.isLeaves(world, x, y + zz, z)) {
                            this.setBlockAndNotifyAdequately(world, x, y + zz, z, this.woodType, this.metaWood);

                            if (this.vinesGrow && zz > 0) {
                                if (random.nextInt(3) > 0 && world.isAirBlock(x - 1, y + zz, z)) {
                                    this.setBlockAndNotifyAdequately(world, x - 1, y + zz, z, Blocks.vine, 8);
                                }

                                if (random.nextInt(3) > 0 && world.isAirBlock(x + 1, y + zz, z)) {
                                    this.setBlockAndNotifyAdequately(world, x + 1, y + zz, z, Blocks.vine, 2);
                                }

                                if (random.nextInt(3) > 0 && world.isAirBlock(x, y + zz, z - 1)) {
                                    this.setBlockAndNotifyAdequately(world, x, y + zz, z - 1, Blocks.vine, 1);
                                }

                                if (random.nextInt(3) > 0 && world.isAirBlock(x, y + zz, z + 1)) {
                                    this.setBlockAndNotifyAdequately(world, x, y + zz, z + 1, Blocks.vine, 4);
                                }
                            }
                        }
                    }

                    if (this.vinesGrow) {
                        for (zz = y - 3 + l; zz <= y + l; ++zz) {
                            i3 = zz - (y + l);
                            l1 = 2 - i3 / 2;

                            for (i2 = x - l1; i2 <= x + l1; ++i2) {
                                for (j2 = z - l1; j2 <= z + l1; ++j2) {
                                    if (world.getBlock(i2, zz, j2).isLeaves(world, i2, zz, j2)) {
                                        if (random.nextInt(4) == 0 && world.getBlock(i2 - 1, zz, j2).isAir(world, i2 - 1, zz, j2)) {
                                            this.growVines(world, i2 - 1, zz, j2, 8);
                                        }

                                        if (random.nextInt(4) == 0 && world.getBlock(i2 + 1, zz, j2).isAir(world, i2 + 1, zz, j2)) {
                                            this.growVines(world, i2 + 1, zz, j2, 2);
                                        }

                                        if (random.nextInt(4) == 0 && world.getBlock(i2, zz, j2 - 1).isAir(world, i2, zz, j2 - 1)) {
                                            this.growVines(world, i2, zz, j2 - 1, 1);
                                        }

                                        if (random.nextInt(4) == 0 && world.getBlock(i2, zz, j2 + 1).isAir(world, i2, zz, j2 + 1)) {
                                            this.growVines(world, i2, zz, j2 + 1, 4);
                                        }
                                    }
                                }
                            }
                        }

                        if (random.nextInt(5) == 0 && l > 5) {
                            for (zz = 0; zz < 2; ++zz) {
                                for (i3 = 0; i3 < 4; ++i3) {
                                    if (random.nextInt(4 - zz) == 0) {
                                        l1 = random.nextInt(3);
                                        this.setBlockAndNotifyAdequately(world, x + Direction.offsetX[Direction.rotateOpposite[i3]], y + l - 5 + zz, z + Direction.offsetZ[Direction.rotateOpposite[i3]], Blocks.cocoa, l1 << 2 | i3);
                                    }
                                }
                            }
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

    private void growVines(World world, int x, int y, int z, int p_76529_5_) {
        this.setBlockAndNotifyAdequately(world, x, y, z, Blocks.vine, p_76529_5_);
        int yy = 4;

        while (true) {
            --y;

            if (!world.getBlock(x, y, z).isAir(world, x, y, z) || yy <= 0) {
                return;
            }

            this.setBlockAndNotifyAdequately(world, x, y, z, Blocks.vine, p_76529_5_);
            --yy;
        }
    }

    protected boolean isReplaceable(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z) || block.isWood(world, x, y, z) || func_150523_a(block);
    }

    protected boolean func_150523_a(Block p_150523_1_) {
        return p_150523_1_.getMaterial() == Material.air || p_150523_1_.getMaterial() == Material.leaves || p_150523_1_ == Blocks.grass || p_150523_1_ == Blocks.dirt || p_150523_1_ == Blocks.log || p_150523_1_ == Blocks.log2 || p_150523_1_ == Blocks.sapling || p_150523_1_ == Blocks.vine;
    }
}
