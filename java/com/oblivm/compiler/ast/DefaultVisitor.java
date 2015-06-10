/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast;

import com.oblivm.compiler.ast.type.ASTArrayType;
import com.oblivm.compiler.ast.type.ASTDummyType;
import com.oblivm.compiler.ast.type.ASTFloatType;
import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTIntType;
import com.oblivm.compiler.ast.type.ASTNativeType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTRndType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTTypeVisitor;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.ast.type.ASTVoidType;

public abstract class DefaultVisitor<T1, T2, T3> extends DefaultStatementExpressionVisitor<T1, T2> implements ASTTypeVisitor<T3> {

	public T3 visit(ASTType type) {
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
		} else
			throw new RuntimeException("Unknown type!");
	}
}
