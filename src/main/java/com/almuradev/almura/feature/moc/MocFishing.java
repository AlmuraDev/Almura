/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.moc;

import com.almuradev.almura.mixin.FishingHookInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Lets players catch Mo' Creatures fish with a fishing rod, as 3.5's.
 *
 * <ol>
 *     <li>A catchable Mo' Creatures fish near a bobber resting in water swims to it.</li>
 *     <li>When it reaches the bobber it bites: it is hooked on it and keeps tugging the bobber under, with splashes and bubbles.</li>
 *     <li>Right clicking while it's on lands it: the fish dies to the player and its drops (Almura's, by {@link MocDrops}: cod gives raw cod)
 *     and experience are thrown from the bobber to the player, as vanilla fishing loot is.</li>
 *     <li>Waiting too long lets it get away; it then won't bite again for a few seconds.</li>
 * </ol>
 *
 * <p>Mo' Creatures' own fish swim to a bobber but never bite it. Only registered with Mo' Creatures loaded, and it names Mo' Creatures by
 * id alone, so there is no dependency on it.</p>
 */
public final class MocFishing {

    // The fish Mo' Creatures lets bite a hook (its small and medium fish)
    private static final Set<String> CATCHABLE = Set.of(
        "anchovy", "angelfish", "angler", "clownfish", "goldfish", "hippotang", "manderin",
        "cod", "bass", "salmon"
    );

    // How often (ticks) bobbers look for fish and hooked fish are steered
    private static final int UPDATE_INTERVAL = 10;
    // How far from the bobber fish notice it
    private static final double ATTRACT_RADIUS = 10.0D;
    // How close a fish must get to the bobber to bite
    private static final double BITE_DISTANCE = 1.5D;
    // How long a fish stays on the hook before getting away (ticks)
    private static final int ESCAPE_TICKS = 100;
    // How long a fish that got away won't bite again (ticks)
    private static final int ESCAPE_COOLDOWN_TICKS = 100;
    private static final double SWIM_SPEED = 1.2D;

    // Fish being landed, by the player who caught them, so their drops and experience are thrown to that player
    private final Map<Entity, UUID> landing = new WeakHashMap<>();
    // Bobbers with a fish on them, by the game time it bit
    private final Map<FishingHook, Long> bites = new WeakHashMap<>();
    // Fish that got away, by the game time they may bite again
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    // Mo' Creatures' getIsTamed(), looked up once a class (tamed fish never bite)
    private final Map<Class<?>, Optional<Method>> tamedMethods = new HashMap<>();

