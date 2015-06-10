/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;



public class RopExp extends Expression {

	public Variable x1;
	public Variable x2;
	public Op op;
	
	public static class Op {
		public static Op Gt = new Op(">", "GCGadgets.GT");
		public static Op Ge = new Op(">=", "GCGadgets.GE");
		public static Op Lt = new Op("<", "GCGadgets.LT");
		public static Op Le = new Op("<=", "GCGadgets.LE");
		public static Op Eq = new Op("==", "GCGadgets.EQ");
		public static Op Ne = new Op("!=", "GCGadgets.NE");
		
		private String name;
		private String sname;
		
		private Op(String name, String sn) {this.name = name; this.sname = sn; }
		
		public String getName() {
			return name;
		}
		
		public String getSName() {
			return sname;
		}
	}
	
	public RopExp(Variable left, Op op2, Variable left2) {
		this.x1 = left;
		this.x2 = left2;
		this.op = op2;
	}

	@Override
	public String toString() {
		return x1.toString() + op.name + x2.toString();
	}

	@Override
	public Label getLabels() {
		return x1.lab.meet(x2.lab);
	}

}
