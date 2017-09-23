/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.shared.event;

import net.kyori.membrane.facet.Activatable;
import net.kyori.membrane.facet.Facet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.GameState;
import org.spongepowered.api.event.Listener;

import java.util.function.Predicate;

/**
 * Something that can listen to events.
 *
 * @see Listener
 * @see SubscribeEvent
 */
public interface Witness extends Facet {

    /**
     * Tests if this witness is subscribable.
     *
     * @return {@code true} if this witness is subscribable
     */
    default boolean subscribable() {
        return true;
    }

    /**
     * Creates a predicate which may be used to check if a witness is subscribable.
     *
     * @param <W> the witness type
     * @return the predicate
     */
    static <W extends Witness> Predicate<W> predicate() {
        return witness -> Activatable.predicate().test(witness) && witness.subscribable();
    }

    /**
     * A witness whose activation depends on a game lifecycle state.
     */
    interface Lifecycle extends Witness {

        /**
         * Tests if this witness is subscribable.
         *
         * @param state the game state
         * @return {@code true} if this witness is subscribable
         */
        boolean lifecycleSubscribable(final GameState state);

        @Override
        default boolean subscribable() {
            return false; // default by false
        }

        /**
         * Creates a predicate which may be used to check if a lifecycle witness is subscribable.
         *
         * @param state the game state
         * @return the predicate
         */
        static Predicate<Lifecycle> predicate(final GameState state) {
            return Witness.<Lifecycle>predicate().and(witness -> witness.lifecycleSubscribable(state));
        }
    }

    /**
     * An abstract helper implementation of a witness.
     *
     * <p>It is not necessary to extend this class.</p>
     */
    class Impl implements Witness {

        boolean subscribed;

        @Override
        public boolean subscribable() {
            return !this.subscribed;
        }
    }
}
