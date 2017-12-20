package com.almuradev.almura.feature.title;

import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlePacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlePacketHandler;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesPacket;
import com.almuradev.almura.feature.title.network.ClientboundPlayerTitlesPacketHandler;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;

public final class TitleModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.packet()
                .bind(ClientboundPlayerTitlePacket.class, binder -> {
                    binder.channel(5);
                    binder.handler(ClientboundPlayerTitlePacketHandler.class, Platform.Type.CLIENT);
                })
                .bind(ClientboundPlayerTitlesPacket.class, binder -> {
                    binder.channel(6);
                    binder.handler(ClientboundPlayerTitlesPacketHandler.class, Platform.Type.CLIENT);
                });
        this.facet()
                .add(TitleManager.class);
        this.requestStaticInjection(TitleCommands.class);
        if (Sponge.getPlatform().getType().isClient()) {
            this.requestMixinInjection();
        }
    }

    @SideOnly(Side.CLIENT)
    // HACK: inject into required mixin target classes
    @SuppressWarnings("UnnecessaryStaticInjection")
    private void requestMixinInjection() {
        this.requestStaticInjection(RenderPlayer.class);
    }
}
