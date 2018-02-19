/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.item.type.food;

import com.almuradev.content.component.apply.Apply;
import com.almuradev.content.component.apply.context.ItemOnlyApplyContext;
import com.almuradev.content.component.apply.impl.FoodLevelChangeApply;
import com.almuradev.content.component.apply.impl.SaturationChangeApply;
import com.almuradev.content.type.item.ItemTooltip;
import com.almuradev.content.type.item.type.food.processor.foodeffect.FoodEffect;
import com.almuradev.content.type.item.type.food.processor.foodeffect.potioneffect.PotionEffectTemplate;
import com.almuradev.toolbox.util.math.DoubleRange;
import com.almuradev.toolbox.util.math.IntRange;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public final class FoodItemImpl extends ItemFood implements FoodItem {
    private final ItemTooltip tooltip = new ItemTooltip.Impl(this);
    private final List<Apply> apply;
    private final FoodEffect foodEffect;
    private IntRange foodLevelChange;
    private DoubleRange saturationChange;

    public FoodItemImpl(final FoodItemBuilder builder) {
        super(0, 0f, false); // We'll handle these values on our own

        this.tabToDisplayOn = null;

        builder.fill(this);
        this.itemUseDuration = builder.durationTicks;

        this.apply = builder.apply;
        this.foodEffect = builder.foodEffect;

        if (this.apply != null) {
            for (final Apply apply : this.apply) {
                if (apply instanceof FoodLevelChangeApply) {
                    this.foodLevelChange = ((FoodLevelChangeApply) apply).getChange();
                } else if (apply instanceof SaturationChangeApply) {
                    this.saturationChange = ((SaturationChangeApply) apply).getChange();
                }
            }
        }

        if (builder.alwaysEdible) {
            this.setAlwaysEdible();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<String> list, final ITooltipFlag flag) {
        this.tooltip.render(list);
    }

    @Override
    public ItemStack onItemUseFinish(final ItemStack stack, final World worldIn, final EntityLivingBase entityLiving) {
        if (worldIn.isRemote) {
            return stack;
        }

        if (this.apply != null) {
            for (final Apply apply : this.apply) {
                apply.apply(entityLiving, new ItemOnlyApplyContext(stack, worldIn.rand));
            }
        }

        if (entityLiving instanceof EntityPlayer) {
            final EntityPlayer entityplayer = (EntityPlayer)entityLiving;
            //entityplayer.getFoodStats().addStats(this, stack); -- Handled by the applys
            worldIn.playSound((EntityPlayer)null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, worldIn.rand.nextFloat() * 0.1F + 0.9F);
            this.onFoodEaten(stack, worldIn, entityplayer);
            entityplayer.addStat(StatList.getObjectUseStats(this));

            if (entityplayer instanceof EntityPlayerMP)
            {
                CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP)entityplayer, stack);
            }
        }

        stack.shrink(1);
        return stack;
    }

    @Override
    protected void onFoodEaten(final ItemStack stack, final World worldIn, final EntityPlayer player) {
        if (worldIn.isRemote || this.foodEffect == null) {
            return;
        }

        final Random random = worldIn.rand;

        this.foodEffect.getPotionEffects().forEach(((potion, template) -> {
            // Run chance calculations
            if (random.nextDouble() <= (template.getChanceRange().random(random) / 100)) {
                final PotionEffectTemplate potionEffectTemplate = template.getPotionEffect();

                final PotionEffect potionEffect = new PotionEffect(potion, potionEffectTemplate.getDuration(), potionEffectTemplate.getAmplifier(),
                        potionEffectTemplate.isAmbient(), potionEffectTemplate.isShowParticles());

                potionEffectTemplate.getCuratives().forEach((curative) -> potionEffect.addCurativeItem(curative.create()));

                player.addPotionEffect(potionEffect);
            }
        }));
    }

    @Override
    public int getMaxItemUseDuration(final ItemStack stack) {
        return this.itemUseDuration;
    }

    // TODO I'm doing it this way to ensure compatibility with mods
    @Override
    public int getHealAmount(final ItemStack stack) {
        if (this.foodLevelChange == null) {
            return 0;
        }

        return this.foodLevelChange.random(new Random()); // TODO Cache a random elsewhere lol?
    }

    @Override
    public float getSaturationModifier(final ItemStack stack) {
        if (this.saturationChange == null) {
            return 0;
        }

        return (float) this.saturationChange.random(new Random()); // TODO Cache a random elsewhere lol?
    }
}
