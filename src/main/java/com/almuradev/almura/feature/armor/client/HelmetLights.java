/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.armor.client;

import com.almuradev.almura.feature.armor.LightedHelmet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The lighted helmet's light, on the client, as 3.5's {@code ClientDynamicLightManager} (done the way OptiFine's dynamic lights are): the
 * light is added when the game reads light to draw, not fed through the world's light engine.
 * <p>
 * Every frame this works out the light sources: for every player wearing the helmet, a bright spot where they look (a beam of five rays) and
 * a dimmer glow at their head. {@link #withHelmetLight} gives a position's light with the brighter of the helmet's and the real one; mixins
 * on the client's light reads use it ({@code LightCoordsUtilMixin} for blocks, block entities and the like as their sections are built and
 * drawn, {@code EntityRendererMixin} for entities). When the sources move, every section they reach is rebuilt. Nothing is written to the
 * world, so the server's light levels never change.
 * <p>
 * Distance is counted in whole blocks along each axis (as light falls off a level a block), but isn't blocked by walls the way real light
 * spreads, the same as 3.5's and OptiFine's. Client only.
 */
public final class HelmetLights {

    private static final Source[] NO_SOURCES = new Source[0];

    // Read from the section building threads while sections are built: replaced, never changed
    private static volatile Source[] sources = NO_SOURCES;
    private static @Nullable ClientLevel sourcesLevel;

    private record Source(int x, int y, int z, int level) {
    }

    /**
     * @return the helmet light at the position, 0 if none
     */
    public static int lightAt(final BlockPos pos) {
        final Source[] current = sources;
        int light = 0;
        for (final Source source : current) {
            final int distance = Math.abs(pos.getX() - source.x) + Math.abs(pos.getY() - source.y) + Math.abs(pos.getZ() - source.z);
            light = Math.max(light, source.level - distance);
        }
        return light;
    }

    /**
     * Packed light coordinates with the helmet's light, keeping the brighter block light.
     */
    public static int withHelmetLight(final int packed, final BlockPos pos) {
        if (sources.length == 0) {
            return packed;
        }
        final int helmet = lightAt(pos);
        return helmet > LightCoordsUtil.block(packed) ? LightCoordsUtil.withBlock(packed, helmet) : packed;
    }

    /**
     * Every frame (not every tick), so the spot follows the view as closely as it can; before the frame is drawn, so a moved light is used
     * for this frame's entities and its sections are queued straight away.
     */
    @SubscribeEvent
    public void onFrame(final RenderFrameEvent.Pre event) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        if (level != sourcesLevel) {
            // Joined, left or changed worlds: the old sources belonged to a world that's gone
            sources = NO_SOURCES;
            sourcesLevel = level;
        }
        if (level == null) {
            return;
        }
        final float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        // Position -> brightest light placed there (rays often land on the same block)
        final Map<BlockPos, Integer> next = new LinkedHashMap<>();
        final float spread = LightedHelmet.BEAM_SPREAD_DEGREES;
        for (final Player player : level.players()) {
            if (player.isAlive() && LightedHelmet.isWearing(player.getItemBySlot(EquipmentSlot.HEAD))) {
                final Vec3 eyes = player.getEyePosition(partialTick);
                // The local player's view is the camera's, which the mouse turns every frame; another's is their head, smoothed between ticks
                final float pitch = player == minecraft.player ? player.getXRot() : player.getViewXRot(partialTick);
                final float yaw = player == minecraft.player ? player.getYRot() : player.getViewYRot(partialTick);

                // A beam rather than a dot: the centre ray where they look, and four rays fanned out up, down, left and right
                next.merge(spot(level, player, eyes, Vec3.directionFromRotation(pitch, yaw)), LightedHelmet.SPOT_LIGHT_LEVEL, Math::max);
                next.merge(spot(level, player, eyes, Vec3.directionFromRotation(pitch - spread, yaw)), LightedHelmet.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spot(level, player, eyes, Vec3.directionFromRotation(pitch + spread, yaw)), LightedHelmet.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spot(level, player, eyes, Vec3.directionFromRotation(pitch, yaw - spread)), LightedHelmet.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(spot(level, player, eyes, Vec3.directionFromRotation(pitch, yaw + spread)), LightedHelmet.BEAM_EDGE_LIGHT_LEVEL, Math::max);
                next.merge(BlockPos.containing(eyes), LightedHelmet.WEARER_LIGHT_LEVEL, Math::max);
            }
        }

        final Source[] previous = sources;
        final Source[] updated = next.isEmpty() ? NO_SOURCES : next.entrySet().stream()
            .map(entry -> new Source(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ(), entry.getValue()))
            .toArray(Source[]::new);
        if (Arrays.equals(previous, updated)) {
            return;
        }
        sources = updated;

        // Rebuild everything the old and new lights reach, all at once, so neighbouring sections don't change a frame apart
        for (final Source source : changed(previous, updated)) {
            final int reach = source.level;
            level.setSectionRangeDirty(SectionPos.blockToSectionCoord(source.x - reach), SectionPos.blockToSectionCoord(source.y - reach),
                SectionPos.blockToSectionCoord(source.z - reach), SectionPos.blockToSectionCoord(source.x + reach),
                SectionPos.blockToSectionCoord(source.y + reach), SectionPos.blockToSectionCoord(source.z + reach));
        }
    }

    /**
     * The sources that appeared, went or moved: in one but not the other.
     */
    private static Set<Source> changed(final Source[] a, final Source[] b) {
        final Set<Source> onlyA = new HashSet<>(Arrays.asList(a));
        final Set<Source> onlyB = new HashSet<>(Arrays.asList(b));
        onlyA.removeAll(Arrays.asList(b));
        onlyB.removeAll(Arrays.asList(a));
        onlyA.addAll(onlyB);
        return onlyA;
    }

    /**
     * Where a wearer's spotlight lands: the open block just in front of the first surface along the look (within range), or the point at
     * the end of the range when they're looking into open space.
     */
    private static BlockPos spot(final ClientLevel level, final Player player, final Vec3 eyes, final Vec3 look) {
        final Vec3 end = eyes.add(look.scale(LightedHelmet.SPOT_RANGE));
        final BlockHitResult hit = level.clip(new ClipContext(eyes, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos().relative(hit.getDirection());
        }
        return BlockPos.containing(end);
    }
}
