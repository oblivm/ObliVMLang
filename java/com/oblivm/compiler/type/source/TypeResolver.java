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
import com.oblivm.compiler.ast.ASTFunctionNative;
import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.ast.DefaultVisitor;
import com.oblivm.compiler.ast.IFunctionVisitor;
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
import com.oblivm.compiler.ast.type.ASTNativeType;
import com.oblivm.compiler.ast.type.ASTNullType;
import com.oblivm.compiler.ast.type.ASTRecType;
import com.oblivm.compiler.ast.type.ASTRndType;
import com.oblivm.compiler.ast.type.ASTTupleType;
import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.ast.type.ASTTypeVisitor;
import com.oblivm.compiler.ast.type.ASTVariableType;
import com.oblivm.compiler.ast.type.ASTVoidType;
import com.oblivm.compiler.util.Pair;

public class TypeResolver extends DefaultVisitor<ASTStatement, ASTExpression, ASTType> implements ASTTypeVisitor<ASTType>, IFunctionVisitor<ASTFunction> {

	private ASTProgram program;

	private boolean isStructDef = false;

	public boolean copy = false;

	public Map<String, ASTType> typeVars;
	public Map<String, ASTExpression> bitVars;
	public Set<String> resolvingSet;

	public ASTProgram resolve(ASTProgram program) {
		this.program = program;
		typeVars = new HashMap<String, ASTType>();
		bitVars = new HashMap<String, ASTExpression>();
		resolvingSet = new HashSet<String>();

		for(Pair<String, ASTType> type : program.typeDef) {
			if(type.right instanceof ASTRecType)
				this.isStructDef = true;
			type.right = visit(type.right);
			this.isStructDef = false;
		}

		for(List<ASTType> i : program.typeVarDef.values()) {
			if(i != null)
				for(ASTType ty : i) {
					if(!(ty instanceof ASTVariableType))
						throw new RuntimeException("Type parameters in definition must be variables!");
				}
		}

		for(int i=0; i<program.functionDef.size(); ++i) {
			program.functionDef.set(i, visit(program.functionDef.get(i)));
		}

		for(Pair<ASTFunctionType, ASTType> ent : program.functionTypeMapping) {
			this.typeVars = new HashMap<String, ASTType>();
			if(ent.left.typeParameter != null) {
				for(ASTType ty : ent.left.typeParameter) {
					ASTVariableType vt = (ASTVariableType)ty;
					typeVars.put(vt.var, ty);
				}
			}
			ent.left = (ASTFunctionType)visit(ent.left);
		}
		return this.program;
	}

	@Override
	public ASTType visit(ASTArrayType type) {
		if(copy) {
			type = new ASTArrayType(visit(type.type), visit(type.size), type.lab);
		} else {
			type.type = visit(type.type);
		}
		return type;
	}

	@Override
	public ASTType visit(ASTIntType type) {
		if(copy)
			return ASTIntType.get(visit(type.getBits()), type.getLabel());
		else
			return type;
	}

	@Override
	public ASTType visit(ASTFloatType type) {
		if(copy)
			return ASTFloatType.get(visit(type.getBits()), type.getLabel());
		else
			return type;
	}

	@Override
	public ASTType visit(ASTRndType type) {
		if(copy)
			return ASTRndType.get(visit(type.getBits()), type.getLabel());
		else
			return type;
	}

	@Override
	public ASTType visit(ASTNativeType type) {
		if(copy) {
			// TODO instantiate bit variables
			return new ASTNativeType(type.name, type.bitVariables);
		} else
			return type;
	}

