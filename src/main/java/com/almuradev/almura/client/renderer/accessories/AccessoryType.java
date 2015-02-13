/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.client.renderer.accessories;

import java.util.HashMap;
import java.util.Map;

public enum AccessoryType {
	TOPHAT(1),
	NOTCHHAT(2),
	BRACELET(3),
	WINGS(4),
	EARS(5),
	SUNGLASSES(6),
	TAIL(7),
	WIZARDHAT(8);
	private final int id;
	private static Map<Integer, AccessoryType> types = new HashMap<Integer, AccessoryType>();

	static {
		for (AccessoryType type : AccessoryType.values()) {
			types.put(type.getId(), type);
		}
	}

	private AccessoryType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static AccessoryType get(int id) {
		return types.get(id);
	}
}
