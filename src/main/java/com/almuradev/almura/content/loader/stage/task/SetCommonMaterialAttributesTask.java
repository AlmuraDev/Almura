/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.content.loader.stage.task;

import com.almuradev.almura.Almura;
import com.almuradev.almura.Constants;
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
        final ConfigurationNode node = context.getAsset().getConfigurationNode();

        final boolean showInCreativeTab = node.getNode(Constants.Config.GENERAL, Constants.Config.SHOW_IN_CREATIVE_TAB).getBoolean(true);

        if (showInCreativeTab) {
            final CreativeTab creativeTab = Sponge.getRegistry().getType(CreativeTab.class, context.getPack().getId()).orElse(null);

            if (creativeTab == null) {
                Almura.instance.logger.debug("Material type [{}] specified creative tab [{}] but it was not found in the registry!", context.getCatalog().getId(), context.getPack().getId());
            } else {
                builder.creativeTab(creativeTab);
            }
        }
    }
}
