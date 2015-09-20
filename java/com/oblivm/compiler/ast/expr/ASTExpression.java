/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import com.oblivm.compiler.ast.AST;
import com.oblivm.compiler.ast.type.ASTType;

/**
 * Top level class for expression.  
 * Type hierarchy of ASTExpression.java:<p>
 * <BLOCKQUOTE>
 * - ASTArrayExpression.java <br>
 * - ASTBinaryExpression.java <br>
 * - ASTConstantExpression.java <br>
 * - ASTConstantStringExpression.java <br>
 * - ASTVariableExpression <br>
 * </BLOCKQUOTE>
 * @see ASTArrayExpression
 * @see ASTBinaryExpression
 * @see ASTConstantExpression
 * @see ASTConstantStringExpression
 * @see ASTVariableExpression
 */
public abstract class ASTExpression extends AST {

	
	public ASTExpression targetBits = null;
	
	public ASTType type = null;
	
	public String toString(int indent) {
		return toString();
	}
	
	public abstract int level();

	public abstract ASTExpression cloneInternal();

	public ASTExpression clone() {
		ASTExpression exp = cloneInternal();
		exp.setBeginPosition(this.beginPosition);
		exp.setEndPosition(this.endPosition);
		return exp;
	}
}
