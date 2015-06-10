/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.stmt;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTPredicate;

/**
 * Defines an "if" statement of the form "if(cond) {truebranch} else {falsebranch}", 
 * where cond is a ASTPredicate, and the branches are both of type 
 * List{@literal <}ASTStatement{@literal >}. 
 */
public class ASTIfStatement extends ASTStatement {
	public ASTPredicate cond;
	public List<ASTStatement> trueBranch;
	public List<ASTStatement> falseBranch;
	
	public ASTIfStatement(ASTPredicate cond) {
		this.cond = cond;
		trueBranch = new ArrayList<ASTStatement>();
		falseBranch = new ArrayList<ASTStatement>();
	}
	
	public String toString(int indent) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.indent(indent));
		sb.append("if ("+cond.toString(0)+") {\n");
		for(int i=0; i<trueBranch.size(); ++i)
			sb.append(trueBranch.get(i).toString(indent+1));
		sb.append(this.indent(indent));
		if(falseBranch.size() == 0) {
			sb.append("}\n");
		} else {
			sb.append("} else {\n");
			for(int i=0; i<falseBranch.size(); ++i)
				sb.append(falseBranch.get(i).toString(indent+1));
			sb.append(this.indent(indent));
			sb.append("}\n");
		}
		return sb.toString();
	}
}
