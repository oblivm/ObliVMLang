/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class BitVariable extends VariableConstant {
	public String var;
	
	public BitVariable(String v) {
		this.var = v;
	}
	
	public String toString() {
		return var;
	}
	
	public boolean equals(VariableConstant obj) {
		if(!(obj instanceof BitVariable))
			return false;
		return var.equals(((BitVariable)obj).var);
	}

	@Override
	public boolean isConstant(int value) {
		return false;
	}

}
