/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.almura.shared.event.Witness;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;

public final class ServerAnimalManager extends Witness.Impl implements Activatable, Witness.Lifecycle {

    private final Game game;
    private final PluginContainer container;

    @Inject
    public ServerAnimalManager(final Game game, final PluginContainer container) {
        this.game = game;
        this.container = container;
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    @Listener(order = Order.LAST)
    public void onSpawnEntity(SpawnEntityEvent event) {
        // Tell all farm animals to render their nameplates
        event.getEntities().stream().filter((entity) -> entity instanceof Animal).forEach((entity) -> ((EntityLiving) entity).setAlwaysRenderNameTag(true));
    }

    @Listener(order = Order.LAST)
    public void onInteractEntity(InteractEntityEvent.Secondary.MainHand event) {
        if (event.getTargetEntity() instanceof EntityAnimal) {
            Task.builder().delayTicks(1).execute(() -> {
                final EntityAnimal animal = (EntityAnimal) event.getTargetEntity();
                if (!animal.isChild() && animal.isInLove() && !animal.hasCustomName()) {
                    animal.setCustomNameTag(TextFormatting.DARK_AQUA + animal.getName());
                }
            }).submit(this.container);
        }
    }
}
