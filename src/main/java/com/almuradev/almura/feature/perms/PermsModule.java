package com.almuradev.almura.feature.perms;

import com.almuradev.almura.feature.title.ServerTitleManager;
import com.almuradev.almura.shared.inject.CommonBinder;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.event.user.track.UserTrackEvent;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ProviderRegistration;

import java.util.Optional;

import javax.inject.Inject;

public final class PermsModule extends AbstractModule implements CommonBinder {

    @Inject
    private static ServerTitleManager manager;

    @Override
    protected void configure() {
        Optional<ProviderRegistration<LuckPermsApi>> provider = Sponge.getServiceManager().getRegistration(LuckPermsApi.class);
        if (provider.isPresent()) {
            LuckPermsApi api = provider.get().getProvider();
            api.getEventBus().subscribe(UserTrackEvent.class, e -> {
            System.out.println("Event: " + e.getAction().name());

            manager.refreshSelectedTitles();

            });
        }
    }

}
