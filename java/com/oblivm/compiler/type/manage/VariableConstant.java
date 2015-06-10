/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public abstract class VariableConstant {
	public abstract boolean isConstant(int value);
	public abstract boolean equals(VariableConstant c);
}
