/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.network.ServerboundAccessoryGuiRequestPacket;
import com.almuradev.almura.feature.accessory.network.ServerboundAccessorySelectPacket;
import com.almuradev.almura.shared.network.NetworkConfig;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * What everyone wears, as told by the server, and the accessory screen's preview for the local player.
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientAccessoryManager implements Witness {

    // One model pixel in blocks, players are drawn at 0.9375 scale
    private static final float PIXEL_TO_BLOCKS = 0.0625F * 0.9375F;
    @Nullable private static ClientAccessoryManager instance;
    // Set while the accessory screen draws its player preview, for the labels it hides there
    private static boolean previewHidesName;
    private static boolean previewHidesTitle;
    private static boolean previewHidesHealth;

    public static boolean previewHidesName() {
        return previewHidesName;
    }

    public static boolean previewHidesTitle() {
        return previewHidesTitle;
    }

    public static boolean previewHidesHealth() {
        return previewHidesHealth;
    }

    public static void setPreviewHidden(final boolean name, final boolean title, final boolean health) {
        previewHidesName = name;
        previewHidesTitle = title;
        previewHidesHealth = health;
    }

    private final ChannelBinding.IndexedMessageChannel network;
    private final Map<UUID, Map<AccessoryType, String>> worn = new HashMap<>();
    // The accessory screen's try-on for the local player: a variant, or null to show the type taken off
    @Nullable private AccessoryType previewType;
    @Nullable private String previewVariant;
    private boolean layersAdded;

    @Inject
    private ClientAccessoryManager(@ChannelId(NetworkConfig.CHANNEL) final ChannelBinding.IndexedMessageChannel network) {
        this.network = network;
        instance = this;
    }

    /**
     * @return How much higher (in blocks) the name, title and health bar above the entity go, for the tallest head accessory it wears
     */
    public static float labelLift(final EntityLivingBase entity) {
        final ClientAccessoryManager manager = instance;
        if (manager == null || !(entity instanceof AbstractClientPlayer) || entity.isInvisible()) {
            return 0.0F;
        }
        float pixels = 0.0F;
        for (final AccessoryType type : manager.getWorn((AbstractClientPlayer) entity).keySet()) {
            pixels = Math.max(pixels, type.heightAboveHead());
        }
        return pixels * PIXEL_TO_BLOCKS;
    }

    @Listener
    public void onGamePostInitialization(final GamePostInitializationEvent event) {
        this.addLayers();
    }

    @SubscribeEvent
    public void onClientConnectedToServer(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        // Network thread
        Minecraft.getMinecraft().addScheduledTask(() -> {
            this.worn.clear();
            this.clearPreview();
            this.addLayers();
        });
    }

    /**
     * Adds the accessory layer to the player renderers (default and slim arms), once.
     */
    private void addLayers() {
        if (this.layersAdded) {
            return;
        }
        for (final RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            renderer.addLayer(new AccessoryLayer(renderer, this));
        }
        this.layersAdded = true;
    }

    public void handleWorn(final UUID player, final Map<AccessoryType, String> accessories) {
        if (accessories.isEmpty()) {
            this.worn.remove(player);
        } else {
            this.worn.put(player, new EnumMap<>(accessories));
        }
    }

    /**
     * @return What the player wears, with the accessory screen's try-on applied for the local player
     */
    public Map<AccessoryType, String> getWorn(final AbstractClientPlayer player) {
        final Map<AccessoryType, String> accessories = this.worn.get(player.getUniqueID());
        if (this.previewType == null || player != Minecraft.getMinecraft().player) {
            return accessories == null ? Collections.emptyMap() : accessories;
        }

        final Map<AccessoryType, String> preview = accessories == null ? new EnumMap<>(AccessoryType.class) : new EnumMap<>(accessories);
        if (this.previewVariant == null) {
            preview.remove(this.previewType);
        } else {
            preview.put(this.previewType, this.previewVariant);
        }
        return preview;
    }

    @Nullable
    public String getWornVariant(final UUID player, final AccessoryType type) {
        final Map<AccessoryType, String> accessories = this.worn.get(player);
        return accessories == null ? null : accessories.get(type);
    }

    public void setPreview(final AccessoryType type, @Nullable final String variant) {
        this.previewType = type;
        this.previewVariant = variant;
    }

    public void clearPreview() {
        this.previewType = null;
        this.previewVariant = null;
    }

    public void requestGui() {
        this.network.sendToServer(new ServerboundAccessoryGuiRequestPacket());
    }

    public void select(final AccessoryType type, @Nullable final String variant) {
        this.network.sendToServer(new ServerboundAccessorySelectPacket(type.id(), variant == null ? "" : variant));
    }
}
