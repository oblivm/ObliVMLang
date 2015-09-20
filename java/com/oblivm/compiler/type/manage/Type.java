/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import java.util.ArrayList;
import java.util.List;

public abstract class Type {
	public String name;
	private List<Method> methods;
	
	public boolean isStatic = false;
	
	public Type(String name, Method ... methods) {
		this.name = name;
		this.methods = new ArrayList<Method>();
		for(Method met : methods)
			this.methods.add(met);
	}
	
	public void addMethod(Method method) {
		this.methods.add(method);
	}
	
	public List<Method> getMethods() {
		return methods;
	}
	
	public String toString() {
		return name;
	}

	public abstract VariableConstant getBits();
	
	public abstract Label getLabel();

	public abstract boolean constructable();

	public abstract boolean writable();

	public abstract boolean similar(Type type);
	
	public boolean rawType() {
		return false;
	}
}
