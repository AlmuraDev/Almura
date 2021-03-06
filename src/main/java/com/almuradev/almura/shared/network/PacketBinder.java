/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.network;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A packet binder.
 */
public class PacketBinder {

    private final Multibinder<EntryImpl<? extends Message>> binder;

    public static PacketBinder create(final Binder binder) {
        return new PacketBinder(binder);
    }

    private PacketBinder(@Nonnull final Binder binder) {
        this.binder = Multibinder.newSetBinder(binder, new TypeLiteral<EntryImpl<? extends Message>>() {});
    }

    /**
     * Create a binding entry for the specified packet.
     *
     * <p>Bound packets will be installed into the network channel.</p>
     *
     * @param packet the packet class
     * @param <M> the packet type
     * @return this binder
     */
    public <M extends Message> PacketBinder bind(final Class<M> packet) {
        return this.bind(packet, null);
    }

    /**
     * Create a binding entry for the specified packet.
     *
     * <p>Bound packets will be installed into the network channel.</p>
     *
     * @param packet the packet class
     * @param consumer the binder consumer
     * @param <M> the packet type
     * @return this binder
     */
    public <M extends Message> PacketBinder bind(final Class<M> packet, @Nullable final Consumer<Entry<M>> consumer) {
        final EntryImpl<M> entry = new EntryImpl<>(packet);
        if (consumer != null) {
            consumer.accept(entry);
        }
        this.binder.addBinding().toInstance(entry);
        return this;
    }

    /**
     * A binding entry.
     *
     * @param <M> the packet type
     */
    public interface Entry<M extends Message> {

        /**
         * Sets the packet handler.
         *
         * @param handler the handler
         * @param side the side
         */
        default void handler(final Class<? extends MessageHandler<M>> handler, final Platform.Type side) {
            this.handler(handler, side, false);
        }

        /**
         * Sets the packet handler.
         *
         * @param handler the handler
         * @param side the side
         * @param strictSide if the side is strict
         */
        void handler(final Class<? extends MessageHandler<M>> handler, final Platform.Type side, final boolean strictSide);
    }

    public static final class EntryImpl<M extends Message> implements Entry<M> {

        private static final Set<Class<?>> REGISTERED_PACKETS = new HashSet<>();
        // State-of-the-art proprietary sequential numbering system
        private static int nextChannel;
        private final int channel = nextChannel++;
        private final Class<M> packet;
        @Nullable private Platform.Type side;
        private boolean strictSide;
        @Nullable private Class<? extends MessageHandler<M>> handler;

        EntryImpl(final Class<M> packet) {
            this.packet = packet;
        }

        public void register(final ChannelBinding.IndexedMessageChannel channel, final Platform.Type side, final Injector injector) {
            // Enforce side for handlers
            if (this.handler != null) {
                checkState(this.side != null, "side not provided");
            }

            if(REGISTERED_PACKETS.add(this.packet)) {
                channel.registerMessage(this.packet, this.channel);
            }
            
            if (this.handler == null) {
                return;
            }

            if (this.compatibleWith(side)) {
                channel.addHandler(this.packet, this.side, injector.getInstance(this.handler));
            }
        }

        private boolean compatibleWith(final Platform.Type side) {
            return this.side == side || (!this.strictSide && side == Platform.Type.CLIENT);
        }

        @Override
        public void handler(final Class<? extends MessageHandler<M>> handler, final Platform.Type side, final boolean strictSide) {
            this.handler = handler;
            this.side = side;
            this.strictSide = strictSide;
        }
    }
}
