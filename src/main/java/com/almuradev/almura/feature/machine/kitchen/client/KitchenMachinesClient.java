/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.kitchen.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.kitchen.KitchenGrinderTileEntity;
import com.almuradev.almura.feature.machine.kitchen.KitchenSlicerTileEntity;
import com.almuradev.core.event.Witness;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Client setup for the kitchen machines' spinning parts: binds their tile entity renderers and bakes the part models, which aren't
 * referenced by any blockstate so they have to be loaded by hand.
 */
@SideOnly(Side.CLIENT)
public final class KitchenMachinesClient implements Witness {

    /** The slicer's blade: axle along x, through y 0.48, z 0.50. */
    private static final SpinningPartRenderer<KitchenSlicerTileEntity> SLICER_BLADE = new SpinningPartRenderer<>(
            new ResourceLocation(Almura.ID, "block/machine/kitchen_slicer_blade.obj"), new float[] {0.0F, 0.48F, 0.50F},
            new float[] {1.0F, 0.0F, 0.0F}, 36.0F);
    /** The grinder's cutting plate: axle along z (the grinding tube), through x 0.50, y 0.44. */
    private static final SpinningPartRenderer<KitchenGrinderTileEntity> GRINDER_PLATE = new SpinningPartRenderer<>(
            new ResourceLocation(Almura.ID, "block/machine/kitchen_grinder_plate.obj"), new float[] {0.50F, 0.44F, 0.0F},
            new float[] {0.0F, 0.0F, 1.0F}, 24.0F);
    private static final List<SpinningPartRenderer<?>> RENDERERS = Arrays.asList(SLICER_BLADE, GRINDER_PLATE);

    private final Logger logger;

    @Inject
    public KitchenMachinesClient(final Logger logger) {
        this.logger = logger;
    }

    @SubscribeEvent
    public void onModelRegistry(final ModelRegistryEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(KitchenSlicerTileEntity.class, SLICER_BLADE);
        ClientRegistry.bindTileEntitySpecialRenderer(KitchenGrinderTileEntity.class, GRINDER_PLATE);
    }

    @SubscribeEvent
    public void onModelBake(final ModelBakeEvent event) {
        for (final SpinningPartRenderer<?> renderer : RENDERERS) {
            try {
                final IModel model = ModelLoaderRegistry.getModel(renderer.modelLocation());
                // Baked in the item vertex format, which is what BlockModelRenderer#renderModelBrightnessColor draws (its texture is
                // already in the block atlas through the machine's body model)
                renderer.model = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            } catch (final Exception e) {
                renderer.model = null;
                this.logger.error("Unable to load the machine part model {}", renderer.modelLocation(), e);
            }
        }
    }
}
