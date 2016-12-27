package com.almuradev.almura.mixin.entity.ai;

import java.util.Random;

import com.almuradev.almura.extension.animal.IMixinEntityAnimal;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityAIMate.class)
public abstract class MixinEntityAIMate extends EntityAIBase {

    @Shadow private EntityAnimal theAnimal;
    @Shadow World theWorld;
    @Shadow private EntityAnimal targetMate;

    @Overwrite
    private void spawnBaby() {

        EntityAgeable entityageable = this.theAnimal.createChild(this.targetMate);

        if (entityageable != null) {
            EntityPlayer entityplayer = this.theAnimal.func_146083_cb();

            if (entityplayer == null && this.targetMate.func_146083_cb() != null) {
                entityplayer = this.targetMate.func_146083_cb();
            }

            if (entityplayer != null) {
                entityplayer.triggerAchievement(StatList.animalsBredStat);

                if (this.theAnimal instanceof EntityCow) {
                    entityplayer.triggerAchievement(AchievementList.breedCow);
                }
            }

            this.theAnimal.setGrowingAge(6000);
            this.targetMate.setGrowingAge(6000);
            this.theAnimal.resetInLove();
            this.targetMate.resetInLove();

            IMixinEntityAnimal theAnimal = (IMixinEntityAnimal) this.theAnimal;
            IMixinEntityAnimal targetMate = (IMixinEntityAnimal) this.targetMate;
            if (theAnimal.canSpawnTwin() && targetMate.canSpawnTwin()) {
                Random rn = new Random();
                int chance = rn.nextInt(2) + 1;
                if (chance == 1) {
                    EntityAgeable entityTwin = this.theAnimal.createChild(this.targetMate);
                    entityTwin.setGrowingAge(-24000);
                    entityTwin.setLocationAndAngles(this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, 0.0F, 0.0F);
                    this.theWorld.spawnEntityInWorld(entityTwin);

                    theAnimal.setCanSpawnTwin(false);
                    targetMate.setCanSpawnTwin(false);
                }
            }

            entityageable.setGrowingAge(-24000);
            entityageable.setLocationAndAngles(this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, 0.0F, 0.0F);
            this.theWorld.spawnEntityInWorld(entityageable);

            Random random = this.theAnimal.getRNG();

            for (int i = 0; i < 7; ++i)
            {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                this.theWorld.spawnParticle("heart", this.theAnimal.posX + (double)(random.nextFloat() * this.theAnimal.width * 2.0F) - (double)this.theAnimal.width, this.theAnimal.posY + 0.5D + (double)(random.nextFloat() * this.theAnimal.height), this.theAnimal.posZ + (double)(random.nextFloat() * this.theAnimal.width * 2.0F) - (double)this.theAnimal.width, d0, d1, d2);
            }

            if (this.theWorld.getGameRules().getGameRuleBooleanValue("doMobLoot"))
            {
                this.theWorld.spawnEntityInWorld(new EntityXPOrb(this.theWorld, this.theAnimal.posX, this.theAnimal.posY, this.theAnimal.posZ, random.nextInt(7) + 1));
            }
        }
    }
}