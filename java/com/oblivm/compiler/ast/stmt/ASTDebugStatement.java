package com.oblivm.compiler.ast.stmt;

import com.oblivm.compiler.ast.expr.ASTExpression;

public class ASTDebugStatement extends ASTStatement {
	public ASTExpression exp;
	
	public ASTDebugStatement(ASTExpression exp) {
		this.exp = exp;
	}
	
	@Override
	public String toString(int indent) {
		return indent(indent) + toString();
	}
	
	public String toString() {
		return "debug("+exp.toString()+")";
	}
	
}
