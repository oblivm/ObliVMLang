/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.bloop;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.stmt.ASTStatement;

public class ASTBranchStatement extends ASTStatement {
	public ASTExpression pred;
	public Label goTrue, goFalse;
	public String stateVar = null;
	
	// assert pred == null <=> goFalse == null
	
	public ASTBranchStatement(Label label) {
		pred = null;
		goFalse = null;
		goTrue = label;
	}

	public ASTBranchStatement() {
		pred = null;
		goTrue = goFalse = null;
	}
	
	@Override
	public String toString(int indent) {
		if(pred == null)
			return indent(indent)+"jmp -> "+goTrue+";\n";
		else
			return indent(indent)+"br "+pred+" | T -> "+goTrue+" | F -> "+goFalse+";\n";
	}

}
