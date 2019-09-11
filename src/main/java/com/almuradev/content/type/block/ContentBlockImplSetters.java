/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.content.type.block;

import net.kyori.mu.Maybe;

public interface ContentBlockImplSetters {
  void cl_topSolid(final Maybe<Boolean> topSolid);
  void cl_fullBlock(final Maybe<Boolean> fullBlock);
  void cl_normalCube(final Maybe<Boolean> normalCube);
}
