/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.type.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oblivm.compiler.ast.ASTFunction;
import com.oblivm.compiler.ast.ASTFunctionDef;
import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.ast.DefaultStatementExpressionVisitor;
import com.oblivm.compiler.ast.Position;
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
import com.oblivm.compiler.ast.stmt.ASTStatement;
import com.oblivm.compiler.ast.stmt.ASTUsingStatement;
import com.oblivm.compiler.ast.stmt.ASTWhileStatement;
import com.oblivm.compiler.ast.type.ASTArrayType;
import com.oblivm.compiler.ast.type.ASTDummyType;
import com.oblivm.compiler.ast.type.ASTFloatType;
import com.oblivm.compiler.ast.type.ASTFunctionType;
import com.oblivm.compiler.ast.type.ASTIntType;
import com.oblivm.compiler.ast.type.ASTLabel;
import com.oblivm.compiler.ast.type.ASTLabelTypeComparator;
import com.oblivm.compiler.ast.type.ASTNullType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTTupleType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.log.Bugs;
import com.oblivm.compiler.util.Pair;

public class TypeChecker extends DefaultStatementExpressionVisitor<Boolean, List<ASTType>> {

	public TypeResolver resolver = new TypeResolver();
	AssignableChecker ac = new AssignableChecker();
	BitChecker bc = new BitChecker();
	BitInference bi = new BitInference();
	AffineTypeChecker atc = AffineTypeChecker.get();
	BudgetConsumer bcon = new BudgetConsumer();
	
	public ASTFunctionDef function = null;
	public ASTProgram program;

	ResourceBudget budget;
	
	Map<String, ASTType> variableMapping;

	private ASTLabel secureContext;
	
	public boolean check(ASTProgram program) {
		resolver.copy = false;
		program = resolver.resolve(program);
		resolver.copy = true;
		this.program = program;

		if(!bc.check(program)) {
			Bugs.LOG.log("Miss usage of bit variables!");
			System.exit(1);
		}
		
		bi.check(program);
		
		variableMapping = new HashMap<String, ASTType>();

		this.secureContext = ASTLabel.Pub;
		
		for(ASTFunction func : program.functionDef) {
			if(func.baseType == null) {
				variableMapping.put(func.name, func.getType());
			}
		}
		
		for(ASTFunction func : program.functionDef) {
			if(func.typeVariables != null && func.typeVariables.size() > 0) {
				Bugs.LOG.log("Type parameters in function definintion are not supported yet.");
				return false;
			}
			if(func instanceof ASTFunctionDef) {
				this.function = ((ASTFunctionDef)func);
				Map<String, ASTType> old = variableMapping;
				
				setBudget(new ResourceBudget());
				
				variableMapping = new HashMap<String, ASTType>(old);
				if(function.baseType instanceof ASTRecType) {
					ASTRecType rt = (ASTRecType)function.baseType;
					for(ASTExpression e : rt.bitVariables) {
						String x = ((ASTVariableExpression)e).var;
						variableMapping.put(x, ASTIntType.get(32, ASTLabel.Pub));
						budget.addStar(x);
					}
					for(Map.Entry<String, ASTType> ent : rt.fieldsType.entrySet()) {
						variableMapping.put(ent.getKey(), ent.getValue());
						if(atc.isAffine(ent.getValue()))
							budget.addAffine(ent.getKey());
						else
							budget.addStar(ent.getKey());
					}
				}
				ResourceBudget old_budget = budget.clone();
				for(String e : function.bitParameter) {
					variableMapping.put(e, ASTIntType.get(32, ASTLabel.Pub));
					budget.addStar(e);
				}
				for(Pair<ASTType, String> v : function.inputVariables) {
					variableMapping.put(v.right, v.left);
					if(atc.isAffine(v.left))
						budget.addAffine(v.right);
					else
						budget.addStar(v.right);
				}
				for(Pair<ASTType, String> v : function.localVariables) {
					variableMapping.put(v.right, v.left);
					if(atc.isAffine(v.left))
						budget.addAffine(v.right);
					else
						budget.addStar(v.right);
				}
				for(ASTStatement stmt : function.body)
					if(!visit(stmt)) {
//						visit(stmt);
						return false;
					}
				if(budget.consumedSince(old_budget)) {
					StringBuffer sb = new StringBuffer();
					sb.append("In function "+func.name+", variables {");
					boolean first = true;
					for(String var : old_budget.difference(budget)) {
						if(first) first = false;
						else sb.append(", ");
						sb.append(var);
					}
					sb.append("} are consumed");
					Bugs.LOG.log(sb.toString());
					System.exit(1);
				}
				variableMapping = old;
			}
		}
		return true;
	}

	ASTType assertOne(List<ASTType> ret) {
		if(ret == null || ret.size() != 1)
			return null;
		else
			return ret.get(0);
	}

	List<ASTType> buildOne(ASTType ret) {
		if(ret == null)
			return null;
		List<ASTType> r = new ArrayList<ASTType>();
		r.add(ret);
		return r;
	}

