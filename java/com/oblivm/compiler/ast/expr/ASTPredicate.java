/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

public abstract class ASTPredicate extends ASTExpression {
	public String toString(int indent) {
		return toString();
	}
	
	public abstract ASTPredicate cloneInternal();
	
	public ASTPredicate clone() {
		ASTPredicate ret = cloneInternal();
		ret.setBeginPosition(this.beginPosition);
		ret.setEndPosition(this.endPosition);
		return ret;
	}
}
