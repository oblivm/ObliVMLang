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
import com.oblivm.compiler.ast.expr.ASTSizeExpression;
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
import com.oblivm.compiler.log.Bugs;

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
		boolean ret = true;
		
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
//				visit(program.typeDef.get(i).right);
				ret = false;
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
//						visit(function.inputVariables.get(i).left);
						ret = false;
					}
				for(int i=0; i<function.localVariables.size(); ++i)
					if(!visit(function.localVariables.get(i).left)) {
//						visit(function.localVariables.get(i).left);
						ret = false;
					}
			}
		return ret;
	}
	
	@Override
	public Boolean visit(ASTArrayType type) {
		boolean a = false;
		if(visit(type.size)) a = true;
		if(visit(type.type)) a = true;
		return a;
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
		boolean ret = true;
		for(ASTExpression e : type.bitVariables)
			if(!visit(e))
				ret = false;
		if(type.typeVariables != null) {
			for(ASTType ty : type.typeVariables) {
				if(!visit(ty)) {
					return false;
				}
			}
		}
		for(ASTType ty : type.fieldsType.values())
			if(!visit(ty)) {
				ret = false;
			}
		return ret;
	}

	@Override
	public Boolean visit(ASTVariableType type) {
		boolean ret = true;
		for(ASTExpression e : type.bitVars)
			if(!visit(e))
				ret = false;
		if(type.typeVars != null)
			for(ASTType ty : type.typeVars)
				if(!visit(ty))
					ret = false;
		return ret;
	}

	@Override
	public Boolean visit(ASTVoidType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTFunctionType type) {
		boolean ret = true;
		if(!visit(type.returnType)) {
			ret = false;
		}
		for(int i=0; i<type.inputTypes.size(); ++i)
			if(!visit(type.inputTypes.get(i))) {
				ret = false;
			}
		return ret;
	}

	@Override
	public Boolean visit(ASTAssignStatement assignStatement) {
		Bugs.LOG.log(assignStatement.beginPosition, "assign statement is not allowed to appear in bit expression");
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
		boolean ret = false;
		if(visit(andPredicate.left)) ret = true;
		if(visit(andPredicate.right)) ret = true;
		return ret;
	}

	@Override
	public Boolean visit(ASTArrayExpression arrayExpression) {
		Bugs.LOG.log(arrayExpression.beginPosition, "array expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTBinaryExpression binaryExpression) {
		boolean ret = false;
		if(visit(binaryExpression.left)) ret = true;
		if(visit(binaryExpression.right)) ret = true;
		return ret;
	}

	@Override
	public Boolean visit(ASTBinaryPredicate binaryPredicate) {
		boolean ret = false;
		if(visit(binaryPredicate.left)) ret = true;
		if(visit(binaryPredicate.right)) ret = true;
		return ret;
	}

	@Override
	public Boolean visit(ASTConstantExpression constantExpression) {
		return true;
	}

	@Override
	public Boolean visit(ASTFuncExpression funcExpression) {
		Bugs.LOG.log(funcExpression.beginPosition, "function expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTNewObjectExpression exp) {
		Bugs.LOG.log(exp.beginPosition, "object instantiation expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTOrPredicate orPredicate) {
		boolean ret = false;
		if(visit(orPredicate.left)) ret = true;
		if(visit(orPredicate.right)) ret = true;
		return ret;
	}

	@Override
	public Boolean visit(ASTRecExpression rec) {
		Bugs.LOG.log(rec.beginPosition, "record expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTRecTupleExpression tuple) {
		Bugs.LOG.log(tuple.beginPosition, "tuple expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTTupleExpression tuple) {
		Bugs.LOG.log(tuple.beginPosition, "tuple expression is not allowed to appear in bit expression");
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
		Bugs.LOG.log(tuple.beginPosition, "range expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTNullType type) {
		Bugs.LOG.log(type.beginPosition, "Null type is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTOnDummyStatement stmt) {
		return null;
	}

	@Override
	public Boolean visit(ASTNullExpression exp) {
		Bugs.LOG.log(exp.beginPosition, "Null expression is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTDummyType type) {
		return visit(type.type);
	}

	@Override
	public Boolean visit(ASTTupleType type) {
		Bugs.LOG.log(type.beginPosition, "Tuple type is not allowed to appear in bit expression");
		return false;
	}

	@Override
	public Boolean visit(ASTUsingStatement stmt) {
		return null;
	}

	@Override
	public Boolean visit(ASTSizeExpression exp) {
		return visit(exp.type);
	}

}
