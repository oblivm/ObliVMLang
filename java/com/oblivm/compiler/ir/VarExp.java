/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class VarExp extends Expression {
	public Variable var;
	
	public VarExp(Variable var) {
		this.var = var;
	}
	
	public String toString() {
		return var.toString();
	}

	@Override
	public Label getLabels() {
		return var.lab;
	}
}
