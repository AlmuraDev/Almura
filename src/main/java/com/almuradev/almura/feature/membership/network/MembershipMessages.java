/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.membership.network;

import com.almuradev.almura.network.AlmuraNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The memberships' messages, as 3.5's packets.
 */
public final class MembershipMessages {

    private MembershipMessages() {
    }

    /**
     * The player asks for the memberships window (the Features menu, the Membership Exchange).
     */
    public record ServerboundOpen() implements CustomPacketPayload {

        public static final Type<ServerboundOpen> TYPE = AlmuraNetwork.type("membership_open");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpen> CODEC = StreamCodec.unit(new ServerboundOpen());

        @Override
        public Type<ServerboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * The player takes a membership (its level): bought with in-game money, or earned with their skills total.
     */
    public record ServerboundUpgrade(int level, boolean bySkills) implements CustomPacketPayload {

        public static final Type<ServerboundUpgrade> TYPE = AlmuraNetwork.type("membership_upgrade");
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUpgrade> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundUpgrade::level,
            ByteBufCodecs.BOOL, ServerboundUpgrade::bySkills,
            ServerboundUpgrade::new);

        @Override
        public Type<ServerboundUpgrade> type() {
            return TYPE;
        }
    }

    /**
     * Opens the memberships window: whether the player is an admin, their skills total (-1 without Skills), their money (as text) and
     * their place on the members track (see {@code Membership.levelOf}).
     */
    public record ClientboundOpen(boolean admin, int skillsTotal, String funds, int current) implements CustomPacketPayload {

        public static final Type<ClientboundOpen> TYPE = AlmuraNetwork.type("membership_open_response");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundOpen::admin,
            ByteBufCodecs.VAR_INT, ClientboundOpen::skillsTotal,
            ByteBufCodecs.STRING_UTF8, ClientboundOpen::funds,
            ByteBufCodecs.VAR_INT, ClientboundOpen::current,
            ClientboundOpen::new);

        @Override
        public Type<ClientboundOpen> type() {
            return TYPE;
        }
    }

    /**
     * A membership was bought: 3.5's Congratulations window.
     */
    public record ClientboundApplied(String membership) implements CustomPacketPayload {

        public static final Type<ClientboundApplied> TYPE = AlmuraNetwork.type("membership_applied");
        public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundApplied> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundApplied::membership, ClientboundApplied::new);

        @Override
        public Type<ClientboundApplied> type() {
            return TYPE;
        }
    }
}
