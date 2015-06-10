/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import com.oblivm.compiler.ast.expr.ASTConstantExpression;
import com.oblivm.compiler.ast.expr.ASTExpression;
import com.oblivm.compiler.ast.expr.ASTVariableExpression;

public class ASTVariableType extends ASTType {

	public String var;
	public List<ASTType> typeVars = null;
	public List<ASTExpression> bitVars = new ArrayList<ASTExpression>();
	public boolean isAffine;
	
	public ASTVariableType(String v, boolean affine) {
		this.var = v;
		this.isAffine = affine;
	}
	
	@Override
	public String toString(int indent) {
		return toString();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(var);
		if(bitVars.size() == 1 && (bitVars.get(0) instanceof ASTConstantExpression))
			sb.append(bitVars.get(0).toString());
		else {
			for(int i=0; i<bitVars.size(); ++i)
			{
				sb.append("@");
				if(!(bitVars.get(i) instanceof ASTVariableExpression))
					sb.append("(");
				sb.append(bitVars.get(i).toString());
				if(!(bitVars.get(i) instanceof ASTVariableExpression))
					sb.append(")");
			}
		}
		if(typeVars != null) {
			sb.append("<");
			for(int i=0; i<typeVars.size(); ++i) {
				if(i > 0) sb.append(", ");
				sb.append(typeVars.get(i).toString());
			}
			sb.append(">");
		}
		return sb.toString();
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(type instanceof ASTDummyType)
			return canFlowTo(((ASTDummyType)type).type);

		if(!(type instanceof ASTVariableType))
			return false;
		return var.equals(((ASTVariableType)type).var);
	}

	@Override
	public String shortName() {
		return toString();
	}

	@Override
	public ASTLabel getLabel() {
		return ASTLabel.Secure;
	}
}
