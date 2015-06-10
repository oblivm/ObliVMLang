/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;


public class BopExp extends Expression {

	public Variable x1;
	public Variable x2;
	public Op op;

	public static class Op {
		public static Op Add = new Op("+");
		public static Op Sub = new Op("-");
		public static Op Mul = new Op("*");
		public static Op Div = new Op("/");
		public static Op Mod = new Op("%");
		
		public static Op And = new Op("^");
		public static Op Or = new Op("|");
		public static Op Xor = new Op("^");

		public static Op Shl = new Op("<<");
		public static Op Shr = new Op(">>");

		private Op() {}
		
		private String name = "";
		
		private Op(String name) { this.name = name; }
		
		public String toString() {
			return name;
		}
	}

	public BopExp(Variable left, Op op, Variable left2) {
		this.x1 = left;
		this.x2 = left2;
		this.op = op;
	}

	@Override
	public String toString() {
		if (op == Op.Add) {
			return x1.toString()+"+"+x2.toString();
		} else if (op == Op.Sub) {
			return x1.toString()+"-"+x2.toString();
		} else if (op == Op.Mul) {
			return x1.toString()+"*"+x2.toString();
		} else if (op == Op.Div) {
			return x1.toString()+"/"+x2.toString();
		} else if (op == Op.Mod) {
			return x1.toString()+"%"+x2.toString();
		} else if (op == Op.And) {
			return x1.toString()+"&"+x2.toString();
		} else if (op == Op.Or) {
			return x1.toString()+"|"+x2.toString();
		} else if (op == Op.Xor) {
			return x1.toString()+"^"+x2.toString();
		} else if (op == Op.Shl) {
			return x1.toString()+"<<"+x2.toString();
		} else if (op == Op.Shr) {
			return x1.toString()+">>"+x2.toString();
		} else
			throw new RuntimeException("Unidentified operator!");
	}

	@Override
	public Label getLabels() {
		return x1.lab.meet(x2.lab);
	}

}
