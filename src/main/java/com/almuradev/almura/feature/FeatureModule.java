/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature;

import com.almuradev.almura.feature.cache.CacheModule;
import com.almuradev.almura.feature.crafting.ItemReturnHelper;
import com.almuradev.almura.feature.exchange.ExchangeModule;
import com.almuradev.almura.feature.guide.GuideModule;
import com.almuradev.almura.feature.complex.ComplexContentModule;
import com.almuradev.almura.feature.hud.HeadUpDisplayModule;
import com.almuradev.almura.feature.nick.NickModule;
import com.almuradev.almura.feature.notification.NotificationModule;
import com.almuradev.almura.feature.sign.SignEditFeature;
import com.almuradev.almura.feature.storage.StorageModule;
import com.almuradev.almura.feature.title.TitleModule;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;

public final class FeatureModule extends AbstractModule implements CommonBinder {

    @Override
    protected void configure() {
        this.install(new HeadUpDisplayModule());
        this.install(new NickModule());
        this.install(new NotificationModule());
        this.install(new TitleModule());
        this.install(new GuideModule());
        this.install(new ComplexContentModule());
        this.install(new CacheModule());
        this.install(new ExchangeModule());
        this.install(new StorageModule());
        this.facet().add(SignEditFeature.class);
        this.facet().add(ItemReturnHelper.class);
    }
}
