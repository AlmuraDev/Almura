/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.type.block.state;

import com.almuradev.almura.asm.mixin.interfaces.IMixinAlmuraBlock;
import com.almuradev.almura.asm.mixin.interfaces.IMixinDelegateBlockAttributes;
import com.almuradev.almura.content.type.block.component.aabb.CollisionBox;
import com.almuradev.almura.content.type.block.component.aabb.WireFrame;
import com.almuradev.almura.content.type.block.component.action.blockbreak.BlockBreak;
import com.almuradev.almura.content.type.block.component.sound.BlockSoundGroup;
import com.almuradev.almura.content.type.material.MapColor;
import com.almuradev.almura.content.type.material.Material;
import com.almuradev.almura.registry.CatalogDelegate;
import net.minecraft.block.Block;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockStateDefinition {

    private final String asset;
    public final String id;
    public final Material material;
    public final MapColor mapColor;
    public final CollisionBox collisionAABB;
    public final WireFrame wireFrameAABB;
    private final OptionalDouble slipperiness;
    private final OptionalDouble hardness;
    private final OptionalDouble lightEmission;
    private final OptionalInt lightOpacity;
    private final OptionalDouble resistance;
    private final Optional<CatalogDelegate<BlockSoundGroup>> soundGroup;
    private final List<BlockBreak> breaks;

    BlockStateDefinition(final BlockStateDefinitionBuilder<?> builder) {
        this.asset = builder.asset();
        this.id = builder.id();
        this.material = builder.material();
        this.mapColor = builder.mapColor();
        this.collisionAABB = builder.collisionAABB();
        this.wireFrameAABB = builder.wireFrameAABB();
        this.slipperiness = builder.slipperiness();
        this.hardness = builder.hardness();
        this.lightEmission = builder.lightEmission();
        this.lightOpacity = builder.lightOpacity();
        this.resistance = builder.resistance();
        this.soundGroup = builder.soundGroup();
        this.breaks = builder.breaks();
    }

    public void fill(final Block block) {
        // material: done in constructor
        // mapColor: done in constructor
        // collisionAABB: done in constructor
        // wireFrameAABB: done in constructor
        this.slipperiness.ifPresent(slipperiness -> block.slipperiness = (float) slipperiness);
        this.hardness.ifPresent(hardness -> block.setHardness((float) hardness));
        this.lightEmission.ifPresent(emission -> block.setLightLevel((float) emission));
        this.lightOpacity.ifPresent(block::setLightOpacity);
        this.resistance.ifPresent(resistance -> block.setResistance((float) resistance));
        this.soundGroup.ifPresent(((IMixinDelegateBlockAttributes) block)::setSoundGroupDelegate);
        ((IMixinAlmuraBlock) block).setBreaks(this.breaks);
    }

    @Override
    public String toString() {
        return this.asset + "{state=" + this.id + '}';
    }
}
