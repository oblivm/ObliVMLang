/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.expr.ASTPredicate;
import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.type.ASTType;

/**
 * Top level class in the abstract syntax tree. 
 * Used by the parser to build the AST. 
 * Type hierarchy of AST.java:<p>
 * <BLOCKQUOTE>
 * - ASTExpression.java<br>
 * - ASTFunction.java<br>
 * - ASTPredicate.java<br>
 * - ASTStatement.java<br>
 * - ASTType.java<br>
 * - ASTVariable.java<br>
 * </BLOCKQUOTE>
 * @see ASTExpression
 * @see ASTFunctionDef
 * @see ASTPredicate
 * @see ASTStatement
 * @see ASTType
 * @see ASTVariable
 */
public abstract class AST {
	public Position beginPosition, endPosition;
	
	public void setBeginPosition(int line, int column) {
		this.beginPosition = new Position(line, column);
	}
	
	public void setEndPosition(int line, int column) {
		this.endPosition = new Position(line, column);
	}
	
	public void setBeginPosition(Position pos) {
		if(pos == null)
			this.beginPosition = null;
		else
			this.beginPosition = new Position(pos.line, pos.column);
	}
	
	public void setEndPosition(Position pos) {
		if(pos == null)
			this.endPosition = null;
		else
			this.endPosition = new Position(pos.line, pos.column);
	}
	
	public abstract String toString(int indent);
	
	public String indent(int n) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<n; ++i)
			sb.append("  ");
		return sb.toString();
	}
}
