/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.core.event.Witness;
import net.kyori.membrane.facet.Activatable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;

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
        System.out.println("Listening to Animals");
        if (event.getTargetEntity() instanceof EntityAnimal) {
            final EntityAnimal animal = (EntityAnimal) event.getTargetEntity();
            System.out.println("isChild: " + animal.isChild() + " inLove: " + animal.isInLove());
            if (!animal.isChild() && animal.isInLove()) { // && !animal.hasCustomName()) {
                animal.setCustomNameTag(TextFormatting.DARK_AQUA + animal.getName());
            } else {
                animal.setCustomNameTag(TextFormatting.WHITE + animal.getName());
            }
        }
    }

    /*@SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void render(RenderLivingEvent.Pre event) {
        float health = event.getEntity().getHealth();
        String name = event.getEntity().getDisplayName().getFormattedText();
        //System.out.println(name + ", " + health);
        //event.getEntity().setCustomNameTag("" + health + "§c❤");
        if (event.getEntity() instanceof EntityCow) {
            System.out.println("Cow");
            System.out.println("inLove: " + ((EntityCow) event.getEntity()).isInLove());
        }
        event.getEntity().setAlwaysRenderNameTag(true);

        return;
    }*/
}