	@Override
	public Boolean visit(ASTAssignStatement assignStatement) {
		if(!ac.visit(assignStatement.var))
			return false;
		List<ASTType> expTypes = visit(assignStatement.expr);
		List<ASTType> varTypes = visit(assignStatement.var);
		if(expTypes == null || varTypes == null) {
//			Bugs.LOG.log(assignStatement.beginPosition, assignStatement+" cannot type check!");
//			visit(assignStatement.expr);
//			visit(assignStatement.var);
			return false;
		}
		if(varTypes.size() != expTypes.size() && expTypes.size() != 1) {
			Bugs.LOG.log(assignStatement.beginPosition, "Number of expressions are not matched in: "+assignStatement);
			return false;
		}
		for(ASTType ty : varTypes) {
			if(!ASTLabelTypeComparator.get().compare(secureContext, ty)) {
				Bugs.LOG.log(assignStatement.beginPosition, "Cannot assign to type " + ty.shortName() + " in the secure context");
				return false;
			}
		}
		if(expTypes.size() == 1 && varTypes.size() != 1) {
			// Handle split statement here
			if(!(expTypes.get(0) instanceof ASTRecType)) {
				Bugs.LOG.log(assignStatement.beginPosition, 
						"assigning more expressions (the assigned variable must be of record type)");
				return false;
			}
			ASTRecType ty = (ASTRecType)expTypes.get(0);
			if(varTypes.size() != ty.fields.size()) {
				Bugs.LOG.log(assignStatement.beginPosition, 
						"assigning more or less expressions (number expressions are different from number of fields");
				return false;
			}
			boolean hasBugs = false;
			for(int i=0; i<varTypes.size(); ++i) {
				if(!ty.fieldsType.get(ty.fields.get(i)).canFlowTo(varTypes.get(i))) {
					Bugs.LOG.log(assignStatement.beginPosition, 
							"values of type "+ty.fieldsType.get(ty.fields.get(i))+" cannot to flow to type "+varTypes.get(i));
					hasBugs = true;
				}
			}
			if(hasBugs)
				return false;
			ASTExpression[] exps = new ASTExpression[ty.fields.size()];
			for(int i=0; i<exps.length; ++i)
				exps[i] = new ASTVariableExpression(ty.fields.get(i));
			ASTRecTupleExpression exp = new ASTRecTupleExpression(assignStatement.expr, new ASTTupleExpression(exps));
			assignStatement.expr = exp;
			visit(exp);
		} else {
			for(int i=0; i<expTypes.size(); ++i) {
				if(!expTypes.get(i).canFlowTo(varTypes.get(i)))
					return false;
			}
		}

		bcon.process(assignStatement.var.type, assignStatement.expr);
		if(assignStatement.var instanceof ASTTupleExpression) {
			ASTTupleExpression tuple = (ASTTupleExpression)assignStatement.var;
			if(tuple instanceof ASTRecTupleExpression) {
				// cannot do anything here.
			} else {
				for(int i=0; i<tuple.exps.size(); ++i) {
					ASTExpression exp = tuple.exps.get(i);
					if(exp instanceof ASTVariableExpression) {
						if(atc.isAffine(exp.type)) {
							budget.addAffine(((ASTVariableExpression) exp).var);
						} else {
							budget.addStar(((ASTVariableExpression) exp).var);
						}
					} else if(exp instanceof ASTRecExpression) {
						ASTRecExpression exp1 = (ASTRecExpression)exp;
						if((exp1.base instanceof ASTVariableExpression) && ((ASTVariableExpression)exp1.base).var.equals("this")) {
							if(atc.isAffine(exp1.type))
								budget.addAffine(exp1.field);
							else
								budget.addStar(exp1.field);
						}
					}
				}
			}
		} else if(assignStatement.var instanceof ASTVariableExpression) {
			ASTVariableExpression exp = (ASTVariableExpression) assignStatement.var;
			if(atc.isAffine(exp.type)) {
				budget.addAffine(exp.var);
			} else {
				budget.addStar(exp.var);
			}
		} else if(assignStatement.var instanceof ASTRecExpression) {
			ASTRecExpression exp = (ASTRecExpression)assignStatement.var;
			if((exp.base instanceof ASTVariableExpression) && ((ASTVariableExpression)exp.base).var.equals("this")) {
				if(atc.isAffine(exp.type))
					budget.addAffine(exp.field);
				else
					budget.addStar(exp.field);
			}
		}
		return true;
	}

	@Override
	public Boolean visit(ASTFuncStatement funcStatement) {
		ASTType ty = assertOne(visit(funcStatement.func));
		bcon.process(ty, funcStatement.func);
		return ty != null;
	}

