/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

public class Skip extends IRCode {

	@Override
	public String toString(int indent) {
		return "";
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		return new Skip();
	}

}
