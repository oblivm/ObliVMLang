/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.VariableConstant;

public class Variable {
	public Label lab;
	public String name;
	public Type type;
	public boolean toHaveType = false;
	
	public Variable(Type type, Label lab, String name, boolean toHaveType) {
		if(type.getLabel() != lab) {
			throw new RuntimeException("Wrong here.");
		}
		this.type = type;
		this.lab = lab;
		this.name = name;
		this.toHaveType = toHaveType;
	}
	
	public Variable(Type type, Label lab, String name) {
		this(type, lab, name, name.startsWith("__"));
	}
	
	public VariableConstant getBits() {
		return type.getBits();
	}
	
	public String toString() {
		return name;
	}
}
