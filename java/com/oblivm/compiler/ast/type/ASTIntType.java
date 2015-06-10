/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;

public class ASTIntType extends ASTType {

	private ASTExpression bit;
	private ASTLabel lab;

	public ASTExpression getBits() {
		return bit;
	}

	public ASTLabel getLabel() {
		return lab;
	}

	public static ASTIntType get(int bit, ASTLabel lab) {
		return ASTIntType.get(new ASTConstantExpression(bit), lab);
	}

	public static ASTIntType get(ASTExpression bit, ASTLabel lab) {
		return new ASTIntType(bit, lab);
	}

	private ASTIntType(ASTExpression bit, ASTLabel lab) {
		this.bit = bit;
		this.lab = lab;
	}

	public String toString(int indent) {
		return toString();
	}

	public String toString() {
		if(bit == null)
			return lab.toString() + " " + "int";
		else
			return lab.toString() + " " + "int"+(bit instanceof ASTConstantExpression ? bit.toString() : "@("+bit.toString()+")");
	}

	public boolean equals(Object obj) {
		if(!(obj instanceof ASTIntType))
			return false;
		ASTIntType other = (ASTIntType)obj;
		return bit.equals(other.bit) && lab == other.lab;
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTDummyType)
			return canFlowTo(((ASTDummyType)type).type);
		if(type instanceof ASTRndType) {
			// Integer cannot flow to Random Type
			return false;
		} else if(type instanceof ASTIntType) {
			ASTIntType it = (ASTIntType)type;
			return this.lab.less(it.lab);
		}
		return false;
	}

	@Override
	public String shortName() {
		return toString();
	}
}
