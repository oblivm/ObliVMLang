/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.manage;

import com.oblivm.compiler.ir.BopExp.Op;

public class BOPVariableConstant extends VariableConstant {
	public VariableConstant left, right;
	public Op op;
	
	public BOPVariableConstant(VariableConstant left, Op op, VariableConstant right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}
	
	public String toString() {
		return "(" + left.toString() + ")" + op + "(" + right.toString() + ")";
	}
	
	public boolean equals(VariableConstant obj) {
		if(!(obj instanceof BOPVariableConstant))
			return false;
		BOPVariableConstant bvc = (BOPVariableConstant)obj;
		return op == bvc.op && left.equals(bvc.left) && right.equals(bvc.right);
	}

	@Override
	public boolean isConstant(int value) {
		return false;
	}
}
