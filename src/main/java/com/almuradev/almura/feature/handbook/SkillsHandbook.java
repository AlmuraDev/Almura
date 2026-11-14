/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook;

import com.almuradev.almura.Almura;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;

/**
 * The Skills Handbook, as 3.5's: it lists what each skill gates by level and rewards with experience, read from the server's Skills
 * plugin. Skills has no 26.3 version yet, so on 4.0 it says what 3.5 said on a server without it.
 */
public final class SkillsHandbook {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "normal/tool/skills_handbook");
    // The creative tab it's in, as on 3.5
    public static final String TAB = Almura.ID + ":tool";

    private SkillsHandbook() {
    }

    /**
     * Registers the handbook, added to its tab.
     */
    public static void register(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        final Item handbook = new HandbookItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ID)).stacksTo(1));
        helper.register(ID, handbook);
        addToTab.accept(TAB, handbook);
    }

    public static final class HandbookItem extends Item {

        HandbookItem(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(final UseOnContext context) {
            // Handled here, so the client doesn't go on to use the item as well and open it twice
            if (context.getPlayer() != null) {
                open(context.getLevel(), context.getPlayer());
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            open(level, player);
            return InteractionResult.SUCCESS;
        }

        private static void open(final Level level, final Player player) {
            if (level.isClientSide()) {
                return;
            }
            player.sendSystemMessage(Component.literal("The ")
                .append(Component.literal("Skills Handbook").withStyle(ChatFormatting.AQUA))
                .append(" needs skills, which this server doesn't have."));
        }
    }
}
