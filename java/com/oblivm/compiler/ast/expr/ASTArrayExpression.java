/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

/**
 * Defines an array expression.<br>
 * <b>Members:</b><p>
 * - public ASTVariable var;<br>
 * - public ASTExpression indexExpr;<br>
 */
public class ASTArrayExpression extends ASTExpression {
	public ASTExpression var;
	public ASTExpression indexExpr;
	
	public ASTArrayExpression(ASTExpression v, ASTExpression e) {
		this.var = v;
		this.indexExpr = e;
	}

	@Override
	public String toString() {
		return var+"["+indexExpr.toString(0)+"]";
	}
	
	public int level() {
		return 100;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ASTArrayExpression))
			return false;
		ASTArrayExpression array = (ASTArrayExpression)obj;
		return var.equals(array.var) && indexExpr.equals(array.indexExpr);
	}
}
