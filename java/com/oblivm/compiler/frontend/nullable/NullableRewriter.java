/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.frontend.nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oblivm.compiler.ast.ASTFunction;
import com.oblivm.compiler.ast.ASTFunctionDef;
import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.ast.DefaultStatementExpressionVisitor;
import com.oblivm.compiler.ast.expr.ASTAndPredicate;
import com.oblivm.compiler.ast.expr.ASTArrayExpression;
import com.oblivm.compiler.ast.expr.ASTBinaryExpression;
import com.oblivm.compiler.ast.expr.ASTBinaryExpression.BOP;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate;
import com.oblivm.compiler.ast.expr.ASTBinaryPredicate.REL_OP;
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
import com.oblivm.compiler.ast.stmt.ASTAssignStatement;
import com.oblivm.compiler.ast.stmt.ASTBoundedWhileStatement;
import com.oblivm.compiler.ast.stmt.ASTDebugStatement;
import com.oblivm.compiler.ast.stmt.ASTFuncStatement;
import com.oblivm.compiler.ast.stmt.ASTIfStatement;
import com.oblivm.compiler.ast.stmt.ASTOnDummyStatement;
import com.oblivm.compiler.ast.stmt.ASTReturnStatement;
import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.stmt.ASTUsingStatement;
import com.oblivm.compiler.ast.stmt.ASTWhileStatement;
import com.oblivm.compiler.ast.type.ASTArrayType;
import com.oblivm.compiler.ast.type.ASTDummyType;
import com.oblivm.compiler.ast.type.ASTFloatType;
import com.oblivm.compiler.ast.type.ASTIntType;
import com.oblivm.compiler.ast.type.ASTLabel;
import com.oblivm.compiler.ast.type.ASTNullType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.frontend.nullable.ASTGetValueExpression.HandleWay;
import com.oblivm.compiler.util.Pair;

public class NullableRewriter extends DefaultStatementExpressionVisitor<ASTStatement, Pair<ASTExpression, List<ASTType>>> {

	public List<String> currentNullExp = new ArrayList<String>();
	public List<String> currentUnNullExp = new ArrayList<String>();

	private boolean rememberPredicate;
	private Map<String, ASTType> variableMapping;
	private ASTFunctionDef function;

	public void reviseProgram(ASTProgram program) {
		for(ASTFunction function : program.functionDef) {
			if(function instanceof ASTFunctionDef) {
				this.function = (ASTFunctionDef) function;
				variableMapping = new HashMap<String, ASTType>();
				if(this.function.baseType != null) {
					ASTRecType rt = (ASTRecType) this.function.baseType;
					for(ASTExpression e : rt.bitVariables) {
						String var = ((ASTVariableExpression)e).var;
						variableMapping.put(var, ASTIntType.get(32, ASTLabel.Pub));
					}
					for(Map.Entry<String, ASTType> ent : rt.fieldsType.entrySet()) {
						variableMapping.put(ent.getKey(), ent.getValue());
					}
				}
				for(String x : this.function.bitParameter) {
					variableMapping.put(x, ASTIntType.get(32, ASTLabel.Pub));
				}
				for(Pair<ASTType, String> ent : this.function.inputVariables) {
					variableMapping.put(ent.right, ent.left);
				}
				for(Pair<ASTType, String> ent : this.function.localVariables) {
					variableMapping.put(ent.right, ent.left);
				}
				this.function.body = visit(this.function.body);
			}
		}
	}
	
	/***
	 * Check if a nullable expression can be retrieved directly.
	 * @param exp
	 * @return
	 */
	private HandleWay checkExpression(ASTExpression exp) {
		String check = exp.toString();
		if(currentUnNullExp.contains(check))
			return HandleWay.GetValue;
		if(currentNullExp.contains(check))
			return HandleWay.PureRandom;
		return HandleWay.Shallow;
	}

	private ASTExpression matchType(ASTExpression exp, ASTType currentType, ASTType targetType) {
		if(currentType.isNullable() && !targetType.isNullable()) {
			exp = new ASTGetValueExpression(checkExpression(exp), exp);
		} else if (!currentType.isNullable() && targetType.isNullable()) {
			exp = new ASTBoxNullableExpression(exp);
		}
		return exp;
	}

