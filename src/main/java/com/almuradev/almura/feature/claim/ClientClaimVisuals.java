/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.claim;

import com.almuradev.almura.feature.claim.network.ClientboundClaimVisualPacket;
import com.almuradev.almura.shared.client.render.OutlineRenderer;
import com.almuradev.core.event.Witness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Draws the outline of the claim the server sent, as a grid on its walls (and on its top and bottom for cuboid claims).
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientClaimVisuals implements Witness {

    // Blocks between grid lines.
    private static final int GRID = 4;

    private List<ClientboundClaimVisualPacket.Box> boxes = Collections.emptyList();
    @Nullable private World world;

    public boolean isShowing() {
        return !this.boxes.isEmpty();
    }

    public void show(final List<ClientboundClaimVisualPacket.Box> boxes) {
        this.boxes = new ArrayList<>(boxes);
        this.world = Minecraft.getMinecraft().world;
    }

    public void clear() {
        this.boxes = Collections.emptyList();
        this.world = null;
    }

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event) {
        if (this.boxes.isEmpty()) {
            return;
        }
        final World world = Minecraft.getMinecraft().world;
        // The outline belongs to the world it was sent in; drop it after a world change or disconnect.
        if (world == null || world != this.world) {
            this.clear();
            return;
        }
        final int height = world.getHeight();
        OutlineRenderer.render(event.getPartialTicks(), () -> {
            final BufferBuilder buffer = OutlineRenderer.beginFill();
            for (final ClientboundClaimVisualPacket.Box box : this.boxes) {
                OutlineRenderer.fillBox(buffer, box.minX, minY(box), box.minZ, box.maxX + 1, maxY(box, height), box.maxZ + 1, box.cuboid,
                    color(box.type), OutlineRenderer.FILL_ALPHA);
            }
            OutlineRenderer.end();
        }, alpha -> {
            BufferBuilder buffer = OutlineRenderer.begin(1.5f);
            for (final ClientboundClaimVisualPacket.Box box : this.boxes) {
                OutlineRenderer.gridBox(buffer, box.minX, minY(box), box.minZ, box.maxX + 1, maxY(box, height), box.maxZ + 1, GRID, box.cuboid,
                    color(box.type), alpha);
            }
            OutlineRenderer.end();

            buffer = OutlineRenderer.begin(3.0f);
            for (final ClientboundClaimVisualPacket.Box box : this.boxes) {
                OutlineRenderer.edgeBox(buffer, box.minX, minY(box), box.minZ, box.maxX + 1, maxY(box, height), box.maxZ + 1, color(box.type), alpha);
            }
            OutlineRenderer.end();
        });
    }

    // Basic claims cover the full height of the world; only cuboid claims have their own floor and ceiling.
    private static int minY(final ClientboundClaimVisualPacket.Box box) {
        return box.cuboid ? box.minY : 0;
    }

    private static int maxY(final ClientboundClaimVisualPacket.Box box, final int worldHeight) {
        return box.cuboid ? box.maxY + 1 : worldHeight;
    }

    private static int color(final byte type) {
        switch (type) {
            case ClientboundClaimVisualPacket.ADMIN:
                return 0xFF3030;
            case ClientboundClaimVisualPacket.TOWN:
                return 0x30E030;
            case ClientboundClaimVisualPacket.SUBDIVISION:
                return 0xE0E0E0;
            case ClientboundClaimVisualPacket.BASIC:
            default:
                return 0xFFD700;
        }
    }
}
