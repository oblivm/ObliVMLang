/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public class ASTNullType extends ASTType {
	private static ASTNullType inst = null;
	
	public static ASTNullType get() {
		if(inst == null)
			inst = new ASTNullType();
		return inst;
	}
	
	private ASTNullType() {
	}
	
	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTDummyType || type == this)
			return true;
		return false;
	}

	@Override
	public String shortName() {
		return "dummy *";
	}

	@Override
	public String toString(int indent) {
		return toString();
	}

	@Override
	public ASTLabel getLabel() {
		return ASTLabel.Pub;
	}

	public boolean isNullable() {
		return true;
	}

}
