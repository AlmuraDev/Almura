/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura;

import com.google.common.base.Optional;

public class LogHelper {
    public static void logPackWarnOrError(String message, Optional<Throwable> optThrowable) {
        if (Configuration.DEBUG_ALL || Configuration.DEBUG_PACKS) {
            Almura.getInstance().logger.error(message, optThrowable.get());
        } else {
            Almura.getInstance().logger.warn(message);
        }
    }
}
