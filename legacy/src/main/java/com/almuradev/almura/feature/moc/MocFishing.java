/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.moc;

import com.almuradev.core.event.Witness;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Lets players catch Mo' Creatures fish with a fishing rod.
 *
 * <ol>
 *     <li>A catchable Mo' Creatures fish near a bobber resting in water swims to it.</li>
 *     <li>When it reaches the bobber it bites: it becomes the hook's caught entity and keeps tugging the bobber under, with splashes and
 *     bubbles.</li>
 *     <li>Right clicking while it's on lands it: the fish dies to the player and its drops (mapped to Almura items by {@link MocDrops}, e.g.
 *     cod -> cod_raw) are thrown from the bobber to the player like vanilla fishing loot.</li>
 *     <li>Waiting too long lets it get away; it then won't bite again for a few seconds.</li>
 * </ol>
 *
 * <p>Mo' Creatures has its own version of the first two steps, but its fish rarely reach the bobber (it paths to the bobber's position,
 * which floats in the air block above the water). Only installed when Mo' Creatures is loaded, so it's optional, and it has no compile
 * dependency on it: fish are identified by registry id.</p>
 */
public final class MocFishing implements Witness {

    private static final String MOD_ID = "mocreatures";
    // The fish Mo' Creatures lets bite a hook (its small and medium fish)
    private static final Set<String> CATCHABLE = ImmutableSet.of(
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

    // Fish being landed -> the player who caught them, so their drops can be thrown to that player. Weak keys: entries vanish with the fish.
    private final Map<Entity, UUID> landing = Collections.synchronizedMap(new WeakHashMap<>());
    // Same, for the experience orbs, which drop later (at the end of the death animation). Only used if they couldn't be thrown at catch time.
    private final Map<Entity, UUID> experienceLanding = Collections.synchronizedMap(new WeakHashMap<>());
    // Landed fish whose experience was already thrown at catch time, so the later drop is cancelled
    private final Map<Entity, Boolean> experienceSpawned = Collections.synchronizedMap(new WeakHashMap<>());
    @Nullable private static Method experiencePoints;
    private static boolean experiencePointsLookedUp;
    // Bobbers with a fish on them -> the world time it bit
    private final Map<EntityFishHook, Long> bites = new WeakHashMap<>();
    // Fish that got away -> world time they may bite again
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    // Mo' Creatures' getIsTamed(), looked up once per class (tamed fish never bite)
    private final Map<Class<?>, Optional<Method>> tamedMethods = new HashMap<>();
    private final Logger logger;

    @Inject
    public MocFishing(final Logger logger) {
        this.logger = logger;
        this.logger.info("[MocFishing] Mo' Creatures fishing enabled");
    }

    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || !(event.world instanceof WorldServer)) {
            return;
        }

        final WorldServer world = (WorldServer) event.world;
        final long now = world.getTotalWorldTime();
        if (now % UPDATE_INTERVAL != 0) {
            return;
        }

        this.cooldowns.values().removeIf(until -> until <= now);

