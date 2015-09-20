/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;
import com.oblivm.compiler.type.manage.VariableConstant;

public class RangeAssign extends IRCode {
	
	public Label lab;
	
	public Variable name;

	public VariableConstant left, right;

	public Variable value;
	
	public RangeAssign(Label lab, Variable arr, VariableConstant left, VariableConstant right, Variable value) {
		this.lab = lab;
		this.name = arr;
		this.left = left;
		this.right = right;
		this.value = value;
	}

	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer(this.indent(indent));
		sb.append(name.name);
		sb.append("$");
		sb.append(left.toString());
		if(right != null) {
			sb.append('~');
			sb.append(right.toString());
		}
		sb.append("$=");
		sb.append(value.toString());
		sb.append(";\n");
		return sb.toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new RangeAssign(lab, name, left, right, value);
		ret.withTypeDef = withTypeDef;
		return ret;
	}

}
