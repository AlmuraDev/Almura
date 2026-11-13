/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import com.almuradev.almura.Almura;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * The debug panel in the top right while the debug screen (F3) is open, as on 1.12: the versions playing (Minecraft, NeoForge, Sponge, Almura),
 * then Java, memory and the frame rate, then where the camera is, the biome and light there, and the block looked at. Reduced debug info
 * leaves only the position within the chunk.
 */
final class InformationDebugPanel {

    private static final float WIDTH = 155f, LINE_GAP = 4f, TITLE_GAP = 4f;
    private static final double XYZ_SINGLE_LINE_MAX = 100000d;
    private static final int GREEN = 0xFF55FF55, RED = 0xFFFF5555, GOLD = 0xFFFFAA00, DARK_AQUA = 0xFF00AAAA;

    private final AnchorPanel panel;
    private final DebugLines lines;
    private final Text title;

    InformationDebugPanel(final ComponentFactory factory, final Function<org.inspirenxe.strata.api.component.Component<?>, AnchorPanel> wrapper) {
        this.title = title();
        this.lines = new DebugLines(factory, LINE_GAP);
        // The versions centred over the lines, a little apart
        final var titleLabel = factory.label(this.title);
        final var column = factory.stackPanel().setSpacing(TITLE_GAP)
            .add(titleLabel.setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP))
            .add(this.lines.component().setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        this.panel = wrapper.apply(column.setMargin(1f, 2f, 1f, 1f));
        this.panel.setMinSize(WIDTH, 0f);
        this.panel.setVisible(false);
    }

    AnchorPanel component() {
        return this.panel;
    }

    /**
     * Shows the game as it is now, while the debug screen is open; hides the panel otherwise.
     *
     * @param shown whether the debug screen is open
     */
    void refresh(final boolean shown) {
        final Minecraft minecraft = Minecraft.getInstance();
        final Entity view = minecraft.getCameraEntity();
        final ClientLevel level = minecraft.level;
        this.panel.setVisible(shown && view != null && level != null);
        if (!this.panel.isVisible()) {
            return;
        }

        this.lines.begin();
        // The game: always shown...
        this.lines.property("Java", javaDetails());
        this.lines.property("Memory", memoryDetails());
        this.lines.property("FPS", String.valueOf(minecraft.getFps()));

        final double x = view.getX(), y = view.getY(), z = view.getZ();
        final int fx = Mth.floor(x), fy = Mth.floor(y), fz = Mth.floor(z);
        // ...the rest as reduced debug info allows
        if (minecraft.showOnlyReducedInfo()) {
            this.lines.property("Chunk-relative", String.format(Locale.ROOT, "%d %d %d", fx & 0xf, fy & 0xf, fz & 0xf));
        } else {
            this.xyz(x, y, z);
            final Direction facing = view.getDirection();
            this.lines.property("Facing", String.format(Locale.ROOT, "%s%s (%.1f, %.1f)", facing.name().charAt(0), describe(facing),
                Mth.wrapDegrees(view.getYRot()), Mth.wrapDegrees(view.getXRot())));
            this.lines.property("Block", fx + ", " + fy + ", " + fz);
            this.lines.property("Chunk", String.format(Locale.ROOT, "%d, %d, %d in %d, %d, %d", fx & 0xf, fy & 0xf, fz & 0xf, fx >> 4, fy >> 4,
                fz >> 4));

            final BlockPos pos = BlockPos.containing(x, y, z);
            if (level.isInsideBuildHeight(pos.getY()) && level.hasChunkAt(pos)) {
                final Holder<Biome> biome = level.getBiome(pos);
                final Optional<Identifier> id = biome.unwrapKey().map(key -> key.identifier());
                this.lines.property("Biome Reg", id.map(Identifier::toString).orElse("[unregistered]"));
                this.lines.property("Biome Name", id.map(key -> Component.translatable(key.toLanguageKey("biome")).getString()).orElse("-"));
                this.lines.property("Biome ID", String.valueOf(level.registryAccess().lookupOrThrow(Registries.BIOME).getId(biome.value())));
                this.lines.property("Temperature", String.valueOf(biome.value().getBaseTemperature()));
                this.lines.property("Rainfall", String.valueOf(biome.value().getModifiedClimateSettings().downfall()));
                this.lines.property("Light", lightDetails(level, pos));
            }

            // The block looked at
            final HitResult hit = minecraft.hitResult;
            if (hit instanceof final BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
                final BlockPos look = block.getBlockPos();
                this.look(look.getX(), look.getY(), look.getZ());
            }
        }
        this.lines.end();
    }

