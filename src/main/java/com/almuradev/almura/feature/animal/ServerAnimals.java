/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.animal;

import com.almuradev.almura.content.registry.ContentCrop;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Almura's farm animals, as 3.5's {@code ServerAnimalManager} and its animal mixins:
 * <ul>
 *   <li>Breeding shows in their names: white when fed and ready to mate, yellow once a mate is near, blue after breeding, and a calf, piglet,
 *   lamb or chick is named in aqua, its name always showing (cows, chickens, horses, pigs and sheep).</li>
 *   <li>Bred on Almura's feed, a litter can be bigger: one more calf at 15% a parent fed soybean, corn or alfalfa (25% more for alfalfa),
 *   one more piglet or lamb at 20% a parent fed soybean or corn, two more chicks at 25% a parent fed soybean or corn (3.5's numbers; a
 *   pair fed at a feeding trough gets none, as on 3.5).</li>
 *   <li>Fed to a young one, its food grows it half its remaining time (vanilla grows it a tenth).</li>
 *   <li>Cows graze grass and alfalfa, sheep alfalfa ({@link GrazeGoal}).</li>
 *   <li>Harvesting a content crop tires the player a little, as 3.5's harvest did.</li>
 * </ul>
 * What they eat and follow is vanilla's food tags with Almura's feed added ({@code data/minecraft/tags/item/*_food.json}); their extra
 * meat is loot ({@code data/almura/loot_modifiers/animal}).
 */
public final class ServerAnimals {

    private static final Set<String> FEED = Set.of("almura:food/food/soybean", "almura:food/food/corn", "almura:normal/crop/alfalfa_item");
    private static final String ALFALFA = "almura:normal/crop/alfalfa_item";
    // How far a mate is looked for (vanilla's breeding goal looks 8 blocks)
    private static final double MATE_RANGE = 8.0;

    // The item each animal was last fed, by its UUID, for its litter
    private final Map<UUID, String> fedWith = new HashMap<>();
    // Animals just fed, to see on the next tick whether they fell in love; and those in love, until a mate is near
    private final List<Animal> fed = new ArrayList<>();
    private final List<Animal> seeking = new ArrayList<>();
    private int ticks;

    private static boolean farmAnimal(final Entity entity) {
        return entity instanceof AbstractCow || entity instanceof Chicken || entity instanceof Horse || entity instanceof Pig
            || entity instanceof Sheep;
    }

    @SubscribeEvent
    public void onJoin(final EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && (event.getEntity() instanceof AbstractCow || event.getEntity() instanceof Sheep)
            && event.getEntity() instanceof final Animal animal) {
            // Beside vanilla's goals, where 3.5 put its grass eating (after mating, before following food)
            animal.goalSelector.addGoal(3, new GrazeGoal(animal));
        }
    }

    @SubscribeEvent
    public void onInteract(final PlayerInteractEvent.EntityInteract event) {
        if (!(event.getLevel() instanceof final ServerLevel level) || !(event.getTarget() instanceof final Animal animal) || !farmAnimal(animal)) {
            return;
        }
        final ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        this.fedWith.put(animal.getUUID(), BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (animal.isBaby() && !(animal instanceof Horse) && animal.isFood(stack)) {
            // 3.5's feeding: half the time left to grow, at once
            final Player player = event.getEntity();
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            animal.ageUp(-animal.getAge() / 2 / 20);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getRandomX(1.0), animal.getRandomY() + 0.5, animal.getRandomZ(1.0), 1, 0, 0,
                0, 0);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
            return;
        }
        // Whether it fell in love shows on the next tick, after vanilla's feeding
        this.fed.add(animal);
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Post event) {
        for (final Animal animal : this.fed) {
            if (!animal.isRemoved() && animal.isInLove()) {
                name(animal, ChatFormatting.WHITE);
                if (!this.seeking.contains(animal)) {
                    this.seeking.add(animal);
                }
            }
        }
        this.fed.clear();
        if (++this.ticks % 10 != 0) {
            return;
        }
        // In love with a mate near: yellow, as 3.5 named an animal that found its mate
        for (final Iterator<Animal> iterator = this.seeking.iterator(); iterator.hasNext(); ) {
            final Animal animal = iterator.next();
            if (animal.isRemoved() || !animal.isInLove()) {
                iterator.remove();
                continue;
            }
            final List<? extends Animal> mates = animal.level().getEntitiesOfClass(animal.getClass(), animal.getBoundingBox().inflate(MATE_RANGE),
                other -> other != animal && other.isInLove());
            if (!mates.isEmpty()) {
                name(animal, ChatFormatting.YELLOW);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void onBabySpawn(final BabyEntitySpawnEvent event) {
        final AgeableMob child = event.getChild();
        if (child == null || !(child.level() instanceof final ServerLevel level) || !farmAnimal(child)) {
            return;
        }
        final Animal parentA = event.getParentA() instanceof final Animal a ? a : null;
        final Animal parentB = event.getParentB() instanceof final Animal b ? b : null;
        nameBaby(child);
        // A litter, by what the parents were fed (none when one of them wasn't fed by hand)
        final @Nullable String itemA = parentA == null ? null : this.fedWith.remove(parentA.getUUID());
        final @Nullable String itemB = parentB == null ? null : this.fedWith.remove(parentB.getUUID());
        final int more = extraLitter(child, itemA, itemB, level.getRandom().nextInt(100));
        final Entity at = parentA != null ? parentA : child;
        for (int i = 0; i < more; i++) {
            @SuppressWarnings("unchecked")
            final EntityType<? extends AgeableMob> type = (EntityType<? extends AgeableMob>) child.getType();
            final AgeableMob other = type.create(level, EntitySpawnReason.BREEDING);
            if (other != null) {
                other.setAge(AgeableMob.BABY_START_AGE);
                other.snapTo(at.getX(), at.getY(), at.getZ(), 0.0F, 0.0F);
                nameBaby(other);
                level.addFreshEntity(other);
            }
        }
        if (parentA != null) {
            name(parentA, ChatFormatting.BLUE);
        }
        if (parentB != null) {
            name(parentB, ChatFormatting.BLUE);
        }
    }

    @SubscribeEvent
    public void onBreak(final BreakBlockEvent event) {
        // Harvesting a content crop tires the player a little ("this is how we make players use more food/drink")
        if (event.getLevel() instanceof ServerLevel && event.getState().getBlock() instanceof ContentCrop) {
            event.getPlayer().causeFoodExhaustion(0.01F);
        }
    }

    /**
     * 3.5's extra young: per parent, what it was fed adds to the chance (and, for pigs, sheep and chickens, to how many); the chance is
     * against one roll from 0 to 99.
     */
    static int extraLitter(final Entity baby, final @Nullable String itemA, final @Nullable String itemB, final int roll) {
        if (itemA == null || itemB == null) {
            return 0;
        }
        int count = 0, chance = 0;
        for (final String item : new String[] {itemA, itemB}) {
            final boolean soyOrCorn = item.equals("almura:food/food/soybean") || item.equals("almura:food/food/corn");
            if (baby instanceof AbstractCow) {
                if (FEED.contains(item)) {
                    // A calf more at most, however fed
                    count = 1;
                    chance += 15;
                }
                if (item.equals(ALFALFA)) {
                    chance += 25;
                }
            } else if (baby instanceof Pig || baby instanceof Sheep) {
                if (soyOrCorn) {
                    count += 1;
                    chance += 20;
                }
            } else if (baby instanceof Chicken) {
                if (soyOrCorn) {
                    count += 2;
                    chance += 25;
                }
            }
        }
        return chance > roll ? count : 0;
    }

    private static void nameBaby(final AgeableMob baby) {
        baby.setCustomName(Component.literal(baby.getType().getDescription().getString()).withStyle(ChatFormatting.AQUA));
        baby.setCustomNameVisible(true);
    }

    /**
     * The animal's name (a name a player gave it, or its kind's) in the colour.
     */
    private static void name(final Animal animal, final ChatFormatting colour) {
        final Component custom = animal.getCustomName();
        final String name = custom != null ? ChatFormatting.stripFormatting(custom.getString()) : animal.getType().getDescription().getString();
        animal.setCustomName(Component.literal(name).withStyle(colour));
    }
}
