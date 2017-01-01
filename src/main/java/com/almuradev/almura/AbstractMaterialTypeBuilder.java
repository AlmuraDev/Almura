package com.almuradev.almura;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.almura.creativetab.CreativeTab;

import java.util.Optional;

public abstract class AbstractMaterialTypeBuilder<MATERIAL extends MaterialType, BUILDER extends AbstractMaterialTypeBuilder<MATERIAL, BUILDER>>
        implements MaterialType.Builder<MATERIAL, BUILDER> {

    private CreativeTab creativeTab;

    public AbstractMaterialTypeBuilder() {
        this.reset();
    }

    @Override
    public final BUILDER creativeTab(CreativeTab creativeTab) {
        this.creativeTab = creativeTab;
        return (BUILDER) this;
    }

    @Override
    public final Optional<CreativeTab> creativeTab() {
        return Optional.ofNullable(this.creativeTab);
    }

    @Override
    public BUILDER from(MaterialType value) {
        checkNotNull(value);
        this.creativeTab = value.getCreativeTab().orElse(null);
        return (BUILDER) this;
    }

    @Override
    public BUILDER reset() {
        this.creativeTab = null;
        return (BUILDER) this;
    }
}
