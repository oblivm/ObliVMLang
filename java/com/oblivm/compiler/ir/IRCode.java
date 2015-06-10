/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

/**
 * Top level class for the target language. Array assignment, assignment, 
 * if, sequence, skip, and while statements inherit from this class.
 */
public abstract class IRCode {
	public abstract String toString(int indent);
	
	public boolean withTypeDef = true;
	
	public abstract IRCode clone(boolean withTypeDef);
	
	public String indent(int n) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<n; ++i) sb.append("\t");
		return sb.toString();
	}
	
	public String toString() {
		return indent(0);
	}
}