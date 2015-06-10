/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class CheckNullExp extends Expression {
	public boolean checkEq;
	public Variable var;
	
	public CheckNullExp(boolean checkEq, Variable var) {
		this.checkEq = checkEq;
		this.var = var;
	}
	
	@Override
	public String toString() {
		return checkEq ? var + " == null" : var + " != null";
	}

	@Override
	public Label getLabels() {
		return var.lab;
	}

}
