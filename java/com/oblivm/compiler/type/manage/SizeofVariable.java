/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class SizeofVariable extends VariableConstant {
	public Type type;
	
	public SizeofVariable(Type type) {
		this.type = type;
	}
	
	public String toString() {
		return "sizeof("+type+")";
	}
	
	public boolean equals(VariableConstant obj) {
		if(!(obj instanceof SizeofVariable))
			return false;
		return type.equals(((SizeofVariable)obj).type);
	}

	@Override
	public boolean isConstant(int value) {
		return false;
	}


}
