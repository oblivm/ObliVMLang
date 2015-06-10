/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import java.util.List;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Type;

public class LocalFuncCallExp extends Expression {
	public String func;
	public List<Variable> inputs;
	public Type type;
	
	public LocalFuncCallExp(Type returnType, String name, List<Variable> inputs) {
		this.func = name;
		this.inputs = inputs;
		this.type = returnType;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
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
		return type.getLabel();
	}

}
