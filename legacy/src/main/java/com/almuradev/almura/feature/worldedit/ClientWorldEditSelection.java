/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit;

import com.almuradev.almura.shared.client.render.OutlineRenderer;
import com.almuradev.core.event.Witness;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Shows the player's WorldEdit selection, replacing the WorldEditCUI mod. WorldEdit sends selection updates on the {@code WECUI} channel as
 * {@code type|arg|arg} text once the client announces itself with {@code v|<protocol>}. Cuboid and polygon selections are drawn; other shapes are
 * ignored.
 */
@SideOnly(Side.CLIENT)
@Singleton
public final class ClientWorldEditSelection implements Witness {

    private static final String CHANNEL = "WECUI";
    private static final int PROTOCOL = 4;
    // Grid lines are spread out on large selections so they don't turn into a solid wall.
    private static final int MAX_GRID_LINES = 64;

    private static final int CUBOID_COLOR = 0xFF8C1A;
    private static final int POINT1_COLOR = 0x33FF33;
    private static final int POINT2_COLOR = 0x33CCFF;
    private static final int POLYGON_COLOR = 0xD64DFF;

    private boolean hooked;
    @Nullable private EntityPlayerSP player;

    private String shape = "cuboid";
    // False while another plugin (GriefDefender) is describing one of its own keyed regions; only the WorldEdit selection is drawn.
    private boolean primary = true;
    @Nullable private int[] point1, point2;
    private final List<int[]> polygon = new ArrayList<>();
    private int minY, maxY;

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        final Minecraft mc = Minecraft.getMinecraft();
        if (!this.hooked) {
            this.hooked = true;
            this.hook();
        }
        // A new player entity means a new connection or world: start over and ask WorldEdit to describe the selection again.
        if (mc.player != this.player) {
            this.player = mc.player;
            this.reset("cuboid");
            if (this.player != null && mc.getConnection() != null) {
                mc.getConnection().sendPacket(new CPacketCustomPayload(CHANNEL, new PacketBuffer(Unpooled.copiedBuffer("v|" + PROTOCOL, StandardCharsets.UTF_8))));
            }
        }
    }

    /**
     * Listens on the {@code WECUI} channel. The Forge WorldEdit mod registers that channel itself when it is installed (and fails if it is
     * taken), so this runs once loading is done: it watches the existing channel when there is one, and only registers its own otherwise.
     */
    private void hook() {
        final FMLEmbeddedChannel existing = NetworkRegistry.INSTANCE.getChannel(CHANNEL, Side.CLIENT);
        if (existing != null) {
            existing.pipeline().addFirst("almura:worldedit_selection", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
                    if (msg instanceof FMLProxyPacket) {
                        ClientWorldEditSelection.this.receive((FMLProxyPacket) msg);
                    }
                    ctx.fireChannelRead(msg);
                }
            });
        } else {
            NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL).register(this);
        }
    }

    @SubscribeEvent
    public void onCustomPacket(final FMLNetworkEvent.ClientCustomPacketEvent event) {
        this.receive(event.getPacket());
    }

    private void receive(final FMLProxyPacket packet) {
        final String message = packet.payload().toString(StandardCharsets.UTF_8);
        Minecraft.getMinecraft().addScheduledTask(() -> this.handle(message));
    }

    private void handle(final String message) {
        final String[] parts = message.split("\\|");
        try {
            switch (parts[0]) {
                case "s":
                    // A third argument names another plugin's region rather than the player's selection.
                    this.primary = parts.length < 3;
                    if (this.primary) {
                        this.reset(parts[1]);
                    }
                    break;
                case "p":
                    if (this.primary) {
                        final int[] point = {Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4])};
                        if ("0".equals(parts[1])) {
                            this.point1 = point;
                        } else if ("1".equals(parts[1])) {
                            this.point2 = point;
                        }
                    }
                    break;
                case "p2":
                    if (this.primary) {
                        final int index = Integer.parseInt(parts[1]);
                        while (this.polygon.size() <= index) {
                            this.polygon.add(null);
                        }
                        this.polygon.set(index, new int[] {Integer.parseInt(parts[2]), Integer.parseInt(parts[3])});
                    }
                    break;
                case "mm":
                    if (this.primary) {
                        this.minY = Integer.parseInt(parts[1]);
                        this.maxY = Integer.parseInt(parts[2]);
                    }
                    break;
                default:
            }
        } catch (final ArrayIndexOutOfBoundsException | NumberFormatException ignored) {
            // A message this client doesn't understand; leave the selection as it was.
        }
    }

    private void reset(final String shape) {
        this.shape = shape;
        this.primary = true;
        this.point1 = null;
        this.point2 = null;
        this.polygon.clear();
        this.minY = 0;
        this.maxY = 0;
    }

    @SubscribeEvent
    public void onRenderWorldLast(final RenderWorldLastEvent event) {
        if ("cuboid".equals(this.shape) && (this.point1 != null || this.point2 != null)) {
            OutlineRenderer.render(event.getPartialTicks(), this::fillCuboid, this::drawCuboid);
        } else if ("polygon2d".equals(this.shape) && this.polygon.stream().anyMatch(p -> p != null)) {
            OutlineRenderer.render(event.getPartialTicks(), this::fillPolygon, this::drawPolygon);
        }
    }

    private void fillCuboid() {
        final int[] a = this.point1, b = this.point2;
        if (a == null || b == null) {
            return;
        }
        final BufferBuilder buffer = OutlineRenderer.beginFill();
        OutlineRenderer.fillBox(buffer, Math.min(a[0], b[0]), Math.min(a[1], b[1]), Math.min(a[2], b[2]), Math.max(a[0], b[0]) + 1,
            Math.max(a[1], b[1]) + 1, Math.max(a[2], b[2]) + 1, true, CUBOID_COLOR, OutlineRenderer.FILL_ALPHA);
        OutlineRenderer.end();
    }

    private void fillPolygon() {
        final BufferBuilder buffer = OutlineRenderer.beginFill();
        OutlineRenderer.fillPrism(buffer, this.polygonPoints(), this.minY, this.maxY + 1, POLYGON_COLOR, OutlineRenderer.FILL_ALPHA);
        OutlineRenderer.end();
    }

    private void drawCuboid(final float alpha) {
        final int[] a = this.point1, b = this.point2;
        if (a != null && b != null) {
            final int x0 = Math.min(a[0], b[0]), y0 = Math.min(a[1], b[1]), z0 = Math.min(a[2], b[2]);
            final int x1 = Math.max(a[0], b[0]) + 1, y1 = Math.max(a[1], b[1]) + 1, z1 = Math.max(a[2], b[2]) + 1;
            final int step = step(Math.max(x1 - x0, Math.max(y1 - y0, z1 - z0)));
            BufferBuilder buffer = OutlineRenderer.begin(1.5f);
            OutlineRenderer.gridBox(buffer, x0, y0, z0, x1, y1, z1, step, true, CUBOID_COLOR, alpha);
            OutlineRenderer.end();
            buffer = OutlineRenderer.begin(3.5f);
            OutlineRenderer.edgeBox(buffer, x0, y0, z0, x1, y1, z1, CUBOID_COLOR, alpha);
            OutlineRenderer.end();
        }
        final BufferBuilder buffer = OutlineRenderer.begin(3.5f);
        if (a != null) {
            OutlineRenderer.edgeBox(buffer, a[0], a[1], a[2], a[0] + 1, a[1] + 1, a[2] + 1, POINT1_COLOR, alpha);
        }
        if (b != null) {
            OutlineRenderer.edgeBox(buffer, b[0], b[1], b[2], b[0] + 1, b[1] + 1, b[2] + 1, POINT2_COLOR, alpha);
        }
        OutlineRenderer.end();
    }

    private List<int[]> polygonPoints() {
        final List<int[]> points = new ArrayList<>();
        for (final int[] point : this.polygon) {
            if (point != null) {
                points.add(point);
            }
        }
        return points;
    }

    private void drawPolygon(final float alpha) {
        final List<int[]> points = this.polygonPoints();
        final int y0 = this.minY, y1 = this.maxY + 1;
        BufferBuilder buffer = OutlineRenderer.begin(1.5f);
        OutlineRenderer.prism(buffer, points, y0, y1, step(y1 - y0), false, POLYGON_COLOR, alpha);
        OutlineRenderer.end();
        buffer = OutlineRenderer.begin(3.5f);
        OutlineRenderer.prism(buffer, points, y0, y1, 1, true, POLYGON_COLOR, alpha);
        for (final int[] point : points) {
            OutlineRenderer.edgeBox(buffer, point[0], y0, point[1], point[0] + 1, y0 + 1, point[1] + 1, POINT1_COLOR, alpha);
        }
        OutlineRenderer.end();
    }

    private static int step(final int size) {
        return Math.max(1, (size + MAX_GRID_LINES - 1) / MAX_GRID_LINES);
    }
}