	@Override
	public ASTStatement visit(ASTAssignStatement assignStatement) {
		Pair<ASTExpression, List<ASTType>> var = visit(assignStatement.var);
		Pair<ASTExpression, List<ASTType>> exp = visit(assignStatement.expr);
		if(var.right.size() != exp.right.size())
			throw new RuntimeException("Assignment numbers mis-match: type checker should have handled this.");
		if(exp.right.size() == 1) {
			assignStatement.expr = matchType(exp.left, exp.right.get(0), var.right.get(0));
		} else {
			ASTTupleExpression tuple = (ASTTupleExpression)exp.left;
			for(int i=0; i<exp.right.size(); ++i) {
				((ASTTupleExpression)assignStatement.expr).exps
				.set(i, matchType(tuple.exps.get(i), exp.right.get(i), var.right.get(i)));
			}
		}
		return assignStatement;
	}

	@Override
	public ASTStatement visit(ASTFuncStatement funcStatement) {
		funcStatement.func = (ASTFuncExpression) visit(funcStatement.func).left;
		return funcStatement;
	}

	private List<ASTStatement>  visit(List<ASTStatement> body) {
		for(int i=0; i<body.size(); ++i)
			body.set(i, visit(body.get(i)));
		return body;
	}

	@Override
	public ASTStatement visit(ASTIfStatement ifStatement) {
		List<String> old1 = new ArrayList<String>(currentNullExp);
		List<String> old2 = new ArrayList<String>(currentUnNullExp);
		this.rememberPredicate = true;
		ifStatement.cond = (ASTPredicate) visit(ifStatement.cond).left;
		ifStatement.trueBranch = visit(ifStatement.trueBranch);
		ifStatement.falseBranch = visit(ifStatement.falseBranch);
		currentNullExp = old1;
		currentUnNullExp = old2;
		return ifStatement;
	}

	@Override
	public ASTStatement visit(ASTReturnStatement returnStatement) {
		Pair<ASTExpression, List<ASTType>> var = visit(returnStatement.exp);
		returnStatement.exp = matchType(var.left, var.right.get(0), this.function.returnType);
		return returnStatement;
	}

	@Override
	public ASTStatement visit(ASTWhileStatement whileStatement) {
		this.rememberPredicate = false;
		whileStatement.cond = (ASTPredicate) visit(whileStatement.cond).left;
		whileStatement.body = visit(whileStatement.body);
		return whileStatement;
	}

	@Deprecated
	@Override
	public ASTStatement visit(ASTOnDummyStatement stmt) {
		return null;
	}

