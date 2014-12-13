package com.almuradev.almura.pack.node;

public class ConsumptionNode implements INode<Boolean> {
    private final boolean isEnabled;
    private final float heal;
    private final float saturation;
    private final boolean alwaysEdible, wolfFavorite;

    public ConsumptionNode(boolean isEnabled, float heal, float saturation, boolean alwaysEdible, boolean wolfFavorite) {
        this.isEnabled = isEnabled;
        this.heal = heal;
        this.saturation = saturation;
        this.alwaysEdible = alwaysEdible;
        this.wolfFavorite = wolfFavorite;
    }

    @Override
    public Boolean getValue() {
        return isEnabled;
    }

    public float getHeal() {
        return heal;
    }

    public float getSaturation() {
        return saturation;
    }

    public boolean isAlwaysEdible() {
        return alwaysEdible;
    }

    public boolean isWolfFavorite() {
        return wolfFavorite;
    }
}
