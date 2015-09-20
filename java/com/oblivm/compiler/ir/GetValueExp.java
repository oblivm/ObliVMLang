/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ir;

import com.oblivm.compiler.frontend.nullable.ASTGetValueExpression.HandleWay;
import com.oblivm.compiler.type.manage.Label;

public class GetValueExp extends Expression {
	public HandleWay way;
	public Variable var;
	
	public GetValueExp(HandleWay way, Variable var) {
		this.way = way;
		this.var = var;
	}
	
	private String toStringInternal() {
		switch(way) {
		case Shallow:
			return var+"==null ? " + var + ".value : "+var + ".fake";
		case GetValue:
			return var + ".value";
		case PureRandom:
			return var + ".fake";
		}
		return null;
	}
	
	@Override
	public String toString() {
		return toStringInternal();
	}

	@Override
	public Label getLabels() {
		return var.lab;
	}

}
