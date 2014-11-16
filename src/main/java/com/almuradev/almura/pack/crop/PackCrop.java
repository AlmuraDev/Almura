package com.almuradev.almura.pack.crop;

import java.util.Random;

import net.minecraft.block.BlockCrops;
import net.minecraft.world.World;

public class PackCrop extends BlockCrops {
    // Constructor Object Needs:
    //  a. seed
    //  b. amount of stages (int)
    //  c. minimum light to grow
    //  d. maximum light to grow
    //  e. growth rate
    //  f. drop type (itemStack)
    //  g. drop quantity (int)
    //  h. drop bonus (itemStack)
    //  i. bonus quantity (int)
    //  j. water required to grow (boolean)
    //  k. water range scan (int)
    //  l. fertilizer type (itemStack)
    //  m. damage player
    //  n. drop seed on grass break (boolean)
    
    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        // Todo: what to do in update tick
    }
    
    private float getGrowthRate(World world, int x, int y, int z) {        
        // Todo: determine growth rate.
        return 0;        
    }
    
    //Todo:  many many more methods needed here.
}
