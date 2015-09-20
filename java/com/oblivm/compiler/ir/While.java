/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.type.manage.Label;

public class While extends IRCode {
	public Label lab;
	public Variable cond;
	public IRCode body;
	
	public While(Label lab, Variable cond, IRCode body) {
		if(lab != cond.lab || !cond.getBits().isConstant(1))
			throw new RuntimeException("Wrong construction of If");
		this.lab = lab;
		this.cond = cond;
		this.body = body;
	}
	
	@Override
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<indent; ++i)
			sb.append('\t');
		sb.append("while("+cond.name+") {\n");
		sb.append(body.toString(indent+1));
		for(int i=0; i<indent; ++i)
			sb.append('\t');
		sb.append("}\n");
		return sb.toString();
	}
	

	@Override
	public IRCode clone(boolean withTypeDef) {
		IRCode ret = new While(lab, cond, body.clone(withTypeDef));
		ret.withTypeDef = withTypeDef;
		return ret;
	}

}
