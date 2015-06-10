/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class LogExp extends Expression {
	public Variable exp;
	public Label lab;
	
	public LogExp(Label lab, Variable exp) {
		this.exp = exp;
		this.lab = lab;
	}
	
	@Override
	public String toString() {
		return "log("+this.exp+")";
	}

	@Override
	public Label getLabels() {
		return lab;
	}

}
