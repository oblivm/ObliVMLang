/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTPredicate;

/**
 * Defines a "while" statement of the from "while(cond) {body}" where cond 
 * is of type ASTPredicate and body is of type List{@literal <}ASTStatement{@literal >}. NOTE:
 * SCVM defines for-loops in terms of while loops. Therefore, if the parser sees a for loop, it 
 * will replace it with a while loop of the form "init while(cond) {body}", where init is the 
 * for-loop initializer defined as type ASTStatement, and where body is updated to include the for-loop
 * update statement. The update is appended to the end of the body. 
 */
public class ASTWhileStatement extends ASTStatement {
	// Initializer necessary for for-loops.
	public ASTStatement init;
	public ASTPredicate cond;
	public List<ASTStatement> body;
	
	public ASTWhileStatement(ASTPredicate cond) {
		this.cond = cond;
		this.body = new ArrayList<ASTStatement>();
	}
	
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		if (!(init == null)) {
//			sb.append(this.indent(indent));
			sb.append(init.toString(indent));
		}
		sb.append(this.indent(indent));
		sb.append("while ("+cond.toString()+") {\n");
		for(int i=0; i<body.size(); ++i)
			sb.append(body.get(i).toString(indent+1));
		sb.append(this.indent(indent));
		sb.append("}\n");
		return sb.toString();
	}
	
	public String toString() {
		return toString(0);
	}
}
