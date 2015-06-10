/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class Constant extends VariableConstant {
	public int value;
	
	public Constant(int v) {
		this.value = v;
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public boolean equals(VariableConstant obj) {
		if(!(obj instanceof Constant))
			return false;
		return value == ((Constant)obj).value;
	}

	@Override
	public boolean isConstant(int value) {
		return value == this.value;
	}
}
