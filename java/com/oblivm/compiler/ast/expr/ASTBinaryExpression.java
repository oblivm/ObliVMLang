/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

/**
 * <b> public ASTBinaryExpression(ASTExpression left, BOP op, ASTExpression right) </b> <br>
 * Extends ASTExpression. It defines a binary 
 * expression of the form "left op right".
 * <p>
 * <b> Member variables </b>:
 * <p>
 * public ASTExpression left;
 * <br>
 * public ASTExpression right;
 * <br>
 * public BOP op;
 * @see BOP 
 * @see ASTExpression
 */
public class ASTBinaryExpression extends ASTExpression {
	/**
	 * <b> public static BOP() </b> <br>
	 * BOP defines a binary operation. 
	 * <p> <b> Member variables </b> <p>
	 * public static BOP ADD; <br> public static BOP SUB; 
	 * <br> public static BOP MUL;
	 * <br> public static BOP DIV; <br> public static BOP MOD;
	 */
	public static class BOP {
		public static BOP ADD = new BOP();
		public static BOP SUB = new BOP();
		public static BOP MUL = new BOP();
		public static BOP DIV = new BOP();
		public static BOP MOD = new BOP();

		public static BOP AND = new BOP();
		public static BOP OR = new BOP();
		public static BOP XOR = new BOP();

		public static BOP SHL = new BOP();
		public static BOP SHR = new BOP();

		private BOP() {}
		
		public String toString() {
			if(this==ADD) return "+";
			else if (this==SUB) return "-";
			else if (this==MUL) return "*";
			else if (this==DIV) return "/";
			else if (this==MOD) return "%";
			else if (this==AND) return "&";
			else if (this==OR) return "|";
			else if (this==XOR) return "^";
			else if (this==SHL) return "<<";
			else if (this==SHR) return ">>";
			else return "Error";
		}
	}
	
	public ASTExpression left;
	public ASTExpression right;
	public BOP op;
	
	public ASTBinaryExpression(ASTExpression left, BOP op, ASTExpression right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(left.level() < this.level()) {
			sb.append("(");
			sb.append(left.toString());
			sb.append(")");
		} else {
			sb.append(left.toString());
		}
		sb.append(op.toString());
		if(right.level() < this.level()) {
			sb.append("(");
			sb.append(right.toString());
			sb.append(")");
		} else {
			sb.append(right.toString());
		}
		return sb.toString();
	}
	
	public int level() {
		if(op == BOP.AND || op == BOP.OR || op == BOP.XOR) return 5;
		if(op == BOP.SHL || op == BOP.SHR) return 6;
		if(op == BOP.ADD || op == BOP.SUB) return 7;
		else return 10;
	}

	public boolean equals(Object obj) {
		if(!(obj instanceof ASTBinaryExpression))
			return false;
		ASTBinaryExpression bexp = ((ASTBinaryExpression)obj);
		return op == bexp.op && left.equals(bexp.left) && right.equals(bexp.right); 
	}
}
