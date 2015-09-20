/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.ArrayList;
import java.util.List;

public class NativeType extends Type {

	public String nativeName;
	public List<VariableType> typeVariables;
	
	public List<VariableConstant> constructor = null;
	
	public NativeType(String name, String nativeName, List<VariableConstant> constructor, Method... methods) {
		super(name, methods);
		this.nativeName = nativeName;
		this.constructor = constructor;
		this.typeVariables = new ArrayList<VariableType>();
	}

	@Override
	public VariableConstant getBits() {
		return null;
	}

	@Override
	public Label getLabel() {
		return Label.Secure;
	}

	@Override
	public boolean constructable() {
		return constructor != null;
	}

	@Override
	public boolean writable() {
		return false;
	}
	
	@Override
	public boolean similar(Type type) {
		if(!(type instanceof NativeType))
			return false;
		NativeType nt = (NativeType)type;
		return this.name.equals(nt.name);
	}

}
