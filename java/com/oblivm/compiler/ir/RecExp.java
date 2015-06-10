/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class RecExp extends Expression {
	public Variable base;
	public String field;
	public Label lab;
	
	public RecExp(Label lab, Variable base, String field) {
		this.base = base;
		this.field = field;
		this.lab = lab;
	}
	
	@Override
	public String toString() {
		return base.name+"."+field;
	}

	@Override
	public Label getLabels() {
		return lab;
	}

}
