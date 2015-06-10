/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;

public class ASTRndType extends ASTType {
	
	private ASTExpression bit;
	private ASTLabel lab;
	
	public ASTExpression getBits() {
		return bit;
	}

	public ASTLabel getLabel() {
		return lab;
	}
	
	public static ASTRndType get(ASTExpression bit, ASTLabel lab) {
		return new ASTRndType(bit, lab);
	}
	
	private ASTRndType(ASTExpression bit, ASTLabel lab) {
		this.bit = bit;
		this.lab = ASTLabel.Secure; // Random Type must be secure
	}
	
	public String toString(int indent) {
		return toString();
	}
	
	public String toString() {
		if(bit == null)
			return "rnd";
		else
			return "rnd"+(bit instanceof ASTConstantExpression ? bit.toString() : "@("+bit.toString()+")");
	}
	
//	public int hashCode() {
//		return bit. * ASTLabel.getLabelNumber() + lab.getId();  
//	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ASTRndType))
			return false;
		ASTRndType other = (ASTRndType)obj;
		return bit.equals(other.bit) && lab == other.lab;
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTDummyType)
			return canFlowTo(((ASTDummyType)type).type);

		if(type instanceof ASTIntType)
			return true;
		
		if(!(type instanceof ASTRndType))
			return false;
		ASTRndType it = (ASTRndType)type;
		if(bit.equals(it.bit) || bit == null || it.bit == null)
			return true;
		return false;
	}

	@Override
	public String shortName() {
		return toString();
	}
}
