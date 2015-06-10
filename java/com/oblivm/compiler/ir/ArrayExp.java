/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class ArrayExp extends Expression {

	public Variable arr;
	public Variable idx;
	
	public ArrayExp(Variable arr, Variable left) {
		this.arr = arr;
		this.idx = left;
	}

	@Override
	public String toString() {
		return arr.name+"["+idx.toString()+"]";
	}

	@Override
	public Label getLabels() {
		return arr.lab;
	}

}
