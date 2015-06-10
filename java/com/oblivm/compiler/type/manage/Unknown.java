/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class Unknown extends VariableConstant {
	public String toString() {
		return "unknown";
	}
	
	public boolean equals(VariableConstant other) {
		return other instanceof Unknown;
	}

	@Override
	public boolean isConstant(int value) {
		return false;
	}
}
