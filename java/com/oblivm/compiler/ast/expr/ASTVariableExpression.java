/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import com.oblivm.compiler.parser.Token;

/**
 * Extends ASTExpression. It defines a variable expression of the form 
 * "n = m;" where "n" and "m" are both variables.
 * <p>
 * <b> Member variables </b>: <p>
 * - public ASTVariable var;
 */
public class ASTVariableExpression extends ASTExpression {
	public String var;
	
	public ASTVariableExpression(String var) {
		this.var = var;
	}
	
	public ASTVariableExpression(String var, int beginLine, int endLine, int beginColumn, int endColumn) {
		this(var);
		this.setBeginPosition(beginLine, beginColumn);
		this.setEndPosition(endLine, endColumn);
	}
	
	public ASTVariableExpression(Token tok) {
		this(tok.image, tok.beginLine, tok.endLine, tok.beginColumn, tok.endColumn);
	}
	
	public String toString() {
		return var;
	}
	
	public int level() {
		return 100;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof ASTVariableExpression))
			return false;
		
		return var.equals(((ASTVariableExpression)obj).var);
	}

	@Override
	public ASTVariableExpression cloneInternal() {
		return new ASTVariableExpression(var);
	}
}
