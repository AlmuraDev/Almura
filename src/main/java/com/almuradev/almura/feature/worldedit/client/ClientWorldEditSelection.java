/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.gizmos.Gizmos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the player's WorldEdit selection, as 3.5 did (replacing the WorldEditCUI mod). WorldEdit, on the server, describes the selection on
 * its {@code worldedit:cui} channel as {@code type|arg|arg} messages once the client announces itself with {@code v|4}: the shape
 * ({@code s|cuboid}), the points ({@code p|0|x|y|z|...}, {@code p2|i|x|z|...} for polygons) and a polygon's heights ({@code mm|min|max}).
 * Cuboid and polygon selections are drawn; other shapes are ignored.
 * <p>
 * WorldEdit doesn't need to be on the client. When it is, its WorldEditCUI protocol mod owns the channel and Almura listens through it
 * ({@link WorldEditCuiChannel}); when it isn't, Almura registers the channel itself ({@link AlmuraCuiChannel}). Without WorldEdit on the
 * server the channel never opens, nothing is sent and nothing is drawn. Client only.
 */
public final class ClientWorldEditSelection {

    /** The mod (inside WorldEdit's jar) that registers {@code worldedit:cui}. */
    static final String PROTOCOL_MOD = "worldeditcui_protocol";
    static final String HANDSHAKE = "v";
    static final String PROTOCOL = "4";

    // Grid lines are spread out on large selections so they don't turn into a solid wall
    private static final int MAX_GRID_LINES = 64;
    // 3.5's line widths, in pixels on a 1920 wide window
    private static final float GRID_WIDTH = 1.5f;
    private static final float EDGE_WIDTH = 3.5f;
    private static final float VANILLA_LINE_WIDTH = 2.5f;

    private static final int CUBOID_COLOR = 0xFF8C1A;
    private static final int POINT1_COLOR = 0x33FF33;
    private static final int POINT2_COLOR = 0x33CCFF;
    private static final int POLYGON_COLOR = 0xD64DFF;

    // Set during loading (client setup runs off the main thread), read by the client tick
    private volatile @Nullable CuiChannel channel;
    private @Nullable LocalPlayer player;

    private String shape = "cuboid";
    // False while another plugin (GriefDefender) is describing one of its own keyed regions; only the WorldEdit selection is drawn
    private boolean primary = true;
    private int @Nullable [] point1, point2;
    private final List<int @Nullable []> polygon = new ArrayList<>();
    private int minY, maxY;

    private ClientWorldEditSelection() {
    }

    public static void register(final IEventBus modBus) {
        final ClientWorldEditSelection selection = new ClientWorldEditSelection();
        if (ModList.get().isLoaded(PROTOCOL_MOD)) {
            modBus.addListener((final FMLClientSetupEvent event) -> selection.channel = WorldEditCuiChannel.hook(selection));
        } else {
            modBus.addListener((final RegisterPayloadHandlersEvent event) -> selection.channel = AlmuraCuiChannel.register(event, selection));
        }
        NeoForge.EVENT_BUS.register(selection);
    }