    private void xyz(final double x, final double y, final double z) {
        if (Math.abs(x) >= XYZ_SINGLE_LINE_MAX || Math.abs(y) >= XYZ_SINGLE_LINE_MAX || Math.abs(z) >= XYZ_SINGLE_LINE_MAX) {
            this.lines.property("X", String.format(Locale.ROOT, "%.3f", x));
            this.lines.property("Y", String.format(Locale.ROOT, "%.3f", y));
            this.lines.property("Z", String.format(Locale.ROOT, "%.3f", z));
        } else {
            this.lines.property("XYZ", String.format(Locale.ROOT, "%.3f, %.3f, %.3f", x, y, z));
        }
    }

    private void look(final int x, final int y, final int z) {
        if (Math.abs(x) >= XYZ_SINGLE_LINE_MAX || Math.abs(y) >= XYZ_SINGLE_LINE_MAX || Math.abs(z) >= XYZ_SINGLE_LINE_MAX) {
            this.lines.property("Look X", String.valueOf(x));
            this.lines.property("Look Y", String.valueOf(y));
            this.lines.property("Look Z", String.valueOf(z));
        } else {
            this.lines.property("Look", x + ", " + y + ", " + z);
        }
    }

    /**
     * The versions playing, each underlined in its colour: "m26.3 f83 s... b..." (NeoForge's build, Sponge's when it is installed, Almura's).
     */
    private static Text title() {
        final List<Text> parts = new ArrayList<>();
        parts.add(Text.of("m" + SharedConstants.getCurrentVersion().name()).color(GREEN).underline());
        parts.add(Text.of("f" + afterLast(version("neoforge"), ".")).color(RED).underline());
        final String sponge = version("sponge");
        if (!sponge.isEmpty()) {
            parts.add(Text.of("s" + afterLast(sponge, "-")).color(GOLD).underline());
        }
        final String almura = version(Almura.ID);
        parts.add(Text.of(almura.contains("-b") ? "b" + afterLast(almura, "-b") : almura).color(DARK_AQUA).underline());
        return Text.join(Text.of(" "), parts);
    }

    private static String version(final String modId) {
        return ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(info -> info.getVersion().toString()).orElse("");
    }

    private static String afterLast(final String text, final String separator) {
        final int index = text.lastIndexOf(separator);
        return index < 0 ? text : text.substring(index + separator.length());
    }

    private static String javaDetails() {
        return System.getProperty("java.version") + " x" + ("64".equals(System.getProperty("sun.arch.data.model")) ? 64 : 86);
    }

    private static String memoryDetails() {
        final long max = Runtime.getRuntime().maxMemory();
        final long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return (used * 100L / max) + "% " + used / 1024L / 1024L + "/" + max / 1024L / 1024L + "MB";
    }

    private static String lightDetails(final ClientLevel level, final BlockPos pos) {
        final int raw = level.getChunkSource().getLightEngine().getRawBrightness(pos, 0);
        return raw + " (" + level.getBrightness(LightLayer.SKY, pos) + " sky, " + level.getBrightness(LightLayer.BLOCK, pos) + " block)";
    }

    private static String describe(final Direction facing) {
        return switch (facing) {
            case NORTH -> " (-z)";
            case SOUTH -> " (+z)";
            case WEST -> " (-x)";
            case EAST -> " (+x)";
            default -> "";
        };
    }
}
