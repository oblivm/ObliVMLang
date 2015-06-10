/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

/**
 * Defines an "and" predicate of the form "(left predicate) AND (right predicate)". 
 * Left and right predicate are both stored as member variables of type ASTPredicate.
 */
public class ASTAndPredicate extends ASTPredicate {
	public ASTPredicate left;
	public ASTPredicate right;
	
	public ASTAndPredicate(ASTPredicate left, ASTPredicate right) {
		this.left = left;
		this.right = right;
	}
	
	public String toString() {
		return "("+left+") and ("+right+")";
	}

	@Override
	public int level() {
		return 1;
	}
}