    /**
     * A new player entity means a new connection, world or life: start over and ask WorldEdit to describe the selection again.
     */
    @SubscribeEvent
    public void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == this.player) {
            return;
        }
        this.player = minecraft.player;
        this.reset("cuboid");
        final ClientPacketListener connection = minecraft.getConnection();
        final CuiChannel channel = this.channel;
        // Only servers with WorldEdit have the channel
        if (this.player != null && connection != null && channel != null && connection.hasChannel(channel.type())) {
            ClientPacketDistributor.sendToServer(channel.handshake());
        }
    }

    /**
     * A message from WorldEdit, handled on the main thread.
     */
    void receive(final boolean multi, final String type, final List<String> args) {
        Minecraft.getInstance().execute(() -> this.handle(multi, type, args));
    }

    private void handle(final boolean multi, final String type, final List<String> args) {
        // Messages about several regions at once ("+" before the type) are other plugins' regions, which 3.5 didn't draw either
        if (multi) {
            return;
        }
        try {
            switch (type) {
                case "s":
                    // A second argument names another plugin's region rather than the player's selection
                    this.primary = args.size() < 2;
                    if (this.primary) {
                        this.reset(args.get(0));
                    }
                    break;
                case "p":
                    if (this.primary) {
                        final int[] point = {Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)), Integer.parseInt(args.get(3))};
                        if ("0".equals(args.get(0))) {
                            this.point1 = point;
                        } else if ("1".equals(args.get(0))) {
                            this.point2 = point;
                        }
                    }
                    break;
                case "p2":
                    if (this.primary) {
                        final int index = Integer.parseInt(args.get(0));
                        while (this.polygon.size() <= index) {
                            this.polygon.add(null);
                        }
                        this.polygon.set(index, new int[] {Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2))});
                    }
                    break;
                case "mm":
                    if (this.primary) {
                        this.minY = Integer.parseInt(args.get(0));
                        this.maxY = Integer.parseInt(args.get(1));
                    }
                    break;
                default:
            }
        } catch (final IndexOutOfBoundsException | NumberFormatException ignored) {
            // A message this client doesn't understand; leave the selection as it was
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

    /**
     * Drawn as gizmos, which the level renderer collects right after this event and draws in the same frame.
     */
    @SubscribeEvent
    public void onSubmitCustomGeometry(final SubmitCustomGeometryEvent event) {
        final boolean cuboid = "cuboid".equals(this.shape) && (this.point1 != null || this.point2 != null);
        final boolean polygon = "polygon2d".equals(this.shape) && this.polygon.stream().anyMatch(point -> point != null);
        if (!cuboid && !polygon) {
            return;
        }
        final Minecraft minecraft = Minecraft.getInstance();
        // As vanilla's outlines do, lines widen with the window
        final float scale = minecraft.getWindow().getAppropriateLineWidth() / VANILLA_LINE_WIDTH;
        try (Gizmos.TemporaryCollection ignored = minecraft.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            if (cuboid) {
                this.drawCuboid(GRID_WIDTH * scale, EDGE_WIDTH * scale);
            } else {
                this.drawPolygon(GRID_WIDTH * scale, EDGE_WIDTH * scale);
            }
        }
    }

    private void drawCuboid(final float gridWidth, final float edgeWidth) {
        final int[] a = this.point1, b = this.point2;
        if (a != null && b != null) {
            final int x0 = Math.min(a[0], b[0]), y0 = Math.min(a[1], b[1]), z0 = Math.min(a[2], b[2]);
            final int x1 = Math.max(a[0], b[0]) + 1, y1 = Math.max(a[1], b[1]) + 1, z1 = Math.max(a[2], b[2]) + 1;
            SelectionOutline.fillBox(x0, y0, z0, x1, y1, z1, CUBOID_COLOR);
            final int step = step(Math.max(x1 - x0, Math.max(y1 - y0, z1 - z0)));
            SelectionOutline.gridBox(x0, y0, z0, x1, y1, z1, step, true, CUBOID_COLOR, gridWidth);
            SelectionOutline.edgeBox(x0, y0, z0, x1, y1, z1, CUBOID_COLOR, edgeWidth);
        }
        if (a != null) {
            SelectionOutline.edgeBox(a[0], a[1], a[2], a[0] + 1, a[1] + 1, a[2] + 1, POINT1_COLOR, edgeWidth);
        }
        if (b != null) {
            SelectionOutline.edgeBox(b[0], b[1], b[2], b[0] + 1, b[1] + 1, b[2] + 1, POINT2_COLOR, edgeWidth);
        }
    }

    private void drawPolygon(final float gridWidth, final float edgeWidth) {
        final List<int[]> points = new ArrayList<>();
        for (final int[] point : this.polygon) {
            if (point != null) {
                points.add(point);
            }
        }
        final int y0 = this.minY, y1 = this.maxY + 1;
        SelectionOutline.fillPrism(points, y0, y1, POLYGON_COLOR);
        SelectionOutline.prism(points, y0, y1, step(y1 - y0), false, POLYGON_COLOR, gridWidth);
        SelectionOutline.prism(points, y0, y1, 1, true, POLYGON_COLOR, edgeWidth);
        for (final int[] point : points) {
            SelectionOutline.edgeBox(point[0], y0, point[1], point[0] + 1, y0 + 1, point[1] + 1, POINT1_COLOR, edgeWidth);
        }
    }

    private static int step(final int size) {
        return Math.max(1, (size + MAX_GRID_LINES - 1) / MAX_GRID_LINES);
    }
}
