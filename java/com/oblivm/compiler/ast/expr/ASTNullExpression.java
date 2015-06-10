/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

public class ASTNullExpression extends ASTExpression {

	@Override
	public int level() {
		return 100;
	}
	
	public String toString() {
		return "null";
	}
}
