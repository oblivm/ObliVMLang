/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class RecordAssign extends IRCode {
	
	public Label lab;
	
	public Variable base;

	public String field;

	public Variable value;
	
	public RecordAssign(Label lab, Variable base, String field, Variable value) {
		this.lab = lab;
		this.base = base;
		this.field = field;
		this.value = value;
	}

	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer(this.indent(indent));
		sb.append(base.name+"."+field+" = "+value.toString()+";\n");
		return sb.toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new RecordAssign(lab, base, field, value);
		ret.withTypeDef = withTypeDef;
		return ret;
	}

}
