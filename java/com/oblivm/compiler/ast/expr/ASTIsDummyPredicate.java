/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

public class ASTIsDummyPredicate extends ASTPredicate {
	public boolean checkDummy = true;
	
	public ASTExpression exp;
	
	public ASTIsDummyPredicate(boolean is, ASTExpression e) {
		this.checkDummy = is;
		this.exp = e;
	}

	@Override
	public int level() {
		return 100;
	}
	
	public String toString() {
		if(checkDummy) {
			return "ISDUMMY("+exp.toString()+")";
		} else 
			return "!ISDUMMY("+exp.toString()+")";
	}
}
