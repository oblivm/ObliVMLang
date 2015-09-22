/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.bloop;

import java.util.List;

import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.stmt.ASTAssignStatement;
import com.oblivm.compiler.ast.stmt.ASTBoundedWhileStatement;
import com.oblivm.compiler.ast.stmt.ASTFuncStatement;
import com.oblivm.compiler.ast.stmt.ASTIfStatement;
import com.oblivm.compiler.ast.stmt.ASTOnDummyStatement;
import com.oblivm.compiler.ast.stmt.ASTReturnStatement;
import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.stmt.ASTUsingStatement;
import com.oblivm.compiler.ast.stmt.ASTWhileStatement;
import com.oblivm.compiler.ast.stmt.StatementVisitor;


public class StateIdentifier implements StatementVisitor<StateMachine> {

	private ASTExpression executionTime = null;

	@Override
	public StateMachine visit(ASTStatement statement) {
		if(statement instanceof ASTIfStatement) {
			return visit((ASTIfStatement)statement);
		} else if(statement instanceof ASTAssignStatement) {
			return visit((ASTAssignStatement)statement);
		} else if(statement instanceof ASTWhileStatement) {
			return visit((ASTWhileStatement)statement);
		} else if(statement instanceof ASTReturnStatement) {
			return visit((ASTReturnStatement)statement);
		} else if(statement instanceof ASTFuncStatement) {
			return visit((ASTFuncStatement)statement);
		} else if(statement instanceof ASTOnDummyStatement) {
			return visit((ASTOnDummyStatement)statement);
		} else if(statement instanceof ASTBoundedWhileStatement) {
			return visit((ASTBoundedWhileStatement)statement);
		}
		throw new RuntimeException("Unknown Statement!");
	}

	private StateMachine buildOne(ASTStatement stmt) {
		State st = new State(executionTime);
		st.stmts.add(stmt);
		StateMachine sm = new StateMachine();
		sm.states.add(st);
		return sm;
	}

	@Override
	public StateMachine visit(ASTAssignStatement assignStatement) {
		return buildOne(assignStatement);
	}

	@Override
	public StateMachine visit(ASTFuncStatement funcStatement) {
		return buildOne(funcStatement);
	}

	@Override
	public StateMachine visit(ASTIfStatement ifStatement) {
		ASTIfStatement newIf = new ASTIfStatement(ifStatement.cond);
		StateMachine sm = buildOne(newIf);
		
		if(ifStatement.trueBranch.size() > 0) {
			StateMachine T = visit(ifStatement.trueBranch);
			newIf.trueBranch.addAll(T.getInitialState().stmts);
			T.states.remove(0);
			if(T.states.size() > 0) {
				sm.states.addAll(T.states);
			}
		}
		
		if(ifStatement.falseBranch.size() > 0) {
			StateMachine F = visit(ifStatement.falseBranch);
			newIf.falseBranch.addAll(F.getInitialState().stmts);
			F.states.remove(0);
			if(F.states.size() > 0) {
				sm.states.addAll(F.states);
			}
		}
		return sm;
	}

	@Override
	public StateMachine visit(ASTReturnStatement returnStatement) {
		return buildOne(returnStatement);
	}

	@Override
	public StateMachine visit(ASTWhileStatement whileStatement) {
		throw new RuntimeException("Shouldn't contain loop in a bounded loop.");
	}

	@Override
	public StateMachine visit(ASTOnDummyStatement stmt) {
		throw new RuntimeException("OnDummy statements are not supported in bounded loop yet.");
	}

	@Override
	public StateMachine visit(ASTBoundedWhileStatement stmt) {
		ASTExpression old = executionTime;
		executionTime = stmt.bound;

		State br;
		if(old == null) {
			br = new State(new ASTConstantExpression(1, 32));
		} else {
			br = new State(old);
		}
		ASTBranchStatement brs = new ASTBranchStatement();
		brs.pred = stmt.cond;
		brs.goFalse = Label.Next;

		StateMachine sm = visit(stmt.body);
		brs.goTrue = sm.getInitialState().label;
		br.stmts.add(brs);
		
		ASTBranchStatement jb = new ASTBranchStatement();
		jb.pred = stmt.cond;
		jb.goTrue = sm.getInitialState().label;
		jb.goFalse = Label.Next;

		if(sm.hasFinalState()) {
			if(sm.getFinalState().getFinalStatement() instanceof ASTBranchStatement) {
				State jbs = new State(executionTime);
				jbs.stmts.add(jb);
				sm.states.add(jbs);
			} else {
				sm.getFinalState().stmts.add(jb);
			}
		} else {
			State jbs = new State(executionTime);
			jbs.stmts.add(jb);
			sm.substituteNext(jbs.label);
			sm.states.add(jbs);
		}
		sm.states.add(0, br);

		executionTime = old;
		return sm;
	}

	public StateMachine visit(List<ASTStatement> stmt) {
		StateMachine sm = new StateMachine();
		for(ASTStatement s : stmt) {
			StateMachine tsm = visit(s);
			if(sm.hasFinalState()) {
				State fs = sm.getFinalState();
				State is = tsm.getInitialState();
				if(fs.getFinalStatement() instanceof ASTBranchStatement) {
					fs.stmts.remove(fs.stmts.size() - 1);
				}
				fs.stmts.addAll(is.stmts);
				tsm.states.remove(0);
			} else {
				sm.substituteNext(tsm.getInitialState().label);
			}
			sm.states.addAll(tsm.states);
		}
		return sm;
	}

	@Override
	public StateMachine visit(ASTUsingStatement stmt) {
		return buildOne(stmt);
	}
}
