/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.core.event.Witness;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.animal.Chicken;
import org.spongepowered.api.entity.living.animal.Cow;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.animal.Pig;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Singleton;

@Singleton
public final class ServerAnimalManager extends Witness.Impl  {

    private final Map<UUID, ItemStackSnapshot> interactedBreeds = new HashMap<>();

    @Listener
    public void onInteractEntitySecondaryMainHand(final InteractEntityEvent.Secondary.MainHand event) {
        if (this.canAdditionalSpawn(event.getTargetEntity())) {
            final ItemStackSnapshot usedItem = event.getContext().get(EventContextKeys.USED_ITEM).orElse(null);

            this.interactedBreeds.put(event.getTargetEntity().getUniqueId(), usedItem);
        }
    }

    @Listener
    public void onSpawnEntity(final SpawnEntityEvent event) {
        final SpawnType spawnType = event.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
        event.getEntities().stream()
            .filter(e -> e instanceof Animal)
            .map(e -> (Animal) e)
            .forEach(e -> {
                e.offer(Keys.CUSTOM_NAME_VISIBLE, true);

                if (spawnType != null && spawnType == SpawnTypes.BREEDING) {


                    if (this.colorifyNameplate(e)) {
                        e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, e.getType().getTranslation().get()));
                    }

                    final ItemStackSnapshot parentA;
                    final ItemStackSnapshot parentB;

                    final List<Animal> animals = event.getCause().allOf(Animal.class);
                    if (animals.size() == 2) {
                        parentA = this.interactedBreeds.remove(animals.get(0).getUniqueId());
                        parentB = this.interactedBreeds.remove(animals.get(1).getUniqueId());

                        if (parentA != null && parentB != null) {
                            if (parentA.getType() == parentB.getType()) {
                                final int spawnCount = this.getAdditionalSpawnCount(e, parentA);

                                for (int i = 0; i < spawnCount; i++) {
                                    final Animal other = (Animal) e.getWorld().createEntity(e.getType(), e.getLocation().getPosition());
                                    other.offer(Keys.AGE, -24000);

                                    if (this.colorifyNameplate(other)) {
                                        other.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, e.getType().getTranslation().get()));
                                    }

                                    event.getEntities().add(other);
                                }
                            }
                        }
                    }
                } else if (this.colorifyNameplate(e) && e.getAgeData().age().get() != 0) {
                    e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, e.getType().getTranslation().get()));
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
    }

    private boolean colorifyNameplate(final Animal animal) {
        return animal instanceof Cow || animal instanceof Chicken || animal instanceof Horse || animal instanceof Pig || animal instanceof Sheep;
    }

    private boolean canAdditionalSpawn(final Entity entity) {
        return entity instanceof Cow;
    }

    private int getAdditionalSpawnCount(final Animal baby, final ItemStackSnapshot usedItem) {
        checkNotNull(baby);
        checkNotNull(usedItem);

        int additionalSpawnCount = 0;  //Physical count of additional spawn besides the original within the event.
        int spawnChance = 0;  // Percentage

        // Format:  almura:normal/ingredient/barley & almura:food/food/soybean
        final String itemName = usedItem.getType().getName();

        // Cows
        if (baby.getType() == EntityTypes.COW) {
            if (itemName.equalsIgnoreCase("almura:food/food/soybean") || itemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount = 1;
            }
        }

        // Pigs
        if (baby.getType() == EntityTypes.PIG) {
            if (itemName.equalsIgnoreCase("almura:food/food/soybean")) {
                additionalSpawnCount = 1;
                spawnChance = 30;
            }

            if (itemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount = 2;
                spawnChance = 20;
            }
        }

        //Todo: write randomizer code here.


        return 0;
    }
}
