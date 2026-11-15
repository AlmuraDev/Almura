/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title.client;

import com.almuradev.almura.feature.title.Title;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.jspecify.annotations.Nullable;

/**
 * Draws a player's title above their name, as 3.5 did: a line over the name tag, as vanilla puts a scoreboard line over it.
 */
public final class TitleTags {

    // Vanilla's line height for name tags: 9 pixels, 1.15 apart, at 0.025 of a block a pixel
    private static final float LINE = 9.0F * 1.15F * 0.025F;

    @SubscribeEvent
    public void onRenderNameTag(final RenderNameTagEvent.DoRender event) {
        if (!(event.getEntityRenderState() instanceof final AvatarRenderState state) || state.nameTag == null || state.nameTagAttachment == null
            || event.getContent() != state.nameTag) {
            return;
        }
        final Player player = player(state.id);
        if (player == null) {
            return;
        }
        final Title title = ClientTitles.shownFor(player.getUUID());
        if (title == null) {
            return;
        }
        final PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(0.0F, LINE, 0.0F);
        event.getSubmitNodeCollector().submitNameTag(pose, state.nameTagAttachment, state.showExtraEars ? -10 : 0,
            Component.literal(title.content()), !state.isDiscrete, state.lightCoords, event.getCameraRenderState());
        pose.popPose();
    }

    private static @Nullable Player player(final int id) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        final Entity entity = minecraft.level.getEntity(id);
        return entity instanceof final Player player ? player : null;
    }
}
