/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public interface ASTTypeVisitor<T> {
	public T visit(ASTArrayType type);
	
	public T visit(ASTIntType type);
	
	public T visit(ASTFloatType type);
	
	public T visit(ASTRndType type);
	
	public T visit(ASTNativeType type);
	
	public T visit(ASTRecType type);
	
	public T visit(ASTVariableType type);
	
	public T visit(ASTVoidType type);

	public T visit(ASTType type);

	public T visit(ASTFunctionType type);
	
	public T visit(ASTNullType type);
	
	public T visit(ASTDummyType type);
	
	public T visit(ASTTupleType type);
}
