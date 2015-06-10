/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.stmt;

import com.oblivm.compiler.ast.AST;

/**
 * Top level class for statements.  
 * Type hierarchy of ASTStatement.java:<p>
 * <BLOCKQUOTE>
 * - ASTArrayAssignStatement.java<br>
 * - ASTAssignStatement.java <br>
 * - ASTIfStatement.java <br>
 * - ASTReturnStatement.java <br>
 * - ASTWhileStatement.java
 * </BLOCKQUOTE>
 * @see ASTArrayAssignStatement 
 * @see ASTAssignStatement 
 * @see ASTIfStatement 
 * @see ASTReturnStatement 
 * @see ASTWhileStatement
 */
public abstract class ASTStatement extends AST {
	
}
