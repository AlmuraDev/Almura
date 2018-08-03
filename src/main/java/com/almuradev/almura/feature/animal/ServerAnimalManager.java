/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.core.event.Witness;
import com.google.inject.Inject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import org.apache.commons.lang3.text.WordUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class ServerAnimalManager extends Witness.Impl  {

    @Inject
    private PluginContainer container;

    @Listener
    public void onSpawnEntity(SpawnEntityEvent event) {
        event.getEntities().stream()
                .filter(e -> e instanceof Animal)
                .map(e -> (Animal) e)
                .forEach(e -> {
                    if (e instanceof EntityCow || e instanceof EntityChicken || e instanceof EntityHorse || e instanceof EntityPig || e instanceof EntitySheep) {
                        Sponge.getScheduler().createTaskBuilder()
                                .delayTicks(5) // Delay this because the animals age isn't set yet.
                                .execute(() -> {
                                    System.out.println("Spawn: " + e.getType().getName() + " / " + ((Entity)e).getName());
                                    if (!e.isRemoved() && e.getAgeData().age().get() != 0) {
                                                e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, ((Entity)e).getName()));
                                                // Todo: The below bugs may affect this feature.
                                                // Bug 1: bug here, using .getTranslation().get() for MoCreatures returns "unknown"
                                                // Bug 2: e.getType().getName() returns a lowerCase name of animals.
                                            }
                                        }
                                )
                                .submit(this.container);
                        e.offer(Keys.CUSTOM_NAME_VISIBLE, true);
                    }
                });
    }

    @Listener
    public void onBreedEntityReadyToMate(final BreedEntityEvent.ReadyToMate event) {
        final Animal animal = event.getTargetEntity();
        animal.offer(Keys.DISPLAY_NAME, Text.of(((Entity) animal).getName())); // Revert name to White
    }

    @Listener
    public void onBreedEntityFindMate(final BreedEntityEvent.FindMate event) {
        // Change Name to Yellow while animal is looking for mate.
        event.getCause().allOf(Animal.class).forEach(a -> {
            a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, ((Entity)a).getName()));
            System.out.println("Find Mate: " + a.toString());
        });
    }

    @Listener
    public void onBreedEntityBreed(final BreedEntityEvent.Breed event) {
        // Change Name color AFTER BRED
        System.out.println("Fired Breed");
        event.getCause().allOf(Animal.class).forEach(a -> {
            // Todo: this no longer works.  Recreate by spawning two cows, mate them, this fires and doesn't change the name blue.
            a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLUE, ((Entity)a).getName()));
            System.out.println("Breed Mate: " + a.toString());
        });
    }
}
