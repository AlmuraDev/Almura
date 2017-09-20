/*
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.asm.mixin.core.entity;

import net.minecraft.entity.EntityList;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityList.class)
public class MixinEntityList {
    @Redirect(method = "createEntityFromNBT", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private static void silenceNonExistantEntity(Logger logger, String message, Object p0) {
        //This Mixin is here to silence the feedback o.
    }
}