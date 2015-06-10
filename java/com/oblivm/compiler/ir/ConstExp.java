/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.VariableConstant;

public class ConstExp extends Expression {
	public int n;
	public double v;
	public VariableConstant bits;
	public boolean isInt;
	
	public ConstExp(int value, VariableConstant bits) {
		this.n = value;
		this.bits = bits;
		this.isInt = true;
	}

	public ConstExp(double value, VariableConstant bits) {
		this.v = value;
		this.bits = bits;
		this.isInt = false;
	}
	
	@Override
	public String toString() {
		return isInt ? Integer.toString(n) : Double.toString(v);
	}

	@Override
	public Label getLabels() {
		return Label.Pub;
	}

}