	@Override
	public Boolean visit(ASTIfStatement ifStatement) {
		ASTLabel old = secureContext;
		ASTType ty = assertOne(visit(ifStatement.cond));
		bcon.process(ty, ifStatement.cond);
		if(!ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Pub).canFlowTo(ty)) {
//			visit(ifStatement.cond);
			Bugs.LOG.log(ifStatement.cond.beginPosition, 
					"if guard "+ifStatement.cond+" needs to be a boolean!");
			return false;
		}
		ResourceBudget oldBudget = this.budget;
		setBudget(oldBudget.clone());
		secureContext = secureContext.meet(ty.getLabel());
		for(ASTStatement stmt : ifStatement.trueBranch)
			if(!visit(stmt)) {
//				Bugs.LOG.log(stmt.beginPosition, stmt.toString()+" cannot type check!");
//				visit(stmt);
				secureContext = old;
				return false;
			}
		ResourceBudget trueBudget = this.budget;
		setBudget(oldBudget);
		for(ASTStatement stmt : ifStatement.falseBranch)
			if(!visit(stmt)) {
//				Bugs.LOG.log(stmt.beginPosition, stmt.toString()+" cannot type check!");
//				visit(stmt);
				secureContext = old;
				return false;
			}
		secureContext = old;
		setBudget(trueBudget.join(budget));
		return true;
	}

	@Override
	public Boolean visit(ASTReturnStatement returnStatement) {
		ASTType ty = assertOne(visit(returnStatement.exp));
		bcon.process(this.function.returnType, returnStatement.exp);
		return ty.canFlowTo(this.function.returnType);
	}

	@Override
	public Boolean visit(ASTWhileStatement whileStatement) {
		ResourceBudget oldBudget = this.budget.clone();
		ASTType ty = assertOne(visit(whileStatement.cond));
		if(secureContext == ASTLabel.Secure) {
			Bugs.LOG.log(whileStatement.beginPosition, 
					"loop cannot appear in high secure context");
			return false;
		}
		if(ty.getLabel() == ASTLabel.Secure) {
			Bugs.LOG.log(whileStatement.beginPosition, 
					"Secret bound can appear only in bounded loop");
			return false;
		}
		// TODO handle case if context=Alice/Bob or label = Alice/Bob
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Pub))) {
			Bugs.LOG.log(whileStatement.cond.beginPosition, 
					"loop guard "+whileStatement.cond+" needs to be a boolean!");
			return false;
		}
		for(ASTStatement stmt : whileStatement.body)
			if(!visit(stmt)) {
//				System.err.println(stmt+" cannot type check!");
//				visit(stmt);
				Bugs.LOG.log(stmt.beginPosition, "statement cannot type check!");
				return false;
			}
		if(!oldBudget.equal(budget)) {
			Set<String> set = oldBudget.difference(budget);
			StringBuffer sb = new StringBuffer();
			sb.append("{");
			boolean first = true;
			for(String s : set) {
				if(first) first = false;
				else sb.append(", ");
				sb.append(s);
			}
			sb.append("}");
			Bugs.LOG.log(whileStatement.beginPosition, 
					"While statement repeatedly consume affine type variables "+sb.toString());
//			oldBudget.equal(budget);
			return false;
		}
		return true;
	}

	@Override
	public Boolean visit(ASTBoundedWhileStatement whileStatement) {
		ResourceBudget oldBudget = budget.clone();
		ASTType ty = assertOne(visit(whileStatement.cond));
		bcon.process(ty, whileStatement.cond);
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
			Bugs.LOG.log(whileStatement.cond.beginPosition,
					"loop's guard must be a boolean value");
			return false;
		}
		ASTLabel old = secureContext;
		secureContext = ASTLabel.Secure;
		ty = assertOne(visit(whileStatement.bound));
		if(ty.getLabel() != ASTLabel.Pub) {
			Bugs.LOG.log(whileStatement.bound.beginPosition,
					"Bound in a bounded loop must be a public integer value");
			return false;
		}
		ResourceBudget afterBudget = budget.clone();
		for(ASTStatement stmt : whileStatement.body)
			if(!visit(stmt)) {
//				System.err.println(stmt+" cannot type check!");
//				visit(stmt);
				Bugs.LOG.log(stmt.beginPosition, "statement cannot type check!");
				secureContext = old;
				return false;
			}
		secureContext = old;
		if(!budget.equal(oldBudget)) {
			setBudget(afterBudget);
			Set<String> set = oldBudget.difference(budget);
			StringBuffer sb = new StringBuffer();
			sb.append("{");
			boolean first = true;
			for(String s : set) {
				if(first) first = false;
				else sb.append(", ");
				sb.append(s);
			}
			sb.append("}");
			Bugs.LOG.log(whileStatement.beginPosition, 
					"While statement repeatedly consume affine type variables "+sb.toString());
			return false;
		}
		return true;
	}

	
	@Override
	public List<ASTType> visit(ASTArrayExpression arrayExpression) {
		ASTType ty = assertOne(visit(arrayExpression.var));
		if(!(ty instanceof ASTArrayType)) {
			Bugs.LOG.log(arrayExpression.var.beginPosition, "cannot get the type of "+arrayExpression.var);
			return null;
		}
		ASTArrayType type = (ASTArrayType)ty;
		ty = assertOne(visit(arrayExpression.indexExpr));
		arrayExpression.type = type.type;
		if(!(ty instanceof ASTIntType)) {
			Bugs.LOG.log(arrayExpression.indexExpr.beginPosition, 
					"array index must be of int type");
			return null;
		}
		if(!ty.getLabel().less(type.lab)) {
			Bugs.LOG.log(arrayExpression.indexExpr.beginPosition, 
					"cannot use secret index for a non-ORAM array");
			return null;
		}
		return buildOne(type.type);
	}

	@Override
	public List<ASTType> visit(ASTBinaryExpression binaryExpression) {
		ASTType ty = assertOne(visit(binaryExpression.left));
		if(ty instanceof ASTIntType) {
			ASTIntType ty1 = (ASTIntType)ty;
			ty = assertOne(visit(binaryExpression.right));
			if(!(ty instanceof ASTIntType)) {
				Bugs.LOG.log(binaryExpression.right.beginPosition, 
						"binary operation cannot be operated between an int value and a non-int value");
				return null;
			}
			ASTIntType ty2 = (ASTIntType)ty;
			binaryExpression.type = ASTIntType.get(ty1.getBits(), ty1.getLabel().meet(ty2.getLabel())); 
			return buildOne(binaryExpression.type);
		} else if (ty instanceof ASTFloatType) {
			ASTFloatType ty1 = (ASTFloatType)ty;
			ty = assertOne(visit(binaryExpression.right));
			if(!(ty instanceof ASTFloatType)) {
				Bugs.LOG.log(binaryExpression.left.beginPosition, 
						"binary operation can be operated only between a float value and a non-float value");
				return null;
			}
			ASTFloatType ty2 = (ASTFloatType)ty;
			binaryExpression.type = ASTFloatType.get(ty1.getBits(), ty1.getLabel().meet(ty2.getLabel())); 
			return buildOne(binaryExpression.type);
		} else {
			Bugs.LOG.log(binaryExpression.right.beginPosition, 
					"binary operation can be operated only between int values or float values");
			return null;
		}
	}

	@Override
	public List<ASTType> visit(ASTConstantExpression constantExpression) {
		constantExpression.type = ASTIntType.get(constantExpression.bitSize, ASTLabel.Pub); 
		return buildOne(constantExpression.type);
	}

	@Override
	public List<ASTType> visit(ASTFuncExpression funcExpression) {
		ASTFuncExpression exp = funcExpression;
		ASTExpression obj = exp.obj;
		if(obj instanceof ASTVariableExpression) {
			String name = ((ASTVariableExpression)obj).var;
			if(exp.inputs.size() > 0) {
				// TODO forgot what to do...
			}
			if(this.variableMapping.get(name) instanceof ASTFunctionType) {
				funcExpression.baseType = null;
				ASTFunctionType type = (ASTFunctionType)this.variableMapping.get(name);
				if(this.secureContext == ASTLabel.Secure && !type.isPhantom) {
					Bugs.LOG.log(funcExpression.beginPosition, 
							"cannot call non-phantom function in a high secure context");
					return null;
				}
				if(type.inputTypes.size() != exp.inputs.size()) {
					Bugs.LOG.log(funcExpression.beginPosition, "numbers of inputs mis-match");
					return null;
				}
				int tpn = 0;
				if(type.typeParameter != null) tpn = type.typeParameter.size();
				int tvn = 0;
				if(funcExpression.typeVars != null) tvn = funcExpression.typeVars.size();
				if(tpn != tvn) {
					Bugs.LOG.log(funcExpression.beginPosition, "numbers of type parameters mis-match");
					return null;
				}
				resolver.typeVars = new HashMap<String, ASTType>();
				if(tpn > 0) {
					for(int i=0; i<type.typeParameter.size(); ++i)
						resolver.typeVars.put(((ASTVariableType)type.typeParameter.get(i)).var, funcExpression.typeVars.get(i));
					type = (ASTFunctionType)resolver.visit(type);
				}
				obj.type = type;
				
				if(function.baseType != null) {
					ASTRecType rt = (ASTRecType)function.baseType;
					if(rt.typeVariables != null) {
						for(ASTType ty : rt.typeVariables) {
							ASTVariableType vt = (ASTVariableType)ty;
							resolver.typeVars.put(vt.var, ty);
						}
					}
				}
				if(funcExpression.bitParameters.size() != type.bitParameter.size()) {
					Bugs.LOG.log(funcExpression.beginPosition, 
							"numbers of bit parameters mis-match");
					return null;
				}
				resolver.bitVars = new HashMap<String, ASTExpression>();
				for(int i=0; i<funcExpression.bitParameters.size(); ++i) {
					resolver.bitVars.put(type.bitParameter.get(i), funcExpression.bitParameters.get(i));
				}
				for(int i=0; i<type.inputTypes.size(); ++i) {
					ASTType t = assertOne(visit(exp.inputs.get(i).right));
					ASTType targetType = resolver.visit(type.inputTypes.get(i)); 
					if(!t.canFlowTo(targetType)) {
						Bugs.LOG.log(exp.inputs.get(i).right.beginPosition,
								"cannot coerce the provided input to the target type");
						return null;
					}
					funcExpression.inputTypes.add(targetType);
					
				}
				exp.type = resolver.visit(type.returnType);
				return buildOne(exp.type);
			}
			for(ASTFunction func : program.functionDef) {
				if(func.name.equals(name) && func.baseType == null) {
					funcExpression.baseType = null;
					if(func.bitParameter.size() != funcExpression.bitParameters.size()) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"numbers of bit parameters mis-match");
						return null;
					}
					if(func.typeVariables.size() != funcExpression.typeVars.size()) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"numbers of type variables mis-match");
						return null;
					}
					if(func.inputVariables.size() != exp.inputs.size()) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"numbers of input variables mis-match");
						return null;
					}
					
					resolver.typeVars = new HashMap<String, ASTType>();
					resolver.bitVars = new HashMap<String, ASTExpression>();
					for(int i=0; i<func.bitParameter.size(); ++i)
						resolver.bitVars.put(func.bitParameter.get(i), funcExpression.bitParameters.get(i));
					if(func.typeVariables != null) {
						for(int i=0; i<func.typeVariables.size(); ++i)
							resolver.typeVars.put(func.typeVariables.get(i).var, funcExpression.typeVars.get(i));
					}

					obj.type = resolver.visit(func.getType());
					if(this.secureContext == ASTLabel.Secure && !func.getType().isPhantom) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"cannot call non-phantom function in a high secure context");
						return null;
					}

					for(int i=0; i<func.inputVariables.size(); ++i) {
						ASTType t = assertOne(visit(exp.inputs.get(i).right));
						ASTType ty = resolver.visit(func.inputVariables.get(i).left);
						if(!t.canFlowTo(ty)) {
							Bugs.LOG.log(exp.inputs.get(i).right.beginPosition,
									"cannot coerce the provided input to the target type");
							return null;
						}
						funcExpression.inputTypes.add(ty);
					}
					funcExpression.type = resolver.visit(func.returnType);
					return buildOne(funcExpression.type);
				}
			}
			funcExpression.obj = new ASTRecExpression(new ASTVariableExpression("this"), name);
			return visit(funcExpression);
		} else if(obj instanceof ASTRecExpression) {
			ASTRecExpression recObj = (ASTRecExpression)obj;
			String name = recObj.field;
			ASTType baseType = assertOne(visit(recObj.base));
			funcExpression.baseType = baseType;
			if(baseType == null) {
				Bugs.LOG.log(recObj.base.beginPosition, "unknown object type");
				return null;
			}
			for(ASTFunction func : program.functionDef) {
				if(func.name.equals(name) && func.baseType.instance(baseType)) {
					if(func.inputVariables.size() != exp.inputs.size()) { 
						Bugs.LOG.log(funcExpression.beginPosition, 
								"numbers of input variables mis-match");
						return null;
					}
					if(func.typeVariables != null && func.typeVariables.size() != 0) {
						if(funcExpression.typeVars == null ||
								func.typeVariables.size() != funcExpression.typeVars.size()) {
							Bugs.LOG.log(funcExpression.beginPosition, 
									"numbers of type variables mis-match");
							return null;
						}
					} else {
						if(funcExpression.typeVars != null &&
								0 != funcExpression.typeVars.size()) {
							Bugs.LOG.log(funcExpression.beginPosition, 
									"numbers of type variables mis-match");
							return null;
						}
					}
					if(this.secureContext == ASTLabel.Secure && !func.getType().isPhantom) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"cannot call non-phantom function in a high secure context");
						return null;
					}

					resolver.typeVars = new HashMap<String, ASTType>();
					resolver.bitVars = new HashMap<String, ASTExpression>();
					if(this.function.baseType instanceof ASTRecType) {
						ASTRecType rt = (ASTRecType)this.function.baseType;
						if(rt.typeVariables != null) {
							for(int i = 0; i<rt.typeVariables.size(); ++i) {
								ASTVariableType vt = (ASTVariableType)rt.typeVariables.get(i);
								resolver.typeVars.put(vt.var, vt);
							}
						}
						for(int i=0; i < rt.bitVariables.size(); ++i) {
							ASTVariableExpression vt = (ASTVariableExpression)rt.bitVariables.get(i);
							resolver.bitVars.put(vt.var, vt);
						}
					}
					
					// The following code block looks weird, but should be correct
					if(func.baseType instanceof ASTRecType) {
						ASTRecType rt = (ASTRecType)func.baseType;
						if(rt.typeVariables != null) {
							for(int i = 0; i<rt.typeVariables.size(); ++i) {
								ASTVariableType vt = (ASTVariableType)rt.typeVariables.get(i);
								resolver.typeVars.put(vt.var, ((ASTRecType)baseType).typeVariables.get(i));
							}
						}
						for(int i=0; i < rt.bitVariables.size(); ++i) {
							ASTVariableExpression vt = (ASTVariableExpression)rt.bitVariables.get(i);
							resolver.bitVars.put(vt.var, ((ASTRecType)baseType).bitVariables.get(i));
						}
					}
					// code block ends
					
					if(exp.bitParameters.size() != func.bitParameter.size()) {
						Bugs.LOG.log(funcExpression.beginPosition, 
								"The numbers of function's bit parameters do not match.");
						return null;
					}
					
					for(int i=0; i<exp.bitParameters.size(); ++i) {
						resolver.bitVars.put(func.bitParameter.get(i), exp.bitParameters.get(i));
					}
					recObj.type = resolver.visit(func.getType());
					for(int i=0; i<exp.inputs.size(); ++i) {
						ASTType t = assertOne(visit(exp.inputs.get(i).right));
						ASTType ty = resolver.visit(func.inputVariables.get(i).left); 
						if(!t.canFlowTo(ty)) {
//							t.canFlowTo(resolver.visit(func.inputVariables.get(i).left));
							Bugs.LOG.log(exp.inputs.get(i).right.beginPosition,
									"cannot coerce the provided input to the target type");
							return null;
						}
						funcExpression.inputTypes.add(ty);
					}
					funcExpression.type = resolver.visit(func.returnType); 
					return buildOne(funcExpression.type);
				}
			}
			Bugs.LOG.log(funcExpression.beginPosition, 
					"unknown function call");
			return null;
		} else {
			Bugs.LOG.log(funcExpression.beginPosition, 
					"unknown object");
			return null;
		}
	}

	@Override
	public List<ASTType> visit(ASTRecExpression rec) {
		ASTType ty = assertOne(visit(rec.base));
		while(ty instanceof ASTDummyType) {
			ty = ((ASTDummyType)ty).type;
		}
		if(!(ty instanceof ASTRecType)) {
			Bugs.LOG.log(rec.base.beginPosition, "Not a record type");
			return null;
		}
		ASTRecType type = (ASTRecType)ty;
		rec.type = type.fieldsType.get(rec.field);
		return buildOne(rec.type);
	}

	@Override
	public List<ASTType> visit(ASTRecTupleExpression tuple) {
		ASTType ty = assertOne(visit(tuple.base));
		while(ty instanceof ASTDummyType) {
			ty = ((ASTDummyType)ty).type;
		}
		if(!(ty instanceof ASTRecType)) {
			Bugs.LOG.log(tuple.base.beginPosition, "Not a record type");
			return null;
		}
		ASTRecType type = (ASTRecType)ty;
		Map<String, ASTType> old = new HashMap<String, ASTType>(this.variableMapping);
		for(Map.Entry<String, ASTType> ent : type.fieldsType.entrySet()) {
			this.variableMapping.put(ent.getKey(), ent.getValue());
		}
		List<ASTType> ret = visit((ASTTupleExpression)tuple);
		this.variableMapping = old;
		return ret;
	}

	@Override
	public List<ASTType> visit(ASTTupleExpression tuple) {
		List<ASTType> ret = new ArrayList<ASTType>();
		for(int i=0; i<tuple.exps.size(); ++i) {
			ASTType ty = assertOne(visit(tuple.exps.get(i)));
			if(ty == null) {
				Bugs.LOG.log(tuple.exps.get(i).beginPosition, "cannot type check "+tuple.exps.get(i));
				return null;
			}
			ret.add(ty);
		}
		tuple.type = new ASTTupleType(ret);
		return ret;
	}

	@Override
	public List<ASTType> visit(ASTVariableExpression variableExpression) {
		if(variableExpression.var.equals("this")) {
			variableExpression.type = this.function.baseType;
			return buildOne(this.function.baseType);
		}
		if(this.variableMapping.containsKey(variableExpression.var)) {
			variableExpression.type = this.variableMapping.get(variableExpression.var);
			return buildOne(variableExpression.type);
		} else {
			for(Pair<ASTType, String> p : this.function.inputVariables)
				if(p.right.equals(variableExpression.var)) {
					variableExpression.type = p.left;
					return buildOne(p.left);
				}
			for(Pair<ASTType, String> p : this.function.localVariables)
				if(p.right.equals(variableExpression.var)) {
					variableExpression.type = p.left;
					return buildOne(p.left);
				}
		}
		Bugs.LOG.log(variableExpression.beginPosition, "Unknown variable "+variableExpression.var);
		return null;
	}

	@Override
	public List<ASTType> visit(ASTOrPredicate orPredicate) {
		ASTType ty = assertOne(visit(orPredicate.left));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
			Bugs.LOG.log(orPredicate.left.beginPosition, 
					"a boolean value is required");
			return null;
		}
		ASTIntType ty1 = (ASTIntType)ty;
		ty = assertOne(visit(orPredicate.right));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
			Bugs.LOG.log(orPredicate.right.beginPosition, 
					"a boolean value is required");
			return null;
		}
		ASTIntType ty2 = (ASTIntType)ty;
		orPredicate.type = ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel())); 
		return buildOne(orPredicate.type);
	}

	@Override
	public List<ASTType> visit(ASTAndPredicate andPredicate) {
		ASTType ty = assertOne(visit(andPredicate.left));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
//			assertOne(visit(andPredicate.left));
			Bugs.LOG.log(andPredicate.left.beginPosition, 
					"a boolean value is required");
			return null;
		}
		ASTIntType ty1 = (ASTIntType)ty;
		ty = assertOne(visit(andPredicate.right));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
			Bugs.LOG.log(andPredicate.right.beginPosition, 
					"a boolean value is required");
			return null;
		}
		ASTIntType ty2 = (ASTIntType)ty;
		andPredicate.type = ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel())); 
		return buildOne(andPredicate.type);
	}

	private boolean isDummyType(ASTType type) {
		return type instanceof ASTDummyType || type instanceof ASTNullType;
	}
	
	private ASTType cmp(Position lPos, ASTType lty, Position rPos, ASTType rty, REL_OP op) {
		if(isDummyType(rty) && !isDummyType(lty)) {
			return cmp(rPos, rty, lPos, lty, op);
		}
		if(lty instanceof ASTIntType) {
			ASTIntType ty1 = (ASTIntType)lty;
			if(!(rty instanceof ASTIntType)) {
				Bugs.LOG.log(lPos, 
						"Cannot compare an Integer value with a non-integer value.");
				return null;
			}
			ASTIntType ty2 = (ASTIntType)rty;
			if(!ty1.canFlowTo(ty2) && !ty2.canFlowTo(ty1)) {
				Bugs.LOG.log(lPos,
						"Type "+ty1+" doesn't match type "+ty2);
				return null;
			}
			return ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel()));
		} else if(lty instanceof ASTFloatType) {
			ASTFloatType ty1 = (ASTFloatType)lty;
			if(!(rty instanceof ASTFloatType)) {
				Bugs.LOG.log(lPos,
						"Cannot compare a floating value with a non-floating value.");
				return null;
			}
			ASTFloatType ty2 = (ASTFloatType)rty;
			if(!ty1.canFlowTo(ty2) && !ty2.canFlowTo(ty1)) {
				Bugs.LOG.log(lPos,
						"Type "+ty1+" doesn't match type "+ty2);
				return null;
			}
			return ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel()));
		} else if(lty instanceof ASTDummyType) {
			ASTType type = ((ASTDummyType)lty).type;
			if(rty instanceof ASTNullType) {
				if(op != REL_OP.EQ && op != REL_OP.NEQ) {
					Bugs.LOG.log(lPos,
							"Can only compare the equality on the null value");
					return null;
				}
				return ASTIntType.get(new ASTConstantExpression(1), type.getLabel());
			} else if(rty instanceof ASTDummyType) {
				return cmp(lPos, type, rPos, ((ASTDummyType)rty).type, op);
			} else {
				return cmp(lPos, type, rPos, rty, op);
			}
		} else if(lty instanceof ASTNullType) {
			if(op != REL_OP.EQ && op != REL_OP.NEQ) {
				Bugs.LOG.log(lPos,
						"Can only compare the equality of two nullable types");
				return null;
			}
			if(isDummyType(rty))
				return ASTIntType.get(new ASTConstantExpression(1), rty.getLabel());
			else {
				Bugs.LOG.log(rPos, "must be a dummy type");
				return null;
			}
		} else {
			Bugs.LOG.log(lPos, "uncomparable types");
			return null;
		}
	}
	
	@Override
	public List<ASTType> visit(ASTBinaryPredicate binaryPredicate) {
		ASTType lty = assertOne(visit(binaryPredicate.left));
		ASTType rty = assertOne(visit(binaryPredicate.right));
		if(lty == null) {
			Bugs.LOG.log(binaryPredicate.left.beginPosition,
					"Cannot type check "+binaryPredicate.left);
//			visit(binaryPredicate.left);
			return null;
		}
		if(rty == null) {
			Bugs.LOG.log(binaryPredicate.right.beginPosition,
					"Cannot type check "+binaryPredicate.right);
//			visit(binaryPredicate.right);
			return null;
		}
		binaryPredicate.type = cmp(binaryPredicate.left.beginPosition, lty, 
				binaryPredicate.right.beginPosition, rty, binaryPredicate.op);
		return buildOne(binaryPredicate.type);
	}

	@Override
	public List<ASTType> visit(ASTNewObjectExpression exp) {
		for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
			ASTType ty = assertOne(visit(ent.getValue()));
			if(!exp.type.fieldsType.containsKey(ent.getKey())) {
				Bugs.LOG.log(ent.getValue().beginPosition, "unknown field");
			}
			if(!ty.canFlowTo(exp.type.fieldsType.get(ent.getKey()))) {
				Bugs.LOG.log(ent.getValue().beginPosition, 
						"cannot coerce the provided value to the field "+ent.getKey());
				return null;
			}
		}
		((ASTExpression)exp).type = exp.type;
		return buildOne(exp.type);
	}

	public void setContext(ASTProgram program, ASTFunctionDef function) {
		this.program = program;
		this.function = function;
		
		this.function = ((ASTFunctionDef)function);
		Map<String, ASTType> old = variableMapping;
		if(old == null)
			variableMapping = new HashMap<String, ASTType>();
		else
			variableMapping = new HashMap<String, ASTType>(old);
		if(function.baseType instanceof ASTRecType) {
			ASTRecType rt = (ASTRecType)function.baseType;
			for(ASTExpression e : rt.bitVariables)
				variableMapping.put(((ASTVariableExpression)e).var, ASTIntType.get(32, ASTLabel.Pub));
		}
		for(Pair<ASTType, String> v : function.inputVariables) {
			variableMapping.put(v.right, v.left);
		}
		for(Pair<ASTType, String> v : function.localVariables) {
			variableMapping.put(v.right, v.left);
		}

	}

	@Override
	public List<ASTType> visit(ASTFloatConstantExpression constantExpression) {
		constantExpression.type = ASTFloatType.get(constantExpression.bitSize, ASTLabel.Pub);
		return buildOne(constantExpression.type);
	}

	@Override
	public List<ASTType> visit(ASTLogExpression tuple) {
		ASTType ty = assertOne(visit(tuple.exp));
		if(!(ty instanceof ASTIntType)) {
			Bugs.LOG.log(tuple.exp.beginPosition,
					"The input of log() function must be of int type");
			return null;
		}
		tuple.type = ASTIntType.get(32, ASTLabel.Pub);
		return buildOne(tuple.type);
	}

	@Override
	public List<ASTType> visit(ASTRangeExpression tuple) {
		ASTType sty = assertOne(visit(tuple.source));
		if(!(sty instanceof ASTIntType)) {
			Bugs.LOG.log(tuple.source.beginPosition,
					"range expression can only be operated over int values");
			return null;
		}
		ASTType lty = assertOne(visit(tuple.rangel));
		if(!(lty instanceof ASTIntType) && 
				((ASTIntType)lty).getLabel() == ASTLabel.Pub) {
			Bugs.LOG.log(tuple.rangel.beginPosition,
					"range values must be public int");
			return null;
		}
		if(tuple.ranger != null) {
			ASTType rty = assertOne(visit(tuple.ranger));
			if(!(rty instanceof ASTIntType) && 
					((ASTIntType)rty).getLabel() == ASTLabel.Pub) {
				Bugs.LOG.log(tuple.ranger.beginPosition,
						"range values must be public int");
				return null;
			}
		}
		tuple.type = ASTIntType.get(
				tuple.ranger == null ? new ASTConstantExpression(1) :
					new ASTBinaryExpression(
						tuple.ranger, BOP.SUB, tuple.rangel),
				((ASTIntType)sty).getLabel()
				);
		return buildOne(tuple.type);
	}

	@Override
	@Deprecated
	public Boolean visit(ASTOnDummyStatement stmt) {
		Map<String, ASTType> old = new HashMap<String, ASTType>();
		for(Map.Entry<String, ASTType> ent : variableMapping.entrySet())
			old.put(ent.getKey(), ent.getValue());
		for(int i=0; i<stmt.condList.size(); ++i) {
			Pair<String, ASTExpression> pair = stmt.condList.get(i);
			ASTType type = assertOne(visit(pair.right));
			if(!(type.canFlowTo(ASTNullType.get()))) {
				System.err.println("ONREAL or ONDUMMY should noly be checked on dummy types!");
				return false;
			}
			if(pair.left != null)
				variableMapping.put(pair.left, type);
		}
		for(int i=0; i<stmt.body.size(); ++i) {
			if(!visit(stmt.body.get(i))) {
				System.err.println("Statement "+stmt.body.get(i)+" can't type check.");
				return false;
			}
		}
		variableMapping = old;
		return true;
	}

	@Override
	public List<ASTType> visit(ASTNullExpression exp) {
		return buildOne(ASTNullType.get());
	}

	Set<String> usingVariable = new HashSet<String>();

	@Override
	public Boolean visit(ASTUsingStatement stmt) {
		Map<String, ASTType> old1 = new HashMap<String, ASTType>(this.variableMapping);
		for(Pair<String, ASTExpression> ent : stmt.use) {
			if(!ac.visit(ent.right)) {
				Bugs.LOG.log(ent.right.beginPosition,
						"the expressions the using header must be assignable");
				return false;
			}
			if(this.function.containVariables(ent.left)) {
				Bugs.LOG.log(ent.right.beginPosition,
						"the using block's variable "+ent.left+" is a local variable or an input variable!");
				return false;
			}
			if(usingVariable.contains(ent.left)) {
				Bugs.LOG.log(ent.right.beginPosition,
						"the using block's variable "+ent.left+" is duplicated!");
				return false;
			}
			usingVariable.add(ent.left);
			ASTType ty = assertOne(visit(ent.right));
			this.variableMapping.put(ent.left, ty);
			bcon.cancelMode = false;
			bcon.targetType = ty;
			bcon.visit(ent.right);
		}
		ResourceBudget old = this.budget.clone();
		for(Pair<String, ASTExpression> ent : stmt.use) {
			if(atc.isAffine(ent.right.type))
				budget.addAffine(ent.left);
			else
				budget.addStar(ent.left);
		}
		ResourceBudget before = budget.clone();
		for(ASTStatement st : stmt.body) {
			if(!visit(st)) {
				Bugs.LOG.log(st.beginPosition,
						"statement doesn't type check.");
				return false;
			}
		}
		if(!before.equal(budget)) {
			Bugs.LOG.log(stmt.beginPosition,
					"Using block consuming variables!");
			return false;
		}
		for(Pair<String, ASTExpression> ent : stmt.use) {
			usingVariable.remove(ent.left);
		}
		setBudget(old);
		this.variableMapping = old1;
		return true;
	}

	private void setBudget(ResourceBudget budget) {
		this.budget = budget;
		this.bcon.budget = budget;
	}

	@Override
	public List<ASTType> visit(ASTSizeExpression exp) {
		return buildOne(ASTIntType.get(32, ASTLabel.Pub));
	}
}
