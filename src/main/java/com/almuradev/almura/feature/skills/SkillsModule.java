package com.almuradev.almura.feature.skills;

import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Platform;

public final class SkillsModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {

        this.facet().add(SkillsHandler.class);

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {
                @Override
                @SuppressWarnings("UnnecessaryStaticInjection") // HACK: inject into required mixin target classes
                protected void configure() {
                   // this.requestStaticInjection(SkillsGUI.class);
                }
            }
            this.install(new ClientModule());
        });
    }
}
