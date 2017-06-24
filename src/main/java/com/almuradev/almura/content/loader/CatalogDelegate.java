package com.almuradev.almura.content.loader;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;

import javax.annotation.Nullable;

/**
 * Due to the nature of how the game loads content, Almura cannot always guarantee that a {@link CatalogType} is currently registered.
 * That being said, we do know that it will be eventually. This class serves as a container of sorts that keeps a hold of the class the
 * {@link CatalogType} will be and the catalog's id. Finally, the {@link CatalogType} is lazy loaded when game logic requests the catalog.
 * @param <C> The type of the catalog
 */
public final class CatalogDelegate<C extends CatalogType> {
    private final String catalogId;
    private final Class<C> catalogClass;
    @Nullable
    private C instance;

    public CatalogDelegate(Class<C> catalogClass, String catalogId) {
        this.catalogClass = catalogClass;
        this.catalogId = catalogId;
    }

    public C getCatalog() {
        if (this.instance != null) {
            return this.instance;
        }

        this.instance = Sponge.getRegistry().getType(this.catalogClass, this.catalogId).orElse(null);

        if (this.instance == null) {
            // TODO
            throw new IllegalStateException();
        }

        return this.instance;
    }
}
