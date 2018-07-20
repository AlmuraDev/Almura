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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
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

                    Sponge.getScheduler().createTaskBuilder()
                            .delayTicks(5) // Delay this because the animals age isn't set yet.
                            .execute(() -> {
                                if (e.getAgeData().age().get() != 0) {
                                    // Todo: this shouldn't be needed.
                                    e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_GRAY, e.getTranslation().get()));
                                }
                            })
                            .submit(this.container);

                    e.offer(Keys.CUSTOM_NAME_VISIBLE, true);
                });
    }

    /* // This event isn't 100% implemented on SpongeCommon yet.
    @Listener
    public void onBreedEntityReadyToMate(final BreedEntityEvent.ReadyToMate event) {
        final Animal animal = event.getTargetEntity();
        animal.offer(Keys.DISPLAY_NAME, Text.of(animal.getTranslation().get()));  // Revert name to White
    }

    @Listener
    public void onBreedEntityFindMate(final BreedEntityEvent.FindMate event) {
        // Change Name to Yellow while animal is looking for mate.
        event.getCause().allOf(Animal.class).forEach(a -> a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, a.getTranslation().get())));
    }

    @Listener
    public void onBreedEntityBreed(final BreedEntityEvent.Breed event) {
        // Change Name color AFTER BRED
        event.getCause().allOf(Animal.class).forEach(a -> a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLUE, a.getTranslation().get())));
    } */
}
