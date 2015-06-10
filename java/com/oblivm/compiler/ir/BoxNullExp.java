/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class BoxNullExp extends Expression {
	public Variable var;
	
	public BoxNullExp(Variable exp) {
		this.var = exp;
	}

	@Override
	public String toString() {
		return "dummy("+var+")";
	}

	@Override
	public Label getLabels() {
		return var.lab;
	}
}
