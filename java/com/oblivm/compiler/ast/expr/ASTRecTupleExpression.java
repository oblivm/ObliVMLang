/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import java.util.ArrayList;
import java.util.List;

public class ASTRecTupleExpression extends ASTTupleExpression {
	public ASTExpression base;
	
	public ASTRecTupleExpression(ASTExpression base, ASTTupleExpression tuple) {
		this.base = base;
		this.exps = tuple.exps;
	}
	
	public ASTRecTupleExpression(ASTExpression base, List<ASTExpression> exps) {
		this.base = base;
		this.exps = exps;
	}
	
	@Override
	public int level() {
		return 100;
	}
	
	public String toString() {
		return base.toString() + ".(" + super.toString() + ")";
	}

	public ASTRecTupleExpression cloneInternal() {
		List<ASTExpression> newExp = new ArrayList<ASTExpression>();
		for(ASTExpression e : exps) {
			newExp.add(e.clone());
		}
		return new ASTRecTupleExpression(base.clone(), newExp);
	}
}
