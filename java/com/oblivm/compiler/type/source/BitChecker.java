/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.source;

import java.util.HashSet;
import java.util.Set;

import com.oblivm.compiler.ast.ASTFunction;
import com.oblivm.compiler.ast.ASTFunctionDef;
import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.ast.DefaultVisitor;
import com.oblivm.compiler.ast.expr.ASTAndPredicate;
import com.oblivm.compiler.ast.expr.ASTArrayExpression;
import com.oblivm.compiler.ast.expr.ASTBinaryExpression;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate;
import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.expr.ASTFloatConstantExpression;
import com.oblivm.compiler.ast.expr.ASTFuncExpression;
import com.oblivm.compiler.ast.expr.ASTLogExpression;
import com.oblivm.compiler.ast.expr.ASTNewObjectExpression;
import com.oblivm.compiler.ast.expr.ASTNullExpression;
import com.oblivm.compiler.ast.expr.ASTOrPredicate;
import com.oblivm.compiler.ast.expr.ASTRangeExpression;
import com.oblivm.compiler.ast.expr.ASTRecExpression;
import com.oblivm.compiler.ast.expr.ASTRecTupleExpression;
import com.oblivm.compiler.ast.expr.ASTTupleExpression;
import com.oblivm.compiler.ast.expr.ASTVariableExpression;
import com.oblivm.compiler.ast.stmt.ASTAssignStatement;
import com.oblivm.compiler.ast.stmt.ASTBoundedWhileStatement;
import com.oblivm.compiler.ast.stmt.ASTFuncStatement;
import com.oblivm.compiler.ast.stmt.ASTIfStatement;
import com.oblivm.compiler.ast.stmt.ASTOnDummyStatement;
import com.oblivm.compiler.ast.stmt.ASTReturnStatement;
import com.oblivm.compiler.ast.stmt.ASTUsingStatement;
import com.oblivm.compiler.ast.stmt.ASTWhileStatement;
import com.oblivm.compiler.ast.type.ASTArrayType;
import com.oblivm.compiler.ast.type.ASTDummyType;
import com.oblivm.compiler.ast.type.ASTFloatType;
import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTIntType;
import com.oblivm.compiler.ast.type.ASTNativeType;
import com.oblivm.compiler.ast.type.ASTNullType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTRndType;
import com.oblivm.compiler.ast.type.ASTTupleType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.ast.type.ASTVoidType;

/**
 * Check if the bit expressions in a program are valid.
 *  
 * @author Chang Liu
 *
 */
public class BitChecker extends DefaultVisitor<Boolean, Boolean, Boolean> {

	Set<String> bitVariables;

	BitVariableExtractor bve = new BitVariableExtractor();
	
	public boolean check(ASTProgram program) {

		bitVariables = new HashSet<String>();

		for(int i = 0; i<program.typeDef.size(); ++i) {
			String name = program.typeDef.get(i).left;
			bitVariables.clear();
			if(program.typeBitVarDef.containsKey(name))
				for(ASTExpression e : program.typeBitVarDef.get(name)) {
					ASTVariableExpression ve = (ASTVariableExpression)e;
					bitVariables.add(ve.var);
				}
			if(!visit(program.typeDef.get(i).right)) {
				visit(program.typeDef.get(i).right);
				return false;
			}
		}
		
		for(ASTFunction func : program.functionDef)
			if(func instanceof ASTFunctionDef) {
				ASTFunctionDef function = ((ASTFunctionDef)func);
				bitVariables.clear();
				if(function.baseType != null) {
					Set<String> t = bve.visit(function.baseType);
					if(t != null)
						bitVariables.addAll(t);
				}
				bitVariables.addAll(function.bitParameter);
				for(int i=0; i<function.inputVariables.size(); ++i)
					if(!visit(function.inputVariables.get(i).left)) {
						visit(function.inputVariables.get(i).left);
						return false;
					}
				for(int i=0; i<function.localVariables.size(); ++i)
					if(!visit(function.localVariables.get(i).left)) {
						visit(function.localVariables.get(i).left);
						return false;
					}
			}
		return true;
	}
	
