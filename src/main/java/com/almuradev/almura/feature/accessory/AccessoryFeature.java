/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory;

import com.almuradev.almura.feature.accessory.model.HaloModel;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import com.almuradev.almura.shared.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;

import javax.inject.Inject;

@SideOnly(Side.CLIENT)
public class AccessoryFeature implements Witness {

    private final AccessoryTypeRegistryModule store;
    private final AccessoryManager manager;

    @Inject
    public AccessoryFeature(final AccessoryTypeRegistryModule store, final AccessoryManager manager) {
        this.store = store;
        this.manager = manager;
    }

    @Listener
    public void onGameLoadComplete(GameLoadCompleteEvent event) {
        this.store.getAll().forEach(this::registerTextures);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Pre event) {
        final EntityPlayer player = event.getEntityPlayer();
        final RenderPlayer renderer = event.getRenderer();
        final ModelPlayer model = renderer.getMainModel();

        this.manager.getSelectedAccessoriesFor(player.getUniqueID()).ifPresent((accessories) -> accessories.forEach((type, accessory) -> {
            accessory.getModel(model).render(player, 0f, 0f, 0f, 0f, 0f, 0.0625f);
        }));
    }

    private void registerTextures(AccessoryType type) {
        final TextureManager manager = Minecraft.getMinecraft().getTextureManager();

        for (ResourceLocation location : type.getTextureLayers()) {
            if (manager.getTexture(location) == null) {
                manager.loadTexture(location, new SimpleTexture(location));
            }
        }
    }
}
