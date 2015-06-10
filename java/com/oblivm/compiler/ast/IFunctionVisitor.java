/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

public interface IFunctionVisitor<T> {
	
	public T visit(ASTFunctionNative func);
	
	public T visit(ASTFunctionDef func);
	
	public T visit(ASTFunction func);
}
