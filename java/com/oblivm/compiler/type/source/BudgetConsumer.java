/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.source;


import java.util.Map;

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
import com.oblivm.compiler.ast.expr.ASTSizeExpression;
import com.oblivm.compiler.ast.expr.ASTTupleExpression;
import com.oblivm.compiler.ast.expr.ASTVariableExpression;
import com.oblivm.compiler.ast.expr.ExpressionVisitor;
import com.oblivm.compiler.ast.type.ASTArrayType;
import com.oblivm.compiler.ast.type.ASTDefaultTypeVisitor;
import com.oblivm.compiler.ast.type.ASTDummyType;
import com.oblivm.compiler.ast.type.ASTFloatType;
import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTIntType;
import com.oblivm.compiler.ast.type.ASTLabel;
import com.oblivm.compiler.ast.type.ASTNativeType;
import com.oblivm.compiler.ast.type.ASTNullType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTRndType;
import com.oblivm.compiler.ast.type.ASTTupleType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.ast.type.ASTVoidType;
import com.oblivm.compiler.util.Pair;

public class BudgetConsumer extends ASTDefaultTypeVisitor<Boolean> implements ExpressionVisitor<Void> {
	public ASTType targetType;
	public ResourceBudget budget;
	public AffineTypeChecker atc = AffineTypeChecker.get();
	
	boolean cancelMode = true;

	public void process(ASTType target, ASTExpression exp) {
		boolean old = cancelMode;
		this.targetType = target;
		cancelMode = true;
		visit(exp);
		this.cancelMode = old;
	}
	
	public Void visit(ASTPredicate predicate) {
		if(predicate instanceof ASTBinaryPredicate) {
			return visit((ASTBinaryPredicate) predicate);
		} else if(predicate instanceof ASTAndPredicate) {
			return visit((ASTAndPredicate) predicate);
		} else if(predicate instanceof ASTOrPredicate) {
			return visit((ASTOrPredicate) predicate);
		} else
			throw new RuntimeException("Unknown Predicate!");
	}

	public Void visit(ASTExpression expression) {
		if(expression instanceof ASTBinaryExpression) {
			return visit((ASTBinaryExpression)expression);
		} else if(expression instanceof ASTConstantExpression) {
			return visit((ASTConstantExpression)expression);
		} else if(expression instanceof ASTFloatConstantExpression) {
			return visit((ASTFloatConstantExpression)expression);
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
			return visit((ASTLogExpression)expression);
		} else if(expression instanceof ASTRangeExpression) {
			return visit((ASTRangeExpression)expression);
		} else if(expression instanceof ASTNullExpression) {
			return visit((ASTNullExpression)expression);
		} else if(expression instanceof ASTSizeExpression) {
			return visit((ASTSizeExpression)expression);
		} else
			throw new RuntimeException("Unknown Expression!");
	}

	
	@Override
	public Void visit(ASTAndPredicate andPredicate) {
		visit(andPredicate.left);
		visit(andPredicate.right);
		return null;
	}

	@Override
	public Void visit(ASTArrayExpression arrayExpression) {
		boolean old = this.cancelMode;
		ASTType target = this.targetType;
		this.cancelMode = true;
		ASTArrayType at = (ASTArrayType)arrayExpression.var.type;
		process(ASTIntType.get(at.size.targetBits, at.lab), 
				arrayExpression.indexExpr);
		this.cancelMode = old;
		this.targetType = new ASTArrayType(target, at.size, at.lab);
		visit(arrayExpression.var);
		return null;
	}

	@Override
	public Void visit(ASTBinaryExpression binaryExpression) {
		visit(binaryExpression.left);
		visit(binaryExpression.right);
		return null;
	}

	@Override
	public Void visit(ASTBinaryPredicate binaryPredicate) {
		if(binaryPredicate.left instanceof ASTNullExpression || binaryPredicate.right instanceof ASTNullExpression) {
			// x == null or null == x don't invalidate x
			return null;
		}
		visit(binaryPredicate.left);
		visit(binaryPredicate.right);
		return null;
	}

	@Override
	public Void visit(ASTConstantExpression constantExpression) {
		// Do nothing
		return null;
	}

	@Override
	public Void visit(ASTFloatConstantExpression constantExpression) {
		// Do nothing
		return null;
	}

	@Override
	public Void visit(ASTFuncExpression funcExpression) {
		ASTType target = this.targetType;
		int i = 0;
		for(Pair<String, ASTExpression> inputs : funcExpression.inputs) {
//			if(i < funcExpression.i)
			this.targetType = funcExpression.inputTypes.get(i++);
			visit(inputs.right);
		}
		this.targetType = target;
		return null;
	}

