/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal.asm.mixin.entity.passive;

import com.almuradev.almura.asm.mixin.accessors.block.BlockCropAccessor;
import com.almuradev.content.type.block.BlockUpdateFlag;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityAIEatGrass.class, priority = 999)
// Had to set priority to 999 because of a conflict when set to default of 1000.
public abstract class MixinEntityAIEatGrass extends EntityAIBase {

    @Shadow @Final
    private static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    /** The entity owner of this AITask */
    @Shadow
    private final EntityLiving grassEaterEntity;
    /** The world the grass eater entity is eating from */
    @Shadow
    private final World entityWorld;
    /** Number of ticks since the entity started to eat grass */
    @Shadow
    int eatingGrassTimer;

    public MixinEntityAIEatGrass(EntityLiving grassEaterEntity, World entityWorld) {
        this.grassEaterEntity = grassEaterEntity;
        this.entityWorld = entityWorld;
    }

    /**
     * @author Dockter
     * @reason Modifying this check for other crops such as Alfalfa
     */
    @Overwrite
    public boolean shouldExecute() {
        boolean executeTask = false;

        // Cows
        if (this.grassEaterEntity instanceof EntityCow && this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 25 : 50) == 0) {
            executeTask = true;
        }

        // Sheep
        if (this.grassEaterEntity instanceof EntitySheep && this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 1000) == 0) {
            executeTask = true;
        }

        if (executeTask) {
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);

            if (this.entityWorld.getBlockState(blockpos).getBlock() instanceof BlockFarmland) {
                // Look up one block because the animal is standing on a farmland block which is just a hair below the blockline.
                blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY + 1, this.grassEaterEntity.posZ);
            }

            Block block = this.entityWorld.getBlockState(blockpos).getBlock();

            if (block instanceof BlockCrops) {
                return true;
            }

            if (IS_TALL_GRASS.apply(this.entityWorld.getBlockState(blockpos))) {
                return true;
            } else {
                return this.entityWorld.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS;
            }
        } else {
            return false;
        }
    }

    /**
     * @author Dockter
     * @reason Modifying this check for other crops such as Alfalfa
     */
    @Overwrite
    public void updateTask() {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);

        if (this.eatingGrassTimer == 4) { // The task runs 40 times, run on timer == 4, starts at 40 and counts down.
            BlockPos blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
            if (this.entityWorld.getBlockState(blockpos).getBlock() instanceof BlockFarmland) {
                // Look up one block because the animal is standing on a farmland block which is just a hair below the blockline.
                blockpos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY + 1, this.grassEaterEntity.posZ);
            }

            Block block =  this.entityWorld.getBlockState(blockpos).getBlock();

            if (block instanceof BlockCrops) {
                BlockCrops cropBlock = (BlockCrops) block;

                // Determine if this is a Alfalfa Plant
                if (cropBlock.getTranslationKey().equalsIgnoreCase("tile.almura.crop.alfalfa")) {
                    IBlockState cropState = this.entityWorld.getBlockState(blockpos);
                    int cropAge = ((BlockCropAccessor) cropState.getBlock()).accessor$getAge(cropState);
                    if (cropAge > 1) {
                        //System.out.println("Crop: " + cropBlock.getTranslationKey() + " age: " + cropAge + " at: " + blockpos);
                        this.entityWorld.setBlockState(blockpos, cropBlock.withAge(cropAge - 1), BlockUpdateFlag.UPDATE_CLIENTS);
                        this.grassEaterEntity.eatGrassBonus();
                        return;
                    }
                }
            }

            if (IS_TALL_GRASS.apply(this.entityWorld.getBlockState(blockpos))) {
                if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)) {
                    this.entityWorld.destroyBlock(blockpos, false);
                }

                this.grassEaterEntity.eatGrassBonus();
            } else {
                BlockPos blockpos1 = blockpos.down();

                if (this.entityWorld.getBlockState(blockpos1).getBlock() == Blocks.GRASS) {
                    if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.entityWorld, this.grassEaterEntity)) {
                        this.entityWorld.playEvent(2001, blockpos1, Block.getIdFromBlock(Blocks.GRASS));
                        this.entityWorld.setBlockState(blockpos1, Blocks.DIRT.getDefaultState(), 2);
                    }

                    this.grassEaterEntity.eatGrassBonus();
                }
            }
        }
    }
}
