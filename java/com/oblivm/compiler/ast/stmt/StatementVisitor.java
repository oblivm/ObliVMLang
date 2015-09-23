/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.stmt;



/**
 * Public abstract interface that defines the visitor functions for ASTStatement, 
 * ASTIfStatement, ASTAssignStatement, ASTArrayAssignStatement, ASTWhileStatement, 
 * ASTReturnStatement, and ASTFunction.
 * @param <T>
 */
public abstract interface StatementVisitor<T> {
	
	public abstract T visit(ASTAssignStatement assignStatement);
	
	public abstract T visit(ASTFuncStatement funcStatement);
	
	public abstract T visit(ASTIfStatement ifStatement);

	public abstract T visit(ASTReturnStatement returnStatement);

	public abstract T visit(ASTStatement statement);
	
	public abstract T visit(ASTWhileStatement whileStatement);
	
	public abstract T visit(ASTOnDummyStatement stmt);
	
	public abstract T visit(ASTBoundedWhileStatement stmt);
	
	public abstract T visit(ASTUsingStatement stmt);
	
	public abstract T visit(ASTDebugStatement stmt);
}
