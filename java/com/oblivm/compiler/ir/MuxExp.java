/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;


public class MuxExp extends Expression {

	public Variable b;
	public Variable x1, x2; // true => x1; false => x2;
	
	public MuxExp(Variable b, Variable tr, Variable fs) {
		if(!b.getBits().isConstant(1))
			throw new RuntimeException("The cond parameter should be 1.");
		this.b = b;
		this.x1 = tr;
		this.x2 = fs;
	}

	@Override
	public String toString() {
		return b.name+" ? "+x1.name+", "+x2.name;
	}

	@Override
	public Label getLabels() {
		return b.lab.meet(x1.lab).meet(x2.lab);
	}

}