    @SubscribeEvent
    public void onLevelTick(final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof final ServerLevel level)) {
            return;
        }
        final long now = level.getGameTime();
        if (now % UPDATE_INTERVAL != 0) {
            return;
        }
        this.cooldowns.values().removeIf(until -> until <= now);
        for (final ServerPlayer player : level.players()) {
            final FishingHook hook = player.fishing;
            if (hook == null || hook.isRemoved() || hook.level() != level) {
                continue;
            }
            final Entity caught = hook.getHookedIn();
            if (caught != null) {
                this.updateBite(level, hook, caught, now);
            } else {
                this.bites.remove(hook);
                this.attract(level, hook, now);
            }
        }
    }

    /**
     * Steers the nearest catchable fish to the bobber, and hooks it when it arrives.
     */
    private void attract(final ServerLevel level, final FishingHook hook, final long now) {
        final BlockPos target = waterBelow(level, hook);
        if (target == null) {
            return;
        }
        final AABB area = hook.getBoundingBox().inflate(ATTRACT_RADIUS);
        Mob nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (final Mob fish : level.getEntitiesOfClass(Mob.class, area, this::canBite)) {
            if (this.cooldowns.containsKey(fish.getUUID())) {
                continue;
            }
            final double distance = fish.distanceToSqr(hook);
            if (distance < nearestDistance) {
                nearest = fish;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            return;
        }
        if (nearestDistance <= BITE_DISTANCE * BITE_DISTANCE) {
            ((FishingHookInvoker) hook).almura$setHookedEntity(nearest);
            this.bites.put(hook, now);
            tug(level, hook, true);
        } else {
            nearest.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, SWIM_SPEED);
        }
    }

    /**
     * Keeps a hooked fish under the bobber tugging it down, until it's reeled in or gets away.
     */
    private void updateBite(final ServerLevel level, final FishingHook hook, final Entity caught, final long now) {
        final Long bitAt = this.bites.get(hook);
        if (bitAt == null) {
            // Hooked some other way (the bobber thrown at it): left alone
            return;
        }
        if (caught.isRemoved() || !(caught instanceof final Mob fish) || fish.isDeadOrDying()) {
            this.bites.remove(hook);
            ((FishingHookInvoker) hook).almura$setHookedEntity(null);
            return;
        }
        if (now - bitAt >= ESCAPE_TICKS) {
            // Got away
            this.bites.remove(hook);
            ((FishingHookInvoker) hook).almura$setHookedEntity(null);
            this.cooldowns.put(fish.getUUID(), now + ESCAPE_COOLDOWN_TICKS);
            fish.getNavigation().stop();
            return;
        }
        final BlockPos target = waterBelow(level, hook);
        if (target != null) {
            fish.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, SWIM_SPEED);
        }
        // Tugs at random rather than on a beat
        if (level.getRandom().nextBoolean()) {
            tug(level, hook, false);
        }
    }

    /**
     * Pulls the bobber under with a splash, as a vanilla bite does.
     */
    private static void tug(final ServerLevel level, final FishingHook hook, final boolean bite) {
        final RandomSource random = level.getRandom();
        hook.setDeltaMovement(hook.getDeltaMovement().add(0.0D, -(bite ? 0.4D : 0.2D + random.nextDouble() * 0.15D), 0.0D));
        hook.needsSync = true;
        level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL,
            bite ? 0.35F : 0.2F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
        final double y = Mth.floor(hook.getBoundingBox().minY) + 1.0D;
        final int count = bite ? 12 : 5;
        level.sendParticles(ParticleTypes.BUBBLE, hook.getX(), y, hook.getZ(), count, 0.15D, 0.0D, 0.15D, 0.2D);
        level.sendParticles(ParticleTypes.FISHING, hook.getX(), y, hook.getZ(), count, 0.15D, 0.0D, 0.15D, 0.2D);
        if (bite) {
            level.sendParticles(ParticleTypes.SPLASH, hook.getX(), y, hook.getZ(), 10, 0.2D, 0.0D, 0.2D, 0.0D);
        }
    }

    /**
     * The water block the bobber floats on (the bobber itself usually sits in the air block above, which fish can't path into).
     */
    private static @Nullable BlockPos waterBelow(final ServerLevel level, final FishingHook hook) {
        BlockPos pos = hook.blockPosition();
        for (int i = 0; i < 3; i++) {
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                return pos;
            }
            pos = pos.below();
        }
        return null;
    }

    /**
     * Runs before the rod reels the bobber in, so a fish on it is landed instead of dragged.
     */
    @SubscribeEvent
    public void onRightClickItem(final PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof final ServerPlayer player) || !(event.getItemStack().getItem() instanceof FishingRodItem)
            || !(player.level() instanceof final ServerLevel level)) {
            return;
        }
        final FishingHook hook = player.fishing;
        if (hook == null || !(hook.getHookedIn() instanceof final LivingEntity fish) || fish.isDeadOrDying() || !catchable(fish)) {
            return;
        }
        this.landing.put(fish, player.getUUID());
        // Dies to the player, so it drops (through MocDrops) exactly what killing it would, and protection gets its say
        fish.hurtServer(level, fish.damageSources().playerAttack(player), Float.MAX_VALUE);
        if (!fish.isDeadOrDying()) {
            // The kill was prevented (a claim, say): reeling in drags it, as vanilla does
            this.landing.remove(fish);
            return;
        }
        // Landed: the rod reels in an empty bobber
        ((FishingHookInvoker) hook).almura$setHookedEntity(null);
        this.bites.remove(hook);
        player.awardStat(Stats.FISH_CAUGHT);
        // A catch wears the rod, as a vanilla catch does
        final ItemStack rod = event.getItemStack();
        rod.hurtAndBreak(1, player, event.getHand());
    }

    /**
     * After {@link MocDrops} has made the drops Almura's, they're thrown from where the fish was to the player who caught it.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLandedFishDrops(final LivingDropsEvent event) {
        final UUID catcher = this.landing.get(event.getEntity());
        if (catcher == null || event.isCanceled()) {
            return;
        }
        // The experience came first (26.3 drops it with the loot); done with this fish
        this.landing.remove(event.getEntity());
        final Player player = event.getEntity().level().getPlayerByUUID(catcher);
        if (player != null) {
            for (final ItemEntity item : event.getDrops()) {
                throwTowards(item, player);
            }
        }
    }

    /**
     * The experience is thrown to the player with the drops, rather than left where the fish died.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLandedFishExperience(final LivingExperienceDropEvent event) {
        final UUID catcher = this.landing.get(event.getEntity());
        if (catcher == null || event.isCanceled() || !(event.getEntity().level() instanceof final ServerLevel level)) {
            return;
        }
        final Player player = level.getPlayerByUUID(catcher);
        int experience = event.getDroppedExperience();
        if (player == null || experience <= 0) {
            return;
        }
        event.setCanceled(true);
        final Entity fish = event.getEntity();
        while (experience > 0) {
            final int value = ExperienceOrb.getExperienceValue(experience);
            experience -= value;
            final ExperienceOrb orb = new ExperienceOrb(level, fish.getX(), fish.getY(), fish.getZ(), value);
            throwTowards(orb, player);
            level.addFreshEntity(orb);
        }
    }

    /**
     * The throw vanilla gives fishing loot: an arc from where it is to the player.
     */
    private static void throwTowards(final Entity entity, final Player player) {
        final double dx = player.getX() - entity.getX();
        final double dy = player.getY() - entity.getY();
        final double dz = player.getZ() - entity.getZ();
        final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        entity.setDeltaMovement(new Vec3(dx * 0.1D, dy * 0.1D + Math.sqrt(distance) * 0.08D, dz * 0.1D));
    }

    private boolean canBite(final @Nullable Mob entity) {
        return entity != null && !entity.isRemoved() && !entity.isDeadOrDying() && entity.isInWater() && catchable(entity)
            && !this.tamed(entity);
    }

    /**
     * Mo' Creatures never lets tamed fish bite (pets in an aquarium); its getIsTamed() is called by reflection, so there's no dependency.
     */
    private boolean tamed(final Entity entity) {
        final Optional<Method> method = this.tamedMethods.computeIfAbsent(entity.getClass(), type -> {
            try {
                return Optional.of(type.getMethod("getIsTamed"));
            } catch (final NoSuchMethodException e) {
                return Optional.empty();
            }
        });
        if (method.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(method.get().invoke(entity));
        } catch (final ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static boolean catchable(final Entity entity) {
        final Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return MocDrops.MOD_ID.equals(id.getNamespace()) && CATCHABLE.contains(id.getPath());
    }
}
