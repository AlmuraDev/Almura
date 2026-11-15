/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.cache;

import com.almuradev.almura.Almura;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A cache's contents, as 3.5's {@code SingleSlotTileEntity}: one item, up to the cache's limit. The players around it are told what it
 * holds, for what is drawn on its front; broken, it keeps it in the item it drops ({@code custom_data} {@code almura:cache}), placed again
 * it holds it again.
 */
public final class CacheBlockEntity extends BlockEntity {

    /** Where an item keeps a cache's contents, in its {@code custom_data}. */
    public static final String ITEM_KEY = Almura.ID + ":cache";

    private final CacheHandler handler;

    public CacheBlockEntity(final BlockPos pos, final BlockState state) {
        super(Caches.blockEntityType(), pos, state);
        this.handler = new CacheHandler(this::limit, this::contentsChanged);
    }

    public CacheHandler handler() {
        return this.handler;
    }

    public CacheStack stack() {
        return this.handler.stack();
    }

    public int limit() {
        return this.getBlockState().getBlock() instanceof final CacheBlock block ? block.limit() : CacheBlock.Tier.WOOD.limit();
    }

    private void contentsChanged() {
        this.setChanged();
        if (this.level instanceof final ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        this.handler.serialize(output);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.handler.deserialize(input);
    }

    /**
     * What it holds, for the players around it (drawn on its front).
     */
    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        this.handler.serialize(output);
        return output.buildResult();
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---- Kept in the item it drops

    @Override
    protected void collectImplicitComponents(final DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.level == null || this.stack().isEmpty()) {
            return;
        }
        final CompoundTag tag = new CompoundTag();
        tag.put(ITEM_KEY, encode(this.level.registryAccess(), this.stack()));
        components.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    protected void applyImplicitComponents(final DataComponentGetter components) {
        super.applyImplicitComponents(components);
        final CustomData data = components.get(DataComponents.CUSTOM_DATA);
        if (this.level == null || data == null || !data.contains(ITEM_KEY)) {
            return;
        }
        this.handler.setNow(decode(this.level.registryAccess(), data.copyTag().get(ITEM_KEY)));
    }

    /**
     * @return what an item of a cache holds, or nothing
     */
    public static CacheStack stackOf(final ItemStack item, final HolderLookup.Provider registries) {
        final CustomData data = item.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(ITEM_KEY)) {
            return CacheStack.EMPTY;
        }
        return decode(registries, data.copyTag().get(ITEM_KEY));
    }

    private static Tag encode(final HolderLookup.Provider registries, final CacheStack stack) {
        return CacheStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).result().orElseGet(CompoundTag::new);
    }

    private static CacheStack decode(final HolderLookup.Provider registries, final @Nullable Tag tag) {
        if (tag == null) {
            return CacheStack.EMPTY;
        }
        return CacheStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(CacheStack.EMPTY);
    }
}
