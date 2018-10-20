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
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.animal.Chicken;
import org.spongepowered.api.entity.living.animal.Cow;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.animal.Pig;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
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
    private static final Random RANDOM = new Random();
    private final Map<UUID, ItemStackSnapshot> usedItemCache = new HashMap<>();

    @Listener
    public void onInteractEntitySecondary(final InteractEntityEvent.Secondary event) {
        if (!this.isValidEntity(event.getTargetEntity())) {
            return;
        }

        final ItemStackSnapshot snapshot = event.getContext().get(EventContextKeys.USED_ITEM).orElse(null);
        if (snapshot == null || snapshot.getType() == ItemTypes.NONE) {
            return;
        }

        this.usedItemCache.put(event.getTargetEntity().getUniqueId(), snapshot);
    }

    @Listener
    public void onBreedEntity(final BreedEntityEvent.Breed event) {
        final List<Animal> parents = event.getCause().allOf(Animal.class);

        if (parents.size() != 2) {
           return;
        }

        final ItemStackSnapshot parentASnapshot = this.usedItemCache.remove(parents.get(0).getUniqueId());
        final ItemStackSnapshot parentBSnapshot = this.usedItemCache.remove(parents.get(1).getUniqueId());

        final Ageable child = event.getOffspringEntity();

        child.offer(Keys.CUSTOM_NAME_VISIBLE, true);

        if (this.colorifyNameplate(child)) {
            child.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, child.getType().getTranslation().get()));
        }

        final int additionalSpawnCount = this.getAdditionalSpawnCount(child, parentASnapshot, parentBSnapshot);

        for (int i = 0; i < additionalSpawnCount; i++) {
            final Ageable other = (Ageable) child.getWorld().createEntity(child.getType(), child.getLocation().getPosition());
            other.offer(Keys.CUSTOM_NAME_VISIBLE, true);
            other.offer(Keys.AGE, -24000);


            if (this.colorifyNameplate(other)) {
                other.offer(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, child.getType().getTranslation().get()));
            }

            child.getWorld().spawnEntity(other);
        }

        parents.forEach(parent -> {
            if (this.colorifyNameplate(parent)) {
                parent.offer(Keys.DISPLAY_NAME, Text.of(TextColors.BLUE, LegacyTexts.stripAll(((net.minecraft.entity.Entity) parent).getName(),
                  SpongeTexts.COLOR_CHAR)));
            }
        });
    }

    @Listener
    public void onBreedEntityReadyToMate(final BreedEntityEvent.ReadyToMate event) {
        // Change Name to White when we are ready to mate
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

    private boolean isValidEntity(final Entity entity) {
        return entity instanceof Cow || entity instanceof Chicken || entity instanceof Horse || entity instanceof Pig || entity instanceof Sheep;
    }

    private boolean colorifyNameplate(final Ageable ageable) {
        return ageable instanceof Cow || ageable instanceof Chicken || ageable instanceof Horse || ageable instanceof Pig || ageable instanceof Sheep;
    }

    private int getAdditionalSpawnCount(final Ageable baby, final ItemStackSnapshot parentAItem, final ItemStackSnapshot parentBItem) {
        checkNotNull(baby);
        checkNotNull(parentAItem);
        checkNotNull(parentBItem);

        final int randomChance = RANDOM.nextInt(100);
        int additionalSpawnCount = 0;  //Physical count of additional spawn besides the original within the event.
        int spawnChance = 0;  // Percentage

        // Format:  almura:normal/ingredient/barley & almura:food/food/soybean
        final String parentAItemName = parentAItem.getType().getName();
        final String parentBItemName = parentBItem.getType().getName();

        // Cows
        if (baby.getType() == EntityTypes.COW) {
            if (parentAItemName.equalsIgnoreCase("almura:food/food/soybean") || parentAItemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount += 1;
                spawnChance = 20;
            }
            if (parentBItemName.equalsIgnoreCase("almura:food/food/soybean") || parentBItemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount += 1;
                spawnChance = 20;
            }
        }

        // Pigs
        if (baby.getType() == EntityTypes.PIG) {
            if (parentAItemName.equalsIgnoreCase("almura:food/food/soybean")) {
                additionalSpawnCount += 2;
                spawnChance = 30;
            }

            if (parentBItemName.equalsIgnoreCase("almura:food/food/soybean")) {
                additionalSpawnCount += 2;
                spawnChance = 30;
            }

            if (parentAItemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount += 2;
                spawnChance = 20;
            }

            if (parentBItemName.equalsIgnoreCase("almura:food/food/corn")) {
                additionalSpawnCount += 2;
                spawnChance = 20;
            }
        }

        if (spawnChance > randomChance) {
            return additionalSpawnCount;
        }

        return 0;
    }
}