/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import java.util.List;

import com.oblivm.compiler.type.manage.FunctionType;
import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.VariableConstant;

public class FuncCallExp extends Expression {
	public Variable base;
	public String func;
	public boolean isNative;
	public List<VariableConstant> bitParameters;
	public List<Variable> inputs;
	public Type type;
	public Label lab;
	public FunctionType fty;
	
	public FuncCallExp(FunctionType fty, Label lab, Type returnType, Variable base, String name, 
			List<VariableConstant> bitParameters, List<Variable> inputs, boolean isNative) {
		this.bitParameters = bitParameters;
		this.fty = fty;
		this.base = base;
		this.lab = lab;
		this.func = name;
		this.inputs = inputs;
		this.type = returnType;
		this.isNative = isNative;
	}

	public FuncCallExp(FunctionType fty, Label lab, Type returnType, String name, 
			List<VariableConstant> bitParameters, List<Variable> inputs, boolean isNative) {
		this(fty, lab, returnType, null, name, bitParameters, inputs, isNative);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(base != null)
			sb.append(base.name+".");
		sb.append(func);
		sb.append("(");
		for(int i=0; i<inputs.size(); ++i) {
			if(i > 0) sb.append(", ");
			sb.append(inputs.get(i).name);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Label getLabels() {
		return lab;
	}

}
