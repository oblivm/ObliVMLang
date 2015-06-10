/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;


public interface ExpressionVisitor<T> {

	public abstract T visit(ASTAndPredicate andPredicate);

	public abstract T visit(ASTArrayExpression arrayExpression);

	public abstract T visit(ASTBinaryExpression binaryExpression);
	
	public abstract T visit(ASTBinaryPredicate binaryPredicate);
	
	public abstract T visit(ASTConstantExpression constantExpression);

	public abstract T visit(ASTFloatConstantExpression constantExpression);

	public abstract T visit(ASTExpression expression);

	public abstract T visit(ASTFuncExpression funcExpression);

	public abstract T visit(ASTNewObjectExpression exp);

	public abstract T visit(ASTOrPredicate orPredicate);
	
	public abstract T visit(ASTPredicate predicate);

	public abstract T visit(ASTRecExpression rec);
	
	public abstract T visit(ASTRecTupleExpression tuple);

	public abstract T visit(ASTTupleExpression tuple);

	public abstract T visit(ASTLogExpression logExp);

	public abstract T visit(ASTRangeExpression rangeExp);

	public abstract T visit(ASTVariableExpression variableExpression);
	
	public abstract T visit(ASTNullExpression exp);
	
}
