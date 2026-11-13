/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.storage.client;

import com.almuradev.content.type.block.type.container.StorageBarrel;
import com.almuradev.core.event.Witness;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives the storage barrel its labelled model: each of its baked models (one per facing) is wrapped to add the label.
 */
@SideOnly(Side.CLIENT)
public final class StorageBarrelModels implements Witness {

    @SubscribeEvent
    public void onModelBake(final ModelBakeEvent event) {
        final List<ModelResourceLocation> barrels = new ArrayList<>();
        for (final ModelResourceLocation location : event.getModelRegistry().getKeys()) {
            if (location.getNamespace().equals(StorageBarrel.ID.getNamespace()) && location.getPath().equals(StorageBarrel.ID.getPath())
                    && !"inventory".equals(location.getVariant())) {
                barrels.add(location);
            }
        }
        for (final ModelResourceLocation location : barrels) {
            final IBakedModel barrel = event.getModelRegistry().getObject(location);
            if (barrel != null && !(barrel instanceof StorageBarrelModel)) {
                event.getModelRegistry().putObject(location, new StorageBarrelModel(barrel));
            }
        }
    }
}
