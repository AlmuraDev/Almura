/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.shared.client.integration;

import net.minecraft.client.gui.inventory.GuiContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NEI 2.4.3 with JEI 4.15: NEI finds the screen areas JEI's advanced gui handlers (other mods' side tabs and the like) cover by calling
 * JeiRuntime.getActiveAdvancedGuiHandlers, which JEI 4.15 replaced with a private method on its GuiScreenHelper. Every container screen then
 * logged a NoSuchMethodError and NEI's item panel stopped avoiding those areas. {@code NeiJeiProxyTransformer} points NEI's call here, which
 * reaches the new method by reflection, so Almura needs neither mod to build. A JEI that doesn't match gives no handlers (logged once).
 *
 * <p>Client only, but not {@code @SideOnly}: it's only ever called from NEI, which is client only.</p>
 */
public final class JeiAdvancedGuiAreas {

    private static final Logger LOGGER = LogManager.getLogger("Almura");

    private static boolean resolved;
    private static boolean failed;
    private static Method getIngredientListOverlay;
    private static Field guiScreenHelper;
    private static Method getActiveAdvancedGuiHandlers;
    private static Class<?> handlerInterface;

    private JeiAdvancedGuiAreas() {
    }

    /**
     * Gets JEI's advanced gui handlers active for the screen. Called by NEI's JEIProxy in place of
     * JeiRuntime.getActiveAdvancedGuiHandlers(GuiContainer), with the same arguments.
     *
     * @param runtime JEI's runtime
     * @param gui the screen
     * @return the handlers, empty if JEI doesn't match
     */
    public static List<?> getActiveAdvancedGuiHandlers(final Object runtime, final GuiContainer gui) {
        if (failed) {
            return Collections.emptyList();
        }
        try {
            resolve();
            final Object overlay = getIngredientListOverlay.invoke(runtime);
            if (overlay == null) {
                return Collections.emptyList();
            }
            final List<?> handlers = (List<?>) getActiveAdvancedGuiHandlers.invoke(guiScreenHelper.get(overlay), gui);
            final List<Object> safe = new ArrayList<>(handlers.size());
            for (final Object handler : handlers) {
                safe.add(guarded(handler));
            }
            return safe;
        } catch (final Throwable t) {
            failed = true;
            LOGGER.error("Couldn't read JEI's gui handlers for NEI; NEI's item panel won't avoid the areas they cover", t);
            return Collections.emptyList();
        }
    }

    /**
     * Wraps the handler so a handler failing to give its areas gives none (NEI skips null) instead of throwing: NEI asks while the screen
     * is still initializing (GuiScreenEvent.InitGuiEvent.Pre), before e.g. the inventory's recipe book exists, whose JEI handler then throws
     * a NullPointerException and NEI dropped every handler's areas for that layout. JEI itself only asks once screens are set up.
     *
     * @param handler the handler
     * @return the wrapped handler
     */
    private static Object guarded(final Object handler) {
        final Class<?> iface = handlerInterface;
        return Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, (proxy, method, args) -> {
            try {
                return method.invoke(handler, args);
            } catch (final InvocationTargetException e) {
                if ("getGuiExtraAreas".equals(method.getName())) {
                    return null;
                }
                throw e.getCause();
            }
        });
    }

    private static void resolve() throws ReflectiveOperationException {
        if (resolved) {
            return;
        }
        handlerInterface = Class.forName("mezz.jei.api.gui.IAdvancedGuiHandler");
        getIngredientListOverlay = Class.forName("mezz.jei.runtime.JeiRuntime").getMethod("getIngredientListOverlay");
        guiScreenHelper = Class.forName("mezz.jei.gui.overlay.IngredientListOverlay").getDeclaredField("guiScreenHelper");
        guiScreenHelper.setAccessible(true);
        getActiveAdvancedGuiHandlers = Class.forName("mezz.jei.gui.GuiScreenHelper").getDeclaredMethod("getActiveAdvancedGuiHandlers",
            GuiContainer.class);
        getActiveAdvancedGuiHandlers.setAccessible(true);
        resolved = true;
    }
}
