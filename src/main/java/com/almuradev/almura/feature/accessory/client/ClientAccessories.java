/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.accessory.client;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.accessory.AccessoryType;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ClientboundOpen;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ClientboundWorn;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ServerboundOpen;
import com.almuradev.almura.feature.accessory.network.AccessoryMessages.ServerboundSelect;
import com.almuradev.almura.feature.menu.InGameScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The accessories on the client, as 3.5's {@code ClientAccessoryManager}: what each player wears, as the server last sent it, put on their
 * render state for the accessory layer (a NeoForge render state modifier: other render data is reset once a state is extracted), the name
 * tag (and so the title and health bar) raised above a tall hat, and the window's try-on for the player themselves.
 */
public final class ClientAccessories {

    static final ContextKey<Map<AccessoryType, String>> WORN = new ContextKey<>(Identifier.fromNamespaceAndPath(Almura.ID, "accessories"));
    // How far a player's labels are raised above their tallest accessory, in blocks; cleared once applied
    private static final ContextKey<Float> LIFT = new ContextKey<>(Identifier.fromNamespaceAndPath(Almura.ID, "accessory_lift"));

    private static final Map<UUID, Map<AccessoryType, String>> worn = new HashMap<>();
    // What the player tries on in the window: a type with a variant to show, or none (null) to show it taken off
    private static @Nullable AccessoryType previewType;
    private static @Nullable String previewVariant;

    private ClientAccessories() {
    }

    /**
     * Asks the server to open the Accessories window (the Features menu's Accessories button).
     */
    public static void requestOpen() {
        ClientPacketDistributor.sendToServer(new ServerboundOpen());
    }

    static void select(final AccessoryType type, final String variant) {
        ClientPacketDistributor.sendToServer(new ServerboundSelect(type.id(), variant));
    }

    public static void onOpen(final ClientboundOpen message) {
        final Minecraft minecraft = Minecraft.getInstance();
        InGameScreens.open(new AccessoryScreen(message), minecraft.gui.screen());
    }

    public static void onWorn(final ClientboundWorn message) {
        final Map<AccessoryType, String> accessories = new EnumMap<>(AccessoryType.class);
        message.worn().forEach((id, variant) -> {
            final AccessoryType type = AccessoryType.byId(id);
            if (type != null) {
                accessories.put(type, variant);
            }
        });
        if (accessories.isEmpty()) {
            worn.remove(message.player());
        } else {
            worn.put(message.player(), accessories);
        }
    }

    static @Nullable String worn(final UUID player, final AccessoryType type) {
        final Map<AccessoryType, String> accessories = worn.get(player);
        return accessories == null ? null : accessories.get(type);
    }

    /**
     * Shows the player wearing the variant (or, with none, without the type) until the window closes or {@link #clearPreview()}.
     */
    static void preview(final AccessoryType type, final @Nullable String variant) {
        previewType = type;
        previewVariant = variant;
    }

    static void clearPreview() {
        previewType = null;
        previewVariant = null;
    }

    /**
     * What the player wears, with the window's try-on for the player themselves.
     */
    private static Map<AccessoryType, String> shown(final UUID player) {
        final Map<AccessoryType, String> accessories = worn.get(player);
        final Minecraft minecraft = Minecraft.getInstance();
        if (previewType == null || minecraft.player == null || !minecraft.player.getUUID().equals(player)) {
            return accessories == null ? Map.of() : accessories;
        }
        final Map<AccessoryType, String> tried = accessories == null ? new EnumMap<>(AccessoryType.class) : new EnumMap<>(accessories);
        if (previewVariant == null) {
            tried.remove(previewType);
        } else {
            tried.put(previewType, previewVariant);
        }
        return tried;
    }

    /**
     * Puts each player's accessories on their render state, and raises the name tag above the tallest.
     */
    public static void onRegisterRenderStateModifiers(final RegisterRenderStateModifiersEvent event) {
        event.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(final T entity, final AvatarRenderState state) {
                final Map<AccessoryType, String> accessories = shown(entity.getUUID());
                state.setRenderData(WORN, accessories.isEmpty() ? null : accessories);
                float pixels = 0F;
                for (final AccessoryType type : accessories.keySet()) {
                    pixels = Math.max(pixels, type.heightAboveHead());
                }
                // A helmet lifts what sits on the head by a pixel
                if (pixels > 0F && AccessoryLayer.isArmor(entity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD)) {
                    pixels += 1F;
                }
                // How far the labels go up; applied as they are drawn (onDoRender), when the name tag's place is known
                state.setRenderData(LIFT, pixels > 0F ? pixels / 16.0F * state.scale : null);
            }
        });
    }

    /**
     * Adds the accessory layer to the players' renderers (wide and slim).
     */
    public static void onAddLayers(final EntityRenderersEvent.AddLayers event) {
        for (final PlayerModelType skin : event.getSkins()) {
            final AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new AccessoryLayer(renderer));
            }
        }
    }

    /**
     * The last server's accessories are forgotten on joining another.
     */
    /**
     * @return how far the state's labels will be raised above its tallest accessory, in blocks (0 for none)
     */
    public static float labelLift(final EntityRenderState state) {
        final Float lift = state.getRenderData(LIFT);
        return lift == null ? 0F : lift;
    }

    /**
     * Raises a player's labels (name, title, health bar) above their tallest accessory: first of the label handlers, before vanilla, the
     * title line and the health bar read where the name tag goes. Applied once a frame (the score line and the name both fire this).
     */
    public static final class Labels {

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onDoRender(final RenderNameTagEvent.DoRender event) {
            final EntityRenderState state = event.getEntityRenderState();
            final Float lift = state.getRenderData(LIFT);
            if (lift != null && state.nameTagAttachment != null) {
                state.nameTagAttachment = state.nameTagAttachment.add(0.0, lift, 0.0);
                state.setRenderData(LIFT, null);
            }
        }
    }

    public static final class Resetter {

        @SubscribeEvent
        public void onLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
            worn.clear();
            clearPreview();
        }

        @SubscribeEvent
        public void onLoggingOut(final ClientPlayerNetworkEvent.LoggingOut event) {
            worn.clear();
            clearPreview();
        }
    }
}