        for (final EntityPlayer player : world.playerEntities) {
            final EntityFishHook hook = player.fishEntity;
            if (hook == null || hook.isDead || hook.world != world) {
                continue;
            }

            final Entity caught = hook.caughtEntity;
            if (caught != null) {
                this.updateBite(world, hook, caught, now);
            } else {
                // Not hook.isInWater(): a floating bobber's box sits mostly above the water, so that's often false. attract() checks for water
                // under the bobber instead, like vanilla fishing does.
                this.bites.remove(hook);
                this.attract(world, hook, now);
            }
        }
    }

    /**
     * Steers the nearest catchable fish to the bobber, and hooks it when it arrives.
     */
    private void attract(final WorldServer world, final EntityFishHook hook, final long now) {
        final BlockPos target = waterBelow(world, hook);
        if (target == null) {
            return;
        }

        final AxisAlignedBB area = hook.getEntityBoundingBox().grow(ATTRACT_RADIUS);
        EntityLiving nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (final EntityLiving fish : world.getEntitiesWithinAABB(EntityLiving.class, area, this::canBite)) {
            if (this.cooldowns.containsKey(fish.getUniqueID())) {
                continue;
            }
            final double distance = fish.getDistanceSq(hook);
            if (distance < nearestDistance) {
                nearest = fish;
                nearestDistance = distance;
            }
        }

        if (nearest == null) {
            return;
        }

        if (nearestDistance <= BITE_DISTANCE * BITE_DISTANCE) {
            hook.caughtEntity = nearest;
            this.bites.put(hook, now);
            tug(world, hook, true);
        } else {
            nearest.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, SWIM_SPEED);
        }
    }

    /**
     * Keeps a hooked fish under the bobber tugging it down, until it's reeled in or gets away.
     */
    private void updateBite(final WorldServer world, final EntityFishHook hook, final Entity caught, final long now) {
        final Long bitAt = this.bites.get(hook);
        if (bitAt == null) {
            // Hooked by something else (e.g. Mo' Creatures' own logic): leave it alone
            return;
        }

        if (caught.isDead || !(caught instanceof EntityLiving)) {
            this.bites.remove(hook);
            hook.caughtEntity = null;
            return;
        }

        final EntityLiving fish = (EntityLiving) caught;
        if (now - bitAt >= ESCAPE_TICKS) {
            // Got away
            this.bites.remove(hook);
            hook.caughtEntity = null;
            this.cooldowns.put(fish.getUniqueID(), now + ESCAPE_COOLDOWN_TICKS);
            fish.getNavigator().clearPath();
            return;
        }

        final BlockPos target = waterBelow(world, hook);
        if (target != null) {
            fish.getNavigator().tryMoveToXYZ(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, SWIM_SPEED);
        }
        // Tug at random rather than on a fixed beat
        if (world.rand.nextBoolean()) {
            tug(world, hook, false);
        }
    }

    /**
     * Pulls the bobber under with a splash, the same way a vanilla bite does.
     */
    private static void tug(final WorldServer world, final EntityFishHook hook, final boolean bite) {
        hook.motionY -= bite ? 0.4D : 0.2D + world.rand.nextDouble() * 0.15D;
        hook.velocityChanged = true;

        world.playSound(null, hook.posX, hook.posY, hook.posZ, SoundEvents.ENTITY_BOBBER_SPLASH, SoundCategory.NEUTRAL,
                bite ? 0.35F : 0.2F, 1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
        final double y = MathHelper.floor(hook.getEntityBoundingBox().minY) + 1.0D;
        world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, hook.posX, y, hook.posZ, bite ? 12 : 5, 0.15D, 0.0D, 0.15D, 0.2D);
        world.spawnParticle(EnumParticleTypes.WATER_WAKE, hook.posX, y, hook.posZ, bite ? 12 : 5, 0.15D, 0.0D, 0.15D, 0.2D);
        if (bite) {
            world.spawnParticle(EnumParticleTypes.WATER_SPLASH, hook.posX, y, hook.posZ, 10, 0.2D, 0.0D, 0.2D, 0.0D);
        }
    }

    /**
     * The water block the bobber floats on (the bobber itself usually sits in the air block above, which fish can't path into).
     */
    @Nullable
    private static BlockPos waterBelow(final World world, final EntityFishHook hook) {
        BlockPos pos = new BlockPos(hook);
        for (int i = 0; i < 3; i++) {
            if (world.getBlockState(pos).getMaterial() == Material.WATER) {
                return pos;
            }
            pos = pos.down();
        }
        return null;
    }

    /**
     * Runs before the fishing rod retracts the hook, so the fish can be landed instead of dragged.
     */
    @SubscribeEvent
    public void onRightClickItem(final PlayerInteractEvent.RightClickItem event) {
        final EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote || !(event.getItemStack().getItem() instanceof ItemFishingRod)) {
            return;
        }

        final EntityFishHook hook = player.fishEntity;
        if (hook == null || !(hook.caughtEntity instanceof EntityLivingBase)) {
            return;
        }

        final EntityLivingBase fish = (EntityLivingBase) hook.caughtEntity;
        if (fish.isDead || !isCatchable(fish)) {
            return;
        }

        this.landing.put(fish, player.getUniqueID());
        this.experienceLanding.put(fish, player.getUniqueID());
        // Dies to the player, so it drops (through MocDrops) exactly what killing it would, and protection plugins get their say
        fish.attackEntityFrom(DamageSource.causePlayerDamage(player), Float.MAX_VALUE);

        if (fish.isDead || fish.getHealth() <= 0) {
            // Throw the experience along with the drops instead of a second later at the end of the death animation
            // (Not marked for the fallback while firing Forge's experience event ourselves, so our own handler leaves that event alone)
            this.experienceLanding.remove(fish);
            if (this.spawnExperienceNow(fish, player)) {
                this.experienceSpawned.put(fish, Boolean.TRUE);
            } else {
                this.experienceLanding.put(fish, player.getUniqueID());
            }
            // Landed: let the rod retract normally instead of dragging the fish
            hook.caughtEntity = null;
            this.bites.remove(hook);
            player.addStat(StatList.FISH_CAUGHT);
            // A catch wears the rod like a vanilla catch does
            event.getItemStack().damageItem(1, player);
        } else {
            // The kill was prevented (e.g. a claim), leave it to Mo' Creatures' normal behaviour
            this.landing.remove(fish);
            this.experienceLanding.remove(fish);
        }
    }

    /**
     * After {@link MocDrops} has mapped the drops, throw them from where the fish was to the player who caught it.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLandedFishDrops(final LivingDropsEvent event) {
        final UUID catcher = this.landing.remove(event.getEntity());
        if (catcher == null || event.isCanceled()) {
            return;
        }

        final EntityPlayer player = event.getEntity().world.getPlayerEntityByUUID(catcher);
        if (player == null) {
            return;
        }

        for (final EntityItem item : event.getDrops()) {
            throwTowards(item, player);
        }
    }

    /**
     * The experience orbs come about a second later, at the end of the fish's death animation. Spawn them with the same throw as the drops
     * instead of letting them drop where the fish was.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLandedFishExperience(final LivingExperienceDropEvent event) {
        if (this.experienceSpawned.remove(event.getEntity()) != null) {
            // Already thrown when the fish was caught
            event.setCanceled(true);
            return;
        }

        // Fallback when the experience couldn't be worked out at catch time: throw it now instead
        final UUID catcher = this.experienceLanding.remove(event.getEntity());
        if (catcher == null || event.isCanceled()) {
            return;
        }

        final Entity fish = event.getEntity();
        final EntityPlayer player = fish.world.getPlayerEntityByUUID(catcher);
        int experience = event.getDroppedExperience();
        if (player == null || experience <= 0) {
            return;
        }

        event.setCanceled(true);
        spawnExperience(fish, player, experience);
    }

    /**
     * Spawns the experience the fish would drop at the end of its death animation right away, thrown to the player. Mirrors
     * EntityLivingBase#onDeathUpdate: the creature's experience value, passed through Forge's experience drop event so other mods can still
     * change it, and only when mobs drop loot.
     *
     * @return false if the experience value couldn't be read, so it should be left to the normal (later) drop
     */
    private boolean spawnExperienceNow(final EntityLivingBase fish, final EntityPlayer player) {
        final Method method = experiencePointsMethod();
        if (method == null) {
            return false;
        }

        final int base;
        try {
            base = (Integer) method.invoke(fish, player);
        } catch (final ReflectiveOperationException | RuntimeException e) {
            return false;
        }

        if (fish.world.getGameRules().getBoolean("doMobLoot")) {
            final int experience = ForgeEventFactory.getExperienceDrop(fish, player, base);
            spawnExperience(fish, player, experience);
        }
        return true;
    }

    private static void spawnExperience(final Entity fish, final EntityPlayer player, int experience) {
        while (experience > 0) {
            final int value = EntityXPOrb.getXPSplit(experience);
            experience -= value;
            final EntityXPOrb orb = new EntityXPOrb(fish.world, fish.posX, fish.posY, fish.posZ, value);
            throwTowards(orb, player);
            fish.world.spawnEntity(orb);
        }
    }

    /**
     * EntityLivingBase#getExperiencePoints(EntityPlayer), which is protected. Found by signature (the only declared int method taking just a
     * player) so it works with both dev and obfuscated names; invoking it still dispatches to Mo' Creatures' overrides.
     */
    @Nullable
    private static Method experiencePointsMethod() {
        if (!experiencePointsLookedUp) {
            experiencePointsLookedUp = true;
            Method found = null;
            for (final Method candidate : EntityLivingBase.class.getDeclaredMethods()) {
                if (candidate.getReturnType() == int.class && candidate.getParameterCount() == 1
                        && candidate.getParameterTypes()[0] == EntityPlayer.class) {
                    if (found != null) {
                        // Ambiguous: don't guess
                        found = null;
                        break;
                    }
                    found = candidate;
                }
            }
            if (found != null) {
                found.setAccessible(true);
            }
            experiencePoints = found;
        }
        return experiencePoints;
    }

    /**
     * Same throw as vanilla fishing loot (EntityFishHook#handleHookRetraction): an arc from where it is to the player.
     */
    private static void throwTowards(final Entity entity, final EntityPlayer player) {
        final double dx = player.posX - entity.posX;
        final double dy = player.posY - entity.posY;
        final double dz = player.posZ - entity.posZ;
        final double distance = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
        entity.motionX = dx * 0.1D;
        entity.motionY = dy * 0.1D + MathHelper.sqrt(distance) * 0.08D;
        entity.motionZ = dz * 0.1D;
    }

    private boolean canBite(@Nullable final EntityLiving entity) {
        return entity != null && !entity.isDead && entity.isInWater() && isCatchable(entity) && !this.isTamed(entity);
    }

    /**
     * Mo' Creatures never lets tamed fish bite (e.g. pets in an aquarium); its getIsTamed() is called reflectively to avoid a compile
     * dependency.
     */
    private boolean isTamed(final Entity entity) {
        final Optional<Method> method = this.tamedMethods.computeIfAbsent(entity.getClass(), type -> {
            try {
                return Optional.of(type.getMethod("getIsTamed"));
            } catch (final NoSuchMethodException e) {
                return Optional.empty();
            }
        });

        if (!method.isPresent()) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(method.get().invoke(entity));
        } catch (final ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static boolean isCatchable(final Entity entity) {
        final ResourceLocation id = EntityList.getKey(entity);
        return id != null && MOD_ID.equals(id.getNamespace()) && CATCHABLE.contains(id.getPath());
    }
}
