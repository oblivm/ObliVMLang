/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.ast.type;

import java.util.List;

import com.oblivm.compiler.ast.expr.ASTExpression;

public class ASTNativeType extends ASTType {

	public String name;
	public List<String> bitVariables;
	public boolean isPhantom = false;
	
	public List<ASTExpression> constructor = null;
	
	public ASTNativeType(String name, List<String> bits) {
		this.name = name;
		this.bitVariables = bits;
	}
	
	@Override
	public String toString(int indent) {
		return toString();
	}
	
	public String toString() {
		return "native " + name;
	}

	@Override
	public boolean canFlowTo(ASTType type) {
		if(!(type instanceof ASTNativeType))
			return false;
		ASTNativeType nt = (ASTNativeType)type;
		
		return nt.name.equals(this.name);
	}

	@Override
	public String shortName() {
		return toString();
	}
	
	public boolean instance(ASTType type) {
		return this.canFlowTo(type);
	}

	@Override
	public ASTLabel getLabel() {
		return isPhantom ? ASTLabel.Secure : ASTLabel.Pub;
	}
}
