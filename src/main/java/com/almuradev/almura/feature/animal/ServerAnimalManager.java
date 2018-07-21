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
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public final class ServerAnimalManager extends Witness.Impl  {

    @Inject
    private PluginContainer container;

    // Todo: Re-enable this once SpongeCommon is updated.
    /*@Listener
    public void onSpawnEntity(SpawnEntityEvent event) {
        event.getEntities().stream()
                .filter(e -> e instanceof Animal)
                .map(e -> (Animal) e)
                .forEach(e -> {
                    if (e instanceof EntityCow || e instanceof EntityChicken || e instanceof EntityHorse || e instanceof EntityPig || e instanceof EntitySheep) {
                        Sponge.getScheduler().createTaskBuilder()
                                .delayTicks(1) // Delay this because the animals age isn't set yet.
                                .execute(() -> {
                                            if (!e.isRemoved() && e.getAgeData().age().get() != 0) {
                                                //e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, e.getType().getTranslation().get()));
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
