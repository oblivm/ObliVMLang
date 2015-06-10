/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends Type {

	public List<String> bitParameters;
	
	public FunctionType(Type returnType, String name, List<Type> inputs, 
			List<String> bitParameters, List<Type> typeParameters, boolean global) {
		super(name);
		this.name = name;
		this.returnType = returnType;
		this.inputTypes = inputs;
		this.typeParameters = typeParameters;
		this.bitParameters = bitParameters;
		this.global = global;
	}

	public Type returnType;
	public String name;
	public List<Type> inputTypes = new ArrayList<Type>();
	public List<Type> typeParameters;
	public boolean global; 
	
	@Override
	public VariableConstant getBits() {
		return null;
	}

	@Override
	public Label getLabel() {
		return Label.Pub;
	}

	@Override
	public boolean constructable() {
		// TODO maybe constructable?
		return false;
	}

}
