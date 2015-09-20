/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import java.util.List;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.Type;

public class NativeFuncCallExp extends Expression {
	public Variable base;
	public String nativeName;
	public List<Variable> inputs;
	public Type type;
	public Label lab;
	
	public NativeFuncCallExp(Label lab, Type returnType, Variable base, String nativeName, List<Variable> inputs) {
		this.base = base;
		this.lab = lab;
		this.nativeName = nativeName;
		this.inputs = inputs;
		this.type = returnType;
	}

	public NativeFuncCallExp(Label lab, Type returnType, String name, List<Variable> inputs) {
		this(lab, returnType, null, name, inputs);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(base.name != null)
			sb.append(base.name+".");
		sb.append(nativeName);
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
