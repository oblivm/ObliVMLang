/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.source;

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
import com.oblivm.compiler.ast.expr.ASTPredicate;
import com.oblivm.compiler.ast.expr.ASTRangeExpression;
import com.oblivm.compiler.ast.expr.ASTRecExpression;
import com.oblivm.compiler.ast.expr.ASTRecTupleExpression;
import com.oblivm.compiler.ast.expr.ASTTupleExpression;
import com.oblivm.compiler.ast.expr.ASTVariableExpression;
import com.oblivm.compiler.ast.expr.ExpressionVisitor;

public class AssignableChecker implements ExpressionVisitor<Boolean> {

	public Boolean visit(ASTPredicate predicate) {
		if(predicate instanceof ASTBinaryPredicate) {
			return visit((ASTBinaryPredicate) predicate);
		} else if(predicate instanceof ASTAndPredicate) {
			return visit((ASTAndPredicate) predicate);
		} else if(predicate instanceof ASTOrPredicate) {
			return visit((ASTOrPredicate) predicate);
		} else
			throw new RuntimeException("Unknown Predicate!");
	}


	public Boolean visit(ASTExpression expression) {
		if(expression instanceof ASTBinaryExpression) {
			return visit((ASTBinaryExpression)expression);
		} else if(expression instanceof ASTConstantExpression) {
			return visit((ASTConstantExpression)expression);
		} else if(expression instanceof ASTArrayExpression) {
			return visit((ASTArrayExpression)expression);
		} else if(expression instanceof ASTRecExpression) {
			return visit((ASTRecExpression)expression);
		} else if(expression instanceof ASTVariableExpression) {
			return visit((ASTVariableExpression)expression);
		} else if(expression instanceof ASTPredicate) {
			return visit((ASTPredicate)expression);
		} else if(expression instanceof ASTFuncExpression) {
			return visit((ASTFuncExpression)expression);
		} else if(expression instanceof ASTNewObjectExpression) {
			return visit((ASTNewObjectExpression)expression);
		} else if(expression instanceof ASTRecTupleExpression) {
			return visit((ASTRecTupleExpression)expression);
		} else if(expression instanceof ASTTupleExpression) {
			return visit((ASTTupleExpression)expression);
		} else if(expression instanceof ASTLogExpression) {
			return false;
		} else if(expression instanceof ASTRangeExpression) {
			return true;
		} else
			throw new RuntimeException("Unknown Expression!");
	}

	@Override
	public Boolean visit(ASTAndPredicate andPredicate) {
		return false;
	}

	@Override
	public Boolean visit(ASTArrayExpression arrayExpression) {
		return visit(arrayExpression.var);
	}

	@Override
	public Boolean visit(ASTBinaryExpression binaryExpression) {
		return false;
	}

	@Override
	public Boolean visit(ASTBinaryPredicate binaryPredicate) {
		return false;
	}

	@Override
	public Boolean visit(ASTConstantExpression constantExpression) {
		return false;
	}

	@Override
	public Boolean visit(ASTFuncExpression funcExpression) {
		return false;
	}

	@Override
	public Boolean visit(ASTOrPredicate orPredicate) {
		return false;
	}

	@Override
	public Boolean visit(ASTRecExpression rec) {
		return visit(rec.base);
	}

	@Override
	public Boolean visit(ASTRecTupleExpression tuple) {
		for(ASTExpression exp : tuple.exps)
			if(!(exp instanceof ASTVariableExpression))
				return false;
		return true;
	}

	@Override
	public Boolean visit(ASTTupleExpression tuple) {
		for(ASTExpression exp : tuple.exps)
			if(!visit(exp))
				return false;
		return true;
	}

	@Override
	public Boolean visit(ASTVariableExpression variableExpression) {
		return true;
	}

	@Override
	public Boolean visit(ASTNewObjectExpression exp) {
		return false;
	}


	@Override
	public Boolean visit(ASTFloatConstantExpression constantExpression) {
		return false;
	}


	@Override
	public Boolean visit(ASTLogExpression tuple) {
		return false;
	}


	@Override
	public Boolean visit(ASTRangeExpression tuple) {
		return true;
	}


	@Override
	public Boolean visit(ASTNullExpression exp) {
		return false;
	}


}
