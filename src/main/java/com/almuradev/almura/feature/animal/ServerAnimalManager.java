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
import com.google.inject.Inject;

import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class ServerAnimalManager extends Witness.Impl  {

    @Inject
    private PluginContainer container;

    private final Map<UUID, ItemStackSnapshot> interactedBreeds = new HashMap<>();

    @Listener
    public void onInteractEntitySecondaryMainHand(final InteractEntityEvent.Secondary.MainHand event) {
        if (this.canAdditionalSpawn(event.getTargetEntity())) {
            final ItemStackSnapshot usedItem = event.getContext().get(EventContextKeys.USED_ITEM).orElse(null);
            System.out.println("InteractEntity");
            this.interactedBreeds.put(event.getTargetEntity().getUniqueId(), usedItem);
        }
    }

    @Listener
    public void onSpawnEntity(SpawnEntityEvent event) {

        event.getEntities().stream()
            .filter(e -> e instanceof Animal)
            .map(e -> (Animal) e)
            .forEach(e -> {
                if (e instanceof EntityCow || e instanceof EntityChicken || e instanceof EntityHorse || e instanceof EntityPig || e instanceof EntitySheep) {
                    System.out.println("Ping1");
                    Sponge.getScheduler().createTaskBuilder()
                        .delayTicks(5) // Delay this because the animals age isn't set yet.
                        .execute(() -> {

                                if (!e.isRemoved() && e.getAgeData().age().get() != 0) {
                                    e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, LegacyTexts.stripAll(((net.minecraft.entity.Entity) e).getName(), SpongeTexts.COLOR_CHAR)));
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

        final SpawnType spawnType = event.getContext().get(EventContextKeys.SPAWN_TYPE).orElse(null);
        event.getEntities().stream()
            .filter(e -> e instanceof Animal)
            .map(e -> (Animal) e)
            .forEach(e -> {
                e.offer(Keys.CUSTOM_NAME_VISIBLE, true);

                if (spawnType != null && spawnType == SpawnTypes.BREEDING) {

                    System.out.println("I got here");
                    if (this.colorifyNameplate(e)) {
                        e.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, e.getType().getTranslation().get()));
                    }

                    final ItemStackSnapshot parentA;
                    final ItemStackSnapshot parentB;

                    final List<Animal> animals = event.getCause().allOf(Animal.class);
                    System.out.println("Size: " + animals.size());
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
        if (animal instanceof EntityCow || animal instanceof EntityChicken || animal instanceof EntityHorse || animal instanceof EntityPig || animal instanceof EntitySheep) {
            animal.offer(Keys.DISPLAY_NAME, Text.of(TextColors.WHITE, LegacyTexts.stripAll(((net.minecraft.entity.Entity) animal).getName(), SpongeTexts.COLOR_CHAR)));
        }
    }

    @Listener
    public void onBreedEntityFindMate(final BreedEntityEvent.FindMate event) {
        // Change Name to Yellow while animal is looking for mate.
        event.getCause().allOf(Animal.class).forEach(a -> {
            if (a instanceof EntityCow || a instanceof EntityChicken || a instanceof EntityHorse || a instanceof EntityPig || a instanceof EntitySheep) {
                a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, LegacyTexts.stripAll(((net.minecraft.entity.Entity) a).getName(), SpongeTexts.COLOR_CHAR)));
            }
        });
    }

    @Listener
    public void onBreedEntityBreed(final BreedEntityEvent.Breed event) {
        // Change Name color AFTER BRED
        System.out.println("Fired Breed");
        event.getCause().allOf(Animal.class).forEach(a -> {
            if (a instanceof EntityCow || a instanceof EntityChicken || a instanceof EntityHorse || a instanceof EntityPig || a instanceof EntitySheep) {
                a.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLUE, LegacyTexts.stripAll(((net.minecraft.entity.Entity) a).getName(), SpongeTexts.COLOR_CHAR)));
            }
        });
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
        final Random random = new Random();
        int randomChance = random.nextInt(100);

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

        System.out.println("Item: " + itemName + " Random: " + randomChance + " Chance: " + spawnChance);

        if (spawnChance > randomChance) {
            return additionalSpawnCount;
        }

        return 0;
    }
}
