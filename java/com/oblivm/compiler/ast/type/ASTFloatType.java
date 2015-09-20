/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;

public class ASTFloatType extends ASTType {
	
	public ASTExpression bit;
	private ASTLabel lab;
	
	public ASTExpression getBits() {
		return bit;
	}

	public ASTLabel getLabel() {
		return lab;
	}
	
	public static ASTFloatType get(ASTExpression bit, ASTLabel lab) {
		return new ASTFloatType(bit, lab);
	}
	
	public static ASTFloatType get(int bit, ASTLabel lab) {
		return get(new ASTConstantExpression(bit), lab);
	}
	
	private ASTFloatType(ASTExpression bit, ASTLabel lab) {
		this.bit = bit;
		this.lab = lab;
	}
	
	public String toString(int indent) {
		return toString();
	}
	
	public String toString() {
		if(bit == null)
			return lab.toString() + " " + "float";
		else
			return lab.toString() + " " + "float"+(bit instanceof ASTConstantExpression ? bit.toString() : "@("+bit.toString()+")");
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ASTFloatType))
			return false;
		ASTFloatType other = (ASTFloatType)obj;
		return bit.equals(other.bit) && lab == other.lab;
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTDummyType)
			return canFlowTo(((ASTDummyType)type).type);
		
		if(!(type instanceof ASTFloatType))
			return false;
		ASTFloatType it = (ASTFloatType)type;
		if(bit == null || bit.equals(it.bit) || it.bit == null)
			return this.lab.less(it.lab);
		return false;
	}

	@Override
	public String shortName() {
		return toString();
	}
}
