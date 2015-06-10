/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

public abstract class ASTDefaultTypeVisitor<T> implements ASTTypeVisitor<T> {
	public T visit(ASTType type) {
		if(type instanceof ASTArrayType) {
			return visit((ASTArrayType)type);
		} else if(type instanceof ASTIntType) {
			return visit((ASTIntType)type);
		} else if(type instanceof ASTFloatType) {
			return visit((ASTFloatType)type);
		} else if(type instanceof ASTRndType) {
			return visit((ASTRndType)type);
		} else if(type instanceof ASTFunctionType) {
			return visit((ASTFunctionType)type);
		} else if(type instanceof ASTNativeType) {
			return visit((ASTNativeType)type);
		} else if(type instanceof ASTRecType) {
			return visit((ASTRecType)type);
		} else if(type instanceof ASTVariableType) {
			return visit((ASTVariableType)type);
		} else if(type instanceof ASTVoidType) {
			return visit((ASTVoidType)type);
		} else if(type instanceof ASTDummyType) {
			return visit((ASTDummyType)type);
		} else if(type instanceof ASTTupleType) {
			return visit((ASTTupleType)type);
		}
			throw new RuntimeException("Unknown type!");
	}
}
