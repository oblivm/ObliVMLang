/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;


public class Ret extends IRCode {
	public Variable exp;

	public Ret(Variable exp) {
		this.exp = exp;
	}
	
	@Override
	public String toString() {
		return "return "+exp+";";
	}

	@Override
	public String toString(int indent) {
		return this.indent(indent)+toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		return new Ret(exp);
	}

	
}