	@Override
	public ASTStatement visit(ASTBoundedWhileStatement stmt) {
		List<String> old1 = new ArrayList<String>(currentNullExp);
		List<String> old2 = new ArrayList<String>(currentUnNullExp);
		this.rememberPredicate = true;
		stmt.cond = (ASTPredicate) visit(stmt.cond).left;
		stmt.body = visit(stmt.body);
		currentNullExp = old1;
		currentUnNullExp = old2;
		return stmt;
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTAndPredicate andPredicate) {
		Pair<ASTExpression, List<ASTType>> left = visit(andPredicate.left);
		Pair<ASTExpression, List<ASTType>> right = visit(andPredicate.right);
		andPredicate.left = (ASTPredicate) left.left;
		andPredicate.right = (ASTPredicate) right.left;
		return new Pair<ASTExpression, List<ASTType>>(andPredicate, left.right);
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTArrayExpression arrayExpression) {
		Pair<ASTExpression, List<ASTType>> var = visit(arrayExpression.var);
		arrayExpression.var = var.left;
		Pair<ASTExpression, List<ASTType>> idx = visit(arrayExpression.indexExpr);
		if(idx.right.get(0) instanceof ASTDummyType) {
			arrayExpression.indexExpr = new ASTGetValueExpression(checkExpression(idx.left), idx.left);
		} else
			arrayExpression.indexExpr = idx.left;
		return new Pair<ASTExpression, List<ASTType>>(arrayExpression, one(((ASTArrayType)var.right.get(0)).type));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(
			ASTBinaryExpression binaryExpression) {
		Pair<ASTExpression, List<ASTType>> var = visit(binaryExpression.right);
		if(var.right.get(0) instanceof ASTDummyType) {
			binaryExpression.right = new ASTGetValueExpression(checkExpression(var.left), var.left);
		} else
			binaryExpression.right = var.left;
		var = visit(binaryExpression.left);
		ASTType ret = var.right.get(0);
		if(var.right.get(0) instanceof ASTDummyType) {
			binaryExpression.left = new ASTGetValueExpression(checkExpression(var.left), var.left);
			ret = ((ASTDummyType)ret).type;
		} else
			binaryExpression.left = var.left;
		return new Pair<ASTExpression, List<ASTType>>(binaryExpression, one(ret));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTBinaryPredicate binaryPredicate) {
		Pair<ASTExpression, List<ASTType>> left = visit(binaryPredicate.left);
		Pair<ASTExpression, List<ASTType>> right = visit(binaryPredicate.right);
		ASTType ltype = left.right.get(0);
		ASTType rtype = right.right.get(0);
		if(ltype.isNullable() && rtype.isNullable()) {
			if(ltype == ASTNullType.get()) {
				if(rememberPredicate) {
					if(rtype instanceof ASTDummyType) {
						if(binaryPredicate.op == REL_OP.EQ) {
							this.currentNullExp.add(right.left.toString());
						} else if(binaryPredicate.op == REL_OP.NEQ) {
							this.currentNullExp.add(right.left.toString());
						} else
							throw new RuntimeException("Something wrong with Type checker: only equality checking is allowed between null and a nullable type");
					}
				}
				// Ensure the condition to be ? == null.
				binaryPredicate.left = right.left;
				binaryPredicate.right = left.left;
				return new Pair<ASTExpression, List<ASTType>>(binaryPredicate, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
			} else if(rtype == ASTNullType.get()) {
				if(rememberPredicate) {
					if(binaryPredicate.op == REL_OP.EQ) {
						this.currentNullExp.add(left.left.toString());
					} else if(binaryPredicate.op == REL_OP.NEQ) {
						this.currentNullExp.add(left.left.toString());
					} else
						throw new RuntimeException("Something wrong with Type checker: only equality checking is allowed between null and a nullable type");
				}
				binaryPredicate.right = right.left;
				binaryPredicate.left = left.left;
				return new Pair<ASTExpression, List<ASTType>>(binaryPredicate, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
			} else {
				ASTPredicate cond;
				if(binaryPredicate.op == REL_OP.EQ || binaryPredicate.op == REL_OP.NEQ) {
					ASTPredicate cond1 = new ASTAndPredicate(
							new ASTBinaryPredicate(left.left, REL_OP.EQ, new ASTNullExpression()),
							new ASTBinaryPredicate(right.left, REL_OP.EQ, new ASTNullExpression()));
					ASTPredicate cond2 = new ASTAndPredicate(new ASTAndPredicate(
							new ASTBinaryPredicate(left.left, REL_OP.NEQ, new ASTNullExpression()),
							new ASTBinaryPredicate(right.left, REL_OP.NEQ, new ASTNullExpression())),
							new ASTBinaryPredicate(
									new ASTGetValueExpression(HandleWay.GetValue, left.left), binaryPredicate.op,
									new ASTGetValueExpression(HandleWay.GetValue, right.left)
									));
					cond = new ASTOrPredicate(cond1, cond2);
				} else {
					cond = new ASTAndPredicate(new ASTAndPredicate(
							new ASTBinaryPredicate(left.left, REL_OP.NEQ, new ASTNullExpression()),
							new ASTBinaryPredicate(right.left, REL_OP.NEQ, new ASTNullExpression())),
							new ASTBinaryPredicate(
									new ASTGetValueExpression(HandleWay.GetValue, left.left), binaryPredicate.op,
									new ASTGetValueExpression(HandleWay.GetValue, right.left)
									));
					if(rememberPredicate) {
						this.currentUnNullExp.add(left.left.toString());
						this.currentUnNullExp.add(right.left.toString());
					}
				}
				return new Pair<ASTExpression, List<ASTType>>(cond, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
			}
		} else if(ltype.isNullable() && !rtype.isNullable()) {
			if(rememberPredicate) {
				this.currentUnNullExp.add(left.left.toString());
			}
			binaryPredicate.right = right.left;
			binaryPredicate.left = new ASTGetValueExpression(HandleWay.GetValue, left.left);
			ASTBinaryPredicate cond = new ASTBinaryPredicate(left.left, REL_OP.NEQ, new ASTNullExpression());
			ASTPredicate pred = new ASTAndPredicate(cond, binaryPredicate);
			return new Pair<ASTExpression, List<ASTType>>(pred, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
		} else if(!ltype.isNullable() && rtype.isNullable()) {
			if(rememberPredicate) {
				this.currentUnNullExp.add(right.left.toString());
			}
			binaryPredicate.left = left.left;
			binaryPredicate.right = new ASTGetValueExpression(HandleWay.GetValue, right.left);
			ASTBinaryPredicate cond = new ASTBinaryPredicate(right.left, REL_OP.NEQ, new ASTNullExpression());
			ASTPredicate pred = new ASTAndPredicate(cond, binaryPredicate);
			return new Pair<ASTExpression, List<ASTType>>(pred, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
		} else {
			// !ltype.isNullable && !rtype.isNullable()
			binaryPredicate.left = left.left;
			binaryPredicate.right = binaryPredicate.right;
			return new Pair<ASTExpression, List<ASTType>>(binaryPredicate, one(ASTIntType.get(1, ltype.getLabel().meet(rtype.getLabel()))));
		}
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(
			ASTConstantExpression constantExpression) {
		return new Pair<ASTExpression, List<ASTType>>(constantExpression, one(ASTIntType.get(32, ASTLabel.Pub)));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(
			ASTFloatConstantExpression constantExpression) {
		return new Pair<ASTExpression, List<ASTType>>(constantExpression, one(ASTFloatType.get(32, ASTLabel.Pub)));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTFuncExpression funcExpression) {
		ASTFuncExpression exp = funcExpression;
		if(exp.baseType instanceof ASTDummyType) {
			ASTRecExpression re = (ASTRecExpression)exp.obj;
			re.base = this.matchType(re.base, exp.baseType, exp.baseType.deNull());
		}
		for(int i=0; i<funcExpression.inputs.size(); ++i) {
			Pair<ASTExpression, List<ASTType>> var = visit(funcExpression.inputs.get(i).right);
			funcExpression.inputs.get(i).right = matchType(var.left, var.right.get(0), funcExpression.inputTypes.get(0));
		}
		return new Pair<ASTExpression, List<ASTType>>(funcExpression, one(funcExpression.type));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTNewObjectExpression exp) {
		for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
			String field = ent.getKey();
			Pair<ASTExpression, List<ASTType>> var = visit(ent.getValue());
			ent.setValue(matchType(var.left, var.right.get(0), exp.type.fieldsType.get(field)));
		}
		return new Pair<ASTExpression, List<ASTType>>(exp, one(exp.type));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTOrPredicate orPredicate) {
		boolean old = rememberPredicate;
		rememberPredicate = false;
		Pair<ASTExpression, List<ASTType>> left = visit(orPredicate.left);
		Pair<ASTExpression, List<ASTType>> right = visit(orPredicate.right);
		orPredicate.left = (ASTPredicate) left.left;
		orPredicate.right = (ASTPredicate) right.left;
		rememberPredicate = old;
		return new Pair<ASTExpression, List<ASTType>>(orPredicate, left.right);
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTRecExpression rec) {
		Pair<ASTExpression, List<ASTType>> var = visit(rec.base);
		ASTType type = var.right.get(0);
		if(var.right.get(0) instanceof ASTDummyType) {
			rec.base = new ASTGetValueExpression(checkExpression(var.left), var.left);
			type = ((ASTDummyType)type).type;
		} else
			rec.base = var.left;
		type = ((ASTRecType)type).fieldsType.get(rec.field);
		return new Pair<ASTExpression, List<ASTType>>(rec, one(type));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTRecTupleExpression tuple) {
		Pair<ASTExpression, List<ASTType>> var = visit(tuple.base);
		tuple.base = var.left;
		Map<String, ASTType> old = new HashMap<String, ASTType>(variableMapping);
		ASTType type = var.right.get(0);
		if(var.right.get(0) instanceof ASTDummyType) {
			tuple.base = new ASTGetValueExpression(checkExpression(var.left), var.left);
			type = ((ASTDummyType)type).type;
		} else
			tuple.base = var.left;

		ASTRecType rt = (ASTRecType)type;
		for(Map.Entry<String, ASTType> ent : rt.fieldsType.entrySet())
			variableMapping.put(ent.getKey(), ent.getValue());
		List<ASTType> ret = new ArrayList<ASTType>();
		for(int i=0; i<tuple.exps.size(); ++i) {
			var = visit(tuple.exps.get(i));
			tuple.exps.set(i, var.left);
			ret.add(var.right.get(0));
		}
		variableMapping = old;
		return new Pair<ASTExpression, List<ASTType>>(tuple, ret);
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTTupleExpression tuple) {
		List<ASTType> ret = new ArrayList<ASTType>();
		for(int i=0; i<tuple.exps.size(); ++i) {
			Pair<ASTExpression, List<ASTType>> var = visit(tuple.exps.get(i));
			tuple.exps.set(i, var.left);
			ret.add(var.right.get(0));
		}
		return new Pair<ASTExpression, List<ASTType>>(tuple, ret);
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTLogExpression tuple) {
		Pair<ASTExpression, List<ASTType>> var = visit(tuple.exp);
		if(var.right.get(0) instanceof ASTDummyType) {
			tuple.exp = new ASTGetValueExpression(checkExpression(var.left), var.left);
		} else
			tuple.exp = var.left;
		return new Pair<ASTExpression, List<ASTType>>(tuple, one(ASTIntType.get(32, ASTLabel.Pub)));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTRangeExpression tuple) {
		Pair<ASTExpression, List<ASTType>> var = visit(tuple.source);
		if(var.right.get(0) instanceof ASTDummyType) {
			tuple.source = new ASTGetValueExpression(checkExpression(var.left), var.left);
		} else
			tuple.source = var.left;
		return new Pair<ASTExpression, List<ASTType>>(tuple, one(ASTIntType.get(
				tuple.ranger == null ? new ASTConstantExpression(1) :
					new ASTBinaryExpression(tuple.ranger, BOP.SUB, tuple.rangel)
				, 
				var.right.get(0).getLabel())));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(
			ASTVariableExpression variableExpression) {
		if(variableExpression.var.equals("this"))
			return new Pair<ASTExpression, List<ASTType>>(variableExpression,
					one(this.function.baseType));
		return new Pair<ASTExpression, List<ASTType>>(variableExpression, 
				one(this.variableMapping.get(variableExpression.var)));
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTNullExpression exp) {
		return new Pair<ASTExpression, List<ASTType>>(exp, one(ASTNullType.get()));
	}

	private List<ASTType> one(ASTType type) {
		List<ASTType> ret = new ArrayList<ASTType>();
		ret.add(type);
		return ret;
	}

	@Override
	public ASTStatement visit(ASTUsingStatement stmt) {
		stmt.body = visit(stmt.body);
		return stmt;
	}

	@Override
	public Pair<ASTExpression, List<ASTType>> visit(ASTSizeExpression exp) {
		return new Pair<ASTExpression, List<ASTType>>(exp, 
				one(ASTIntType.get(32, ASTLabel.Pub)));
	}

	@Override
	public ASTStatement visit(ASTDebugStatement stmt) {
		stmt.exp = visit(stmt.exp).left;
		return stmt;
	}
}
