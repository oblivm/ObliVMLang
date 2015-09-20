package com.oblivm.compiler.ast.expr;

import com.oblivm.compiler.ast.type.ASTType;

public class ASTSizeExpression extends ASTExpression {
	public ASTType type;
	
	public ASTSizeExpression(ASTType type) {
		this.type = type;
	}
	
	@Override
	public int level() {
		return 100;
	}

	@Override
	public ASTExpression cloneInternal() {
		return new ASTSizeExpression(type);
	}

	public String toString() {
		return "sizeof("+type.toString()+")";
	}
}
