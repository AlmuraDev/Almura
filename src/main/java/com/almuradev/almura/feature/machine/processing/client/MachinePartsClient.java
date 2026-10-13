/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine.processing.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.machine.kitchen.KitchenGrinderTileEntity;
import com.almuradev.almura.feature.machine.kitchen.KitchenSlicerTileEntity;
import com.almuradev.almura.feature.machine.seedprocessor.SeedProcessorTileEntity;
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
 * Client setup for the machines' spinning parts: binds their tile entity renderers and bakes the part models, which aren't referenced by
 * any blockstate so they have to be loaded by hand.
 */
@SideOnly(Side.CLIENT)
public final class MachinePartsClient implements Witness {

    private static final float[] ALONG_X = {1.0F, 0.0F, 0.0F};
    private static final float[] ALONG_Z = {0.0F, 0.0F, 1.0F};

    /** The seed processor's rollers: axles along x, through y 0.52 and z 0.335 / 0.455, turning towards each other. */
    private static final SpinningPartRenderer<SeedProcessorTileEntity> SEED_PROCESSOR = new SpinningPartRenderer<>(
            new SpinningPart(model("seed_processor_roller_front"), new float[] {0.0F, 0.52F, 0.335F}, ALONG_X, 30.0F),
            new SpinningPart(model("seed_processor_roller_back"), new float[] {0.0F, 0.52F, 0.455F}, ALONG_X, -30.0F));
    /** The slicer's blade: axle along x, through y 0.48, z 0.50. */
    private static final SpinningPartRenderer<KitchenSlicerTileEntity> KITCHEN_SLICER = new SpinningPartRenderer<>(
            new SpinningPart(model("kitchen_slicer_blade"), new float[] {0.0F, 0.48F, 0.50F}, ALONG_X, 36.0F));
    /** The grinder's cutting plate: axle along z (the grinding tube), through x 0.50, y 0.44. */
    private static final SpinningPartRenderer<KitchenGrinderTileEntity> KITCHEN_GRINDER = new SpinningPartRenderer<>(
            new SpinningPart(model("kitchen_grinder_plate"), new float[] {0.50F, 0.44F, 0.0F}, ALONG_Z, 24.0F));
    private static final List<SpinningPartRenderer<?>> RENDERERS = Arrays.asList(SEED_PROCESSOR, KITCHEN_SLICER, KITCHEN_GRINDER);

    private final Logger logger;

    @Inject
    public MachinePartsClient(final Logger logger) {
        this.logger = logger;
    }

    private static ResourceLocation model(final String name) {
        return new ResourceLocation(Almura.ID, "block/machine/" + name + ".obj");
    }

    @SubscribeEvent
    public void onModelRegistry(final ModelRegistryEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(SeedProcessorTileEntity.class, SEED_PROCESSOR);
        ClientRegistry.bindTileEntitySpecialRenderer(KitchenSlicerTileEntity.class, KITCHEN_SLICER);
        ClientRegistry.bindTileEntitySpecialRenderer(KitchenGrinderTileEntity.class, KITCHEN_GRINDER);
    }

    @SubscribeEvent
    public void onModelBake(final ModelBakeEvent event) {
        for (final SpinningPartRenderer<?> renderer : RENDERERS) {
            for (final SpinningPart part : renderer.parts()) {
                try {
                    final IModel model = ModelLoaderRegistry.getModel(part.modelLocation);
                    // Baked in the item vertex format, which is what BlockModelRenderer#renderModelBrightnessColor draws (its texture is
                    // already in the block atlas through the machine's body model)
                    part.model = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                } catch (final Exception e) {
                    part.model = null;
                    this.logger.error("Unable to load the machine part model {}", part.modelLocation, e);
                }
            }
        }
    }
}
