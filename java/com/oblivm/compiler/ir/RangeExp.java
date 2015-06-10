/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.VariableConstant;

public class RangeExp extends Expression {
	public Variable base;
	public Label lab;
	public VariableConstant rl, rr;
	
	public RangeExp(Label lab, Variable base, VariableConstant rl, VariableConstant rr) {
		this.base = base;
		this.rl = rl;
		this.rr = rr;
		this.lab = lab;
	}
	
	@Override
	public String toString() {
		return base.name+".$"+rl+(rr == null ? "$" : "~"+rr+"$");
	}

	@Override
	public Label getLabels() {
		return lab;
	}

}
