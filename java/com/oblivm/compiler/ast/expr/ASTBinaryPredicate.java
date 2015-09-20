/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

/**
 * Defines a binary predicate of the form "(left expression) REL_OP (right expression)", 
 * where left and right are members of type ASTExpression, and REL_OP is an instance of the static 
 * class REL_OP which defines the following operations: equals ("=="), negation ("!="), 
 * greater than (">"), greater than or equal to (">="), less than ("<"), less than or equal to ("<=").
 */
public class ASTBinaryPredicate extends ASTPredicate {
	public static class REL_OP {
		public static REL_OP EQ = new REL_OP();
		public static REL_OP NEQ = new REL_OP();
		public static REL_OP GT = new REL_OP();
		public static REL_OP GET = new REL_OP();
		public static REL_OP LT = new REL_OP();
		public static REL_OP LET = new REL_OP();
		
		private REL_OP() {}
		
		public String toString() {
			if(this == EQ) return "==";
			else if(this == NEQ) return "!=";
			else if(this == GT) return ">";
			else if(this == GET) return ">=";
			else if(this == LT) return "<";
			else if(this == LET) return "<=";
			else return "error";
		}
	}
	
	public ASTExpression left;
	public ASTExpression right;
	public REL_OP op;
	
	public ASTBinaryPredicate(ASTExpression left, REL_OP op, ASTExpression right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	public String toString() {
		return left+op.toString()+right;
	}

	@Override
	public int level() {
		return 2;
	}

	public ASTBinaryPredicate cloneInternal() {
		return new ASTBinaryPredicate(left.clone(), op, right.clone());
	}
}
