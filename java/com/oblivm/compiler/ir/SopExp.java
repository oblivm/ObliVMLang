/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;


public class SopExp extends Expression {

	public Variable x1;
	public int bit;
	public Op op;

	public static class Op {
		public static Op Shl = new Op("<<", "GCGadgets.CONSTMULT");
		public static Op Shr = new Op(">>", "ORAM.fastDivide");
		public static Op Mul = new Op("*", "GCGadgets.CONSTMULT");

		private String name;
		private String sname;

		private Op(String name, String sname) {this.name = name; this.sname = sname;}

		public String getName() {
			return name;
		}

		public String getSName() {
			return sname;
		}
	}

	public SopExp(Variable x1, Op op, int bit) {
		this.x1 = x1;
		this.op = op;
		this.bit = bit;
	}

	@Override
	public String toString() {
		return x1.toString()+op.name+bit;
	}

	@Override
	public Label getLabels() {
		return x1.lab;
	}

}