	@Override
	public ASTType visit(ASTRecType type) {
		if(resolvingSet.contains(type.name))
			throw new RuntimeException("Recursive type is forbidden!");


		ASTRecType ret = new ASTRecType(type.name, type.lab);

		Map<String, ASTType> oldTypeVars = null;
		if(this.isStructDef) {
			Map<String, ASTExpression> oldBitVar = this.bitVars;
			this.bitVars = new HashMap<String, ASTExpression>(oldBitVar);
			ret.bitVariables = new ArrayList<ASTExpression>();
			for(ASTExpression e : type.bitVariables) {
				ASTVariableExpression ve = (ASTVariableExpression)e;
				ret.bitVariables.add(e);
				bitVars.put(ve.var, ve);
			}

			if(type.typeVariables != null) {
				oldTypeVars = new HashMap<String, ASTType>(typeVars);
				ret.typeVariables = new ArrayList<ASTType>();
				for(ASTType i : type.typeVariables) {
					if(!(i instanceof ASTVariableType))
						throw new RuntimeException("Type parameters in definition must be variables!\n"+type.toString());
					ASTVariableType var = (ASTVariableType)i;
					ret.typeVariables.add(var);
					typeVars.put(var.var, var);
				}
			}
			resolvingSet.add(type.name);
			for(Map.Entry<String, ASTType> ent : type.fieldsType.entrySet()) {
				ret.fieldsType.put(ent.getKey(), visit(ent.getValue()));
			}
			for(String i : type.fields)
				ret.fields.add(i);
			if(type.typeVariables != null) {
				typeVars = oldTypeVars;
			}

			resolvingSet.remove(type.name);

			this.bitVars = oldBitVar;

		} else {

			ret.bitVariables = new ArrayList<ASTExpression>();
			for(ASTExpression e : type.bitVariables) {
				ret.bitVariables.add(visit(e));
			}

			if(type.typeVariables != null) {
				ret.typeVariables = new ArrayList<ASTType>();
				for(ASTType i : type.typeVariables) {
					ret.typeVariables.add(visit(i));
				}
			}
			for(Map.Entry<String, ASTType> ent : type.fieldsType.entrySet()) {
				ret.fieldsType.put(ent.getKey(), visit(ent.getValue()));
			}
			for(String i : type.fields)
				ret.fields.add(i);
		}

		return ret;
	}

