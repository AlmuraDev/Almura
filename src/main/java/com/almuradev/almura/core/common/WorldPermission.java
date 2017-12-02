package com.almuradev.almura.core.common;

import com.almuradev.shared.event.Witness;
import net.kyori.membrane.facet.Activatable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

public class WorldPermission extends Witness.Impl implements Activatable, Witness.Lifecycle {

    /***
     * The following permissions check implementation is INCOMPLETE
     *
     * This was added because Nucleus lacks a decent listener for generalized teleportation events.
     * It seems to only care about command based Teleports.
     *
     * Debugging left in place for down the road testing.
     */
    private final Game game;

    @Inject
    private WorldPermission(final Game game) {
        this.game = game;
    }

    @Listener(order = Order.PRE)
    public void playerMove(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
        if (differentExtent(event.getFromTransform(), event.getToTransform())) {
            //System.out.println("From: " + event.getFromTransform().getExtent().getName() + " To: " + event.getToTransform().getExtent().getName());
            if (!player.hasPermission("almura.world." + event.getToTransform().getExtent().getName())) {
                //System.out.println("Almura Teleport Denied");
                event.setCancelled(true);
            } else {
                //System.out.println("Almura Teleport Granted");
            }
        }
    }

    @Override
    public boolean active() {
        return this.game.isServerAvailable();
    }

    @Override
    public boolean lifecycleSubscribable(final GameState state) {
        return state == GameState.SERVER_STARTING;
    }

    private static boolean differentExtent(final Transform<World> from, final Transform<World> to) {
        return !from.getExtent().equals(to.getExtent());
    }
}
