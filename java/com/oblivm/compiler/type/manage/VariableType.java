/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

public class VariableType extends Type {

	public VariableType(String name) {
		super(name);
	}

	@Override
	public VariableConstant getBits() {
		return new Unknown();
	}

	@Override
	public Label getLabel() {
		return Label.Secure;
	}

	@Override
	public boolean constructable() {
		return true;
	}

	@Override
	public boolean writable() {
		return true;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof VariableType))
			return false;
		return name.equals(((VariableType)obj).name);
	}
	
	public boolean similar(Type type) {
		return type instanceof VariableType;
	}
}