	@Override
	public Boolean visit(ASTArrayType type) {
		return visit(type.size) && visit(type.type);
	}

	@Override
	public Boolean visit(ASTIntType type) {
		return type.getBits() == null || visit(type.getBits());
	}

	@Override
	public Boolean visit(ASTFloatType type) {
		return type.getBits() == null || visit(type.getBits());
	}

	@Override
	public Boolean visit(ASTRndType type) {
		return type.getBits() == null || visit(type.getBits());
	}

	@Override
	public Boolean visit(ASTNativeType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTRecType type) {
		for(ASTExpression e : type.bitVariables)
			if(!visit(e))
				return false;
		for(ASTType ty : type.fieldsType.values())
			if(!visit(ty))
				return false;
		return true;
	}

	@Override
	public Boolean visit(ASTVariableType type) {
		for(ASTExpression e : type.bitVars)
			if(!visit(e))
				return false;
		if(type.typeVars != null)
			for(ASTType ty : type.typeVars)
				if(!visit(ty))
					return false;
		return true;
	}

	@Override
	public Boolean visit(ASTVoidType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTFunctionType type) {
		if(!visit(type.returnType))
			return false;
		for(int i=0; i<type.inputTypes.size(); ++i)
			if(!visit(type.inputTypes.get(i)))
				return false;
		return true;
	}

	@Override
	public Boolean visit(ASTAssignStatement assignStatement) {
		return false;
	}

	@Override
	public Boolean visit(ASTFuncStatement funcStatement) {
		return null;
	}

	@Override
	public Boolean visit(ASTIfStatement ifStatement) {
		return null;
	}

	@Override
	public Boolean visit(ASTReturnStatement returnStatement) {
		return null;
	}

	@Override
	public Boolean visit(ASTWhileStatement whileStatement) {
		return null;
	}

	@Override
	public Boolean visit(ASTBoundedWhileStatement whileStatement) {
		return null;
	}
	
	@Override
	public Boolean visit(ASTAndPredicate andPredicate) {
		return visit(andPredicate.left) && visit(andPredicate.right);
	}

	@Override
	public Boolean visit(ASTArrayExpression arrayExpression) {
		return false;
	}

	@Override
	public Boolean visit(ASTBinaryExpression binaryExpression) {
		return visit(binaryExpression.left) && visit(binaryExpression.right);
	}

	@Override
	public Boolean visit(ASTBinaryPredicate binaryPredicate) {
		return visit(binaryPredicate.left) && visit(binaryPredicate.right);
	}

	@Override
	public Boolean visit(ASTConstantExpression constantExpression) {
		return true;
	}

	@Override
	public Boolean visit(ASTFuncExpression funcExpression) {
		return false;
	}

	@Override
	public Boolean visit(ASTNewObjectExpression exp) {
		return false;
	}

	@Override
	public Boolean visit(ASTOrPredicate orPredicate) {
		return visit(orPredicate.left) && visit(orPredicate.right);
	}

	@Override
	public Boolean visit(ASTRecExpression rec) {
		return false;
	}

	@Override
	public Boolean visit(ASTRecTupleExpression tuple) {
		return false;
	}

	@Override
	public Boolean visit(ASTTupleExpression tuple) {
		return false;
	}

	@Override
	public Boolean visit(ASTVariableExpression variableExpression) {
		return this.bitVariables.contains(variableExpression.var);
	}

	@Override
	public Boolean visit(ASTFloatConstantExpression constantExpression) {
		return true;
	}

	@Override
	public Boolean visit(ASTLogExpression tuple) {
		return true;
	}

	@Override
	public Boolean visit(ASTRangeExpression tuple) {
		return false;
	}

	@Override
	public Boolean visit(ASTNullType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTOnDummyStatement stmt) {
		return null;
	}

	@Override
	public Boolean visit(ASTNullExpression exp) {
		return false;
	}

	@Override
	public Boolean visit(ASTDummyType type) {
		return visit(type.type);
	}

	@Override
	public Boolean visit(ASTTupleType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTUsingStatement stmt) {
		return null;
	}

}
