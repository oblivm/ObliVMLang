/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.VariableConstant;

public class EnforceBitExp extends Expression {
	public Variable v;
	public VariableConstant bits;
	
	public EnforceBitExp(Variable v, VariableConstant bits) {
		this.v = v;
		this.bits = bits;
	}
	
	@Override
	public String toString() {
		return v+" :> int@"+bits;
	}

	@Override
	public Label getLabels() {
		return v.lab;
	}

}
