/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

public class ReverseRecordAssign extends IRCode {
	public Variable[] var;
	public String[] fields;
	public Variable base;
	
	public ReverseRecordAssign(Variable base, Variable[] var, String[] fields) {
		this.base = base;
		this.var = var;
		this.fields = fields;
	}

	public ReverseRecordAssign(Variable base, Variable var, String fields) {
		this.var = new Variable[]{var};
		this.fields = new String[]{fields};
		this.base = base;
	}
	
	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(indent(indent)+base.name+".");
		if(var.length > 1) sb.append("(");
		for(int i=0; i<var.length; ++i) {
			if(i > 0) sb.append(", ");
			sb.append(var[i].name);
		}
		sb.append(" = "+base.name+".(");
		for(int i=0; i<var.length; ++i) {
			if(i > 0) sb.append(", ");
			sb.append(fields[i]);
		}
		if(var.length > 1) sb.append(");");
		return sb.toString();
	}

	@Override
	public IRCode clone(boolean withTypeDef) {
		return new ReverseRecordAssign(base, var, fields);
	}
}
