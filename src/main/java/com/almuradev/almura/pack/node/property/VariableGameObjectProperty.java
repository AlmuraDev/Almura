package com.almuradev.almura.pack.node.property;

public class VariableGameObjectProperty extends GameObjectProperty {
    private final RangeProperty<Integer> amountProperty;

    public VariableGameObjectProperty(Object object, RangeProperty<Integer> amount, int data) {
        super(object, data);
        this.amountProperty = amount;
    }

    public RangeProperty<Integer> getAmountProperty() {
        return amountProperty;
    }
}