	@Override
	public ASTType visit(ASTVariableType type) {
		if(resolvingSet.contains(type.var))
			throw new RuntimeException("Recursive type is forbidden.");
		if(this.typeVars.containsKey(type.var)) {
			if(type.typeVars != null)
				throw new RuntimeException("Type variable "+type.var+" cannot have type parameters in "+type.toString());
			return this.typeVars.get(type.var);
		}

		for(Pair<String, ASTType> ent : program.typeDef) {
			if(ent.left.equals(type.var)) {
				if(ent.right instanceof ASTArrayType) {
					ASTArrayType t = (ASTArrayType)ent.right;
					return new ASTArrayType(visit(t.type), visit(t.size), t.lab);
				} else if(ent.right instanceof ASTIntType) {
					if(type.bitVars.size() >= 2)
						throw new RuntimeException("Too many bit variables for an int type!");
					ASTIntType ret = (ASTIntType)ent.right;
					ASTExpression bits;
					if(type.bitVars.size() > 0 && ret.getBits() == null)
						bits = visit(type.bitVars.get(0));
					else
						bits = visit(ret.getBits());
					return ASTIntType.get(bits, ret.getLabel());
				} else if(ent.right instanceof ASTFloatType) {
					if(type.bitVars.size() >= 2)
						throw new RuntimeException("Too many bit variables for a float type!");
					return ASTFloatType.get(type.bitVars.size() == 0 ? null : visit(type.bitVars.get(0)), ((ASTFloatType)ent.right).getLabel());
				} else if(ent.right instanceof ASTRndType) {
					if(type.bitVars.size() >= 2)
						throw new RuntimeException("Too many bit variables for a rnd type!");
					return ASTRndType.get(type.bitVars.size() == 0 ? null : visit(type.bitVars.get(0)), ((ASTRndType)ent.right).getLabel());
				} else if(ent.right instanceof ASTNativeType) {
					return ent.right;
				} else if(ent.right instanceof ASTRecType) {
					ASTRecType t = (ASTRecType)ent.right;
					ASTRecType ret = new ASTRecType(t.name, t.lab);

					Map<String, ASTExpression> oldBitVars = new HashMap<String, ASTExpression>(bitVars);
					for(ASTExpression e : type.bitVars) 
						ret.bitVariables.add(visit(e));
					for(int i=0; i<type.bitVars.size(); ++i) {
						ASTVariableExpression var = (ASTVariableExpression)t.bitVariables.get(i);
						bitVars.put(var.var, ret.bitVariables.get(i));
					}

					Map<String, ASTType> oldTypeVars = new HashMap<String, ASTType>(typeVars);
					if(!((type.typeVars == null && t.typeVariables == null) 
							|| (type.typeVars != null 
							&& t.typeVariables != null 
							&& type.typeVars.size() == t.typeVariables.size())))
						throw new RuntimeException("The number of type variables does not match.");

					if(t.typeVariables != null) {
						ret.typeVariables = new ArrayList<ASTType>();
						for(int i=0; i<t.typeVariables.size(); ++i)  {
							ret.typeVariables.add(visit(type.typeVars.get(i)));
						}
						for(int i=0; i<t.typeVariables.size(); ++i)  {
							if(!(t.typeVariables.get(i) instanceof ASTVariableType))
								throw new RuntimeException("Type parameters in definition must be variables!\n"+type.toString());
							ASTVariableType var = (ASTVariableType)t.typeVariables.get(i);
							typeVars.put(var.var, ret.typeVariables.get(i));
						}
					}

					ret.fields = new ArrayList<String>(t.fields);
					for(Map.Entry<String, ASTType> x : t.fieldsType.entrySet()) {
						ret.fieldsType.put(x.getKey(), visit(x.getValue()));
					}

					typeVars = oldTypeVars;
					bitVars = oldBitVars;
					return ret;
				} else if(ent.right instanceof ASTVariableType) {
					return visit(ent.right);
				} else if(ent.right instanceof ASTVoidType) {
					return ent.right;
				} else
					throw new RuntimeException("Unknown Type.");
			} else if (type.var.startsWith(ent.left)) {
				if(type.bitVars.size() != 0 || (!(ent.right instanceof ASTIntType) && !(ent.right instanceof ASTFloatType) && !(ent.right instanceof ASTRndType))) 
					continue;
				if(ent.right instanceof ASTIntType) {
					if (((ASTIntType)ent.right).getBits() != null)
						continue;
					ASTIntType it = (ASTIntType)ent.right;
					if(it.getBits() != null)
						continue;
					String suf = type.var.substring(ent.left.length());
					try {
						int bit = Integer.parseInt(suf);
						return ASTIntType.get(new ASTConstantExpression(bit), it.getLabel());
					} catch (Exception e) {
						continue;
					}
				} else if(ent.right instanceof ASTFloatType) {
					if (((ASTFloatType)ent.right).getBits() != null)
						continue;
					ASTFloatType it = (ASTFloatType)ent.right;
					if(it.getBits() != null)
						continue;
					String suf = type.var.substring(ent.left.length());
					try {
						int bit = Integer.parseInt(suf);
						return ASTFloatType.get(new ASTConstantExpression(bit), it.getLabel());
					} catch (Exception e) {
						continue;
					}
				} else if(ent.right instanceof ASTRndType) {
					if (((ASTRndType)ent.right).getBits() != null)
						continue;
					ASTRndType it = (ASTRndType)ent.right;
					if(it.getBits() != null)
						continue;
					String suf = type.var.substring(ent.left.length());
					try {
						int bit = Integer.parseInt(suf);
						return ASTRndType.get(new ASTConstantExpression(bit), it.getLabel());
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
		throw new RuntimeException("Unresolved type: " + type.toString());
	}

	@Override
	public ASTType visit(ASTVoidType type) {
		return type;
	}

	public ASTFunction visit(ASTFunction func) {
		Map<String, ASTType> tmpTypeVars = new HashMap<String, ASTType>(this.typeVars);
		Map<String, ASTExpression> tmpBitVars = new HashMap<String, ASTExpression>(this.bitVars);
		if(!(func.baseType instanceof ASTVariableType) && func.baseType != null)
			throw new RuntimeException("Parser should parse the base type of a function as a ASTVariableType!");
		// TODO This may be wrong for some sophisticated programs.
		if(func.typeVariables != null)
			for(ASTVariableType s : func.typeVariables) {
				typeVars.put(s.var, s);
			}
		for(String s : func.bitParameter) {
			bitVars.put(s, new ASTVariableExpression(s));
		}
		if(func.baseType != null) {
			ASTVariableType type = (ASTVariableType)func.baseType;
			if(type.typeVars != null) {
				for(ASTType v : type.typeVars) {
					if(!(v instanceof ASTVariableType))
						throw new RuntimeException("Type parameters must be a string symbol!");
					typeVars.put(((ASTVariableType)v).var, v);
				}
			}
			for(ASTExpression v : type.bitVars) {
				if(!(v instanceof ASTVariableExpression))
					throw new RuntimeException("Bit parameters must be a string symbol!");
				bitVars.put(((ASTVariableExpression)v).var, v);
			}
			func.baseType = visit(func.baseType);
		}

		for(int i=0; i<func.inputVariables.size(); ++i) {
			func.inputVariables.get(i).left = visit(func.inputVariables.get(i).left);
		}
		//		if(func instanceof ASTFunctionDef) {
		//			ASTFunctionDef funcDef = (ASTFunctionDef)func;
		//			for(int i=0; i<funcDef.localVariables.size(); ++i) {
		//				funcDef.localVariables.get(i).left = visit(funcDef.localVariables.get(i).left);
		//			}
		//		}
		func.returnType = visit(func.returnType);
		ASTFunction ret;
		if(func instanceof ASTFunctionNative) {
			ret = visit((ASTFunctionNative)func);
		} else if (func instanceof ASTFunctionDef) {
			ret = visit((ASTFunctionDef)func);
		} else
			throw new RuntimeException("Unknown function type.");

		this.typeVars = tmpTypeVars;
		this.bitVars = tmpBitVars;
		return ret;
	}

	public ASTFunction visit(ASTFunctionNative func) {
		return func;
	}

	public ASTFunction visit(ASTFunctionDef func) {
		List<Pair<ASTType, String>> localVariables = new ArrayList<Pair<ASTType, String>>();
		for(int i=0; i<func.localVariables.size(); ++i) {
			boolean a = true;
			for(int j=0; j<i; ++j)
				if(func.localVariables.get(i).right.equals(func.localVariables.get(j).right))
					a = false;
			if(a) {
				boolean oldCopy = this.copy;
				this.copy = true;
				Pair<ASTType, String> pp = func.localVariables.get(i);
				localVariables.add(new Pair<ASTType, String>(visit(pp.left), pp.right));
				this.copy = oldCopy;
			}
		}
		func.localVariables = localVariables;
		for(int i=0; i<func.body.size(); ++i)
			func.body.set(i, visit(func.body.get(i)));
		return func;
	}

	@Override
	public ASTStatement visit(ASTAssignStatement assignStatement) {
		assignStatement.expr = visit(assignStatement.expr);
		assignStatement.var = visit(assignStatement.var);
		return assignStatement;
	}

	@Override
	public ASTStatement visit(ASTFuncStatement funcStatement) {
		ASTExpression stmt = visit(funcStatement.func);
		if(!(stmt instanceof ASTFuncExpression))
			throw new RuntimeException(stmt+" is not a function call!");
		funcStatement.func = (ASTFuncExpression)stmt;
		return funcStatement;
	}

	@Override
	public ASTStatement visit(ASTIfStatement ifStatement) {
		for(int i=0; i<ifStatement.trueBranch.size(); ++i)
			ifStatement.trueBranch.set(i, visit(ifStatement.trueBranch.get(i)));
		for(int i=0; i<ifStatement.falseBranch.size(); ++i)
			ifStatement.falseBranch.set(i, visit(ifStatement.falseBranch.get(i)));
		return ifStatement;
	}

	@Override
	public ASTStatement visit(ASTReturnStatement returnStatement) {
		returnStatement.exp = visit(returnStatement.exp);
		return returnStatement;
	}

	@Override
	public ASTStatement visit(ASTWhileStatement whileStatement) {
		whileStatement.cond = (ASTPredicate)visit(whileStatement.cond);
		for(int i=0; i<whileStatement.body.size(); ++i)
			whileStatement.body.set(i, visit(whileStatement.body.get(i)));
		return whileStatement;
	}

	@Override
	public ASTStatement visit(ASTBoundedWhileStatement whileStatement) {
		whileStatement.bound = visit(whileStatement.bound);
		whileStatement.cond = (ASTPredicate)visit(whileStatement.cond);
		for(int i=0; i<whileStatement.body.size(); ++i)
			whileStatement.body.set(i, visit(whileStatement.body.get(i)));
		return whileStatement;
	}

	@Override
	public ASTExpression visit(ASTAndPredicate andPredicate) {
		andPredicate.left = (ASTPredicate)visit(andPredicate.left);
		andPredicate.right = (ASTPredicate)visit(andPredicate.right);
		return andPredicate;
	}

	@Override
	public ASTExpression visit(ASTArrayExpression arrayExpression) {
		arrayExpression.var = visit(arrayExpression.var);
		arrayExpression.indexExpr = visit(arrayExpression.indexExpr);
		return arrayExpression;
	}

	@Override
	public ASTExpression visit(ASTBinaryExpression binaryExpression) {
		if(copy) {
			ASTBinaryExpression bp = new ASTBinaryExpression(visit(binaryExpression.left),
					binaryExpression.op,
					visit(binaryExpression.right));
			bp.setBeginPosition(binaryExpression.beginPosition);
			bp.setEndPosition(binaryExpression.endPosition);
			return bp;
		} else {
			binaryExpression.left = visit(binaryExpression.left);
			binaryExpression.right = visit(binaryExpression.right);
			return binaryExpression;
		}
	}

	@Override
	public ASTExpression visit(ASTBinaryPredicate binaryPredicate) {
		if(copy) {
			ASTBinaryPredicate bp = new ASTBinaryPredicate(visit(binaryPredicate.left),
					binaryPredicate.op,
					visit(binaryPredicate.right));
			bp.setBeginPosition(binaryPredicate.beginPosition);
			bp.setEndPosition(binaryPredicate.endPosition);
			return bp;
		} else {
			binaryPredicate.left = visit(binaryPredicate.left);
			binaryPredicate.right = visit(binaryPredicate.right);
			return binaryPredicate;
		}
	}

	@Override
	public ASTExpression visit(ASTConstantExpression constantExpression) {
		return constantExpression;
	}

	@Override
	public ASTExpression visit(ASTFuncExpression funcExpression) {
		if(funcExpression.typeVars != null) {
			for(int i=0; i<funcExpression.typeVars.size(); ++i)
				funcExpression.typeVars.set(i, visit(funcExpression.typeVars.get(i)));
		}
		for(int i=0; i<funcExpression.bitParameters.size(); ++i) {
			funcExpression.bitParameters.set(i, visit(funcExpression.bitParameters.get(i)));
		}
		for(Pair<String, ASTExpression> ent : funcExpression.inputs) {
			ent.right = visit(ent.right);
		}
		if(!(funcExpression.obj instanceof ASTVariableExpression))
			return funcExpression;
		ASTVariableExpression exp = (ASTVariableExpression)funcExpression.obj;
		if(exp.var.equals("log") && funcExpression.bitParameters.size() == 0 
				&& funcExpression.typeVars == null && funcExpression.inputs.size() == 1) {
			return new ASTLogExpression(funcExpression.inputs.get(0).right);
		}
		// TODO Security Label
		ASTVariableType tmp = new ASTVariableType(exp.var, false);
		tmp.bitVars = funcExpression.bitParameters;
		tmp.typeVars = funcExpression.typeVars;
		ASTType ty;
		try {
			ty = visit(tmp);
		} catch(Exception e) {
			return funcExpression;
		}
		if(ty instanceof ASTRecType) {
			ASTRecType type = (ASTRecType)ty;
			Map<String, ASTExpression> initialValue = new HashMap<String, ASTExpression>();
			if(funcExpression.inputs.size() == 0) {
				if(type.fields.size() != 0)
					throw new RuntimeException("Type "+tmp.var+" has "+type.fields.size()+" fields!");
				return new ASTNewObjectExpression(type, initialValue);
			}
			boolean withField = funcExpression.inputs.get(0).left != null;
			if(withField) {
				for(Pair<String, ASTExpression> ent : funcExpression.inputs) {
					if(ent.left == null) {
						throw new RuntimeException("Fields in the type constructor "+type.shortName()+" are missing for "+ent.right+"!");
					}
					initialValue.put(ent.left, visit(ent.right));
				}
				return new ASTNewObjectExpression(type, initialValue);
			} else {
				if(funcExpression.inputs.size() != type.fields.size())
					throw new RuntimeException("Type "+tmp.var+" has "+type.fields.size()+" fields!");
				for(int i=0; i<type.fields.size(); ++i) {
					if(funcExpression.inputs.get(i).left != null)
						throw new RuntimeException("Redudant field "+funcExpression.inputs.get(i)+"!");
					initialValue.put(type.fields.get(i), visit(funcExpression.inputs.get(i).right));
				}
				return new ASTNewObjectExpression(type, initialValue);
			}
		}
		return funcExpression;
	}

	@Override
	public ASTExpression visit(ASTOrPredicate orPredicate) {
		if(copy) {
			ASTOrPredicate orp = new ASTOrPredicate((ASTPredicate)visit(orPredicate.left), (ASTPredicate)visit(orPredicate.right));
			return orp;
		} else {
			orPredicate.left = (ASTPredicate)visit(orPredicate.left);
			orPredicate.right = (ASTPredicate)visit(orPredicate.right);
			return orPredicate;
		}
	}

	@Override
	public ASTExpression visit(ASTRecExpression rec) {
		if(copy) {
			ASTRecExpression ret = new ASTRecExpression(visit(rec.base), rec.field);
			ret.setBeginPosition(rec.beginPosition);
			ret.setEndPosition(rec.endPosition);
			return ret;
		} else {
			rec.base = visit(rec.base);
			return rec;
		}
	}

	@Override
	public ASTExpression visit(ASTRecTupleExpression tuple) {
		if(copy) {
			ASTRecTupleExpression tupleExp = new ASTRecTupleExpression(visit(tuple.base),
					new ASTTupleExpression());
			for(int i=0; i<tuple.exps.size(); ++i)
				tupleExp.exps.add(tuple.exps.get(i));
			return tupleExp;
		} else {
			tuple.base = visit(tuple.base);
			for(int i=0; i<tuple.exps.size(); ++i)
				tuple.exps.set(i, tuple.exps.get(i));
			return tuple;
		}
	}

	@Override
	public ASTExpression visit(ASTTupleExpression tuple) {
		if(copy) {
			ASTTupleExpression tupleExp = new ASTTupleExpression();
			for(int i=0; i<tuple.exps.size(); ++i)
				tupleExp.exps.add(tuple.exps.get(i));
			return tupleExp;
		} else {
			for(int i=0; i<tuple.exps.size(); ++i)
				tuple.exps.set(i, tuple.exps.get(i));
			return tuple;
		}
	}

	@Override
	public ASTExpression visit(ASTVariableExpression variableExpression) {
		if(bitVars.containsKey(variableExpression.var))
			return bitVars.get(variableExpression.var);
		else
			return variableExpression;
	}

	@Override
	public ASTExpression visit(ASTNewObjectExpression exp) {
		ASTVariableType vt = new ASTVariableType(exp.type.name, AffineTypeChecker.get().isAffine(exp.type));
		vt.bitVars = exp.type.bitVariables;
		vt.typeVars = exp.type.typeVariables;
		ASTType type = visit(vt);
		if(!(type instanceof ASTRecType)) {
			throw new RuntimeException(vt.var+" is not a record type!");
		}
		if(copy) {
			ASTNewObjectExpression ret = new ASTNewObjectExpression((ASTRecType)type, 
					new HashMap<String, ASTExpression>(exp.valueMapping));
			ret.setBeginPosition(exp.beginPosition);
			ret.setEndPosition(exp.endPosition);
			for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
				ret.valueMapping.put(ent.getKey(), visit(ent.getValue()));
			}
			return ret;
		} else {
			exp.type = (ASTRecType)type;
			for(Map.Entry<String, ASTExpression> ent : exp.valueMapping.entrySet()) {
				ent.setValue(visit(ent.getValue()));
			}
			return exp;
		}
		//		throw new RuntimeException("Should have new object expression from the parser!");
	}

	@Override
	public ASTType visit(ASTFunctionType type) {
		if(copy) {
			ASTFunctionType ret = new ASTFunctionType(
					visit(type.returnType), 
					type.name, 
					new ArrayList<String>(type.bitParameter),
					new ArrayList<ASTType>(),
					type.global);
			if(type.typeParameter != null) {
				ret.typeParameter = new ArrayList<ASTType>();
				for(ASTType ty : type.typeParameter)
					ret.typeParameter.add(visit(ty));
			} else
				type.typeParameter = null;
			for(int i=0; i<type.inputTypes.size(); ++i)
				ret.inputTypes.add(visit(type.inputTypes.get(i)));
			return ret;
		} else {
			type.returnType = visit(type.returnType);
			if(type.typeParameter != null) {
				for(int i=0; i<type.typeParameter.size(); ++i)
					type.typeParameter.set(i, visit(type.typeParameter.get(i)));
			}
			for(int i=0; i<type.inputTypes.size(); ++i)
				type.inputTypes.set(i, visit(type.inputTypes.get(i)));
			return type;
		}
	}

	@Override
	public ASTExpression visit(ASTFloatConstantExpression constantExpression) {
		return constantExpression;
	}

	@Override
	public ASTExpression visit(ASTLogExpression exp) {
		if(copy) {
			new ASTLogExpression(visit(exp.exp));
		} else {
			exp.exp = visit(exp.exp);
		}
		return exp;
	}

	@Override
	public ASTExpression visit(ASTRangeExpression exp) {
		if(copy) {
			ASTRangeExpression rexp = new ASTRangeExpression(
					visit(exp.source),
					visit(exp.rangel),
					visit(exp.ranger)
					);
			rexp.setBeginPosition(exp.beginPosition);
			rexp.setEndPosition(exp.endPosition);
			return rexp;
		} else {
			exp.source = visit(exp.source);
			exp.rangel = visit(exp.rangel);
			if(exp.ranger != null)
				exp.ranger = visit(exp.ranger);
		}
		return exp;
	}

	@Deprecated
	@Override
	public ASTStatement visit(ASTOnDummyStatement stmt) {
		for(int i=0; i<stmt.condList.size(); ++i) {
			Pair<String, ASTExpression> pair = stmt.condList.get(i);
			pair.right = visit(pair.right);
		}
		for(int i=0; i<stmt.body.size(); ++i) {
			stmt.body.set(i, visit(stmt.body.get(i)));
		}
		return stmt;
	}

	@Override
	public ASTExpression visit(ASTNullExpression exp) {
		return exp;
	}

	@Override
	public ASTType visit(ASTNullType type) {
		return type;
	}

	@Override
	public ASTType visit(ASTDummyType type) {
		if(copy) {
			ASTDummyType dt = new ASTDummyType(visit(type.type));
			return dt;
		} else {
			type.type = visit(type.type);
			return type;
		}
	}

	@Override
	public ASTType visit(ASTTupleType type) {
		if(copy) {
			ASTTupleType tuples = new ASTTupleType();
			for(ASTType ty : type.types) {
				tuples.types.add(visit(ty));
			}
			return tuples;
		} else {
			for(int i=0; i<type.types.size(); ++i) {
				type.types.set(i, visit(type.types.get(i)));
			}
			return type;
		}
	}

	@Override
	public ASTStatement visit(ASTUsingStatement stmt) {
		if(copy) {
			ASTUsingStatement use = new ASTUsingStatement();
			use.setBeginPosition(stmt.beginPosition);
			use.setEndPosition(stmt.endPosition);
			for(Pair<String, ASTExpression> p : stmt.use) {
				use.use.add(new Pair<String, ASTExpression>(p.left, visit(p.right)));
			}
			for(int i=0; i<stmt.body.size(); ++i) {
				use.body.add(visit(stmt.body.get(i)));
			}
			return use;
		} else {
			for(Pair<String, ASTExpression> p : stmt.use) {
				p.right = visit(p.right);
			}
			for(int i=0; i<stmt.body.size(); ++i) {
				stmt.body.set(i, visit(stmt.body.get(i)));
			}
			return stmt;
		}
	}

}
