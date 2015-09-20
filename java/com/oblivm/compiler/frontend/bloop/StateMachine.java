/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.bloop;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTBinaryExpression;
import com.oblivm.compiler.ast.expr.ASTBinaryExpression.BOP;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate.REL_OP;
import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.expr.ASTVariableExpression;
import com.oblivm.compiler.ast.stmt.ASTAssignStatement;
import com.oblivm.compiler.ast.stmt.ASTIfStatement;
import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.stmt.ASTWhileStatement;

public class StateMachine {
	public List<State> states = new ArrayList<State>();
	
	public State getInitialState() {
		return states.get(0);
	}
	
	public State getFinalState() {
		if(hasFinalState()) {
			for(int i=0; i<states.size(); ++i) {
				State s = states.get(i);
				if(s.getFinalStatement() instanceof ASTBranchStatement) {
					ASTBranchStatement br = (ASTBranchStatement)s.getFinalStatement();
					if(br.goTrue == Label.Next && br.goFalse == null) {
						return s;
					}
				} else if (i == states.size() - 1) {
					return s;
				}
			}
			return null;
		} else
			return null;
	}
	
	public boolean hasFinalState() {
		boolean f = false;
		for(int i=0; i<states.size(); ++i) {
			State s = states.get(i);
			if(s.getFinalStatement() instanceof ASTBranchStatement) {
				ASTBranchStatement br = (ASTBranchStatement)s.getFinalStatement();
				if(br.goTrue == Label.Next) {
					if(br.goFalse == null) {
						if(!f) f = true;
						else return false;
					} else 
						return false;
				}
				if(br.goFalse == Label.Next) {
					return false;
				}
			} else if (i == states.size() - 1) {
				if(!f) f = true;
				else return false;
			}
		}
		return f;
	}
	
	public void substituteNext(Label label) {
		for(int i=0; i<states.size(); ++i) {
			State s = states.get(i);
			if(s.getFinalStatement() instanceof ASTBranchStatement) {
				ASTBranchStatement br = (ASTBranchStatement)s.getFinalStatement();
				if(br.goTrue == Label.Next) br.goTrue = label;
				if(br.goFalse == Label.Next) br.goFalse = label;
			} else if (i == states.size() - 1) {
				s.stmts.add(new ASTBranchStatement(label));
			}
		}
	}

	public List<ASTStatement> toWhileLoop(String stateVar, String countVar) {
		List<ASTStatement> ret = new ArrayList<ASTStatement>();
		ASTExpression bound = new ASTConstantExpression(0, 32);
		for(int i=0; i<states.size(); ++i) {
			bound = new ASTBinaryExpression(bound, BOP.ADD, states.get(i).executedTimes);
		}
		ASTVariableExpression cv = new ASTVariableExpression(countVar);
		ASTVariableExpression sv = new ASTVariableExpression(stateVar);
		
		ret.add(new ASTAssignStatement(cv, bound));
		ret.add(new ASTAssignStatement(sv, new ASTConstantExpression(this.getInitialState().label.getId())));
		ASTWhileStatement loop = new ASTWhileStatement(new ASTBinaryPredicate(new ASTVariableExpression(countVar), REL_OP.GET, new ASTConstantExpression(0)));
		List<ASTStatement> now = loop.body;
		for(State s : states) {
			if(s.getFinalStatement() instanceof ASTBranchStatement) {
				ASTBranchStatement br = (ASTBranchStatement)s.getFinalStatement();
				br.stateVar = stateVar;
			}
			ASTIfStatement ifs = new ASTIfStatement(
					new ASTBinaryPredicate(sv, REL_OP.EQ, new ASTConstantExpression(s.label.getId())));
			ifs.trueBranch.addAll(s.stmts);
			now.add(ifs);
			now = ifs.falseBranch;
		}
		loop.body.add(
				new ASTAssignStatement(cv, 
						new ASTBinaryExpression(cv, BOP.SUB, new ASTConstantExpression(1)))
				);
		ret.add(loop);
		return ret;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(State s : states) {
			sb.append(s.toString());
		}
		return sb.toString();
	}
}
