/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor.client;

import com.almuradev.almura.feature.armor.LightedHelmetItem;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Client side "dynamic light" for players wearing a {@link LightedHelmetItem}, done the way OptiFine's dynamic lights are: the light is
 * added when the game reads light for drawing, not fed through the world's light engine.
 *
 * <p>Every rendered frame this works out the light sources: for every player wearing the helmet, a bright spot where they look and a dimmer glow at
 * their head. {@link #lightAt} gives a position's helmet light from its distance to those sources; mixins on the client's light reads
 * ({@code MixinChunkCache_DynamicLight} for block rendering, {@code MixinWorld_DynamicLight} for entities, tile entities and particles) use
 * the brighter of that and the real light. When the sources move, every chunk section they reach is re-rendered together. Nothing is written
 * to the world, so the server's light levels (mob spawning, crops, ...) never change.</p>
 *
 * <p>Distance is counted in whole blocks along each axis (like vanilla light falls off by one level per block), but it isn't blocked by
 * walls the way real light spreads, the same as OptiFine's dynamic lights.</p>
 */
@SideOnly(Side.CLIENT)
public final class ClientDynamicLightManager implements Witness {

    private static final Source[] NO_SOURCES = new Source[0];

    // Read from the chunk builder threads while sections are rendered: replaced, never modified
    private static volatile Source[] sources = NO_SOURCES;
    @Nullable private static WorldClient sourcesWorld;

    /**
     * @return the helmet light at {@code pos}, 0 if none
     */
    public static int lightAt(final BlockPos pos) {
        final Source[] current = sources;
        if (current.length == 0) {
            return 0;
        }

        int light = 0;
        for (final Source source : current) {
            final int distance = Math.abs(pos.getX() - source.x) + Math.abs(pos.getY() - source.y) + Math.abs(pos.getZ() - source.z);
            light = Math.max(light, source.level - distance);
        }
        return light;
    }

    /**
     * Applies the helmet light to a packed lightmap value (sky light in bits 20+, block light in bits 4-7), keeping the brighter block light.
     */
    public static int withDynamicLight(final int packed, final BlockPos pos) {
        final int dynamic = lightAt(pos);
        if (dynamic <= 0) {
            return packed;
        }
        final int block = (packed >> 4) & 15;
        return dynamic > block ? (packed & ~(15 << 4)) | (dynamic << 4) : packed;
    }

    /**
     * Every rendered frame (not every 50ms tick), so the spot follows the view as closely as possible. Runs before the frame is drawn, so a
     * moved light is used for this frame's entities and its sections are queued straight away.
     */
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final WorldClient world = mc.world;
        if (world != sourcesWorld) {
            // Joined, left or changed world: the old sources belonged to a world that's gone
            sources = NO_SOURCES;
            sourcesWorld = world;
        }
        if (world == null) {
            return;
        }

        // Position -> brightest light placed there (rays often land on the same block)
        final Map<BlockPos, Integer> next = new LinkedHashMap<>();
        final float spread = LightedHelmetItem.BEAM_SPREAD_DEGREES;
        for (final EntityPlayer player : world.playerEntities) {
            if (!player.isDead && LightedHelmetItem.isWearing(player.getItemStackFromSlot(EntityEquipmentSlot.HEAD))) {
                final Vec3d eyes = player.getPositionEyes(event.renderTickTime);
                final float[] view = viewOf(mc, player, event.renderTickTime);
                final float pitch = view[0];
                final float yaw = view[1];

                // A beam rather than a laser dot: the centre ray where they look, and four rays fanned out up, down, left and right
                next.merge(spotFor(world, eyes, direction(pitch, yaw)), LightedHelmetItem.SPOT_LIGHT_LEVEL, Math::max);
                next.merge(spotFor(world, eyes, direction(pitch - spread, yaw)), LightedHelmetItem.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spotFor(world, eyes, direction(pitch + spread, yaw)), LightedHelmetItem.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spotFor(world, eyes, direction(pitch, yaw - spread)), LightedHelmetItem.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spotFor(world, eyes, direction(pitch, yaw + spread)), LightedHelmetItem.BEAM_EDGE_LIGHT_LEVEL, Math::max);

                next.merge(new BlockPos(eyes), LightedHelmetItem.WEARER_LIGHT_LEVEL, Math::max);
            }
        }

        final Source[] previous = sources;
        final Source[] updated = next.isEmpty() ? NO_SOURCES
                : next.entrySet().stream().map(entry -> new Source(entry.getKey(), entry.getValue())).toArray(Source[]::new);
        if (Arrays.equals(previous, updated)) {
            return;
        }
        sources = updated;

        // Re-render everything the old and new lights reach, all through the same (queued) path so neighbouring sections don't update a frame
        // apart and show their edges
        for (final Source source : symmetricDifference(previous, updated)) {
            final int reach = source.level;
            world.markBlockRangeForRenderUpdate(source.x - reach, source.y - reach, source.z - reach, source.x + reach, source.y + reach,
                    source.z + reach);
        }
    }

    /**
     * The sources that appeared, disappeared or moved: in one array but not the other.
     */
    private static Set<Source> symmetricDifference(final Source[] a, final Source[] b) {
        final Set<Source> onlyA = new HashSet<>(Arrays.asList(a));
        final Set<Source> onlyB = new HashSet<>(Arrays.asList(b));
        onlyA.removeAll(Arrays.asList(b));
        onlyB.removeAll(Arrays.asList(a));
        onlyA.addAll(onlyB);
        return onlyA;
    }

    /**
     * Where a wearer's spotlight lands: the open block just in front of the first surface along their look direction (within range), or the
     * point at the end of the range when they're looking into open space.
     */
    private static BlockPos spotFor(final WorldClient world, final Vec3d eyes, final Vec3d look) {
        final Vec3d end = eyes.add(look.scale(LightedHelmetItem.SPOT_RANGE));
        final RayTraceResult hit = world.rayTraceBlocks(eyes, end, false, true, false);
        if (hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK) {
            return hit.getBlockPos().offset(hit.sideHit);
        }
        return new BlockPos(end);
    }

    /**
     * The pitch and yaw a player is looking at this frame. For the local player that's the camera's rotation, which the mouse turns every
     * frame (the head's rotation only catches up once a tick). For other players it's their synced head rotation, smoothed between ticks.
     */
    private static float[] viewOf(final Minecraft mc, final EntityPlayer player, final float partialTicks) {
        if (player == mc.player) {
            return new float[] {player.rotationPitch, player.rotationYaw};
        }
        return new float[] {
                player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks,
                player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * partialTicks
        };
    }

    /**
     * The unit vector for a pitch and yaw (same maths as Entity#getVectorForRotation, which is protected).
     */
    private static Vec3d direction(final float pitch, final float yaw) {
        final float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        final float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        final float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
        final float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    private static final class Source {

        final int x;
        final int y;
        final int z;
        final int level;

        Source(final BlockPos pos, final int level) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.level = level;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Source)) {
                return false;
            }
            final Source that = (Source) other;
            return this.x == that.x && this.y == that.y && this.z == that.z && this.level == that.level;
        }

        @Override
        public int hashCode() {
            return ((this.x * 31 + this.y) * 31 + this.z) * 31 + this.level;
        }
    }
}
