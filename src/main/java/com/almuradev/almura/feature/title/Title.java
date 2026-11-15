/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.title;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * A title, as 3.5's: shown above the name of the players who pick it, offered to those with its permission; a hidden one only to title
 * administrators. Its content keeps its colour codes as section signs.
 */
public record Title(String id, long created, UUID creator, String permission, String content, boolean hidden) {

    public static final StreamCodec<ByteBuf, Title> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, Title::id,
        ByteBufCodecs.VAR_LONG, Title::created,
        UUIDUtil.STREAM_CODEC, Title::creator,
        ByteBufCodecs.STRING_UTF8, Title::permission,
        ByteBufCodecs.STRING_UTF8, Title::content,
        ByteBufCodecs.BOOL, Title::hidden,
        Title::new);
}
