/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import com.oblivm.compiler.ast.AST;

/**
 * Top level class for types.  
 * Type hierarchy of ASTType.java:<p>
 * <BLOCKQUOTE>
 * - ASTArrayType.java<br>
 * - ASTIntType.java<br> 
 * - ASTStringType.java<br>
 * </BLOCKQUOTE>
 * @see ASTArrayType 
 * @see ASTIntType 
 * @see ASTStringType
 */
public abstract class ASTType extends AST {
	public abstract boolean canFlowTo(ASTType type);
	
	public abstract String shortName();
	
	public boolean instance(ASTType type) {
		return false;
	}
	
	public boolean isNullable() {
		return false;
	}
	
	public ASTType deNull() {
		return this;
	}
	
	public abstract ASTLabel getLabel();
}
