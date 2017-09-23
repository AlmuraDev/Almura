/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.material.processor;

import static com.almuradev.toolbox.config.ConfigurationNodes.whenRealBoolean;
import static com.almuradev.toolbox.config.ConfigurationNodes.whenRealString;

import com.almuradev.content.type.mapcolor.MapColor;
import com.almuradev.content.type.material.Material;
import com.almuradev.content.type.material.MaterialConfig;
import com.almuradev.toolbox.config.processor.ConfigProcessor;
import net.minecraft.block.material.EnumPushReaction;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.GameRegistry;

import java.util.Locale;
import java.util.function.Function;

import javax.inject.Inject;

public final class MaterialProcessor implements ConfigProcessor<Material.Builder> {

    private static final Function<String, EnumPushReaction> PUSH = id -> EnumPushReaction.valueOf(id.toUpperCase(Locale.ENGLISH));
    private final GameRegistry gr;

    @Inject
    private MaterialProcessor(final GameRegistry gr) {
        this.gr = gr;
    }

    @Override
    public void process(final ConfigurationNode config, final Material.Builder builder) {
        whenRealBoolean(config.getNode(MaterialConfig.BLOCKS_LIGHT), builder::blocksLight);
        whenRealBoolean(config.getNode(MaterialConfig.BLOCKS_MOVEMENT), builder::blocksMovement);
        whenRealBoolean(config.getNode(MaterialConfig.LIQUID), builder::liquid);
        whenRealString(config.getNode(MaterialConfig.MAP_COLOR), value -> this.gr.getType(MapColor.class, value).ifPresent(builder::mapColor));
        whenRealString(config.getNode(MaterialConfig.PUSH), value -> builder.push(PUSH.apply(value)));
        whenRealBoolean(config.getNode(MaterialConfig.SOLID), builder::solid);
        whenRealBoolean(config.getNode(MaterialConfig.REPLACEMENT), builder::replaceable);
        whenRealBoolean(config.getNode(MaterialConfig.TRANSLUCENT), builder::translucent);
        whenRealBoolean(config.getNode(MaterialConfig.TOOL_REQUIRED), builder::toolRequired);
    }
}
