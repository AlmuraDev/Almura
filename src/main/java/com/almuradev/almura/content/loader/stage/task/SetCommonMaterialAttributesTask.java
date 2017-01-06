/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.MaterialType;
import com.almuradev.almura.content.loader.AssetContext;
import com.almuradev.almura.creativetab.CreativeTab;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;

public class SetCommonMaterialAttributesTask implements StageTask<MaterialType, MaterialType.Builder> {

    public static final SetCommonMaterialAttributesTask instance = new SetCommonMaterialAttributesTask();

    @Override
    public void execute(AssetContext<MaterialType, MaterialType.Builder> context) throws TaskExecutionFailedException {
        final MaterialType.Builder builder = context.getBuilder();
        final ConfigurationNode root = context.getAsset().getRoot();

        final boolean showInCreativeTab = root.getNode("general", "show-in-creative-tab").getBoolean(true);

        if (showInCreativeTab) {
            final CreativeTab creativeTab = Sponge.getRegistry().getType(CreativeTab.class, context.getPack().getId()).orElse(null);

            if (creativeTab == null) {
                // TODO Log this
            } else {
                builder.creativeTab(creativeTab);
            }
        }
    }
}
