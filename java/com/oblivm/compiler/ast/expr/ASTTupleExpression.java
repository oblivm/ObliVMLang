/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import java.util.ArrayList;
import java.util.List;

public class ASTTupleExpression extends ASTExpression {
	public List<ASTExpression> exps;
	
	public ASTTupleExpression(ASTExpression... es) {
		exps = new ArrayList<ASTExpression>();
		for(int i=0; i<es.length; ++i)
			exps.add(es[i]);
	}
	
	@Override
	public int level() {
		return -1;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<exps.size(); ++i) {
			if(i > 0)
				sb.append(", ");
			sb.append(exps.get(i).toString());
		}
		return sb.toString();
	}

	public ASTTupleExpression cloneInternal() {
		ASTTupleExpression ret = new ASTTupleExpression();
		for(ASTExpression e : exps) {
			ret.exps.add(e.clone());
		}
		return ret;
	}
}
