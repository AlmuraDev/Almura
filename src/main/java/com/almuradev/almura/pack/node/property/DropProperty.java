package com.almuradev.almura.pack.node.property;


import net.minecraft.item.Item;

public class DropProperty extends ItemProperty {

    private final BonusProperty<Integer, Integer> bonusProperty;

    public DropProperty(Item item, int amount, int damage, BonusProperty<Integer, Integer> bonusProperty) {
        super(item, amount, damage);
        this.bonusProperty = bonusProperty;
    }

    public BonusProperty<Integer, Integer> getBonus() {
        return bonusProperty;
    }
}
