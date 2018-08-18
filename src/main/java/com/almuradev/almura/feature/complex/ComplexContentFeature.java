/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.complex;

import static java.util.Objects.requireNonNull;

import com.almuradev.almura.feature.complex.block.ComplexBlock;
import com.almuradev.almura.feature.complex.block.ComplexBlocks;
import com.almuradev.almura.feature.complex.item.almanac.item.FarmersAlmanacItem;
import com.almuradev.almura.feature.complex.item.wand.LightRepairWand;
import com.almuradev.core.event.Witness;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryModifiable;

public final class ComplexContentFeature implements Witness {

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        // Register Complex Blocks Here
        event.getRegistry().register(ComplexBlocks.COIN_EXCHANGE);
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event) {
        // Register Complex Items Here
        event.getRegistry().register(new LightRepairWand());
        event.getRegistry().register(new FarmersAlmanacItem());

        // Register Complex Block; ItemBlocks Here
        registerItem(event.getRegistry(), ComplexBlocks.COIN_EXCHANGE);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event) {
        // Register Complex Block Models
        // Note: Complex Item Models register within their creation class because their registration order isn't important.
        registerModel(Item.getItemFromBlock(ComplexBlocks.COIN_EXCHANGE));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        // Register Complex Block & Item Recipes Here
       this.fixStoneBrickMadness(event);
    }

    // Supporting Methods Below
    private void registerItem(final IForgeRegistry<Item> registry, final ComplexBlock complexBlock) {
        final ResourceLocation registryName = requireNonNull(complexBlock.getRegistryName());
        final ItemBlock item = (ItemBlock) new ItemBlock(complexBlock).setRegistryName(registryName);
        registry.register(item);
    }

    @SideOnly(Side.CLIENT)
    private void registerModel(Item item) {
        this.registerInventoryModel(item, requireNonNull(item.getRegistryName()));
    }

    @SideOnly(Side.CLIENT)
    private void registerInventoryModel(Item item, ResourceLocation blockName) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(blockName, "inventory"));
    }

    // Utility Methods
    private void fixStoneBrickMadness(RegistryEvent.Register<IRecipe> event) {
        // Note: This section is a complete hack to remove the vanilla recipe for stone brick because it includes 4 variants.  We add mossy and cracked stone brick
        //       so in order for this to work correctly we remove the vanilla recipe that includes all the variants and re-add back just the single recipe.  Re-adding
        //       is required here in order to prevent Advancments from breaking.
        ResourceLocation StoneBrick = new ResourceLocation("minecraft:stone_brick_stairs");
        IForgeRegistryModifiable modRegistry = (IForgeRegistryModifiable) event.getRegistry();
        modRegistry.remove(StoneBrick);

        GameRegistry.addShapedRecipe(new ResourceLocation("minecraft:stone_brick_stairs"), new ResourceLocation("minecraft:stone_brick_stairs"), new
                        ItemStack(Blocks.STONE_BRICK_STAIRS, 4),
                "AXX",
                "AAX",
                "AAA",
                'A', Blocks.STONEBRICK,
                'X', Blocks.AIR
        );
    }
}
