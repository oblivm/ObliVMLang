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
import com.oblivm.compiler.util.Pair;

public class TypeChecker extends DefaultStatementExpressionVisitor<Boolean, List<ASTType>> {

	public TypeResolver resolver = new TypeResolver();
	AssignableChecker ac = new AssignableChecker();
	BitChecker bc = new BitChecker();
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

		if(!bc.check(program))
			return false;
		
		variableMapping = new HashMap<String, ASTType>();

		this.secureContext = ASTLabel.Pub;
		
		for(ASTFunction func : program.functionDef) {
			if(func.baseType == null) {
				variableMapping.put(func.name, func.getType());
			}
		}
		
		for(ASTFunction func : program.functionDef)
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
						visit(stmt);
						return false;
					}
				variableMapping = old;
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
			System.err.println("Statement:\n"+assignStatement+"\ncannot type check!");
			visit(assignStatement.expr);
			visit(assignStatement.var);
			return false;
		}
		if(varTypes.size() != expTypes.size() && expTypes.size() != 1) {
			System.err.println("Number of expressions are not matched in: "+assignStatement);
			return false;
		}
		for(ASTType ty : varTypes) {
			if(!ASTLabelTypeComparator.get().compare(secureContext, ty)) {
				System.err.println("Cannot assign to type " + ty.shortName() + " in the secure context");
				return false;
			}
		}
		if(expTypes.size() == 1 && varTypes.size() != 1) {
			// Handle split statement here
			if(!(expTypes.get(0) instanceof ASTRecType))
				return false;
			ASTRecType ty = (ASTRecType)expTypes.get(0);
			if(varTypes.size() != ty.fields.size())
				return false;
			for(int i=0; i<varTypes.size(); ++i) {
				if(!ty.fieldsType.get(ty.fields.get(i)).canFlowTo(varTypes.get(i)))
					return false;
			}
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
			visit(ifStatement.cond);
			System.err.println("Condition: "+ifStatement.cond+" is not a boolean!");
			return false;
		}
		ResourceBudget oldBudget = this.budget;
		setBudget(oldBudget.clone());
		secureContext = secureContext.meet(ty.getLabel());
		for(ASTStatement stmt : ifStatement.trueBranch)
			if(!visit(stmt)) {
				System.err.println(stmt+" cannot type check!");
				visit(stmt);
				secureContext = old;
				return false;
			}
		ResourceBudget trueBudget = this.budget;
		setBudget(oldBudget);
		for(ASTStatement stmt : ifStatement.falseBranch)
			if(!visit(stmt)) {
				System.err.println(stmt+" cannot type check!");
				visit(stmt);
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
		if(secureContext == ASTLabel.Secure || ty.getLabel() == ASTLabel.Secure) {
			System.err.println("Secret bound can appear only in bounded loop");
			return false;
		}
		// TODO handle case if context=Alice/Bob or label = Alice/Bob
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Pub)))
			return false;
		for(ASTStatement stmt : whileStatement.body)
			if(!visit(stmt)) {
				System.err.println(stmt+" cannot type check!");
				visit(stmt);
				return false;
			}
		if(!oldBudget.equal(budget)) {
			System.err.println("While statement repeatedly consume affine type variables");
			oldBudget.equal(budget);
			return false;
		}
		return true;
	}

	@Override
	public Boolean visit(ASTBoundedWhileStatement whileStatement) {
		ResourceBudget oldBudget = budget.clone();
		ASTType ty = assertOne(visit(whileStatement.cond));
		bcon.process(ty, whileStatement.cond);
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure)))
			return false;
		ASTLabel old = secureContext;
		secureContext = ASTLabel.Secure;
		ty = assertOne(visit(whileStatement.bound));
		if(ty.getLabel() != ASTLabel.Pub) {
			System.err.println("Bound in a bounded loop must be a public integer value");
			return false;
		}
		ResourceBudget afterBudget = budget.clone();
		for(ASTStatement stmt : whileStatement.body)
			if(!visit(stmt)) {
				System.err.println(stmt+" cannot type check!");
				visit(stmt);
				secureContext = old;
				return false;
			}
		secureContext = old;
		if(!budget.equal(oldBudget)) {
			setBudget(afterBudget);
			System.err.println("While loop repeatedly use affine type variables");
			return false;
		}
		return true;
	}

	
	@Override
	public List<ASTType> visit(ASTArrayExpression arrayExpression) {
		ASTType ty = assertOne(visit(arrayExpression.var));
		if(!(ty instanceof ASTArrayType))
			return null;
		ASTArrayType type = (ASTArrayType)ty;
		ty = assertOne(visit(arrayExpression.indexExpr));
		arrayExpression.type = type.type; 
		if(!(ty instanceof ASTIntType) || !ty.getLabel().less(type.lab))
			return null;
		return buildOne(type.type);
	}

	@Override
	public List<ASTType> visit(ASTBinaryExpression binaryExpression) {
		ASTType ty = assertOne(visit(binaryExpression.left));
		if(ty instanceof ASTIntType) {
			ASTIntType ty1 = (ASTIntType)ty;
			ty = assertOne(visit(binaryExpression.right));
			if(!(ty instanceof ASTIntType))
				return null;
			ASTIntType ty2 = (ASTIntType)ty;
			binaryExpression.type = ASTIntType.get(ty1.getBits(), ty1.getLabel().meet(ty2.getLabel())); 
			return buildOne(binaryExpression.type);
		} else if (ty instanceof ASTFloatType) {
			ASTFloatType ty1 = (ASTFloatType)ty;
			ty = assertOne(visit(binaryExpression.right));
			if(!(ty instanceof ASTFloatType))
				return null;
			ASTFloatType ty2 = (ASTFloatType)ty;
			binaryExpression.type = ASTFloatType.get(ty1.getBits(), ty1.getLabel().meet(ty2.getLabel())); 
			return buildOne(binaryExpression.type);
		} else
			return null;
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
				if(type.inputTypes.size() != exp.inputs.size()) {
					System.err.println("input numbers mis-match in "+funcExpression);
					return null;
				}
				int tpn = 0;
				if(type.typeParameter != null) tpn = type.typeParameter.size();
				int tvn = 0;
				if(funcExpression.typeVars != null) tvn = funcExpression.typeVars.size();
				if(tpn != tvn) {
					System.err.println("numbers of type parameters mis-match in "+funcExpression);
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
					System.err.println("numbers of bit parameters mis-match in "+funcExpression);
					return null;
				}
				resolver.bitVars = new HashMap<String, ASTExpression>();
				for(int i=0; i<funcExpression.bitParameters.size(); ++i) {
					resolver.bitVars.put(type.bitParameter.get(i), funcExpression.bitParameters.get(i));
				}
				for(int i=0; i<type.inputTypes.size(); ++i) {
//					System.out.println(funcExpression);
					ASTType t = assertOne(visit(exp.inputs.get(i).right));
					ASTType targetType = resolver.visit(type.inputTypes.get(i)); 
					if(!t.canFlowTo(targetType))
						return null;
					funcExpression.inputTypes.add(targetType);
					
				}
				exp.type = resolver.visit(type.returnType);
				return buildOne(exp.type);
			}
			for(ASTFunction func : program.functionDef) {
				if(func.name.equals(name) && func.baseType == null) {
					funcExpression.baseType = null;
					if(func.bitParameter.size() != funcExpression.bitParameters.size())
						return null;
					if(func.typeVariables.size() != funcExpression.typeVars.size()) {
						return null;
					}
					if(func.inputVariables.size() != exp.inputs.size())
						return null;
					
					resolver.typeVars = new HashMap<String, ASTType>();
					resolver.bitVars = new HashMap<String, ASTExpression>();
					for(int i=0; i<func.bitParameter.size(); ++i)
						resolver.bitVars.put(func.bitParameter.get(i), funcExpression.bitParameters.get(i));
					if(func.typeVariables != null) {
						for(int i=0; i<func.typeVariables.size(); ++i)
							resolver.typeVars.put(func.typeVariables.get(i).var, funcExpression.typeVars.get(i));
					}

					obj.type = resolver.visit(func.getType());

					for(int i=0; i<func.inputVariables.size(); ++i) {
						ASTType t = assertOne(visit(exp.inputs.get(i).right));
						ASTType ty = resolver.visit(func.inputVariables.get(i).left);
						if(!t.canFlowTo(ty))
							return null;
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
			if(baseType == null)
				return null;
			for(ASTFunction func : program.functionDef) {
				if(func.name.equals(name) && func.baseType.instance(baseType)) {
					if(func.inputVariables.size() != exp.inputs.size()) 
							// Deprecated + ((func.isDummy && !(func instanceof ASTFunctionNative)) ? 1 : 0))
						return null;
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
					// The following code block looks weird
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
						System.err.println("The numbers of function's bit parameters do not match.");
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
							t.canFlowTo(resolver.visit(func.inputVariables.get(i).left));
							System.err.println("Input type doesn't match!");
							return null;
						}
						funcExpression.inputTypes.add(ty);
					}
					funcExpression.type = resolver.visit(func.returnType); 
					return buildOne(funcExpression.type);
				}
			}
			return null;
		} else {
			System.err.println(obj+" is not a function!");
			return null;
		}
	}

	@Override
	public List<ASTType> visit(ASTRecExpression rec) {
		ASTType ty = assertOne(visit(rec.base));
		while(ty instanceof ASTDummyType) {
			ty = ((ASTDummyType)ty).type;
		}
		if(!(ty instanceof ASTRecType))
			return null;
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
		if(!(ty instanceof ASTRecType))
			return null;
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
				visit(tuple.exps.get(i));
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
		return null;
	}

	@Override
	public List<ASTType> visit(ASTOrPredicate orPredicate) {
		ASTType ty = assertOne(visit(orPredicate.left));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure)))
			return null;
		ASTIntType ty1 = (ASTIntType)ty;
		ty = assertOne(visit(orPredicate.right));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure)))
			return null;
		ASTIntType ty2 = (ASTIntType)ty;
		orPredicate.type = ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel())); 
		return buildOne(orPredicate.type);
	}

	@Override
	public List<ASTType> visit(ASTAndPredicate andPredicate) {
		ASTType ty = assertOne(visit(andPredicate.left));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure))) {
			assertOne(visit(andPredicate.left));
			return null;
		}
		ASTIntType ty1 = (ASTIntType)ty;
		ty = assertOne(visit(andPredicate.right));
		if(!ty.canFlowTo(ASTIntType.get(new ASTConstantExpression(1), ASTLabel.Secure)))
			return null;
		ASTIntType ty2 = (ASTIntType)ty;
		andPredicate.type = ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel())); 
		return buildOne(andPredicate.type);
	}

	private boolean isDummyType(ASTType type) {
		return type instanceof ASTDummyType || type instanceof ASTNullType;
	}
	
	private ASTType cmp(ASTType lty, ASTType rty, REL_OP op) {
		if(isDummyType(rty) && !isDummyType(lty)) {
			return cmp(rty, lty, op);
		}
		if(lty instanceof ASTIntType) {
			ASTIntType ty1 = (ASTIntType)lty;
			if(!(rty instanceof ASTIntType)) {
				System.err.println("Cannot compare an Integer value with a non-integer value.");
				return null;
			}
			ASTIntType ty2 = (ASTIntType)rty;
			if(!ty1.canFlowTo(ty2) && !ty2.canFlowTo(ty1)) {
				System.err.println("Type "+ty1+" doesn't match type "+ty2);
				return null;
			}
			return ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel()));
		} else if(lty instanceof ASTFloatType) {
			ASTFloatType ty1 = (ASTFloatType)lty;
			if(!(rty instanceof ASTFloatType)) {
				System.err.println("Cannot compare a floating value with a non-floating value.");
				return null;
			}
			ASTFloatType ty2 = (ASTFloatType)rty;
			if(!ty1.canFlowTo(ty2) && !ty2.canFlowTo(ty1)) {
				System.err.println("Type "+ty1+" doesn't match type "+ty2);
				return null;
			}
			return ASTIntType.get(new ASTConstantExpression(1), ty1.getLabel().meet(ty2.getLabel()));
		} else if(lty instanceof ASTDummyType) {
			ASTType type = ((ASTDummyType)lty).type;
			if(rty instanceof ASTNullType) {
				if(op != REL_OP.EQ && op != REL_OP.NEQ) {
					System.err.println("Can only compare the equality on the null value");
					return null;
				}
				return ASTIntType.get(new ASTConstantExpression(1), type.getLabel());
			} else if(rty instanceof ASTDummyType) {
				return cmp(type, ((ASTDummyType)rty).type, op);
			} else {
				return cmp(type, rty, op);
			}
		} else if(lty instanceof ASTNullType) {
			if(op != REL_OP.EQ && op != REL_OP.NEQ) {
				System.err.println("Can only compare the equality of two nullable types");
				return null;
			}
			if(isDummyType(rty))
				return ASTIntType.get(new ASTConstantExpression(1), rty.getLabel());
			else
				return null;
		} else
			return null;
	}
	
	@Override
	public List<ASTType> visit(ASTBinaryPredicate binaryPredicate) {
		ASTType lty = assertOne(visit(binaryPredicate.left));
		ASTType rty = assertOne(visit(binaryPredicate.right));
		if(lty == null) {
			System.err.println("Cannot type check "+binaryPredicate.left);
			visit(binaryPredicate.left);
			return null;
		}
		if(rty == null) {
			System.err.println("Cannot type check "+binaryPredicate.right);
			visit(binaryPredicate.right);
			return null;
		}
		binaryPredicate.type = cmp(lty, rty, binaryPredicate.op);
		return buildOne(binaryPredicate.type);
	}

	@Override
	public List<ASTType> visit(ASTNewObjectExpression exp) {
		for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
			ASTType ty = assertOne(visit(ent.getValue()));
			if(!exp.type.fieldsType.containsKey(ent.getKey())
					|| !ty.canFlowTo(exp.type.fieldsType.get(ent.getKey())))
				return null;
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
		if(!(ty instanceof ASTIntType))
			return null;
		tuple.type = ASTIntType.get(32, ASTLabel.Pub);
		return buildOne(tuple.type);
	}

	@Override
	public List<ASTType> visit(ASTRangeExpression tuple) {
		ASTType sty = assertOne(visit(tuple.source));
		if(!(sty instanceof ASTIntType))
			return null;
		ASTType lty = assertOne(visit(tuple.rangel));
		if(!(lty instanceof ASTIntType) && 
				((ASTIntType)lty).getLabel() == ASTLabel.Pub)
			return null;
		if(tuple.ranger != null) {
			ASTType rty = assertOne(visit(tuple.ranger));
			if(!(rty instanceof ASTIntType) && 
					((ASTIntType)rty).getLabel() == ASTLabel.Pub)
				return null;
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
				System.err.println("the expressions the using header must be assignable");
				return false;
			}
			if(this.function.containVariables(ent.left)) {
				System.err.println("the using block's variable "+ent.left+" is a local variable or an input variable!");
				return false;
			}
			if(usingVariable.contains(ent.left)) {
				System.err.println("the using block's variable "+ent.left+" is duplicated!");
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
				System.err.println(st+" doesn't type check.");
				return false;
			}
		}
		if(!before.equal(budget)) {
			System.err.println("Using block consuming variables!");
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
}
