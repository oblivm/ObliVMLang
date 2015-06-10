/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

/**
 * Extends ASTExpression. It defines a constant expression of
 * the form "n = a;" where "a" is a natural number and "n" is a 
 * variable.
 * <p>
 * <b> Member variables </b>: <p>
 * - public int value <br>
 * - public int bitSize
 */
public class ASTFloatConstantExpression extends ASTExpression {
	public double value;
	public ASTExpression bitSize;
	
	public ASTFloatConstantExpression(double v) {
		this(v, null);
	}
	
	public ASTFloatConstantExpression(double v, ASTExpression bitsize) {
		this.value = v;
		this.bitSize = bitsize;
	}
	
	public String toString() {
		return Double.toString(value);
	}
	
	public int level() {
		return 100;
	}
	
	public ASTExpression getBits() {
		return bitSize;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ASTFloatConstantExpression))
			return false;
		return value == ((ASTFloatConstantExpression)obj).value; 
	}
}
