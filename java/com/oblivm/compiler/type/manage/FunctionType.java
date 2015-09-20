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

	@Override
	public boolean writable() {
		// TODO maybe writable?
		return false;
	}
	
	@Override
	public boolean similar(Type type) {
		if(!(type instanceof FunctionType))
			return false;
		FunctionType ft = (FunctionType)type;
		if(!this.returnType.similar(ft.returnType))
			return false;
		if(typeParameters != null) {
			if(ft.typeParameters == null ||
					typeParameters.size() != ft.typeParameters.size())
				return false;
			for(int i=0; i<typeParameters.size(); ++i) {
				if(!ft.typeParameters.get(i).similar(typeParameters.get(i)))
					return false;
			}
		} else {
			if(ft.typeParameters != null && ft.typeParameters.size() != 0)
				return false;
		}
		for(int i=0; i<this.inputTypes.size(); ++i) {
			if(!ft.inputTypes.get(i).similar(inputTypes.get(i)))
				return false;
		}
		return true;
	}

}
