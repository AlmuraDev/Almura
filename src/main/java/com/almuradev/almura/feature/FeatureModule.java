/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature;

import com.almuradev.almura.asm.mixin.accessors.item.ItemFoodAccessor;
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
import com.almuradev.almura.feature.moc.MocModule;
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
import com.almuradev.almura.shared.util.UchatUtil;
import net.kyori.violet.AbstractModule;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.Loader;
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
        if (Loader.isModLoaded("mocreatures")) {
            this.install(new MocModule());
        }
        if (Sponge.getPluginManager().isLoaded("ultimatechat")) {
            this.facet().add(UchatUtil.class);
        }
    }

    //ToDo: put into its own module ones the ability to turn features on and off is implemented.

    private void nerfVanillaFood() {
        ((ItemFoodAccessor) Items.BEEF).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.BEEF).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_BEEF).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_BEEF).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_BEEF).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.FISH).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.FISH).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_FISH).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_FISH).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_FISH).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.RABBIT).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.RABBIT).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_RABBIT).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_RABBIT).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_RABBIT).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.CHICKEN).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.CHICKEN).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_CHICKEN).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_CHICKEN).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_CHICKEN).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.PORKCHOP).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.PORKCHOP).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_PORKCHOP).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_PORKCHOP).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_PORKCHOP).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.MUTTON).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.MUTTON).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.COOKED_MUTTON).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.COOKED_MUTTON).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.COOKED_MUTTON).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.CARROT).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.CARROT).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.POTATO).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.POTATO).accessor$setSaturationModifier(0.0F);

        ((ItemFoodAccessor) Items.BAKED_POTATO).accessor$setHealAmount(2);
        ((ItemFoodAccessor) Items.BAKED_POTATO).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.BAKED_POTATO).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.BREAD).accessor$setHealAmount(2);
        ((ItemFoodAccessor) Items.BREAD).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.BREAD).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.APPLE).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.APPLE).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.APPLE).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.COOKIE).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.COOKIE).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.COOKIE).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.MELON).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.MELON).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.MELON).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.ROTTEN_FLESH).accessor$setHealAmount(0);
        ((ItemFoodAccessor) Items.ROTTEN_FLESH).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.ROTTEN_FLESH).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.SPIDER_EYE).accessor$setHealAmount(0);
        ((ItemFoodAccessor) Items.SPIDER_EYE).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.SPIDER_EYE).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.POISONOUS_POTATO).accessor$setHealAmount(0);
        ((ItemFoodAccessor) Items.POISONOUS_POTATO).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.POISONOUS_POTATO).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.PUMPKIN_PIE).accessor$setHealAmount(0);
        ((ItemFoodAccessor) Items.PUMPKIN_PIE).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.PUMPKIN_PIE).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.BEETROOT).accessor$setHealAmount(1);
        ((ItemFoodAccessor) Items.BEETROOT).accessor$setSaturationModifier(0.0F);
        ((ItemFoodAccessor) Items.BEETROOT).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.RABBIT_STEW).accessor$setHealAmount(2);
        ((ItemFoodAccessor) Items.RABBIT_STEW).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.RABBIT_STEW).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.BEETROOT_SOUP).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.BEETROOT_SOUP).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.BEETROOT_SOUP).accessor$setAlwaysEdible(true);

        ((ItemFoodAccessor) Items.MUSHROOM_STEW).accessor$setHealAmount(3);
        ((ItemFoodAccessor) Items.MUSHROOM_STEW).accessor$setSaturationModifier(0.2F);
        ((ItemFoodAccessor) Items.MUSHROOM_STEW).accessor$setAlwaysEdible(true);
    }
}
