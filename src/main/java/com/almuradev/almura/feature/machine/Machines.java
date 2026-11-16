/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.machine;

import com.almuradev.almura.Almura;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Registers Almura's processing machines ({@link MachineType}: the seed processor, kitchen slicer and kitchen grinder): their blocks, items
 * (in the Agriculture tab, as 3.5's), block entity and menu.
 */
public final class Machines {

    private static final String TAB = Almura.ID + ":agriculture";
    private static final Map<MachineType, ProcessingBlock> blocks = new EnumMap<>(MachineType.class);
    private static @Nullable BlockEntityType<ProcessingBlockEntity> blockEntityType;
    private static @Nullable MenuType<ProcessingMenu> menuType;

    private Machines() {
    }

    public static BlockEntityType<ProcessingBlockEntity> blockEntityType() {
        return Objects.requireNonNull(blockEntityType, "The machines' block entity isn't registered yet");
    }

    public static MenuType<ProcessingMenu> menuType() {
        return Objects.requireNonNull(menuType, "The machines' menu isn't registered yet");
    }

    /**
     * @return the menu, or null before it is registered (for the client's screen registration)
     */
    public static @Nullable MenuType<ProcessingMenu> menuTypeOrNull() {
        return menuType;
    }

    public static Collection<ProcessingBlock> blocks() {
        return blocks.values();
    }

    /**
     * Registers the blocks: 3.5's hardness 3.5, resistance 10, metal. The kitchen machines' shaped models aren't solid.
     */
    public static void registerBlocks(final RegisterEvent.RegisterHelper<Block> helper) {
        for (final MachineType type : MachineType.values()) {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, type.id()))
                .strength(3.5f, 10f).mapColor(MapColor.METAL).sound(SoundType.METAL);
            if (type.customModel()) {
                properties = properties.noOcclusion();
            }
            final ProcessingBlock block = new ProcessingBlock(properties, type);
            blocks.put(type, block);
            helper.register(type.id(), block);
        }
    }

    /**
     * Registers their items, added to the Agriculture tab.
     */
    public static void registerItems(final RegisterEvent.RegisterHelper<Item> helper, final BiConsumer<String, Item> addToTab) {
        for (final Map.Entry<MachineType, ProcessingBlock> entry : blocks.entrySet()) {
            final Identifier id = entry.getKey().id();
            final Item item = new BlockItem(entry.getValue(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                .useBlockDescriptionPrefix());
            helper.register(id, item);
            addToTab.accept(TAB, item);
        }
    }

    public static void registerBlockEntityType(final RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        blockEntityType = new BlockEntityType<>(ProcessingBlockEntity::new, new HashSet<>(blocks.values()));
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "processing_machine"), blockEntityType);
    }

    @SuppressWarnings("unchecked")
    public static void registerMenu(final RegisterEvent.RegisterHelper<MenuType<?>> helper) {
        final MenuType<ProcessingMenu>[] holder = new MenuType[1];
        holder[0] = IMenuTypeExtension.create((id, inventory, buffer) -> ProcessingMenu.client(holder[0], id, inventory, buffer.readVarInt()));
        menuType = holder[0];
        helper.register(Identifier.fromNamespaceAndPath(Almura.ID, "processing_machine"), menuType);
    }
}
