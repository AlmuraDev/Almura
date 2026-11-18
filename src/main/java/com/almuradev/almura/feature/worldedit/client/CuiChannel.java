/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.worldedit.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * WorldEdit's CUI channel ({@code worldedit:cui}) as this client has it: through WorldEdit's own protocol when WorldEdit is installed here
 * ({@link WorldEditCuiChannel}), or registered by Almura when it isn't ({@link AlmuraCuiChannel}). Names no WorldEdit class, so it's safe to
 * load without WorldEdit.
 */
interface CuiChannel {

    /**
     * @return the channel's payload type, to check the server has the channel before writing to it
     */
    CustomPacketPayload.Type<?> type();

    /**
     * @return the {@code v|4} message that tells WorldEdit this client shows selections, after which it describes them
     */
    CustomPacketPayload handshake();
}
