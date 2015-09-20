/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.bloop;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.stmt.ASTStatement;

public class State {
	public Label label;
	public List<ASTStatement> stmts;
	public ASTExpression executedTimes;
	
	public State(Label label, ASTExpression times) {
		this.label = label;
		stmts = new ArrayList<ASTStatement>();
		this.executedTimes = times;
	}
	
	public State(ASTExpression executedTimes) {
		this(Label.newLabel(), executedTimes);
	}
	
	public ASTStatement getFinalStatement() {
		return stmts.get(stmts.size() - 1);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(label+":\n");
		for(ASTStatement s : stmts) {
			sb.append(s.toString(1));
		}
		return sb.toString();
	}
}
