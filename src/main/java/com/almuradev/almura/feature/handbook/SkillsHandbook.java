/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.handbook;

import com.almuradev.almura.Almura;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook;
import com.almuradev.almura.feature.handbook.network.ClientboundHandbook.SkillLevel;
import com.almuradev.almura.feature.skills.server.ServerSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The Skills Handbook, as 3.5's: right click it to see, for each skill, what needs a level and what gives experience and money, read from
 * the server's Skills plugin, with the player's own level in each. Without Skills it says so, as 3.5 did.
 * <p>
 * Permission {@code almura.item.skills_handbook} opens it (everyone unless a permission plugin takes it away, as the Farmer's Almanac's).
 */
public final class SkillsHandbook {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "normal/tool/skills_handbook");
    // The creative tab it's in, as on 3.5
    public static final String TAB = Almura.ID + ":tool";

    public static final PermissionNode<Boolean> PERMISSION = new PermissionNode<>(Almura.ID, "item.skills_handbook", PermissionTypes.BOOLEAN,
        (player, uuid, context) -> true);

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

    /**
     * Registers the handbook's permission (see {@link ServerSkills#install}).
     */
    public static final class Permission {

        @SubscribeEvent
        public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
            event.addNodes(PERMISSION);
        }
    }

    /**
     * Opens the handbook for the player, if they may and the server has skills.
     */
    static void open(final ServerPlayer player) {
        if (!PermissionAPI.getPermission(player, PERMISSION)) {
            player.sendSystemMessage(Component.literal("Access denied, missing permission: ")
                .append(Component.literal("almura.item.skills_handbook").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
            return;
        }
        if (!ServerSkills.available()) {
            player.sendSystemMessage(Component.literal("The ")
                .append(Component.literal("Skills Handbook").withStyle(ChatFormatting.AQUA))
                .append(" needs skills, which this server doesn't have."));
            return;
        }
        final List<SkillLevel> levels = new ArrayList<>();
        for (final String skill : ServerSkills.skills()) {
            levels.add(new SkillLevel(skill, Math.max(-1, ServerSkills.level(player, skill))));
        }
        PacketDistributor.sendToPlayer(player, new ClientboundHandbook(levels, ServerSkills.handbookRules()));
    }

    public static final class HandbookItem extends Item {

        HandbookItem(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(final UseOnContext context) {
            // Handled here, so the client doesn't go on to use the item as well and open it twice
            if (context.getPlayer() != null) {
                this.open(context.getLevel(), context.getPlayer());
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            this.open(level, player);
            return InteractionResult.SUCCESS;
        }

        private void open(final Level level, final Player player) {
            // The arm swings on the client, from the success returned
            if (!level.isClientSide() && player instanceof final ServerPlayer serverPlayer) {
                SkillsHandbook.open(serverPlayer);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
            final Consumer<Component> tooltip, final TooltipFlag flag) {
            tooltip.accept(Component.literal("Shows what each skill needs and rewards").withStyle(ChatFormatting.GRAY));
        }
    }
}
