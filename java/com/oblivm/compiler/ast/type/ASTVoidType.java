/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public class ASTVoidType extends ASTType {
	
	private static ASTVoidType inst = null;
	
	public static ASTVoidType get() {
		if(inst == null)
			inst = new ASTVoidType();
		return inst;
	}
	
	private ASTVoidType() {}
	
	@Override
	public String toString(int indent) {
		return toString();
	}

	public String toString() {
		return "void";
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		return type instanceof ASTVoidType;
	}

	@Override
	public String shortName() {
		return toString();
	}

	@Override
	public ASTLabel getLabel() {
		return ASTLabel.Pub;
	}
}
