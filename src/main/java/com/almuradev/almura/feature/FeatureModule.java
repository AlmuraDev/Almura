/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature;

import com.almuradev.almura.feature.animal.AnimalModule;
import com.almuradev.almura.feature.biome.BiomeModule;
import com.almuradev.almura.feature.cache.CacheModule;
import com.almuradev.almura.feature.claim.ClaimModule;
import com.almuradev.almura.feature.complex.ComplexContentModule;
import com.almuradev.almura.feature.crafting.ItemReturnHelper;
import com.almuradev.almura.feature.death.DeathModule;
import com.almuradev.almura.feature.exchange.ExchangeModule;
import com.almuradev.almura.feature.guide.GuideModule;
import com.almuradev.almura.feature.hud.HeadUpDisplayModule;
import com.almuradev.almura.feature.membership.MembershipModule;
import com.almuradev.almura.feature.menu.ingame.FeaturesModule;
import com.almuradev.almura.feature.nick.NickModule;
import com.almuradev.almura.feature.notification.NotificationModule;
import com.almuradev.almura.feature.offhand.OffHandListener;
import com.almuradev.almura.feature.permission.PermissionsModule;
import com.almuradev.almura.feature.sign.SignEditFeature;
import com.almuradev.almura.feature.skills.SkillsModule;
import com.almuradev.almura.feature.storage.StorageModule;
import com.almuradev.almura.feature.store.StoreModule;
import com.almuradev.almura.feature.title.TitleModule;
import com.almuradev.almura.shared.inject.ClientBinder;
import com.almuradev.almura.shared.inject.CommonBinder;
import net.kyori.violet.AbstractModule;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.SpongeImplHooks;

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
        this.install(new StoreModule());
        this.install(new DeathModule());
        this.install(new StorageModule());
        this.install(new BiomeModule());
        this.install(new FeaturesModule());
        this.install(new AnimalModule());
        this.facet().add(SignEditFeature.class);
        this.facet().add(ItemReturnHelper.class);
        this.nerfVanillaFood();
        this.install(new ClaimModule());
        this.install(new MembershipModule());
        this.install(new SkillsModule());

        if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
            this.loadServerSideModules(); // Force loading this because it will fail the Platform.Type checks below during normal startup.
        }

        this.on(Platform.Type.CLIENT, () -> {
            final class ClientModule extends AbstractModule implements ClientBinder {

                @SideOnly(Side.CLIENT)
                @Override
                protected void configure() {
                    this.facet().add(OffHandListener.class);
                }
            }
            this.install(new ClientModule());
        });
        this.on(Platform.Type.SERVER, () -> { // Dedicated Server Only!
            final class ServerModule extends AbstractModule implements CommonBinder {

                @SideOnly(Side.SERVER)
                @Override
                protected void configure() {
                    loadServerSideModules();  // This is never touched in single player / de-obfuscated environments.
                }
            }
            this.install(new ServerModule());
        });
    }

    private void loadServerSideModules() {
        if (Sponge.getPluginManager().isLoaded("luckperms")) {
            this.install(new PermissionsModule());
        }
    }

    //ToDo: put into its own module ones the ability to turn features on and off is implemented.

    private void nerfVanillaFood() {
        ((ItemFood) Items.BEEF).healAmount = 1;
        ((ItemFood) Items.BEEF).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_BEEF).healAmount = 3;
        ((ItemFood) Items.COOKED_BEEF).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_BEEF).alwaysEdible = true;

        ((ItemFood) Items.FISH).healAmount = 1;
        ((ItemFood) Items.FISH).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_FISH).healAmount = 3;
        ((ItemFood) Items.COOKED_FISH).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_FISH).alwaysEdible = true;

        ((ItemFood) Items.RABBIT).healAmount = 1;
        ((ItemFood) Items.RABBIT).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_RABBIT).healAmount = 3;
        ((ItemFood) Items.COOKED_RABBIT).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_RABBIT).alwaysEdible = true;

        ((ItemFood) Items.CHICKEN).healAmount = 1;
        ((ItemFood) Items.CHICKEN).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_CHICKEN).healAmount = 3;
        ((ItemFood) Items.COOKED_CHICKEN).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_CHICKEN).alwaysEdible = true;

        ((ItemFood) Items.PORKCHOP).healAmount = 1;
        ((ItemFood) Items.PORKCHOP).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_PORKCHOP).healAmount = 3;
        ((ItemFood) Items.COOKED_PORKCHOP).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_PORKCHOP).alwaysEdible = true;

        ((ItemFood) Items.MUTTON).healAmount = 1;
        ((ItemFood) Items.MUTTON).saturationModifier = 0.0F;

        ((ItemFood) Items.COOKED_MUTTON).healAmount = 3;
        ((ItemFood) Items.COOKED_MUTTON).saturationModifier = 0.2F;
        ((ItemFood) Items.COOKED_MUTTON).alwaysEdible = true;

        ((ItemFood) Items.POTATO).healAmount = 1;
        ((ItemFood) Items.POTATO).saturationModifier = 0.0F;

        ((ItemFood) Items.CARROT).healAmount = 1;
        ((ItemFood) Items.CARROT).saturationModifier = 0.0F;

        ((ItemFood) Items.BAKED_POTATO).healAmount = 2;
        ((ItemFood) Items.BAKED_POTATO).saturationModifier = 0.2F;
        ((ItemFood) Items.BAKED_POTATO).alwaysEdible = true;

        ((ItemFood) Items.BREAD).healAmount = 2;
        ((ItemFood) Items.BREAD).saturationModifier = 0.0F;
        ((ItemFood) Items.BREAD).alwaysEdible = true;

        ((ItemFood) Items.APPLE).healAmount = 1;
        ((ItemFood) Items.APPLE).saturationModifier = 0.0F;
        ((ItemFood) Items.APPLE).alwaysEdible = true;

        ((ItemFood) Items.COOKIE).healAmount = 1;
        ((ItemFood) Items.COOKIE).saturationModifier = 0.0F;
        ((ItemFood) Items.COOKIE).alwaysEdible = true;

        ((ItemFood) Items.MELON).healAmount = 1;
        ((ItemFood) Items.MELON).saturationModifier = 0.0F;
        ((ItemFood) Items.MELON).alwaysEdible = true;

        ((ItemFood) Items.ROTTEN_FLESH).healAmount = 0;
        ((ItemFood) Items.ROTTEN_FLESH).saturationModifier = 0.0F;
        ((ItemFood) Items.ROTTEN_FLESH).alwaysEdible = true;

        ((ItemFood) Items.SPIDER_EYE).healAmount = 0;
        ((ItemFood) Items.SPIDER_EYE).saturationModifier = 0.0F;
        ((ItemFood) Items.SPIDER_EYE).alwaysEdible = true;

        ((ItemFood) Items.POISONOUS_POTATO).healAmount = 0;
        ((ItemFood) Items.POISONOUS_POTATO).saturationModifier = 0.0F;
        ((ItemFood) Items.POISONOUS_POTATO).alwaysEdible = true;

        ((ItemFood) Items.PUMPKIN_PIE).healAmount = 3;
        ((ItemFood) Items.PUMPKIN_PIE).saturationModifier = 0.2F;
        ((ItemFood) Items.PUMPKIN_PIE).alwaysEdible = true;

        ((ItemFood) Items.BEETROOT).healAmount = 1;
        ((ItemFood) Items.BEETROOT).saturationModifier = 0.0F;
        ((ItemFood) Items.BEETROOT).alwaysEdible = true;

        ((ItemFood) Items.RABBIT_STEW).healAmount = 3;
        ((ItemFood) Items.RABBIT_STEW).saturationModifier = 0.2F;
        ((ItemFood) Items.RABBIT_STEW).alwaysEdible = true;

        ((ItemFood) Items.BEETROOT_SOUP).healAmount = 3;
        ((ItemFood) Items.BEETROOT_SOUP).saturationModifier = 0.2F;
        ((ItemFood) Items.BEETROOT_SOUP).alwaysEdible = true;

        ((ItemFood) Items.MUSHROOM_STEW).healAmount = 3;
        ((ItemFood) Items.MUSHROOM_STEW).saturationModifier = 0.2F;
        ((ItemFood) Items.MUSHROOM_STEW).alwaysEdible = true;
    }
}
