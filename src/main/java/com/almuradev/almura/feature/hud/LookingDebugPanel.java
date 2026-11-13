/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.inspirenxe.strata.api.ComponentFactory;
import org.inspirenxe.strata.api.control.display.Image;
import org.inspirenxe.strata.api.layout.AnchorPanel;
import org.inspirenxe.strata.api.layout.HorizontalAlignment;
import org.inspirenxe.strata.api.layout.Orientation;
import org.inspirenxe.strata.api.layout.VerticalAlignment;
import org.inspirenxe.strata.api.text.Text;
import org.inspirenxe.strata.minecraft.MinecraftVisuals;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

/**
 * The debug panel under the player's while the debug screen (F3) is open and the player looks at a block or an entity, as on 1.12: the
 * block's item and id with its state's properties (true green, false red), or the entity's spawn egg (a player's face) with its id and name.
 */
final class LookingDebugPanel {

    private static final float WIDTH = 124f, ICON = 16f, ICON_GAP = 4f, LINE_GAP = 4f;
    private static final int WHITE = 0xFFFFFFFF, GREEN = 0xFF55FF55, RED = 0xFFFF5555;

    private final AnchorPanel panel;
    private final Image icon;
    private final DebugLines lines;
    // What the icon shows, so it's made again only when that changes
    private @Nullable Object iconOf;

    LookingDebugPanel(final ComponentFactory factory, final Function<org.inspirenxe.strata.api.component.Component<?>, AnchorPanel> wrapper) {
        this.icon = factory.image();
        this.icon.setImageSize(ICON, ICON).setSize(ICON, ICON);
        this.lines = new DebugLines(factory, LINE_GAP);
        final var row = factory.stackPanel(Orientation.HORIZONTAL).setSpacing(ICON_GAP)
            .add(this.icon.setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP))
            .add(this.lines.component().setMargin(0f, 4f, 0f, 0f));
        this.panel = wrapper.apply(row.setMargin(1f, 1f, 1f, 1f));
        this.panel.setMinSize(WIDTH, 0f);
        this.panel.setVisible(false);
    }

    AnchorPanel component() {
        return this.panel;
    }

    /**
     * Shows what the player looks at, while the debug screen is open; hides the panel otherwise, or when they look at nothing.
     *
     * @param shown whether the debug screen is open
     */
    void refresh(final boolean shown) {
        final Minecraft minecraft = Minecraft.getInstance();
        final ClientLevel level = minecraft.level;
        final HitResult hit = minecraft.hitResult;
        this.lines.begin();
        if (shown && level != null && minecraft.getCameraEntity() != null && hit != null) {
            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof final BlockHitResult block) {
                this.block(level, block.getBlockPos());
            } else if (hit.getType() == HitResult.Type.ENTITY && hit instanceof final EntityHitResult entity) {
                this.entity(entity.getEntity());
            }
        }
        this.lines.end();
        this.panel.setVisible(!this.lines.isEmpty());
    }

    private void block(final ClientLevel level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        // The item the block gives when picked, if it has one
        final ItemStack pick = state.getCloneItemStack(level, pos, false);
        this.showIcon(state, () -> pick.isEmpty() ? null : MinecraftVisuals.item(pick));
        // Lined up after the icon's place even without one, as on 1.12
        this.icon.setVisible(true);

        this.lines.line(Text.of(state.typeHolder().getRegisteredName()).color(WHITE));
        state.getValues().forEach(value -> this.property(value));
    }

    private void property(final Property.Value<?> value) {
        final String name = value.property().getName();
        if (value.value() instanceof final Boolean bool) {
            this.lines.line(Text.concat(Text.of(name + ": ").color(WHITE), Text.of(value.valueName()).color(bool ? GREEN : RED)));
        } else {
            this.lines.property(name, value.valueName());
        }
    }

    private void entity(final Entity entity) {
        final boolean player = entity instanceof Player;
        if (player) {
            // The player's face, where 1.12 drew their head
            this.showIcon(((Player) entity).getGameProfile(), () -> MinecraftVisuals.playerFace(((Player) entity).getGameProfile()));
            this.icon.setVisible(true);
        } else {
            final ItemStack egg = SpawnEggItem.byId(entity.getType()).map(holder -> new ItemStack(holder.value())).orElse(ItemStack.EMPTY);
            this.showIcon(entity.getType(), () -> egg.isEmpty() ? null : MinecraftVisuals.item(egg));
            this.icon.setVisible(!egg.isEmpty());
        }

        this.lines.line(Text.of(entity.typeHolder().getRegisteredName()).color(WHITE));
        if (player || entity.hasCustomName()) {
            this.lines.property("name", entity.getName().getString());
        }
    }

    private void showIcon(final Object of, final java.util.function.Supplier<org.inspirenxe.strata.api.visual.@Nullable Visual> visual) {
        if (of != this.iconOf) {
            this.iconOf = of;
            final var shown = visual.get();
            if (shown == null) {
                this.icon.setPixels(null);
            } else {
                // Drawn at the icon's size, not the visual's own (a face is 8 across)
                this.icon.setVisual(shown).setImageSize(ICON, ICON);
            }
        }
    }
}
