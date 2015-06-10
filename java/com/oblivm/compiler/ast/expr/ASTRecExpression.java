/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

public class ASTRecExpression extends ASTExpression {
	public ASTExpression base;
	public String field;
	
	public ASTRecExpression(ASTExpression base, String field) {
		this.base = base;
		this.field = field;
	}
	
	@Override
	public int level() {
		return 100;
	}
	
	public String toString() {
		return base.toString() + "." + field;
	}

}
