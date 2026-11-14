/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.almanac;

import com.almuradev.almura.Almura;
import com.almuradev.almura.content.registry.ContentCrop;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacCrops;
import com.almuradev.almura.feature.almanac.network.ClientboundAlmanacField;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.function.BiConsumer;

/**
 * The Farmer's Almanac, as 3.5's: right click farmland, a crop or an irrigation pipe to see how that spot grows (the server sends what only
 * it knows: the biome's temperature and rain, the light, a heat lamp or an irrigation pipe near); right click anything else, or the air, for
 * the list of every crop. Needs {@code almura.item.farmers_almanac}, which everyone has unless a permission plugin takes it away (3.5's
 * servers gave it to every player).
 */
public final class FarmersAlmanac {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Almura.ID, "normal/tool/farmers_almanac");
    // The creative tab it's in, as on 3.5
    public static final String TAB = Almura.ID + ":tool";
    public static final String AGRICULTURE = "almura:horizontal/agriculture/";
    private static final Identifier PIPE = Identifier.parse("almura:horizontal/agriculture/pipe");
    private static final int IRRIGATION_RANGE = 6;

    public static final PermissionNode<Boolean> PERMISSION = new PermissionNode<>(Almura.ID, "item.farmers_almanac", PermissionTypes.BOOLEAN,
        (player, uuid, context) -> true);

    /**
     * Registers the almanac, added to its tab.
     */
    public static void register(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        final Item almanac = new AlmanacItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ID)).stacksTo(1));
        helper.register(ID, almanac);
        addToTab.accept(TAB, almanac);
    }

    @SubscribeEvent
    public void onPermissionNodes(final PermissionGatherEvent.Nodes event) {
        event.addNodes(PERMISSION);
    }

    /**
     * Whether a block is one of the content's agriculture blocks (the irrigation pipes).
     */
    public static boolean isAgriculture(final Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString().startsWith(AGRICULTURE);
    }

    private static boolean allowed(final ServerPlayer player) {
        if (PermissionAPI.getPermission(player, PERMISSION)) {
            return true;
        }
        player.sendSystemMessage(Component.literal("Access denied, missing permission: ")
            .append(Component.literal("almura.item.farmers_almanac").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.WHITE)));
        return false;
    }

    /**
     * What the server knows about the spot: as 3.5 sent it.
     */
    private static ClientboundAlmanacField field(final ServerLevel level, final BlockPos pos, final BlockState state) {
        final boolean pipe = isAgriculture(state.getBlock());
        boolean pipeNear = false;
        if (BuiltInRegistries.BLOCK.containsKey(PIPE)) {
            // The farmland's irrigation area (FarmlandBlockMixin): 6 around, 1 to 6 above the farmland, which is under a crop
            final BlockPos farmland = state.getBlock() instanceof CropBlock ? pos.below() : pos;
            for (final BlockPos at : BlockPos.betweenClosed(farmland.offset(-IRRIGATION_RANGE, 1, -IRRIGATION_RANGE),
                farmland.offset(IRRIGATION_RANGE, IRRIGATION_RANGE, IRRIGATION_RANGE))) {
                if (level.isLoaded(at) && isAgriculture(level.getBlockState(at).getBlock())) {
                    pipeNear = true;
                    break;
                }
            }
        }
        final Biome biome = level.getBiome(pos).value();
        final String biomeId = level.getBiome(pos).unwrapKey().map(key -> key.identifier().toString()).orElse("");
        final int skyLight = Math.max(0, level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken());
        return new ClientboundAlmanacField(pos, biomeId, ContentCrop.temperature(level, pos), biome.getModifiedClimateSettings().downfall(),
            level.getBrightness(LightLayer.BLOCK, pos), skyLight, level.getMaxLocalRawBrightness(pos), level.isBrightOutside(),
            level.canSeeSky(pos), ContentCrop.heatLampNear(level, pos), pipe, pipeNear);
    }

    /**
     * The list of every crop, with the ground temperature where the player stands.
     */
    private static ClientboundAlmanacCrops crops(final ServerPlayer player) {
        final ServerLevel level = player.level();
        final BlockPos pos = player.blockPosition();
        final String biomeId = level.getBiome(pos).unwrapKey().map(key -> key.identifier().toString()).orElse("");
        return new ClientboundAlmanacCrops(biomeId, ContentCrop.temperature(level, pos));
    }

    public static final class AlmanacItem extends Item {

        AlmanacItem(final Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(final UseOnContext context) {
            // Handled on both sides, so the client doesn't go on to use the item as well and open the list twice
            if (context.getPlayer() instanceof final ServerPlayer player && allowed(player)) {
                final ServerLevel level = player.level();
                final BlockPos pos = context.getClickedPos();
                final BlockState state = level.getBlockState(pos);
                final Block block = state.getBlock();
                if (block instanceof FarmlandBlock || block instanceof CropBlock || isAgriculture(block)) {
                    PacketDistributor.sendToPlayer(player, field(level, pos, state));
                } else {
                    // Anything that isn't farming shows the list of every crop
                    PacketDistributor.sendToPlayer(player, crops(player));
                }
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
            // The air shows the list of every crop
            if (player instanceof final ServerPlayer serverPlayer && allowed(serverPlayer)) {
                PacketDistributor.sendToPlayer(serverPlayer, crops(serverPlayer));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public FarmersAlmanac() {
    }
}
