/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.registry.AccessoryTypeRegistryModule;
import com.almuradev.almura.feature.accessory.type.Accessory;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SideOnly(Side.CLIENT)
@Singleton
public final class ClientAccessoryManager implements Witness {

    private final AccessoryTypeRegistryModule store;
    private final AccessoryTypeRegistryModule accessoryRegistry;

    private Map<UUID, Map<AccessoryType, Accessory>> selectedAccessories = new HashMap<>();

    @Inject
    public ClientAccessoryManager(final AccessoryTypeRegistryModule store, final AccessoryTypeRegistryModule accessoryRegistry) {
        this.store = store;
        this.accessoryRegistry = accessoryRegistry;
    }

    @Listener
    public void onGameLoadComplete(final GameLoadCompleteEvent event) {
        this.store.getAll().forEach(this::registerTextures);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientConnectedToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.selectedAccessories.clear();
    }

    @SubscribeEvent
    public void onRenderPlayerPost(final RenderPlayerEvent.Pre event) {
        final EntityPlayer player = event.getEntityPlayer();
        final RenderPlayer renderer = event.getRenderer();
        final ModelPlayer model = renderer.getMainModel();

        final Map<AccessoryType, Accessory> accessories = this.selectedAccessories.get(player.getUniqueID());
        if (accessories != null) {
            accessories.values().forEach(accessory -> accessory.getModel(model).render(player, 0f, 0f, 0f, 0f, 0f, 0.0625f));
        }
    }

    public void handleSelectedAccessories(final UUID uniqueId, @Nullable final Set<String> accessoryIds) {
        checkNotNull(uniqueId);

        this.selectedAccessories.remove(uniqueId);

        if (accessoryIds == null) {
            return;
        }

        final Map<AccessoryType, Accessory> accessories = new HashMap<>();

        accessoryIds.forEach(id -> this.accessoryRegistry.getById(id).ifPresent(accessoryType -> {
            final Accessory accessory;
            try {
                accessory =
                  accessoryType.getAccessoryClass().getConstructor(UUID.class, AccessoryType.class).newInstance(uniqueId, accessoryType);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            accessories.put(accessoryType, accessory);
        }));

        if (!accessories.isEmpty()) {
            this.selectedAccessories.put(uniqueId, accessories);
        }
    }

    private void registerTextures(final AccessoryType type) {
        final TextureManager manager = Minecraft.getMinecraft().getTextureManager();

        for (ResourceLocation location : type.getTextureLayers()) {
            if (manager.getTexture(location) == null) {
                manager.loadTexture(location, new SimpleTexture(location));
            }
        }
    }
}
