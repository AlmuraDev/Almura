package com.almuradev.almura.content.item;

import org.spongepowered.api.Sponge;

public interface SeedType extends BuildableItemType {

    static SeedType.Builder builder() {
        return Sponge.getRegistry().createBuilder(SeedType.Builder.class);
    }

    interface Builder extends BuildableItemType.Builder<SeedType, SeedType.Builder> {

    }
}