	@Override
	public Void visit(ASTNewObjectExpression exp) {
		ASTType target = this.targetType;
		ASTRecType rt;
		if(target instanceof ASTDummyType) {
			rt = (ASTRecType)((ASTDummyType)target).type;
		} else
			rt = (ASTRecType)target; 
		for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
			this.targetType = rt.fieldsType.get(ent.getKey());
			visit(ent.getValue());
		}
		this.targetType = target;
		return null;
	}

	@Override
	public Void visit(ASTOrPredicate orPredicate) {
		visit(orPredicate.left);
		visit(orPredicate.right);
		return null;
	}

	@Override
	public Void visit(ASTRecExpression rec) {
		boolean old = this.cancelMode;
		if(!this.willCancelQ(rec.type)) {
			cancelMode = false;
		}
		ASTType oldType = targetType;
		this.targetType = rec.base.type;
		visit(rec.base);
		this.cancelMode = old;
		this.targetType = oldType;
		return null;
	}

	@Override
	public Void visit(ASTRecTupleExpression tuple) {
		boolean old = this.cancelMode;
		ASTType oldType = targetType;
		if(!this.willCancelQ(tuple.type)) {
			cancelMode = false;
		}
		this.targetType = tuple.base.type;
		visit(tuple.base);
		this.cancelMode = old;
		this.targetType = oldType;
		ResourceBudget tmp = this.budget;
		this.budget = tmp.clone();
		ASTRecType rt;
		if(tuple.base.type instanceof ASTDummyType)
			rt = (ASTRecType) ((ASTDummyType)tuple.base.type).type;
		else
			rt = (ASTRecType) tuple.base.type;
		for(Map.Entry<String, ASTType> ent : rt.fieldsType.entrySet()) {
			if(atc.isAffine(ent.getValue()))
				budget.addAffine(ent.getKey());
			else
				budget.addStar(ent.getKey());
		}
		visit((ASTTupleExpression) tuple);
		this.budget = tmp;
		return null;
	}

	@Override
	public Void visit(ASTTupleExpression tuple) {
		ASTTupleType oldType = (ASTTupleType) targetType;
		for(int i=0; i<oldType.types.size(); ++i) {
			this.targetType = oldType.types.get(i);
			visit(tuple.exps.get(i));
		}
		this.targetType = oldType;
		return null;
	}

	@Override
	public Void visit(ASTLogExpression logExp) {
		// Do nothing: Getting the length of a random number is free
		return null;
	}

	@Override
	public Void visit(ASTRangeExpression rangeExp) {
		visit(rangeExp.source);
		// Both rangel and ranger should be public
		return null;
	}

	@Override
	public Void visit(ASTVariableExpression variableExpression) {
		if(cancelMode && visit(variableExpression.type)) {
			if(!budget.use(variableExpression.var)) {
				System.err.println("Cannot use variable "+variableExpression.var+"!");
				throw new RuntimeException("Cannot use variable "+variableExpression.var+"!");
			}
		}
		return null;
	}

	@Override
	public Void visit(ASTNullExpression exp) {
		// Do nothing
		return null;
	}

	/***
	 * Check if assign type -> targetType need to cancel the instance of type
	 * @param type 
	 * @return
	 */
	public boolean willCancelQ(ASTType type) {
		 return visit(type);
	}
	
	@Override
	public Boolean visit(ASTArrayType type) {
		ASTArrayType tmp = (ASTArrayType)this.targetType;
		this.targetType = tmp.type;
		boolean ret = visit(type.type);
		this.targetType = tmp;
		return ret;
	}

	@Override
	public Boolean visit(ASTIntType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTFloatType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTRndType type) {
		if(targetType instanceof ASTIntType && targetType.getLabel() == ASTLabel.Secure)
			return false;
		return true;
	}

	@Override
	public Boolean visit(ASTNativeType type) {
		return true;
	}

	@Override
	public Boolean visit(ASTRecType type) {
		return atc.isAffine(type);
	}

	@Override
	public Boolean visit(ASTVariableType type) {
		// Variable Type is affine
		return true;
	}

	@Override
	public Boolean visit(ASTVoidType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTFunctionType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTNullType type) {
		return false;
	}

	@Override
	public Boolean visit(ASTDummyType type) {
		return visit(type.type);
	}

	@Override
	public Boolean visit(ASTTupleType type) {
		ASTTupleType tmp = (ASTTupleType) targetType;
		for(int i=0; i<type.types.size(); ++i) {
			targetType = tmp.types.get(i);
			if(visit(type.types.get(i)))
				return true;
		}
		this.targetType = tmp;
		return false;
	}

	@Override
	public Void visitNull() {
		throw new RuntimeException("Unsupported Null Expression!");
	}

	@Override
	public Void visit(ASTSizeExpression exp) {
		return null;
	}

}
