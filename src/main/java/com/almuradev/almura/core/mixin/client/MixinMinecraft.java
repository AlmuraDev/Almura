package com.almuradev.almura.core.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
    @Inject(method = "runGameLoop", at = @At("HEAD"))
    public void mixinTest(CallbackInfo ci) {
        System.err.println("Mixin has been bootstrapped");
    }
}
