/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.block.builder.rotable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.Almura;
import com.almuradev.almura.api.block.rotable.RotableBlockType;
import com.almuradev.almura.block.GenericRotable;
import com.almuradev.almura.block.builder.AbstractBlockTypeBuilder;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("unchecked")
public abstract class AbstractRotableTypeBuilder<ROTABLE extends RotableBlockType, BUILDER extends AbstractRotableTypeBuilder<ROTABLE, BUILDER>>
        extends AbstractBlockTypeBuilder<ROTABLE, BUILDER> implements RotableBlockType.Builder<ROTABLE, BUILDER> {

    public static final class BuilderImpl extends AbstractRotableTypeBuilder<RotableBlockType, BuilderImpl> {

        @Override
        public RotableBlockType build(String id) {
            checkNotNull(id);

            final GenericRotable block = GameRegistry.register(new GenericRotable(Almura.PLUGIN_ID, id, this));

            final ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(Almura.PLUGIN_ID, id);

            ModelLoader.setCustomModelResourceLocation(itemBlock, 0, new ModelResourceLocation(Almura.PLUGIN_ID + ":" + id, "facing=south"));

            // TODO Make this configurable and make Almura GenericItemBlock
            GameRegistry.register(itemBlock);

            return (RotableBlockType) (Object) block;
        }
    }
}
