/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class UnaryOpExp extends Expression {

	public Variable x;
	public Op op;
	
	public static class Op {
		public static Op Neg = new Op();
		private Op() {}
	}
	

	public UnaryOpExp(Op op, Variable var) {
		this.x = var;
		this.op = op;
	}

	@Override
	public String toString() {
		if (op == Op.Neg) {
			return "~"+x.toString()+")";
		} else
			throw new RuntimeException("Unknown Operator.");
	}

	@Override
	public Label getLabels() {
		return x.lab;
	}

}
