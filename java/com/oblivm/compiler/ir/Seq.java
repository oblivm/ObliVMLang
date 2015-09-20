/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

public class Seq extends IRCode {
	public IRCode s1, s2;

	private Seq(IRCode s1, IRCode s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	
	public static IRCode seq(IRCode s1, IRCode s2) {
		if(s1 instanceof Skip)
			return s2;
		if(s2 instanceof Skip)
			return s1;
		return new Seq(s1, s2);
	}
	
	@Override
	public String toString(int indent) {
		return s1.toString(indent)+s2.toString(indent);
	}

	public String toString() {
		return toString(0);
	}
	
	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new Seq(s1.clone(withTypeDef), s2.clone(withTypeDef));
		ret.withTypeDef = withTypeDef;
		return ret;
	}
}
