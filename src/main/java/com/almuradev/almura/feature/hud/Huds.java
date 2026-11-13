/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.Almura;
import com.almuradev.almura.mixin.client.HudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

/**
 * Puts Almura's HUD on the game's: a HUD layer that draws it, vanilla's hearts, armour, food, air, mount health and experience hidden (the
 * HUD shows them), and what vanilla draws at the top middle and top right (boss bars, the player list, effects) moved below its panels.
 */
public final class Huds {

    private static final Identifier LAYER = Identifier.fromNamespaceAndPath(Almura.ID, "origin_hud");
    // Shown by Almura's HUD instead
    private static final Set<Identifier> REPLACED = Set.of(VanillaGuiLayers.PLAYER_HEALTH, VanillaGuiLayers.ARMOR_LEVEL,
        VanillaGuiLayers.FOOD_LEVEL, VanillaGuiLayers.AIR_LEVEL, VanillaGuiLayers.VEHICLE_HEALTH, VanillaGuiLayers.EXPERIENCE_LEVEL);
    private static final Set<Identifier> CONTEXTUAL_BAR = Set.of(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
        VanillaGuiLayers.CONTEXTUAL_INFO_BAR);
    private static final float GAP = 1f;

    // Made on the first frame: mods are constructed before the game is
    private @Nullable OriginHud hud;
    private @Nullable HudHost host;

    public static void register(final IEventBus modBus) {
        final Huds huds = new Huds();
        modBus.addListener(huds::onRegisterLayers);
        NeoForge.EVENT_BUS.register(huds);
    }

    private void onRegisterLayers(final RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EFFECTS, LAYER, (graphics, deltaTracker) -> {
            if (this.shows()) {
                if (this.hud == null) {
                    this.hud = new OriginHud();
                    this.host = new HudHost(Minecraft.getInstance(), this.hud);
                }
                this.hud.refresh();
                this.host.render(graphics);
            }
        });
        // Below the panels at the top middle and top right, where vanilla would draw over them
        event.wrapLayer(VanillaGuiLayers.BOSS_OVERLAY, layer -> this.lowered(layer, () -> this.hud == null ? 0f : this.hud.worldPanelBottom()));
        event.wrapLayer(VanillaGuiLayers.TAB_LIST, layer -> this.lowered(layer, () -> this.hud == null ? 0f : this.hud.worldPanelBottom()));
        event.wrapLayer(VanillaGuiLayers.EFFECTS, layer -> this.lowered(layer, () -> this.hud == null ? 0f : this.hud.detailsPanelBottom()));
    }

    private GuiLayer lowered(final GuiLayer layer, final Supplier<Float> by) {
        return (graphics, deltaTracker) -> {
            final float offset = this.shows() ? by.get() + GAP : 0f;
            graphics.pose().pushMatrix();
            graphics.pose().translate(0f, offset);
            layer.render(graphics, deltaTracker);
            graphics.pose().popMatrix();
        };
    }

    /**
     * Hides vanilla's parts of the HUD that Almura's shows, while it shows.
     */
    @SubscribeEvent
    public void onRenderLayer(final RenderGuiLayerEvent.Pre event) {
        if (!this.shows()) {
            return;
        }
        if (REPLACED.contains(event.getName())
            || CONTEXTUAL_BAR.contains(event.getName()) && this.vanillaBarShowsExperience()) {
            event.setCanceled(true);
        }
    }

    private boolean vanillaBarShowsExperience() {
        final Object shown = ((HudAccessor) Minecraft.getInstance().gui.hud).almura$contextualInfoBar().getFirst();
        return shown instanceof final Enum<?> info && info.name().equals("EXPERIENCE");
    }

    /**
     * @return Whether the HUD shows: in a world, with the HUD on (F1 hides it)
     */
    private boolean shows() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().level != null && !Minecraft.getInstance().gui.hud.isHidden();
    }
}
