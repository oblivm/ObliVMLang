/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public class ASTDummyType extends ASTType {

	public ASTType type;
	
	public ASTDummyType(ASTType type) {
		this.type = type;
	}
	
	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTNullType) {
			return true;
		} else if (type instanceof ASTDummyType) {
			ASTDummyType dt = (ASTDummyType)type;
			return type.canFlowTo(dt.type);
		}
		return this.type.canFlowTo(type);
	}

	@Override
	public String shortName() {
		return "dummy "+type.shortName();
	}

	@Override
	public String toString(int indent) {
		return "dummy "+type.toString();
	}

	public String toString() {
		return toString(0);
	}
	
	@Override
	public ASTLabel getLabel() {
		return type.getLabel();
	}

	public boolean isNullable() {
		return true;
	}
	
	public ASTType deNull() {
		return type.deNull();
	}

}
