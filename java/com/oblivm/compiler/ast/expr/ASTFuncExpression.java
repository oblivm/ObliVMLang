/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.expr;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.type.ASTType;
import com.oblivm.compiler.util.Pair;

public class ASTFuncExpression extends ASTExpression {
	public ASTExpression obj;
	public List<ASTType> typeVars = null; 
	public List<Pair<String, ASTExpression>> inputs;
	public List<ASTExpression> bitParameters;
	
	/***
	 * To fill by TypeChecker
	 */
	public ASTType baseType;
	public List<ASTType> inputTypes = new ArrayList<ASTType>();
	
	public ASTFuncExpression(ASTExpression obj, List<ASTExpression> bitParameters, List<ASTType> typeVars) {
		this.obj = obj;
		if(bitParameters == null)
			this.bitParameters = new ArrayList<ASTExpression>();
		else
			this.bitParameters = bitParameters;
		this.inputs = new ArrayList<Pair<String, ASTExpression>>();
		this.typeVars = typeVars;
	}
	
	public void addInputs(String field, ASTExpression exp) {
		inputs.add(new Pair<String, ASTExpression>(field, exp));
	}
	
	public void addInputs(ASTExpression exp) {
		addInputs(null, exp);
	}
	
	@Override
	public int level() {
		return 100;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(obj.toString());
		for(int i=0; i<this.bitParameters.size(); ++i) {
			sb.append("@");
			ASTExpression exp = bitParameters.get(i);
			if(exp instanceof ASTConstantExpression) {
				sb.append(((ASTConstantExpression)exp).value);
			} else if(exp instanceof ASTVariableExpression) {
				sb.append(((ASTVariableExpression)exp).var);
			} else if (exp instanceof ASTLogExpression) {
				sb.append("log(");
				sb.append(((ASTLogExpression)exp).exp.toString());
				sb.append(")");
			} else
				sb.append("(" + exp.toString()+")");
		}
		if(typeVars != null) {
			sb.append("{");
			for(int i=0; i<typeVars.size(); ++i) {
				if(i > 0) sb.append(", ");
				sb.append(typeVars.get(i).toString());
			}
			sb.append("}");
		}
		sb.append("(");
		for(int i=0; i<inputs.size(); ++i) {
			if(i > 0) sb.append(", ");
			if(inputs.get(i).left != null) {
				sb.append(inputs.get(i).left+" = ");
			}
			sb.append(inputs.get(i).right);
		}
		sb.append(")");
		return sb.toString();
	}
}
